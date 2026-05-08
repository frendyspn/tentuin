-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — Agent & Commission System
-- ─────────────────────────────────────────────────────────────────────────────

-- ─── 1. Schools ───────────────────────────────────────────────────────────────
create table if not exists public.schools (
  id              uuid        default gen_random_uuid() primary key,
  name            text        not null,
  npsn            text        unique,           -- Nomor Pokok Sekolah Nasional
  city            text        not null,
  province        text        not null,
  address         text,
  email           text,
  phone           text,
  logo_url        text,
  total_students  integer     default 0 check (total_students >= 0),
  is_active       boolean     default true,
  created_at      timestamptz default now(),
  updated_at      timestamptz default now()
);

alter table public.schools enable row level security;
create policy "Anyone can view active schools"
  on public.schools for select using (is_active = true);
create policy "Admin can manage schools"
  on public.schools for all using (auth.role() = 'service_role');

create index if not exists idx_schools_city     on public.schools(city);
create index if not exists idx_schools_province on public.schools(province);
create index if not exists idx_schools_npsn     on public.schools(npsn);

create trigger schools_updated_at
  before update on public.schools
  for each row execute function public.handle_updated_at();


-- ─── 2. Agents ────────────────────────────────────────────────────────────────
create table if not exists public.agents (
  id                  uuid        references auth.users(id) on delete cascade primary key,
  full_name           text        not null,
  email               text        not null,
  phone               text,
  referral_code       text        not null unique,
  status              text        not null default 'active'
                                  check (status in ('active','suspended','inactive')),
  is_owner            boolean     default false,   -- true hanya untuk Agen 001
  last_active_at      timestamptz default now(),
  bank_name           text,
  bank_account_number text,
  bank_account_name   text,
  notes               text,       -- catatan admin
  created_at          timestamptz default now(),
  updated_at          timestamptz default now()
);

alter table public.agents enable row level security;
create policy "Agents can view own data"
  on public.agents for select using (auth.uid() = id);
create policy "Agents can update own data"
  on public.agents for update using (auth.uid() = id);
create policy "Admin can manage agents"
  on public.agents for all using (auth.role() = 'service_role');

create index if not exists idx_agents_referral_code on public.agents(referral_code);
create index if not exists idx_agents_status        on public.agents(status);

create trigger agents_updated_at
  before update on public.agents
  for each row execute function public.handle_updated_at();


-- ─── 3. Agent School Claims ───────────────────────────────────────────────────
create table if not exists public.agent_school_claims (
  id          uuid        default gen_random_uuid() primary key,
  agent_id    uuid        references public.agents(id) on delete cascade not null,
  school_id   uuid        references public.schools(id) on delete cascade not null,
  is_active   boolean     default true,
  claimed_at  timestamptz default now(),
  released_at timestamptz,
  unique (school_id, is_active) -- hanya 1 klaim aktif per sekolah
);

alter table public.agent_school_claims enable row level security;
create policy "Agents can view own school claims"
  on public.agent_school_claims for select using (auth.uid() = agent_id);
create policy "Admin can manage school claims"
  on public.agent_school_claims for all using (auth.role() = 'service_role');

create index if not exists idx_asc_agent_id  on public.agent_school_claims(agent_id);
create index if not exists idx_asc_school_id on public.agent_school_claims(school_id);


-- ─── 4. Agent University Claims ───────────────────────────────────────────────
create table if not exists public.agent_university_claims (
  id              uuid        default gen_random_uuid() primary key,
  agent_id        uuid        references public.agents(id) on delete cascade not null,
  university_id   uuid        references public.universities(id) on delete cascade not null,
  is_active       boolean     default true,
  claimed_at      timestamptz default now(),
  released_at     timestamptz,
  unique (university_id, is_active) -- hanya 1 klaim aktif per universitas
);

alter table public.agent_university_claims enable row level security;
create policy "Agents can view own university claims"
  on public.agent_university_claims for select using (auth.uid() = agent_id);
create policy "Admin can manage university claims"
  on public.agent_university_claims for all using (auth.role() = 'service_role');

create index if not exists idx_auc_agent_id      on public.agent_university_claims(agent_id);
create index if not exists idx_auc_university_id on public.agent_university_claims(university_id);


-- ─── 5. School Targets ────────────────────────────────────────────────────────
create table if not exists public.school_targets (
  id              uuid        default gen_random_uuid() primary key,
  school_id       uuid        references public.schools(id) on delete cascade not null,
  year            integer     not null check (year >= 2024),
  annual_target   integer     not null check (annual_target > 0),
  -- monthly_targets[1..12]: null = pakai default (annual/12), isi jika dikustomisasi
  monthly_targets integer[]   default null,
  created_at      timestamptz default now(),
  updated_at      timestamptz default now(),
  unique (school_id, year)
);

alter table public.school_targets enable row level security;
create policy "Admin can manage targets"
  on public.school_targets for all using (auth.role() = 'service_role');
create policy "Agents can view targets for claimed schools"
  on public.school_targets for select
  using (
    exists (
      select 1 from public.agent_school_claims asc2
      where asc2.school_id = school_targets.school_id
        and asc2.agent_id  = auth.uid()
        and asc2.is_active = true
    )
  );

create trigger school_targets_updated_at
  before update on public.school_targets
  for each row execute function public.handle_updated_at();


-- ─── 6. University Subscribe Logs ─────────────────────────────────────────────
create table if not exists public.university_subscribe_logs (
  id                  uuid        default gen_random_uuid() primary key,
  university_id       uuid        references public.universities(id) on delete cascade not null,
  agent_id            uuid        references public.agents(id) on delete set null,
  amount              integer     not null check (amount > 0),   -- total bayar (Rp)
  quota_purchased     integer     not null check (quota_purchased > 0),
  commission_agent    integer     not null default 0,            -- 10% jika ada agen
  subscribed_at       timestamptz default now()
);

