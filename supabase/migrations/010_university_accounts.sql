-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — University Accounts (Individu & Enterprise)
-- Jalankan di: Supabase Dashboard → SQL Editor
--
-- Model:
--   • university_subscription_plans — pricing config-driven (JANGAN hardcoded)
--   • university_accounts           — kantong kuota (personal | enterprise)
--   • university_account_members    — owner + N member untuk enterprise
--   • prospect_followups            — assignment 1 prospek ke 1 member per account
--   • prospect_followup_activities  — log call/WA/email/meeting/note
--   • system_settings               — tunables (mis. prospect_auto_release_days)
--
-- Keputusan bisnis (lihat memory project-university-business-flow):
--   1. Saat individu join enterprise → kuota personal di-MERGE ke enterprise
--   2. Kuota TIDAK expired (seumur hidup account)
--   3. Re-claim prospek di team yang sama TIDAK potong kuota (sekali bayar per
--      account_id+prospect_id) — dijamin oleh logika unlock di migration 011
--   4. Auto-release prospek setelah 3 hari idle (di system_settings)
-- ─────────────────────────────────────────────────────────────────────────────


-- ═══════════════════════════════════════════════════════════════════════════
--   1. system_settings — key/value tunables
-- ═══════════════════════════════════════════════════════════════════════════

create table if not exists public.system_settings (
  key         text        primary key,
  value       text        not null,
  description text,
  updated_at  timestamptz default now()
);

alter table public.system_settings enable row level security;

create policy "Public read system settings"
  on public.system_settings for select using (true);

create policy "Admin manage system settings"
  on public.system_settings for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

insert into public.system_settings (key, value, description) values
  ('prospect_auto_release_days', '3', 'Hari idle sebelum prospect_followups status=claimed auto-released')
on conflict (key) do nothing;


-- ═══════════════════════════════════════════════════════════════════════════
--   2. university_subscription_plans — pricing
-- ═══════════════════════════════════════════════════════════════════════════

create table if not exists public.university_subscription_plans (
  code           text        primary key,
  name           text        not null,
  account_type   text        not null check (account_type in ('personal','enterprise')),
  price          integer     not null check (price > 0),
  quota          integer     not null check (quota > 0),
  is_active      boolean     default true,
  effective_from timestamptz default now(),
  created_at     timestamptz default now()
);

alter table public.university_subscription_plans enable row level security;

create policy "Public read active plans"
  on public.university_subscription_plans for select
  using (is_active = true);

create policy "Admin manage plans"
  on public.university_subscription_plans for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());

insert into public.university_subscription_plans (code, name, account_type, price, quota) values
  ('personal_100',    'Paket Personal 100 Data',    'personal',     100000,  100),
  ('enterprise_5000', 'Paket Enterprise 5000 Data', 'enterprise',  5000000, 5000)
on conflict (code) do nothing;


-- ═══════════════════════════════════════════════════════════════════════════
--   3. university_accounts — kantong kuota
-- ═══════════════════════════════════════════════════════════════════════════

create table if not exists public.university_accounts (
  id                     uuid        default gen_random_uuid() primary key,
  account_type           text        not null check (account_type in ('personal','enterprise')),
  owner_user_id          uuid        references auth.users(id) on delete restrict not null,
  university_id          uuid        references public.universities(id) on delete set null,
  display_name           text        not null,
  quota_balance          integer     not null default 0 check (quota_balance >= 0),
  total_quota_purchased  integer     not null default 0 check (total_quota_purchased >= 0),
  status                 text        not null default 'active'
                                     check (status in ('active','suspended','merged')),
  merged_into_account_id uuid        references public.university_accounts(id) on delete set null,
  created_at             timestamptz default now(),
  updated_at             timestamptz default now()
);

alter table public.university_accounts enable row level security;

create index if not exists idx_ua_owner       on public.university_accounts(owner_user_id);
create index if not exists idx_ua_university  on public.university_accounts(university_id);
create index if not exists idx_ua_status      on public.university_accounts(status);

-- 1 user hanya boleh punya 1 personal account aktif
create unique index if not exists uq_ua_personal_owner_active
  on public.university_accounts(owner_user_id)
  where account_type = 'personal' and status = 'active';

create trigger university_accounts_updated_at
  before update on public.university_accounts
  for each row execute function public.handle_updated_at();


-- ═══════════════════════════════════════════════════════════════════════════
--   4. university_account_members — siapa boleh pakai kuota
-- ═══════════════════════════════════════════════════════════════════════════

