-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — School PIC App
-- Jalankan di: Supabase Dashboard → SQL Editor
--
-- Menambahkan role 'school_pic', kolom profiles.school_id, helper is_school_pic(),
-- RPC bind_school_pic_to_school(), dan RLS yang dibutuhkan aplikasi PIC sekolah:
--   • PIC bisa SELECT siswa di sekolahnya (profiles.school_id = pic.school_id)
--   • PIC bisa UPDATE profil sekolahnya (kolom non-sensitive)
--   • PIC bisa SELECT komisi sekolahnya (school_commissions)
--   • PIC bisa SELECT klaim agen di sekolahnya (agent_school_claims)
--
-- MVP: 1 PIC per sekolah (partial unique index).
-- Kolom profiles.school_name TIDAK dihapus untuk backward-compat dgn student app
-- yang masih kirim school_name (Expo). Migrasi penuh ke school_id menyusul.
-- ─────────────────────────────────────────────────────────────────────────────


-- ═══════════════════════════════════════════════════════════════════════════
--   1. profiles: tambah role 'school_pic' + kolom school_id
-- ═══════════════════════════════════════════════════════════════════════════

-- Drop & recreate check constraint utk tambah 'school_pic' ke role
alter table public.profiles
  drop constraint if exists profiles_role_check;

alter table public.profiles
  add constraint profiles_role_check
  check (role in ('student','agent','admin','super_admin','school_pic'));

-- Kolom school_id (FK ke schools), nullable krn student lama belum punya
alter table public.profiles
  add column if not exists school_id uuid references public.schools(id) on delete set null;

create index if not exists idx_profiles_school_id
  on public.profiles(school_id)
  where school_id is not null;

-- MVP: 1 PIC per sekolah
create unique index if not exists profiles_one_pic_per_school
  on public.profiles(school_id)
  where role = 'school_pic' and school_id is not null;


-- ═══════════════════════════════════════════════════════════════════════════
--   2. is_school_pic() helper
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.is_school_pic(uid uuid default auth.uid())
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
      and role = 'school_pic'
      and school_id is not null
  );
$$;

revoke all on function public.is_school_pic(uuid) from public;
grant execute on function public.is_school_pic(uuid) to authenticated, service_role;


-- Helper: ambil school_id PIC saat ini (atau NULL kalau bukan PIC)
create or replace function public.current_pic_school_id()
returns uuid
language sql
stable
security definer
set search_path = public
as $$
  select school_id
  from public.profiles
  where id = auth.uid()
    and role = 'school_pic';
$$;

revoke all on function public.current_pic_school_id() from public;
grant execute on function public.current_pic_school_id() to authenticated, service_role;


-- ═══════════════════════════════════════════════════════════════════════════
--   3. RPC: bind_school_pic_to_school
--   Dipanggil oleh aplikasi PIC setelah signup berhasil.
--   - Validasi claim_code: pending & belum expired
--   - Pastikan belum ada PIC lain di sekolah itu
--   - Set profiles.role='school_pic' + school_id
--   - Activate klaim agen (status='active') via redeem_school_claim_code()
-- ═══════════════════════════════════════════════════════════════════════════

create or replace function public.bind_school_pic_to_school(
  p_claim_code text
) returns table (success boolean, message text, school_id uuid)
language plpgsql security definer
set search_path = public as $$
declare
  v_uid          uuid := auth.uid();
  v_claim_id     uuid;
  v_school_id    uuid;
  v_status       text;
  v_expires      timestamptz;
  v_existing_pic uuid;
  v_redeem       record;
begin
  if v_uid is null then
    return query select false, 'Tidak terautentikasi.', null::uuid;
    return;
  end if;

  -- Cari claim berdasarkan kode
  select id, agent_school_claims.school_id, status, expires_at
    into v_claim_id, v_school_id, v_status, v_expires
  from public.agent_school_claims
  where claim_code = p_claim_code
  limit 1;

  if v_claim_id is null then
    return query select false, 'Kode registrasi tidak ditemukan.', null::uuid;
    return;
  end if;

  if v_status <> 'pending' then
    return query select false, format('Kode sudah %s, tidak bisa dipakai.', v_status), null::uuid;
    return;
  end if;

  if v_expires is not null and v_expires < now() then
    update public.agent_school_claims set status = 'expired' where id = v_claim_id;
    return query select false, 'Kode sudah kadaluarsa.', null::uuid;
    return;
  end if;

  -- Pastikan belum ada PIC aktif di sekolah ini
  select id into v_existing_pic
  from public.profiles
  where role = 'school_pic'
    and profiles.school_id = v_school_id
  limit 1;

  if v_existing_pic is not null and v_existing_pic <> v_uid then
    return query select false, 'Sekolah ini sudah memiliki PIC terdaftar.', null::uuid;
    return;
  end if;

  -- Update profile user → role school_pic + link ke sekolah
  update public.profiles
  set role      = 'school_pic',
      school_id = v_school_id
  where id = v_uid;

  -- Activate klaim agen
  select * into v_redeem
  from public.redeem_school_claim_code(p_claim_code, v_school_id);

  if v_redeem.success is not true then
    -- Rollback profile change kalau redeem gagal
    update public.profiles set role = 'student', school_id = null where id = v_uid;
    return query select false, coalesce(v_redeem.message, 'Gagal mengaktifkan klaim.'), null::uuid;
    return;
  end if;

  return query select true, 'PIC sekolah berhasil terdaftar.', v_school_id;
