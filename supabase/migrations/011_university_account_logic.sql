-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — University Accounts: RPC, Triggers, pg_cron
-- Jalankan di: Supabase Dashboard → SQL Editor (setelah 010_university_accounts.sql)
--
-- Konten:
--   1. Trigger: bump prospect_followups.last_activity_at saat ada activity
--   2. RPC: create_personal_account / create_enterprise_account
--   3. RPC: subscribe_plan (atomic: log + add quota)
--   4. RPC: invite_to_enterprise / accept_enterprise_invite (+ merge kuota)
--   5. RPC: unlock_prospect (potong kuota sekali per account+prospek)
--   6. RPC: log_followup_activity
--   7. RPC: release_followup (manual) / change_followup_status
--   8. RPC: leave_enterprise_team
--   9. Function + pg_cron: auto_release_idle_followups (default 3 hari)
-- ─────────────────────────────────────────────────────────────────────────────


-- ═══════════════════════════════════════════════════════════════════════════
--   1. Trigger: auto-bump last_activity_at saat insert activity
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.bump_followup_last_activity()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.prospect_followups
     set last_activity_at = new.created_at
   where id = new.followup_id;
  return new;
end;
$$;

drop trigger if exists trg_bump_followup_last_activity on public.prospect_followup_activities;
create trigger trg_bump_followup_last_activity
  after insert on public.prospect_followup_activities
  for each row execute function public.bump_followup_last_activity();


-- ═══════════════════════════════════════════════════════════════════════════
--   2. RPC: create_personal_account
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.create_personal_account(
  p_display_name  text,
  p_university_id uuid default null
) returns table (success boolean, message text, account_id uuid)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid        uuid := auth.uid();
  v_account_id uuid;
begin
  if v_uid is null then
    return query select false, 'Tidak terautentikasi.', null::uuid;
    return;
  end if;

  -- Cek apakah user sudah aktif di account lain
  if exists (
    select 1 from public.university_account_members
    where user_id = v_uid and left_at is null
  ) then
    return query select false, 'Anda sudah aktif di account lain.', null::uuid;
    return;
  end if;

  insert into public.university_accounts (account_type, owner_user_id, university_id, display_name)
  values ('personal', v_uid, p_university_id, coalesce(p_display_name, 'Personal Account'))
  returning id into v_account_id;

  insert into public.university_account_members (account_id, user_id, role)
  values (v_account_id, v_uid, 'owner');

  return query select true, 'Personal account dibuat.', v_account_id;
end;
$$;

revoke all on function public.create_personal_account(text, uuid) from public;
grant execute on function public.create_personal_account(text, uuid) to authenticated;


-- ═══════════════════════════════════════════════════════════════════════════
--   3. RPC: create_enterprise_account
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.create_enterprise_account(
  p_display_name  text,
  p_university_id uuid default null
) returns table (success boolean, message text, account_id uuid)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid        uuid := auth.uid();
  v_account_id uuid;
begin
  if v_uid is null then
    return query select false, 'Tidak terautentikasi.', null::uuid;
    return;
  end if;

  if p_display_name is null or length(trim(p_display_name)) = 0 then
    return query select false, 'Nama enterprise wajib diisi.', null::uuid;
    return;
  end if;

  if exists (
    select 1 from public.university_account_members
    where user_id = v_uid and left_at is null
  ) then
    return query select false, 'Anda sudah aktif di account lain. Keluar dulu sebelum buat enterprise.', null::uuid;
    return;
  end if;

  insert into public.university_accounts (account_type, owner_user_id, university_id, display_name)
  values ('enterprise', v_uid, p_university_id, p_display_name)
  returning id into v_account_id;

  insert into public.university_account_members (account_id, user_id, role)
  values (v_account_id, v_uid, 'owner');

  return query select true, 'Enterprise account dibuat.', v_account_id;
end;
$$;

revoke all on function public.create_enterprise_account(text, uuid) from public;
grant execute on function public.create_enterprise_account(text, uuid) to authenticated;


-- ═══════════════════════════════════════════════════════════════════════════
--   4. RPC: subscribe_plan — atomic (log + tambah kuota)
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.subscribe_plan(
  p_account_id uuid,
  p_plan_code  text
) returns table (success boolean, message text, subscribe_log_id uuid, new_balance integer)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid         uuid := auth.uid();
  v_plan        record;
  v_account     record;
  v_log_id      uuid;
  v_uni_id      uuid;
  v_agent_id    uuid;
  v_commission  integer := 0;
  v_new_balance integer;
