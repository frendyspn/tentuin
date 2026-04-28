-- ─────────────────────────────────────────────────────────────────────────────
-- University Bookmarks
-- ─────────────────────────────────────────────────────────────────────────────

-- ─── 6. University Bookmarks (track user interest dalam universitas) ─────────
create table if not exists public.university_bookmarks (
  id              uuid        default gen_random_uuid() primary key,
  user_id         uuid        references auth.users(id) on delete cascade not null,
  university_id   uuid        references public.universities(id) on delete cascade not null,
  major_names     text[]      default '{}', -- nama jurusan yang diminati user
  created_at      timestamptz default now(),
  updated_at      timestamptz default now(),
  unique(user_id, university_id)
);

-- RLS
alter table public.university_bookmarks enable row level security;

create policy "Users can view their own bookmarks"
  on public.university_bookmarks for select
  using (auth.uid() = user_id);

create policy "Users can insert their own bookmarks"
  on public.university_bookmarks for insert
  with check (auth.uid() = user_id);

create policy "Users can update their own bookmarks"
  on public.university_bookmarks for update
  using (auth.uid() = user_id);

create policy "Users can delete their own bookmarks"
  on public.university_bookmarks for delete
  using (auth.uid() = user_id);

-- Auto-update updated_at
create or replace trigger university_bookmarks_updated_at
  before update on public.university_bookmarks
  for each row
  execute function public.handle_updated_at();

-- Indexes untuk performa query
create index if not exists university_bookmarks_user_id_idx on public.university_bookmarks(user_id);
create index if not exists university_bookmarks_university_id_idx on public.university_bookmarks(university_id);
create index if not exists university_bookmarks_created_at_idx on public.university_bookmarks(created_at desc);
