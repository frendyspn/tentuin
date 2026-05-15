-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — Two-step Claim Approval Flow
-- Jalankan di: Supabase Dashboard → SQL Editor
--
-- Flow lama: agent klaim → langsung jadi punya agent (active)
-- Flow baru: agent klaim → generate kode → PIC sekolah/kampus input kode di
--            aplikasi mereka → claim baru jadi 'active'.
-- Berlaku untuk agent_school_claims DAN agent_university_claims.
-- Masa berlaku kode pending: 30 hari.
-- ─────────────────────────────────────────────────────────────────────────────


-- ═══════════════════════════════════════════════════════════════════════════
--   1. Helper: function generator kode klaim
-- ═══════════════════════════════════════════════════════════════════════════
create or replace function public.gen_claim_code()
returns text language sql volatile as $$
  select 'TNT-' || upper(substring(md5(random()::text || clock_timestamp()::text), 1, 6))
$$;


-- ═══════════════════════════════════════════════════════════════════════════
--   2. Agent School Claims — schema upgrade
-- ═══════════════════════════════════════════════════════════════════════════

alter table public.agent_school_claims
  add column if not exists claim_code  text,
  add column if not exists status      text not null default 'pending'
                          check (status in ('pending','active','expired','cancelled')),
  add column if not exists verified_at timestamptz,
  add column if not exists expires_at  timestamptz default (now() + interval '30 days');

-- Backfill: existing claims yang is_active=true → status='active'
update public.agent_school_claims
set status      = 'active',
    verified_at = coalesce(verified_at, claimed_at)
where is_active = true and status = 'pending';

-- claim_code unique constraint
create unique index if not exists agent_school_claims_claim_code_unique
  on public.agent_school_claims (claim_code) where claim_code is not null;

-- Drop old unique constraint (1 active per school via is_active boolean)
alter table public.agent_school_claims drop constraint if exists agent_school_claims_school_id_is_active_key;

-- New constraint: max 1 claim per sekolah (pending atau active) lintas semua agen
create unique index if not exists agent_school_claims_one_per_school
  on public.agent_school_claims (school_id) where status in ('pending', 'active');

-- Trigger: auto-generate claim_code on insert
create or replace function public.set_school_claim_code()
returns trigger language plpgsql as $$
begin
  if new.claim_code is null then
    new.claim_code := public.gen_claim_code();
  end if;
  return new;
end;
$$;

drop trigger if exists agent_school_claims_set_code on public.agent_school_claims;
create trigger agent_school_claims_set_code
  before insert on public.agent_school_claims
  for each row execute function public.set_school_claim_code();


-- ═══════════════════════════════════════════════════════════════════════════
--   3. Agent University Claims — schema upgrade (sama persis)
-- ═══════════════════════════════════════════════════════════════════════════

alter table public.agent_university_claims
  add column if not exists claim_code  text,
  add column if not exists status      text not null default 'pending'
                          check (status in ('pending','active','expired','cancelled')),
  add column if not exists verified_at timestamptz,
  add column if not exists expires_at  timestamptz default (now() + interval '30 days');

update public.agent_university_claims
set status      = 'active',
    verified_at = coalesce(verified_at, claimed_at)
where is_active = true and status = 'pending';

create unique index if not exists agent_university_claims_claim_code_unique
  on public.agent_university_claims (claim_code) where claim_code is not null;

alter table public.agent_university_claims drop constraint if exists agent_university_claims_university_id_is_active_key;

create unique index if not exists agent_university_claims_one_per_university
  on public.agent_university_claims (university_id) where status in ('pending', 'active');

create or replace function public.set_university_claim_code()
returns trigger language plpgsql as $$
begin
  if new.claim_code is null then
    new.claim_code := public.gen_claim_code();
  end if;
  return new;
end;
$$;

drop trigger if exists agent_university_claims_set_code on public.agent_university_claims;
create trigger agent_university_claims_set_code
  before insert on public.agent_university_claims
  for each row execute function public.set_university_claim_code();