begin
  -- Hanya owner yang boleh subscribe
  if not public.is_account_owner(p_account_id, v_uid) and not public.is_admin() then
    return query select false, 'Hanya owner account yang boleh subscribe.', null::uuid, 0;
    return;
  end if;

  select * into v_plan from public.university_subscription_plans
   where code = p_plan_code and is_active = true;
  if v_plan is null then
    return query select false, 'Plan tidak ditemukan atau tidak aktif.', null::uuid, 0;
    return;
  end if;

  select * into v_account from public.university_accounts where id = p_account_id for update;
  if v_account is null then
    return query select false, 'Account tidak ditemukan.', null::uuid, 0;
    return;
  end if;

  if v_account.status <> 'active' then
    return query select false, 'Account tidak aktif.', null::uuid, 0;
    return;
  end if;

  if v_account.account_type <> v_plan.account_type then
    return query select false, format('Plan %s hanya untuk account %s.', v_plan.code, v_plan.account_type), null::uuid, 0;
    return;
  end if;

  v_uni_id := v_account.university_id;

  -- Cek apakah ada agent aktif yang mengklaim universitas ini
  if v_uni_id is not null then
    select agent_id into v_agent_id
      from public.agent_university_claims
     where university_id = v_uni_id and status = 'active'
     limit 1;

    if v_agent_id is not null then
      v_commission := round(v_plan.price * 0.10);  -- 10% komisi agen
    end if;
  end if;

  -- Insert log subscribe (admin-notif trigger akan auto-fire)
  insert into public.university_subscribe_logs (
    university_id, agent_id, amount, quota_purchased, commission_agent, account_id, plan_code
  ) values (
    v_uni_id, v_agent_id, v_plan.price, v_plan.quota, v_commission, p_account_id, v_plan.code
  ) returning id into v_log_id;

  -- Tambah kuota
  update public.university_accounts
     set quota_balance         = quota_balance + v_plan.quota,
         total_quota_purchased = total_quota_purchased + v_plan.quota
   where id = p_account_id
  returning quota_balance into v_new_balance;

  return query select true, format('Berhasil subscribe %s. +%s kuota.', v_plan.name, v_plan.quota),
                      v_log_id, v_new_balance;
end;
$$;

revoke all on function public.subscribe_plan(uuid, text) from public;
grant execute on function public.subscribe_plan(uuid, text) to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   5. RPC: invite_to_enterprise + accept_enterprise_invite (dengan merge)
-- ═══════════════════════════════════════════════════════════════════════════
-- Flow sederhana: owner langsung add user by user_id (UI hanya untuk owner)
-- Saat add member: kuota personal user yang join → di-merge ke enterprise

create or replace function public.add_enterprise_member(
  p_enterprise_account_id uuid,
  p_user_id               uuid
) returns table (success boolean, message text, merged_quota integer, enterprise_balance integer)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid               uuid := auth.uid();
  v_enterprise        record;
  v_personal          record;
  v_personal_id       uuid;
  v_quota_to_merge    integer := 0;
  v_new_balance       integer;
