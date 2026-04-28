-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — Initial Database Schema
-- Jalankan di: Supabase Dashboard → SQL Editor
-- ─────────────────────────────────────────────────────────────────────────────

-- ─── 1. Profiles (extend auth.users) ────────────────────────────────────────
create table if not exists public.profiles (
  id              uuid        references auth.users(id) on delete cascade primary key,
  full_name       text,
  school_name     text,
  city            text,
  birth_year      integer     check (birth_year >= 1990 and birth_year <= 2015),
  avatar_url      text,
  has_completed_onboarding boolean default false,
  created_at      timestamptz default now(),
  updated_at      timestamptz default now()
);

-- RLS
alter table public.profiles enable row level security;

create policy "Users can view own profile"
  on public.profiles for select
  using (auth.uid() = id);

create policy "Users can update own profile"
  on public.profiles for update
  using (auth.uid() = id);

-- Auto-create profile saat user baru daftar
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name, avatar_url)
  values (
    new.id,
    new.raw_user_meta_data->>'full_name',
    new.raw_user_meta_data->>'avatar_url'
  );
  return new;
end;
$$;

create or replace trigger on_auth_user_created
  after insert on auth.users
  for each row
  execute function public.handle_new_user();

-- Auto-update updated_at
create or replace function public.handle_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace trigger profiles_updated_at
  before update on public.profiles
  for each row
  execute function public.handle_updated_at();


-- ─── 2. Majors (master data jurusan) ────────────────────────────────────────
create table if not exists public.majors (
  id                uuid        default gen_random_uuid() primary key,
  name              text        not null,
  category          text        not null check (category in ('realistic','investigative','artistic','social','enterprising','conventional')),
  description       text,
  career_prospects  jsonb       default '[]',
  riasec_match      text[]      default '{}',
  created_at        timestamptz default now()
);

alter table public.majors enable row level security;
create policy "Anyone can view majors" on public.majors for select using (true);


-- ─── 3. Universities (master data universitas) ───────────────────────────────
create table if not exists public.universities (
  id              uuid        default gen_random_uuid() primary key,
  name            text        not null,
  city            text        not null,
  province        text,
  logo_url        text,
  cover_url       text,
  description     text,
  accreditation   text,
  website_url     text,
  is_partner      boolean     default false,
  partner_tier    text        check (partner_tier in ('basic','partner','premium')),
  majors          uuid[]      default '{}',
  created_at      timestamptz default now()
);

alter table public.universities enable row level security;
create policy "Anyone can view universities" on public.universities for select using (true);


-- ─── 4. Test Results ────────────────────────────────────────────────────────
create table if not exists public.test_results (
  id                  uuid        default gen_random_uuid() primary key,
  user_id             uuid        references auth.users(id) on delete cascade not null,
  scores              jsonb       not null, -- {realistic: 85, investigative: 72, ...}
  riasec_code         text        not null, -- e.g. "SAI"
  recommended_majors  jsonb       default '[]',
  completed_at        timestamptz default now()
);

alter table public.test_results enable row level security;

create policy "Users can view own results"
  on public.test_results for select
  using (auth.uid() = user_id);

create policy "Users can insert own results"
  on public.test_results for insert
  with check (auth.uid() = user_id);


-- ─── 5. Seed: Contoh data jurusan ────────────────────────────────────────────
insert into public.majors (name, category, description, career_prospects, riasec_match) values
('Psikologi',               'social',         'Mempelajari perilaku manusia dan proses mental.', '["Psikolog","Konselor","HRD","Researcher"]', '{SAI,SIA,SEA}'),
('Teknik Informatika',      'investigative',  'Pengembangan software, algoritma, dan sistem komputer.', '["Software Engineer","Data Scientist","Product Manager"]', '{IRE,IRA,IRC}'),
('Desain Komunikasi Visual','artistic',       'Menggabungkan seni dan teknologi untuk komunikasi visual.', '["Graphic Designer","UI/UX Designer","Art Director"]', '{AIS,AIE,ASI}'),
('Manajemen',               'enterprising',   'Pengelolaan organisasi, strategi bisnis, dan kepemimpinan.', '["Manager","Entrepreneur","Konsultan Bisnis"]', '{ESA,ECS,ESI}'),
('Kedokteran',              'investigative',  'Ilmu kesehatan dan pengobatan untuk kesejahteraan manusia.', '["Dokter","Peneliti Medis","Spesialis"]', '{ISR,IRS,ISA}'),
('Akuntansi',               'conventional',  'Pengelolaan keuangan, audit, dan pelaporan bisnis.', '["Akuntan","Auditor","Financial Analyst"]', '{CSE,CES,CSI}')
on conflict do nothing;
