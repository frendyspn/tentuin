-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — Admin Dashboard (role, is_admin helper, audit logs, push tokens)
-- Jalankan di: Supabase Dashboard → SQL Editor
-- ─────────────────────────────────────────────────────────────────────────────

-- ─── 1. profiles.role ────────────────────────────────────────────────────────
alter table public.profiles
  add column if not exists role text not null default 'student'
    check (role in ('student','agent','admin','super_admin'));

create index if not exists idx_profiles_role
  on public.profiles(role)
  where role <> 'student';


-- ─── 2. is_admin() helper (SECURITY DEFINER, recursion-safe) ─────────────────
create or replace function public.is_admin(uid uuid default auth.uid())
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.profiles
    where id = uid
      and role in ('admin','super_admin')
  );
$$;

revoke all on function public.is_admin(uuid) from public;
grant execute on function public.is_admin(uuid) to authenticated, service_role;


-- ─── 3. RLS overhaul: ganti admin policy 'service_role' → 'service_role OR is_admin()'
--      Tabel-tabel ini sebelumnya hanya bisa diakses dari service_role.
--      Setelah migration ini, akun dengan role admin/super_admin juga bisa.

-- schools
drop policy if exists "Admin can manage schools" on public.schools;
create policy "Admin can manage schools"
  on public.schools for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- agents
drop policy if exists "Admin can manage agents" on public.agents;
create policy "Admin can manage agents"
  on public.agents for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- agent_school_claims
drop policy if exists "Admin can manage school claims" on public.agent_school_claims;
create policy "Admin can manage school claims"
  on public.agent_school_claims for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- agent_university_claims
drop policy if exists "Admin can manage university claims" on public.agent_university_claims;
create policy "Admin can manage university claims"
  on public.agent_university_claims for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- school_targets
drop policy if exists "Admin can manage targets" on public.school_targets;
create policy "Admin can manage targets"
  on public.school_targets for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- university_subscribe_logs
drop policy if exists "Admin can manage subscribe logs" on public.university_subscribe_logs;
create policy "Admin can manage subscribe logs"
  on public.university_subscribe_logs for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- prospect_usage_logs
drop policy if exists "Admin can manage prospect logs" on public.prospect_usage_logs;
create policy "Admin can manage prospect logs"
  on public.prospect_usage_logs for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- agent_commissions
drop policy if exists "Admin can manage commissions" on public.agent_commissions;
create policy "Admin can manage commissions"
  on public.agent_commissions for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- school_commissions
drop policy if exists "Admin can manage school commissions" on public.school_commissions;
create policy "Admin can manage school commissions"
  on public.school_commissions for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- agent_withdrawals
drop policy if exists "Admin can manage withdrawals" on public.agent_withdrawals;
create policy "Admin can manage withdrawals"
  on public.agent_withdrawals for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- universities (001 belum punya admin policy → tambah)
drop policy if exists "Admin can manage universities" on public.universities;
create policy "Admin can manage universities"
  on public.universities for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

-- profiles (admin perlu read all profiles untuk rekap student & lookup admin)
drop policy if exists "Admin can read all profiles" on public.profiles;
create policy "Admin can read all profiles"
  on public.profiles for select
  using (public.is_admin());


-- ─── 4. admin_audit_logs ─────────────────────────────────────────────────────
create table if not exists public.admin_audit_logs (
  id            uuid        default gen_random_uuid() primary key,
  admin_id      uuid        references auth.users(id) on delete set null,
  action        text        not null,                  -- 'withdrawal.approve', 'agent.suspend', dll
  resource_type text        not null,                  -- 'agent_withdrawal', 'agent', 'school', dll
  resource_id   uuid,
  old_values    jsonb,
  new_values    jsonb,
  ip_address    inet,
  user_agent    text,
  created_at    timestamptz default now()
);

create index if not exists idx_aal_admin    on public.admin_audit_logs(admin_id);
create index if not exists idx_aal_resource on public.admin_audit_logs(resource_type, resource_id);
create index if not exists idx_aal_created  on public.admin_audit_logs(created_at desc);

alter table public.admin_audit_logs enable row level security;

create policy "Admin can read audit logs"
  on public.admin_audit_logs for select
  using (public.is_admin());

create policy "Admin can insert own audit log"
  on public.admin_audit_logs for insert
  with check (public.is_admin() and admin_id = auth.uid());


-- ─── 5. admin_push_tokens (FCM tokens — terpisah dari profiles.push_token Expo)
create table if not exists public.admin_push_tokens (
  id            uuid        default gen_random_uuid() primary key,
  user_id       uuid        not null references auth.users(id) on delete cascade,
  fcm_token     text        not null,
  platform      text        not null check (platform in ('android','ios','web')),
  device_label  text,
  last_seen_at  timestamptz default now(),
  created_at    timestamptz default now(),
  unique (user_id, fcm_token)
);

create index if not exists idx_apt_user on public.admin_push_tokens(user_id);

alter table public.admin_push_tokens enable row level security;

create policy "Admin can manage own tokens"
  on public.admin_push_tokens for all
  using (auth.uid() = user_id and public.is_admin())
  with check (auth.uid() = user_id and public.is_admin());

create policy "Service role full access push tokens"
  on public.admin_push_tokens for all
  using (auth.role() = 'service_role')
  with check (auth.role() = 'service_role');


-- ─── 6. Notes ────────────────────────────────────────────────────────────────
-- Setelah migration di-apply:
--   1. Tandai user pertama sebagai admin:
--      update public.profiles set role='admin' where id='<uuid-admin>';
--      atau: update public.profiles set role='admin'
--            where id = (select id from auth.users where email='admin@tentuin.id');
--
--   2. Verifikasi:
--      select public.is_admin('<admin-uuid>');   -- true
--      select public.is_admin('<student-uuid>'); -- false
--
--   3. Trigger notifikasi & pg_cron jobs ditambahkan di migration berikutnya
--      (adm_app_010_admin_notifications.sql) setelah Edge Function send-admin-push
--      deploy dan settings 'app.admin_push_fn_url' / 'app.admin_push_fn_key'
--      terpasang.
