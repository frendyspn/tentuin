-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — Admin Push Notifications: helper, triggers, scheduled jobs
-- Jalankan di: Supabase Dashboard → SQL Editor
--
-- Prereq:
--   1. Edge Function `send-admin-push` sudah di-deploy.
--   2. Extensions diaktifkan di Dashboard → Database → Extensions:
--        - pg_net
--        - pg_cron
--   3. Settings di-set (sekali saja, ganti URL & key sesuai project):
--      alter database postgres set "app.admin_push_fn_url" = 'https://<ref>.supabase.co/functions/v1/send-admin-push';
--      alter database postgres set "app.admin_push_fn_key" = '<INTERNAL_PUSH_KEY same as edge function env>';
-- ─────────────────────────────────────────────────────────────────────────────


-- ─── 1. Helper: invoke edge function via pg_net ──────────────────────────────
create or replace function public._invoke_admin_push(
  p_event text,
  p_title text,
  p_body  text,
  p_data  jsonb default '{}'::jsonb
) returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  fn_url text := current_setting('app.admin_push_fn_url', true);
  fn_key text := current_setting('app.admin_push_fn_key', true);
begin
  if fn_url is null or fn_url = '' then
    -- settings belum di-config → silently skip (jangan block insert)
    return;
  end if;

  perform net.http_post(
    url     := fn_url,
    headers := jsonb_build_object(
      'Content-Type',  'application/json',
      'x-internal-key', coalesce(fn_key, '')
    ),
    body    := jsonb_build_object(
      'event', p_event,
      'title', p_title,
      'body',  p_body,
      'data',  p_data
    )
  );
exception when others then
  -- Jangan rollback transaksi user kalau notif gagal.
  raise notice 'admin push invoke failed: %', sqlerrm;
end;
$$;

revoke all on function public._invoke_admin_push(text,text,text,jsonb) from public;
grant execute on function public._invoke_admin_push(text,text,text,jsonb) to authenticated, service_role;


-- ─── 2. Trigger: notif saat agent request withdrawal ─────────────────────────
create or replace function public.notify_withdrawal_requested()
returns trigger language plpgsql security definer set search_path = public as $$
declare
  v_agent_name text;
begin
  if new.status <> 'requested' then
    return new;
  end if;

  select full_name into v_agent_name from public.agents where id = new.agent_id;

  perform public._invoke_admin_push(
    p_event := 'withdrawal.requested',
    p_title := 'Withdraw baru',
    p_body  := format('Rp%s dari %s',
                      to_char(new.amount, 'FM999G999G999'),
                      coalesce(v_agent_name, 'agen')),
    p_data  := jsonb_build_object(
      'route',         'withdrawal/' || new.id,
      'withdrawal_id', new.id,
      'agent_id',      new.agent_id,
      'amount',        new.amount
    )
  );
  return new;
end $$;

drop trigger if exists trg_notify_withdrawal_requested on public.agent_withdrawals;
create trigger trg_notify_withdrawal_requested
  after insert on public.agent_withdrawals
  for each row execute function public.notify_withdrawal_requested();


-- ─── 3. Trigger: notif saat agent klaim sekolah ──────────────────────────────
create or replace function public.notify_school_claimed()
returns trigger language plpgsql security definer set search_path = public as $$
declare
  v_agent_name  text;
  v_school_name text;
begin
  select full_name into v_agent_name  from public.agents where id = new.agent_id;
  select name      into v_school_name from public.schools where id = new.school_id;

  perform public._invoke_admin_push(
    p_event := 'school.claimed',
    p_title := 'Klaim sekolah baru',
    p_body  := format('%s mengklaim %s',
                      coalesce(v_agent_name, 'agen'),
                      coalesce(v_school_name, 'sekolah')),
    p_data  := jsonb_build_object(
      'route',     'school/' || new.school_id,
      'claim_id',  new.id,
      'school_id', new.school_id,
      'agent_id',  new.agent_id
    )
  );
  return new;
end $$;

drop trigger if exists trg_notify_school_claimed on public.agent_school_claims;
create trigger trg_notify_school_claimed
  after insert on public.agent_school_claims
  for each row execute function public.notify_school_claimed();


-- ─── 4. Trigger: notif saat agent klaim universitas ─────────────────────────
create or replace function public.notify_university_claimed()
returns trigger language plpgsql security definer set search_path = public as $$
declare
  v_agent_name text;
  v_uni_name   text;
begin
  select full_name into v_agent_name from public.agents where id = new.agent_id;
  select name      into v_uni_name   from public.universities where id = new.university_id;

  perform public._invoke_admin_push(
    p_event := 'university.claimed',
    p_title := 'Klaim universitas baru',
    p_body  := format('%s mengklaim %s',
                      coalesce(v_agent_name, 'agen'),
                      coalesce(v_uni_name, 'universitas')),
    p_data  := jsonb_build_object(
      'route',         'university/' || new.university_id,
      'claim_id',      new.id,
      'university_id', new.university_id,
      'agent_id',      new.agent_id
    )
  );
  return new;
end $$;

drop trigger if exists trg_notify_university_claimed on public.agent_university_claims;
create trigger trg_notify_university_claimed
  after insert on public.agent_university_claims
  for each row execute function public.notify_university_claimed();


-- ─── 5. Trigger: notif saat universitas subscribe baru ──────────────────────
create or replace function public.notify_university_subscribed()
returns trigger language plpgsql security definer set search_path = public as $$
declare
  v_uni_name text;
