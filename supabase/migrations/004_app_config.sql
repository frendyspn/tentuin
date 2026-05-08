-- ─── App Config (force update & store URLs) ──────────────────────────────────
create table if not exists public.app_config (
  platform    text primary key,          -- 'android' | 'ios'
  min_version text not null default '1.0.0',
  store_url   text not null default '',
  updated_at  timestamptz default now()
);

-- Public read — semua user (termasuk guest) bisa baca versi minimum
alter table public.app_config enable row level security;
create policy "Public read app_config"
  on public.app_config for select using (true);

-- Seed data awal
insert into public.app_config (platform, min_version, store_url) values
  ('android', '1.0.0', 'https://play.google.com/store/apps/details?id=id.tentuin.student'),
  ('ios',     '1.0.0', 'https://apps.apple.com/app/id000000000')
on conflict (platform) do nothing;