begin
  -- Owner check
  if not public.is_account_owner(p_enterprise_account_id, v_uid) and not public.is_admin() then
    return query select false, 'Hanya owner enterprise yang boleh menambah member.', 0, 0;
    return;
  end if;

  select * into v_enterprise from public.university_accounts
   where id = p_enterprise_account_id for update;

  if v_enterprise is null or v_enterprise.account_type <> 'enterprise' then
    return query select false, 'Account bukan enterprise.', 0, 0;
    return;
  end if;

  if v_enterprise.status <> 'active' then
    return query select false, 'Enterprise account tidak aktif.', 0, 0;
    return;
  end if;

  -- User belum boleh aktif di account lain
  if exists (
    select 1 from public.university_account_members
    where user_id = p_user_id and left_at is null
      and account_id <> p_enterprise_account_id
  ) then
    -- Cek apakah account aktif yang lain adalah personal-nya → bisa di-merge
    select ua.* into v_personal
      from public.university_account_members m
      join public.university_accounts ua on ua.id = m.account_id
     where m.user_id = p_user_id
       and m.left_at is null
       and ua.account_type = 'personal'
       and ua.status = 'active'
     limit 1;

    if v_personal is null then
      return query select false, 'User sudah aktif di enterprise lain. Tidak bisa di-merge.', 0, 0;
      return;
    end if;

    -- Lock personal account
    select * into v_personal from public.university_accounts
     where id = v_personal.id for update;

    v_personal_id    := v_personal.id;
    v_quota_to_merge := v_personal.quota_balance;

    -- Transfer kuota
    update public.university_accounts
       set quota_balance         = quota_balance + v_quota_to_merge,
           total_quota_purchased = total_quota_purchased + v_quota_to_merge
     where id = p_enterprise_account_id
    returning quota_balance into v_new_balance;

    -- Tandai personal account merged
    update public.university_accounts
       set status                 = 'merged',
           quota_balance          = 0,
           merged_into_account_id = p_enterprise_account_id
     where id = v_personal_id;

    -- Pindah subscribe_logs & prospect_usage_logs ke enterprise (history follow)
    update public.university_subscribe_logs
       set account_id = p_enterprise_account_id
     where account_id = v_personal_id;

    update public.prospect_usage_logs
       set account_id = p_enterprise_account_id
     where account_id = v_personal_id
       and not exists (
         select 1 from public.prospect_usage_logs p2
          where p2.account_id = p_enterprise_account_id
            and p2.student_id = prospect_usage_logs.student_id
       );

    -- Pindah prospect_followups yang TIDAK konflik. Yang konflik → release di personal.
    update public.prospect_followups
       set account_id = p_enterprise_account_id
     where account_id = v_personal_id
       and status in ('claimed','contacted','qualified')
       and not exists (
         select 1 from public.prospect_followups f2
          where f2.account_id = p_enterprise_account_id
            and f2.prospect_id = prospect_followups.prospect_id
            and f2.status in ('claimed','contacted','qualified')
       );

    update public.prospect_followups
       set status          = 'released',
           released_at     = now(),
           released_reason = 'left_team'
     where account_id = v_personal_id
       and status in ('claimed','contacted','qualified');

    -- Tutup membership personal
    update public.university_account_members
       set left_at = now()
     where account_id = v_personal_id
       and user_id   = p_user_id
       and left_at is null;

    -- Audit log
    insert into public.admin_audit_logs (admin_id, action, resource_type, resource_id, new_values)
    values (
      v_uid, 'account.merge_personal_to_enterprise', 'university_account', v_personal_id,
      jsonb_build_object(
        'enterprise_account_id', p_enterprise_account_id,
        'user_id', p_user_id,
        'quota_merged', v_quota_to_merge
      )
    );
  else
    -- User belum punya account aktif → cukup tambah membership
    select quota_balance into v_new_balance
      from public.university_accounts where id = p_enterprise_account_id;
  end if;

  -- Insert membership baru
  insert into public.university_account_members (account_id, user_id, role, invited_by)
  values (p_enterprise_account_id, p_user_id, 'member', v_uid);

  return query select true,
                      case when v_quota_to_merge > 0
                           then format('Member ditambahkan. %s kuota dari personal di-merge.', v_quota_to_merge)
                           else 'Member ditambahkan.'
                      end,
                      v_quota_to_merge, v_new_balance;
end;
$$;

revoke all on function public.add_enterprise_member(uuid, uuid) from public;
grant execute on function public.add_enterprise_member(uuid, uuid) to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   6. RPC: unlock_prospect — atomic (potong kuota + create followup)
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.unlock_prospect(
  p_account_id  uuid,
  p_prospect_id uuid
) returns table (success boolean, message text, followup_id uuid, quota_charged boolean, new_balance integer)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid          uuid := auth.uid();
  v_account      record;
  v_already_paid boolean := false;
  v_existing_fu  record;
  v_followup_id  uuid;
  v_new_balance  integer;
  v_school_id    uuid;
  v_agent_id     uuid;
