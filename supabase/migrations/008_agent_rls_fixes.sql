-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — RLS Fixes: izinkan agen self-register & klaim sendiri
-- Jalankan di: Supabase Dashboard → SQL Editor
--
-- Migration 006 hanya punya policy SELECT untuk agen + ALL untuk service_role,
-- sehingga agen tidak bisa INSERT row mereka sendiri (HTTP 403, error 42501).
-- ─────────────────────────────────────────────────────────────────────────────

-- ─── 1. Agents: self-register ────────────────────────────────────────────────
drop policy if exists "Agents can self register" on public.agents;
create policy "Agents can self register"
  on public.agents for insert
  with check (auth.uid() = id);


-- ─── 2. Agent School Claims: agen klaim sekolah sendiri ──────────────────────
drop policy if exists "Agents can claim schools" on public.agent_school_claims;
create policy "Agents can claim schools"
  on public.agent_school_claims for insert
  with check (auth.uid() = agent_id);


-- ─── 3. Agent University Claims: agen klaim kampus sendiri ───────────────────
drop policy if exists "Agents can claim universities" on public.agent_university_claims;
create policy "Agents can claim universities"
  on public.agent_university_claims for insert
  with check (auth.uid() = agent_id);