create table if not exists public.university_account_members (
  id          uuid        default gen_random_uuid() primary key,
  account_id  uuid        references public.university_accounts(id) on delete cascade not null,
  user_id     uuid        references auth.users(id) on delete cascade not null,
  role        text        not null check (role in ('owner','member')),
  invited_by  uuid        references auth.users(id) on delete set null,
  joined_at   timestamptz default now(),
  left_at     timestamptz
);

alter table public.university_account_members enable row level security;

create index if not exists idx_uam_account on public.university_account_members(account_id);
create index if not exists idx_uam_user    on public.university_account_members(user_id);

-- 1 user hanya boleh aktif di 1 account pada satu waktu
create unique index if not exists uq_uam_active_user
  on public.university_account_members(user_id)
  where left_at is null;


-- ═══════════════════════════════════════════════════════════════════════════
--   5. Extend university_subscribe_logs — link ke account & plan
-- ═══════════════════════════════════════════════════════════════════════════

alter table public.university_subscribe_logs
  add column if not exists account_id uuid references public.university_accounts(id) on delete set null,
  add column if not exists plan_code  text references public.university_subscription_plans(code) on delete set null;

create index if not exists idx_usl_account_id on public.university_subscribe_logs(account_id);


-- ═══════════════════════════════════════════════════════════════════════════
--   6. Extend prospect_usage_logs — link ke account
-- ═══════════════════════════════════════════════════════════════════════════

alter table public.prospect_usage_logs
  add column if not exists account_id    uuid references public.university_accounts(id) on delete set null,
  add column if not exists unlocked_by   uuid references auth.users(id) on delete set null;

create index if not exists idx_pul_account_id on public.prospect_usage_logs(account_id);

-- Sekali bayar per (account_id, prospect_id) — guard re-claim tidak potong kuota lagi
create unique index if not exists uq_pul_account_prospect
  on public.prospect_usage_logs(account_id, student_id)
  where account_id is not null;


-- ═══════════════════════════════════════════════════════════════════════════
--   7. prospect_followups — assignment per account
-- ═══════════════════════════════════════════════════════════════════════════

create table if not exists public.prospect_followups (
  id               uuid        default gen_random_uuid() primary key,
  account_id       uuid        references public.university_accounts(id) on delete cascade not null,
  prospect_id      uuid        references public.profiles(id) on delete cascade not null,
  assigned_to      uuid        references auth.users(id) on delete set null,
  status           text        not null default 'claimed'
                               check (status in ('claimed','contacted','qualified','converted','rejected','released')),
  claimed_at       timestamptz default now(),
  last_activity_at timestamptz default now(),
  released_at      timestamptz,
  released_reason  text        check (released_reason in ('manual','auto_idle','left_team','converted','rejected')),
  notes            text,
  created_at       timestamptz default now(),
  updated_at       timestamptz default now()
);

alter table public.prospect_followups enable row level security;

create index if not exists idx_pf_account     on public.prospect_followups(account_id);
create index if not exists idx_pf_prospect    on public.prospect_followups(prospect_id);
create index if not exists idx_pf_assigned    on public.prospect_followups(assigned_to);
create index if not exists idx_pf_status      on public.prospect_followups(status);
create index if not exists idx_pf_last_act    on public.prospect_followups(last_activity_at)
  where status in ('claimed','contacted','qualified');

-- Kunci utama: 1 prospek hanya 1 followup aktif per account
create unique index if not exists uq_pf_active_per_account
  on public.prospect_followups(account_id, prospect_id)
  where status in ('claimed','contacted','qualified');

create trigger prospect_followups_updated_at
  before update on public.prospect_followups
  for each row execute function public.handle_updated_at();


-- ═══════════════════════════════════════════════════════════════════════════
--   8. prospect_followup_activities — log aktivitas
-- ═══════════════════════════════════════════════════════════════════════════

create table if not exists public.prospect_followup_activities (
  id            uuid        default gen_random_uuid() primary key,
  followup_id   uuid        references public.prospect_followups(id) on delete cascade not null,
  user_id       uuid        references auth.users(id) on delete set null,
  activity_type text        not null check (activity_type in ('call','whatsapp','email','meeting','note','status_change')),
  note          text,
  metadata      jsonb       default '{}'::jsonb,
  created_at    timestamptz default now()
);

alter table public.prospect_followup_activities enable row level security;