end;
$$;

revoke all on function public.bind_school_pic_to_school(text) from public;
grant execute on function public.bind_school_pic_to_school(text) to authenticated;


-- ═══════════════════════════════════════════════════════════════════════════
--   4. RLS: izinkan PIC akses data sekolahnya
-- ═══════════════════════════════════════════════════════════════════════════

-- ─── 4a. profiles: PIC bisa SELECT siswa di sekolahnya ──────────────────────
drop policy if exists "School PIC can read students in their school" on public.profiles;
create policy "School PIC can read students in their school"
  on public.profiles for select
  using (
    role = 'student'
    and school_id is not null
    and school_id = public.current_pic_school_id()
  );


-- ─── 4b. schools: PIC bisa UPDATE sekolahnya ────────────────────────────────
-- Catatan: di Postgres RLS UPDATE policy berlaku per-row. Kita tidak bisa
-- batasi kolom mana yg di-update lewat RLS — itu dijaga dari sisi client
-- (whitelist field) + (opsional) trigger yg mencegah perubahan kolom sensitif.
drop policy if exists "School PIC can update own school" on public.schools;
create policy "School PIC can update own school"
  on public.schools for update
  using (id = public.current_pic_school_id())
  with check (id = public.current_pic_school_id());

-- Trigger guard: blok perubahan kolom sensitif oleh PIC (npsn, is_active,
-- total_students). Admin/service_role bypass karena auth.role()='service_role'.
create or replace function public.guard_school_update_by_pic()
returns trigger language plpgsql as $$
begin
  if auth.role() = 'service_role' or public.is_admin() then
    return new;
  end if;

  if public.is_school_pic() then
    if new.npsn           is distinct from old.npsn           then
      raise exception 'PIC tidak boleh mengubah NPSN.';
    end if;
    if new.is_active      is distinct from old.is_active      then
      raise exception 'PIC tidak boleh mengubah status aktif sekolah.';
    end if;
    if new.total_students is distinct from old.total_students then
      raise exception 'PIC tidak boleh mengubah jumlah siswa.';
    end if;
  end if;

  return new;
end;
$$;

drop trigger if exists schools_guard_pic_update on public.schools;
create trigger schools_guard_pic_update
  before update on public.schools
  for each row execute function public.guard_school_update_by_pic();


-- ─── 4c. school_commissions: PIC bisa SELECT komisi sekolahnya ─────────────
drop policy if exists "School PIC can read own school commissions" on public.school_commissions;
create policy "School PIC can read own school commissions"
  on public.school_commissions for select
  using (school_id = public.current_pic_school_id());


-- ─── 4d. agent_school_claims: PIC bisa SELECT klaim sekolahnya ─────────────
drop policy if exists "School PIC can read claims for own school" on public.agent_school_claims;
create policy "School PIC can read claims for own school"
  on public.agent_school_claims for select
  using (school_id = public.current_pic_school_id());


-- ═══════════════════════════════════════════════════════════════════════════
--   5. Notes
-- ═══════════════════════════════════════════════════════════════════════════
-- Setelah migration ini di-apply:
--   1. Verifikasi schema:
--        select column_name, data_type from information_schema.columns
--        where table_schema='public' and table_name='profiles' order by ordinal_position;
--        -- harus ada kolom school_id uuid
--
--   2. Verifikasi role check:
--        insert into public.profiles(id, role) values (gen_random_uuid(), 'school_pic');
--        -- harus berhasil (atau gagal di FK auth.users, BUKAN di check constraint)
--
--   3. Smoke test RPC (di SQL Editor sbg user PIC):
--        select * from public.bind_school_pic_to_school('TNT-XXXXXX');
--
--   4. Cek RLS:
--        set role authenticated;
--        select set_config('request.jwt.claim.sub', '<uuid-pic>', true);
--        select count(*) from public.profiles;             -- harus return siswa di sekolahnya
--        select * from public.school_commissions;          -- hanya sekolahnya