begin
  if not public.is_account_member(p_account_id, v_uid) then
    return query select false, 'Anda bukan member account ini.', null::uuid, false, 0;
    return;
  end if;

  select * into v_account from public.university_accounts
   where id = p_account_id for update;

  if v_account is null or v_account.status <> 'active' then
    return query select false, 'Account tidak aktif.', null::uuid, false, 0;
    return;
  end if;

  -- Cek apakah student valid
  if not exists (select 1 from public.profiles where id = p_prospect_id) then
    return query select false, 'Prospek tidak ditemukan.', null::uuid, false, 0;
    return;
  end if;

  -- Cek apakah account sudah pernah unlock prospek ini (re-claim TIDAK potong kuota)
  v_already_paid := exists (
    select 1 from public.prospect_usage_logs
    where account_id = p_account_id and student_id = p_prospect_id
  );

  -- Cek apakah sudah ada followup aktif (account lain member follow-up)
  select * into v_existing_fu from public.prospect_followups
   where account_id  = p_account_id
     and prospect_id = p_prospect_id
     and status in ('claimed','contacted','qualified')
   limit 1;

  if v_existing_fu.id is not null then
    if v_existing_fu.assigned_to = v_uid then
      return query select true, 'Prospek sudah Anda follow-up.', v_existing_fu.id, false, v_account.quota_balance;
      return;
    else
      return query select false,
                          'Prospek sedang di-follow-up member lain di team Anda.',
                          v_existing_fu.id, false, v_account.quota_balance;
      return;
    end if;
  end if;

  -- Potong kuota kalau belum pernah dibayar
  if not v_already_paid then
    if v_account.quota_balance < 1 then
      return query select false, 'Kuota tidak cukup. Silakan top-up.', null::uuid, false, 0;
      return;
    end if;

    update public.university_accounts
       set quota_balance = quota_balance - 1
     where id = p_account_id
    returning quota_balance into v_new_balance;

    -- Resolve school & agent untuk komisi agen
    select school_id into v_school_id
      from public.profiles where id = p_prospect_id;

    if v_school_id is not null then
      select agent_id into v_agent_id
        from public.agent_school_claims
       where school_id = v_school_id and status = 'active'
       limit 1;
    end if;

    insert into public.prospect_usage_logs (
      university_id, student_id, school_id, agent_id, account_id, unlocked_by
    ) values (
      v_account.university_id, p_prospect_id, v_school_id, v_agent_id, p_account_id, v_uid
    );
  else
    v_new_balance := v_account.quota_balance;
  end if;

  -- Create followup baru
  insert into public.prospect_followups (account_id, prospect_id, assigned_to, status)
  values (p_account_id, p_prospect_id, v_uid, 'claimed')
  returning id into v_followup_id;

  insert into public.prospect_followup_activities (followup_id, user_id, activity_type, note)
  values (v_followup_id, v_uid, 'status_change', 'Prospek di-claim');

  return query select true,
                      case when v_already_paid
                           then 'Prospek di-claim ulang (tidak potong kuota).'
                           else 'Prospek berhasil di-unlock & di-claim.'
                      end,
                      v_followup_id, not v_already_paid, v_new_balance;
end;
$$;

revoke all on function public.unlock_prospect(uuid, uuid) from public;
grant execute on function public.unlock_prospect(uuid, uuid) to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   7. RPC: log_followup_activity (bump last_activity_at otomatis via trigger)
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.log_followup_activity(
  p_followup_id   uuid,
  p_activity_type text,
  p_note          text default null,
  p_metadata      jsonb default '{}'::jsonb
) returns table (success boolean, message text, activity_id uuid)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid       uuid := auth.uid();
  v_followup  record;
  v_act_id    uuid;
begin
  select f.* into v_followup from public.prospect_followups f
   where f.id = p_followup_id;

  if v_followup is null then
    return query select false, 'Followup tidak ditemukan.', null::uuid;
    return;
  end if;

  if not public.is_account_member(v_followup.account_id, v_uid) then
    return query select false, 'Anda bukan member account ini.', null::uuid;
    return;
  end if;

  if v_followup.assigned_to is distinct from v_uid
     and not public.is_account_owner(v_followup.account_id, v_uid) then
    return query select false, 'Anda bukan member yang ditugaskan.', null::uuid;
    return;
  end if;

  insert into public.prospect_followup_activities (followup_id, user_id, activity_type, note, metadata)
  values (p_followup_id, v_uid, p_activity_type, p_note, coalesce(p_metadata, '{}'::jsonb))
  returning id into v_act_id;

  return query select true, 'Aktivitas dicatat.', v_act_id;
end;
$$;

revoke all on function public.log_followup_activity(uuid, text, text, jsonb) from public;
grant execute on function public.log_followup_activity(uuid, text, text, jsonb) to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   8. RPC: change_followup_status (contacted/qualified/converted/rejected/released)
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.change_followup_status(
  p_followup_id uuid,
  p_new_status  text,
  p_note        text default null
) returns table (success boolean, message text)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid       uuid := auth.uid();
  v_followup  record;
  v_reason    text;