create index if not exists idx_pfa_followup   on public.prospect_followup_activities(followup_id);
create index if not exists idx_pfa_user       on public.prospect_followup_activities(user_id);
create index if not exists idx_pfa_created    on public.prospect_followup_activities(created_at desc);


-- ═══════════════════════════════════════════════════════════════════════════
--   9. Helper: is_account_member(account_id) — dipakai oleh banyak RLS
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.is_account_member(
  p_account_id uuid,
  p_user_id    uuid default auth.uid()
) returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.university_account_members m
    where m.account_id = p_account_id
      and m.user_id    = p_user_id
      and m.left_at is null
  );
$$;

revoke all on function public.is_account_member(uuid, uuid) from public;
grant execute on function public.is_account_member(uuid, uuid) to authenticated, service_role;


create or replace function public.is_account_owner(
  p_account_id uuid,
  p_user_id    uuid default auth.uid()
) returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.university_account_members m
    where m.account_id = p_account_id
      and m.user_id    = p_user_id
      and m.role       = 'owner'
      and m.left_at is null
  );
$$;

revoke all on function public.is_account_owner(uuid, uuid) from public;
grant execute on function public.is_account_owner(uuid, uuid) to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   10. RLS Policies
-- ═══════════════════════════════════════════════════════════════════════════

-- ── university_accounts ─────────────────────────────────────────────────────
create policy "Members can view own account"
  on public.university_accounts for select
  using (public.is_account_member(id));

create policy "Owner can update own account"
  on public.university_accounts for update
  using (public.is_account_owner(id))
  with check (public.is_account_owner(id));

create policy "Authenticated can create account"
  on public.university_accounts for insert
  with check (auth.uid() = owner_user_id);

create policy "Admin manage accounts"
  on public.university_accounts for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());


-- ── university_account_members ──────────────────────────────────────────────
create policy "Members can view team members"
  on public.university_account_members for select
  using (public.is_account_member(account_id));

create policy "Owner can manage team members"
  on public.university_account_members for all
  using (public.is_account_owner(account_id))
  with check (public.is_account_owner(account_id));

create policy "Admin manage account members"
  on public.university_account_members for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());


-- ── university_subscribe_logs — extend existing (admin policy sudah ada) ────
create policy "Members can view own subscribe logs"
  on public.university_subscribe_logs for select
  using (account_id is not null and public.is_account_member(account_id));


-- ── prospect_usage_logs — extend existing (admin policy sudah ada) ──────────
create policy "Members can view own usage logs"
  on public.prospect_usage_logs for select
  using (account_id is not null and public.is_account_member(account_id));


-- ── prospect_followups ──────────────────────────────────────────────────────
create policy "Members can view team followups"
  on public.prospect_followups for select
  using (public.is_account_member(account_id));

create policy "Assigned member can update own followup"
  on public.prospect_followups for update
  using (
    public.is_account_member(account_id)
    and (assigned_to = auth.uid() or public.is_account_owner(account_id))
  )
  with check (
    public.is_account_member(account_id)
    and (assigned_to = auth.uid() or public.is_account_owner(account_id))
  );

create policy "Admin manage followups"
  on public.prospect_followups for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());


-- ── prospect_followup_activities ────────────────────────────────────────────
create policy "Members can view team followup activities"
  on public.prospect_followup_activities for select
  using (
    exists (
      select 1 from public.prospect_followups f
      where f.id = prospect_followup_activities.followup_id
        and public.is_account_member(f.account_id)
    )
  );

create policy "Members can insert activities on team followups"
  on public.prospect_followup_activities for insert
  with check (
    user_id = auth.uid()
    and exists (
      select 1 from public.prospect_followups f
      where f.id = prospect_followup_activities.followup_id
        and public.is_account_member(f.account_id)
    )
  );

create policy "Admin manage followup activities"
  on public.prospect_followup_activities for all
  using (auth.role() = 'service_role' or public.is_admin())
  with check (auth.role() = 'service_role' or public.is_admin());


-- ═══════════════════════════════════════════════════════════════════════════
--   11. Notes
-- ═══════════════════════════════════════════════════════════════════════════
-- Setelah migration ini:
--   1. Jalankan migration 011_university_account_logic.sql untuk RPC, trigger,
--      dan pg_cron job auto-release.
--   2. Verifikasi seed plan:
--        select * from public.university_subscription_plans;
--   3. Verifikasi setting:
--        select * from public.system_settings where key='prospect_auto_release_days';