alter table public.university_subscribe_logs enable row level security;
create policy "Admin can manage subscribe logs"
  on public.university_subscribe_logs for all using (auth.role() = 'service_role');
create policy "Agents can view own subscribe logs"
  on public.university_subscribe_logs for select using (auth.uid() = agent_id);

create index if not exists idx_usl_university_id on public.university_subscribe_logs(university_id);
create index if not exists idx_usl_agent_id      on public.university_subscribe_logs(agent_id);
create index if not exists idx_usl_subscribed_at on public.university_subscribe_logs(subscribed_at);


-- ─── 7. Prospect Usage Logs ───────────────────────────────────────────────────
-- Dicatat setiap kali universitas menggunakan 1 kuota untuk melihat data siswa
create table if not exists public.prospect_usage_logs (
  id                      uuid        default gen_random_uuid() primary key,
  university_id           uuid        references public.universities(id) on delete cascade not null,
  student_id              uuid        references public.profiles(id) on delete cascade not null,
  school_id               uuid        references public.schools(id) on delete set null,
  agent_id                uuid        references public.agents(id) on delete set null,
  commission_calculated   boolean     default false,
  used_at                 timestamptz default now()
);

alter table public.prospect_usage_logs enable row level security;
create policy "Admin can manage prospect logs"
  on public.prospect_usage_logs for all using (auth.role() = 'service_role');

create index if not exists idx_pul_university_id on public.prospect_usage_logs(university_id);
create index if not exists idx_pul_school_id     on public.prospect_usage_logs(school_id);
create index if not exists idx_pul_agent_id      on public.prospect_usage_logs(agent_id);
create index if not exists idx_pul_used_at       on public.prospect_usage_logs(used_at);


-- ─── 8. Agent Commissions ─────────────────────────────────────────────────────
-- Rekap komisi per bulan per agen (di-generate oleh cron job)
create table if not exists public.agent_commissions (
  id                uuid        default gen_random_uuid() primary key,
  agent_id          uuid        references public.agents(id) on delete cascade not null,
  month             integer     not null check (month between 1 and 12),
  year              integer     not null check (year >= 2024),
  stream_a_amount   integer     not null default 0,  -- dari prospek siswa
  stream_b_amount   integer     not null default 0,  -- dari subscribe universitas
  total_amount      integer     generated always as (stream_a_amount + stream_b_amount) stored,
  status            text        not null default 'pending'
                                check (status in ('pending','paid','cancelled')),
  notes             text,
  created_at        timestamptz default now(),
  updated_at        timestamptz default now(),
  unique (agent_id, month, year)
);

alter table public.agent_commissions enable row level security;
create policy "Agents can view own commissions"
  on public.agent_commissions for select using (auth.uid() = agent_id);
create policy "Admin can manage commissions"
  on public.agent_commissions for all using (auth.role() = 'service_role');

create index if not exists idx_ac_agent_id on public.agent_commissions(agent_id);
create index if not exists idx_ac_year     on public.agent_commissions(year, month);

create trigger agent_commissions_updated_at
  before update on public.agent_commissions
  for each row execute function public.handle_updated_at();


-- ─── 9. School Commissions ────────────────────────────────────────────────────
create table if not exists public.school_commissions (
  id          uuid        default gen_random_uuid() primary key,
  school_id   uuid        references public.schools(id) on delete cascade not null,
  agent_id    uuid        references public.agents(id) on delete set null,
  month       integer     not null check (month between 1 and 12),
  year        integer     not null check (year >= 2024),
  amount      integer     not null default 0,
  status      text        not null default 'pending'
              check (status in ('pending','paid','cancelled')),
  created_at  timestamptz default now(),
  updated_at  timestamptz default now(),
  unique (school_id, month, year)
);

alter table public.school_commissions enable row level security;
create policy "Admin can manage school commissions"
  on public.school_commissions for all using (auth.role() = 'service_role');
create policy "Agents can view school commissions for claimed schools"
  on public.school_commissions for select
  using (auth.uid() = agent_id);

create trigger school_commissions_updated_at
  before update on public.school_commissions
  for each row execute function public.handle_updated_at();


-- ─── 10. Agent Withdrawals ────────────────────────────────────────────────────
create table if not exists public.agent_withdrawals (
  id              uuid        default gen_random_uuid() primary key,
  agent_id        uuid        references public.agents(id) on delete cascade not null,
  amount          integer     not null check (amount > 0),
  status          text        not null default 'requested'
                  check (status in ('requested','approved','rejected','transferred')),
  requested_at    timestamptz default now(),
  processed_at    timestamptz,
  admin_notes     text
);

alter table public.agent_withdrawals enable row level security;
create policy "Agents can view own withdrawals"
  on public.agent_withdrawals for select using (auth.uid() = agent_id);
create policy "Agents can request withdrawal"
  on public.agent_withdrawals for insert with check (auth.uid() = agent_id);
create policy "Admin can manage withdrawals"
  on public.agent_withdrawals for all using (auth.role() = 'service_role');

create index if not exists idx_aw_agent_id on public.agent_withdrawals(agent_id);
create index if not exists idx_aw_status   on public.agent_withdrawals(status);


-- ─── 11. Update Universities: tambah kolom kuota & PIC ────────────────────────
alter table public.universities
  add column if not exists pic_name             text,
  add column if not exists pic_phone            text,
  add column if not exists quota_balance        integer default 0 check (quota_balance >= 0),
  add column if not exists total_quota_purchased integer default 0 check (total_quota_purchased >= 0);