begin
  if p_new_status not in ('contacted','qualified','converted','rejected','released') then
    return query select false, 'Status tidak valid.';
    return;
  end if;

  select * into v_followup from public.prospect_followups
   where id = p_followup_id for update;

  if v_followup is null then
    return query select false, 'Followup tidak ditemukan.';
    return;
  end if;

  if not public.is_account_member(v_followup.account_id, v_uid) then
    return query select false, 'Anda bukan member account ini.';
    return;
  end if;

  if v_followup.assigned_to is distinct from v_uid
     and not public.is_account_owner(v_followup.account_id, v_uid) then
    return query select false, 'Hanya member yang ditugaskan atau owner yang boleh ubah status.';
    return;
  end if;

  v_reason := case p_new_status
                when 'released'  then 'manual'
                when 'converted' then 'converted'
                when 'rejected'  then 'rejected'
                else null
              end;

  update public.prospect_followups
     set status          = p_new_status,
         released_at     = case when p_new_status in ('released','converted','rejected') then now() else released_at end,
         released_reason = coalesce(v_reason, released_reason),
         last_activity_at = now()
   where id = p_followup_id;

  insert into public.prospect_followup_activities (followup_id, user_id, activity_type, note)
  values (p_followup_id, v_uid, 'status_change', coalesce(p_note, format('Status diubah ke %s.', p_new_status)));

  return query select true, format('Status diubah ke %s.', p_new_status);
end;
$$;

revoke all on function public.change_followup_status(uuid, text, text) from public;
grant execute on function public.change_followup_status(uuid, text, text) to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   9. RPC: leave_enterprise_team
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.leave_enterprise_team(
  p_account_id uuid
) returns table (success boolean, message text)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid       uuid := auth.uid();
  v_member    record;
begin
  select * into v_member from public.university_account_members
   where account_id = p_account_id and user_id = v_uid and left_at is null;

  if v_member is null then
    return query select false, 'Anda bukan member aktif di account ini.';
    return;
  end if;

  if v_member.role = 'owner' then
    return query select false, 'Owner tidak boleh leave. Transfer kepemilikan dulu atau suspend account.';
    return;
  end if;

  -- Release semua followup user ini di account ini
  update public.prospect_followups
     set status          = 'released',
         released_at     = now(),
         released_reason = 'left_team'
   where account_id  = p_account_id
     and assigned_to = v_uid
     and status in ('claimed','contacted','qualified');

  update public.university_account_members
     set left_at = now()
   where id = v_member.id;

  return query select true, 'Berhasil keluar dari team.';
end;
$$;

revoke all on function public.leave_enterprise_team(uuid) from public;
grant execute on function public.leave_enterprise_team(uuid) to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   10. Function: auto_release_idle_followups + pg_cron
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.auto_release_idle_followups()
returns integer
language plpgsql
security definer
set search_path = public
as $$
declare
  v_days  integer;
  v_count integer;
begin
  select value::int into v_days from public.system_settings
   where key = 'prospect_auto_release_days';

  if v_days is null or v_days < 1 then
    v_days := 3;
  end if;

  with released as (
    update public.prospect_followups
       set status          = 'released',
           released_at     = now(),
           released_reason = 'auto_idle'
     where status in ('claimed','contacted','qualified')
       and last_activity_at < now() - (v_days || ' days')::interval
     returning id
  )
  select count(*) into v_count from released;

  return v_count;
end;
$$;

revoke all on function public.auto_release_idle_followups() from public;
grant execute on function public.auto_release_idle_followups() to service_role;


-- Schedule via pg_cron (hourly check)
do $$ begin
  if exists (select 1 from pg_extension where extname = 'pg_cron') then
    perform cron.unschedule('tnt_auto_release_idle_followups')
      where exists (select 1 from cron.job where jobname = 'tnt_auto_release_idle_followups');
    perform cron.schedule(
      'tnt_auto_release_idle_followups',
      '15 * * * *',  -- menit ke-15 setiap jam
      $job$ select public.auto_release_idle_followups() $job$
    );
  else
    raise notice 'pg_cron extension belum aktif. Aktifkan di Dashboard → Database → Extensions, lalu jalankan ulang blok ini.';
  end if;
end $$;


-- ═══════════════════════════════════════════════════════════════════════════
--   11. Smoke test (manual, dijalankan satu-satu untuk verifikasi)
-- ═══════════════════════════════════════════════════════════════════════════
-- -- A. Buat personal account untuk user yang sedang login:
-- select * from public.create_personal_account('Akun Personal Budi', null);
--
-- -- B. Subscribe paket personal (ganti UUID account_id):
-- select * from public.subscribe_plan('<account-uuid>', 'personal_100');
--
-- -- C. Unlock prospek (ganti UUID):
-- select * from public.unlock_prospect('<account-uuid>', '<prospect-uuid>');
--
-- -- D. Log aktivitas:
-- select * from public.log_followup_activity('<followup-uuid>', 'call', 'Nelpon, gak diangkat');
--
-- -- E. Auto-release test (untuk testing manual, set last_activity ke 4 hari lalu):
-- update public.prospect_followups set last_activity_at = now() - interval '4 days';
-- select public.auto_release_idle_followups();