-- ═══════════════════════════════════════════════════════════════════════════
--   4. RLS — agent boleh lihat klaim milik agen LAIN selama pending/active
--          (agar tahu sekolah/kampus mana yang sudah ke-klaim)
-- ═══════════════════════════════════════════════════════════════════════════

drop policy if exists "Anyone can see active or pending school claims" on public.agent_school_claims;
create policy "Anyone can see active or pending school claims"
  on public.agent_school_claims for select
  using (status in ('pending', 'active'));

drop policy if exists "Anyone can see active or pending university claims" on public.agent_university_claims;
create policy "Anyone can see active or pending university claims"
  on public.agent_university_claims for select
  using (status in ('pending', 'active'));


-- ═══════════════════════════════════════════════════════════════════════════
--   5. RPC — redeem code (dipakai aplikasi sekolah/kampus nanti)
-- ═══════════════════════════════════════════════════════════════════════════

-- Sekolah PIC redeem kode → claim status='active'
create or replace function public.redeem_school_claim_code(
  p_claim_code text,
  p_school_id  uuid
) returns table (success boolean, message text, claim_id uuid)
language plpgsql security definer
set search_path = public as $$
declare
  v_claim_id  uuid;
  v_status    text;
  v_expires   timestamptz;
begin
  select id, status, expires_at
    into v_claim_id, v_status, v_expires
  from public.agent_school_claims
  where claim_code = p_claim_code and school_id = p_school_id
  limit 1;

  if v_claim_id is null then
    return query select false, 'Kode tidak valid atau bukan untuk sekolah ini.', null::uuid;
    return;
  end if;

  if v_status <> 'pending' then
    return query select false, format('Kode sudah %s.', v_status), v_claim_id;
    return;
  end if;

  if v_expires is not null and v_expires < now() then
    update public.agent_school_claims set status = 'expired' where id = v_claim_id;
    return query select false, 'Kode sudah kadaluarsa.', v_claim_id;
    return;
  end if;

  update public.agent_school_claims
  set status      = 'active',
      verified_at = now(),
      is_active   = true
  where id = v_claim_id;

  return query select true, 'Klaim berhasil diaktifkan.', v_claim_id;
end;
$$;

-- Universitas PIC redeem kode (sama)
create or replace function public.redeem_university_claim_code(
  p_claim_code   text,
  p_university_id uuid
) returns table (success boolean, message text, claim_id uuid)
language plpgsql security definer
set search_path = public as $$
declare
  v_claim_id  uuid;
  v_status    text;
  v_expires   timestamptz;
begin
  select id, status, expires_at
    into v_claim_id, v_status, v_expires
  from public.agent_university_claims
  where claim_code = p_claim_code and university_id = p_university_id
  limit 1;

  if v_claim_id is null then
    return query select false, 'Kode tidak valid atau bukan untuk kampus ini.', null::uuid;
    return;
  end if;

  if v_status <> 'pending' then
    return query select false, format('Kode sudah %s.', v_status), v_claim_id;
    return;
  end if;

  if v_expires is not null and v_expires < now() then
    update public.agent_university_claims set status = 'expired' where id = v_claim_id;
    return query select false, 'Kode sudah kadaluarsa.', v_claim_id;
    return;
  end if;

  update public.agent_university_claims
  set status      = 'active',
      verified_at = now(),
      is_active   = true
  where id = v_claim_id;

  return query select true, 'Klaim berhasil diaktifkan.', v_claim_id;
end;
$$;


-- ═══════════════════════════════════════════════════════════════════════════
--   6. Optional cleanup: function untuk auto-expire pending claims
--      (bisa dipanggil via Supabase scheduled function / cron)
-- ═══════════════════════════════════════════════════════════════════════════
create or replace function public.expire_old_pending_claims()
returns integer language plpgsql security definer
set search_path = public as $$
declare
  v_count integer := 0;
  v_school_count integer;
  v_uni_count    integer;
begin
  update public.agent_school_claims set status = 'expired'
   where status = 'pending' and expires_at < now();
  get diagnostics v_school_count = row_count;

  update public.agent_university_claims set status = 'expired'
   where status = 'pending' and expires_at < now();
  get diagnostics v_uni_count = row_count;

  return v_school_count + v_uni_count;
end;
$$;
