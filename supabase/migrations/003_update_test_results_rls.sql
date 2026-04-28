-- ─────────────────────────────────────────────────────────────────────────────
-- Update test_results RLS policy to allow filtering by user_id without auth
-- Jalankan di: Supabase Dashboard → SQL Editor
-- ─────────────────────────────────────────────────────────────────────────────

-- Drop old policies
drop policy if exists "Users can view own results" on public.test_results;
drop policy if exists "Users can insert own results" on public.test_results;

-- Create new permissive policies
create policy "Anyone can view results for any user"
  on public.test_results for select
  using (true);

create policy "Authenticated users can insert own results"
  on public.test_results for insert
  with check (auth.uid() = user_id);