begin
  select name into v_uni_name from public.universities where id = new.university_id;

  perform public._invoke_admin_push(
    p_event := 'university.subscribed',
    p_title := 'Subscribe universitas',
    p_body  := format('%s beli %s kuota (Rp%s)',
                      coalesce(v_uni_name, 'universitas'),
                      new.quota_purchased,
                      to_char(new.amount, 'FM999G999G999')),
    p_data  := jsonb_build_object(
      'route',         'university/' || new.university_id,
      'subscribe_id',  new.id,
      'university_id', new.university_id,
      'amount',        new.amount,
      'quota',         new.quota_purchased
    )
  );
  return new;
end $$;

drop trigger if exists trg_notify_university_subscribed on public.university_subscribe_logs;
create trigger trg_notify_university_subscribed
  after insert on public.university_subscribe_logs
  for each row execute function public.notify_university_subscribed();


-- ─── 6. Cron: hourly student summary ────────────────────────────────────────
create or replace function public.cron_hourly_student_summary()
returns void language plpgsql security definer set search_path = public as $$
declare
  v_count integer;
begin
  select count(*) into v_count
  from public.profiles
  where role = 'student'
    and created_at > now() - interval '1 hour';

  if v_count > 0 then
    perform public._invoke_admin_push(
      p_event := 'student.hourly',
      p_title := 'Student baru daftar',
      p_body  := format('%s student baru daftar dalam 1 jam terakhir', v_count),
      p_data  := jsonb_build_object('route', 'dashboard', 'count', v_count)
    );
  end if;
end $$;


-- ─── 7. Cron: daily summary (09:00 WIB = 02:00 UTC) ─────────────────────────
create or replace function public.cron_daily_summary()
returns void language plpgsql security definer set search_path = public as $$
declare
  v_students  integer;
  v_withdrawals integer;
  v_claims    integer;
  v_subs      integer;
begin
  select count(*) into v_students
    from public.profiles
   where role = 'student' and created_at > now() - interval '1 day';

  select count(*) into v_withdrawals
    from public.agent_withdrawals
   where requested_at > now() - interval '1 day';

  select count(*) into v_claims
    from (
      select claimed_at from public.agent_school_claims
       where claimed_at > now() - interval '1 day'
      union all
      select claimed_at from public.agent_university_claims
       where claimed_at > now() - interval '1 day'
    ) t;

  select count(*) into v_subs
    from public.university_subscribe_logs
   where subscribed_at > now() - interval '1 day';

  perform public._invoke_admin_push(
    p_event := 'summary.daily',
    p_title := 'Rekap Harian',
    p_body  := format('%s student • %s withdraw • %s klaim • %s subscribe',
                      v_students, v_withdrawals, v_claims, v_subs),
    p_data  := jsonb_build_object(
      'route',        'dashboard',
      'students',     v_students,
      'withdrawals',  v_withdrawals,
      'claims',       v_claims,
      'subscribes',   v_subs
    )
  );
end $$;


-- ─── 8. Cron: weekly summary (Senin 09:00 WIB = 02:00 UTC) ──────────────────
create or replace function public.cron_weekly_summary()
returns void language plpgsql security definer set search_path = public as $$
declare
  v_students  integer;
  v_withdrawals integer;
  v_subs_total bigint;
begin
  select count(*) into v_students
    from public.profiles
   where role = 'student' and created_at > now() - interval '7 days';

  select count(*) into v_withdrawals
    from public.agent_withdrawals
   where requested_at > now() - interval '7 days';

  select coalesce(sum(amount), 0) into v_subs_total
    from public.university_subscribe_logs
   where subscribed_at > now() - interval '7 days';

  perform public._invoke_admin_push(
    p_event := 'summary.weekly',
    p_title := 'Rekap Mingguan',
    p_body  := format('%s student baru • %s withdraw • Rp%s subscribe',
                      v_students,
                      v_withdrawals,
                      to_char(v_subs_total, 'FM999G999G999')),
    p_data  := jsonb_build_object('route', 'dashboard')
  );
end $$;


-- ─── 9. Schedule (pg_cron) ──────────────────────────────────────────────────
-- Catatan: pg_cron schema biasanya `cron.*`. Skip di-create kalau extension belum ada.

do $$ begin
  if exists (select 1 from pg_extension where extname = 'pg_cron') then
    -- Hourly student summary (menit ke-5 setiap jam)
    perform cron.unschedule('adm_hourly_student_summary')
      where exists (select 1 from cron.job where jobname = 'adm_hourly_student_summary');
    perform cron.schedule(
      'adm_hourly_student_summary', '5 * * * *',
      $job$ select public.cron_hourly_student_summary() $job$
    );

    -- Daily summary (09:00 WIB = 02:00 UTC)
    perform cron.unschedule('adm_daily_summary')
      where exists (select 1 from cron.job where jobname = 'adm_daily_summary');
    perform cron.schedule(
      'adm_daily_summary', '0 2 * * *',
      $job$ select public.cron_daily_summary() $job$
    );

    -- Weekly summary (Senin 09:00 WIB)
    perform cron.unschedule('adm_weekly_summary')
      where exists (select 1 from cron.job where jobname = 'adm_weekly_summary');
    perform cron.schedule(
      'adm_weekly_summary', '0 2 * * 1',
      $job$ select public.cron_weekly_summary() $job$
    );
  else
    raise notice 'pg_cron extension belum aktif. Aktifkan di Supabase Dashboard → Database → Extensions, lalu jalankan blok ini ulang.';
  end if;
end $$;


-- ─── 10. Smoke test (manual) ─────────────────────────────────────────────────
-- Setelah Edge Function deploy + settings di-set, test dengan:
--   select public._invoke_admin_push('test','Halo','Test notif', '{"route":"dashboard"}'::jsonb);
-- Lalu cek: select * from net._http_response order by created desc limit 5;
