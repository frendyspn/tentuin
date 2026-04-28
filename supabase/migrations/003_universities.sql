-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — Universities & Majors
-- ─────────────────────────────────────────────────────────────────────────────

-- ─── 1. Update Universities table ─────────────────────────────────────────────

-- Add missing columns if they don't exist
alter table if exists public.universities
  add column if not exists short_name text,
  add column if not exists type text check (type in ('negeri','swasta')),
  add column if not exists email text,
  add column if not exists phone text,
  add column if not exists is_active boolean default true,
  add column if not exists updated_at timestamptz default now();

-- ─── 2. Migrate Majors table ──────────────────────────────────────────────────

-- Backup old data if it exists
create table if not exists public.majors_old as select * from public.majors;

-- Drop old majors table
drop table if exists public.majors cascade;

-- Create new majors table with correct schema
create table public.majors (
  id             uuid    default gen_random_uuid() primary key,
  university_id  uuid    references public.universities(id) on delete cascade not null,
  name           text    not null,
  faculty        text,
  riasec_codes   text[]  default '{}',   -- e.g. ARRAY['R','I']
  is_active      boolean default true,
  created_at     timestamptz default now()
);

alter table public.majors enable row level security;
create policy "Anyone can view active majors"
  on public.majors for select using (is_active = true);

-- Index untuk query cepat
create index if not exists idx_majors_university_id  on public.majors(university_id);
create index if not exists idx_majors_riasec_codes   on public.majors using gin(riasec_codes);
create index if not exists idx_universities_partner  on public.universities(is_partner) where is_partner = true;

-- ─── 3. Seed: Universities ────────────────────────────────────────────────────

insert into public.universities (id, name, short_name, city, province, type, website_url) values

-- Jawa Barat & DKI Jakarta
('00000000-0001-0000-0000-000000000000', 'Universitas Indonesia',        'UI',          'Depok',      'Jawa Barat',        'negeri', 'https://www.ui.ac.id'),
('00000000-0002-0000-0000-000000000000', 'Institut Teknologi Bandung',   'ITB',         'Bandung',    'Jawa Barat',        'negeri', 'https://www.itb.ac.id'),
('00000000-0003-0000-0000-000000000000', 'Universitas Padjadjaran',      'Unpad',       'Bandung',    'Jawa Barat',        'negeri', 'https://www.unpad.ac.id'),
('00000000-0004-0000-0000-000000000000', 'Institut Pertanian Bogor',     'IPB',         'Bogor',      'Jawa Barat',        'negeri', 'https://www.ipb.ac.id'),
('00000000-0005-0000-0000-000000000000', 'Institut Pemerintahan Dalam Negeri', 'IPDN',  'Jatinangor', 'Jawa Barat',        'negeri', 'https://www.ipdn.ac.id'),
('00000000-0006-0000-0000-000000000000', 'Universitas Bina Nusantara',   'Binus',       'Jakarta',    'DKI Jakarta',       'swasta', 'https://www.binus.ac.id'),
('00000000-0007-0000-0000-000000000000', 'Universitas Trisakti',         'Trisakti',    'Jakarta',    'DKI Jakarta',       'swasta', 'https://www.trisakti.ac.id'),
('00000000-0008-0000-0000-000000000000', 'Universitas Tarumanagara',     'Untar',       'Jakarta',    'DKI Jakarta',       'swasta', 'https://www.untar.ac.id'),
('00000000-0009-0000-0000-000000000000', 'Universitas Paramadina',       'Paramadina',  'Jakarta',    'DKI Jakarta',       'swasta', 'https://www.paramadina.ac.id'),

-- Jawa Tengah & D.I. Yogyakarta
('00000000-0010-0000-0000-000000000000', 'Universitas Gadjah Mada',      'UGM',         'Yogyakarta', 'D.I. Yogyakarta',   'negeri', 'https://www.ugm.ac.id'),
('00000000-0011-0000-0000-000000000000', 'Universitas Diponegoro',       'Undip',       'Semarang',   'Jawa Tengah',       'negeri', 'https://www.undip.ac.id'),
('00000000-0012-0000-0000-000000000000', 'Universitas Negeri Semarang',  'Unnes',       'Semarang',   'Jawa Tengah',       'negeri', 'https://www.unnes.ac.id'),
('00000000-0013-0000-0000-000000000000', 'Universitas Islam Indonesia',  'UII',         'Yogyakarta', 'D.I. Yogyakarta',   'swasta', 'https://www.uii.ac.id'),
('00000000-0014-0000-0000-000000000000', 'Institut Seni Indonesia Yogyakarta', 'ISI Yogyakarta', 'Yogyakarta', 'D.I. Yogyakarta', 'negeri', 'https://www.isi.ac.id'),

-- Jawa Timur
('00000000-0015-0000-0000-000000000000', 'Institut Teknologi Sepuluh Nopember', 'ITS',   'Surabaya',   'Jawa Timur',        'negeri', 'https://www.its.ac.id'),
('00000000-0016-0000-0000-000000000000', 'Universitas Airlangga',        'Unair',       'Surabaya',   'Jawa Timur',        'negeri', 'https://www.unair.ac.id'),
('00000000-0017-0000-0000-000000000000', 'Universitas Brawijaya',        'UB',          'Malang',     'Jawa Timur',        'negeri', 'https://www.ub.ac.id'),
('00000000-0018-0000-0000-000000000000', 'Universitas Negeri Malang',    'UM',          'Malang',     'Jawa Timur',        'negeri', 'https://www.um.ac.id'),

-- Bandung (Swasta)
('00000000-0019-0000-0000-000000000000', 'Telkom University',            'Tel-U',       'Bandung',    'Jawa Barat',        'swasta', 'https://www.telkomuniversity.ac.id'),

-- Sumatera
('00000000-0020-0000-0000-000000000000', 'Universitas Sumatera Utara',   'USU',         'Medan',      'Sumatera Utara',    'negeri', 'https://www.usu.ac.id'),
('00000000-0021-0000-0000-000000000000', 'Universitas Sriwijaya',        'Unsri',       'Palembang',  'Sumatera Selatan',  'negeri', 'https://www.unsri.ac.id'),

-- Kalimantan
('00000000-0022-0000-0000-000000000000', 'Universitas Mulawarman',       'Unmul',       'Samarinda',  'Kalimantan Timur',  'negeri', 'https://www.unmul.ac.id'),

-- Sulawesi
('00000000-0023-0000-0000-000000000000', 'Universitas Hasanuddin',       'Unhas',       'Makassar',   'Sulawesi Selatan',  'negeri', 'https://www.unhas.ac.id'),

-- Bali & NTB
('00000000-0024-0000-0000-000000000000', 'Universitas Udayana',          'Unud',        'Denpasar',   'Bali',              'negeri', 'https://www.unud.ac.id'),
('00000000-0025-0000-0000-000000000000', 'Universitas Mataram',          'Unram',       'Mataram',    'Nusa Tenggara Barat', 'negeri', 'https://www.unram.ac.id'),

-- Seni Jakarta
('00000000-0026-0000-0000-000000000000', 'Institut Kesenian Jakarta',    'IKJ',         'Jakarta',    'DKI Jakarta',       'swasta', 'https://www.ikj.ac.id')

on conflict (id) do nothing;

-- ─── 4. Seed: Majors ──────────────────────────────────────────────────────────
-- riasec_codes: R=Realistic, I=Investigative, A=Artistic, S=Social, E=Enterprising, C=Conventional

insert into public.majors (university_id, name, faculty, riasec_codes) values

-- ── UI ───────────────────────────────────────────────────────────────────────
('00000000-0001-0000-0000-000000000000', 'Kedokteran',              'Fakultas Kedokteran',              ARRAY['I','S']),
('00000000-0001-0000-0000-000000000000', 'Kedokteran Gigi',         'Fakultas Kedokteran Gigi',         ARRAY['I','S']),
('00000000-0001-0000-0000-000000000000', 'Keperawatan',             'Fakultas Ilmu Keperawatan',        ARRAY['S','I']),
('00000000-0001-0000-0000-000000000000', 'Farmasi',                 'Fakultas Farmasi',                 ARRAY['I','C']),
('00000000-0001-0000-0000-000000000000', 'Kesehatan Masyarakat',    'Fakultas Kesehatan Masyarakat',    ARRAY['S','I']),
('00000000-0001-0000-0000-000000000000', 'Teknik Sipil',            'Fakultas Teknik',                  ARRAY['R','I']),
('00000000-0001-0000-0000-000000000000', 'Teknik Mesin',            'Fakultas Teknik',                  ARRAY['R','I']),
('00000000-0001-0000-0000-000000000000', 'Teknik Elektro',          'Fakultas Teknik',                  ARRAY['R','I']),
('00000000-0001-0000-0000-000000000000', 'Teknik Kimia',            'Fakultas Teknik',                  ARRAY['R','I']),
('00000000-0001-0000-0000-000000000000', 'Teknik Industri',         'Fakultas Teknik',                  ARRAY['R','C']),
('00000000-0001-0000-0000-000000000000', 'Teknik Komputer',         'Fakultas Teknik',                  ARRAY['R','I']),
('00000000-0001-0000-0000-000000000000', 'Arsitektur',              'Fakultas Teknik',                  ARRAY['R','A']),
('00000000-0001-0000-0000-000000000000', 'Ilmu Komputer',           'Fakultas Ilmu Komputer',           ARRAY['I','C']),
('00000000-0001-0000-0000-000000000000', 'Sistem Informasi',        'Fakultas Ilmu Komputer',           ARRAY['I','C']),
('00000000-0001-0000-0000-000000000000', 'Hukum',                   'Fakultas Hukum',                   ARRAY['E','C']),
('00000000-0001-0000-0000-000000000000', 'Ilmu Ekonomi',            'Fakultas Ekonomi dan Bisnis',      ARRAY['I','C']),
('00000000-0001-0000-0000-000000000000', 'Akuntansi',               'Fakultas Ekonomi dan Bisnis',      ARRAY['C','I']),
('00000000-0001-0000-0000-000000000000', 'Manajemen',               'Fakultas Ekonomi dan Bisnis',      ARRAY['E','C']),
('00000000-0001-0000-0000-000000000000', 'Psikologi',               'Fakultas Psikologi',               ARRAY['S','I']),
('00000000-0001-0000-0000-000000000000', 'Ilmu Komunikasi',         'FISIP',                            ARRAY['S','A']),
('00000000-0001-0000-0000-000000000000', 'Ilmu Politik',            'FISIP',                            ARRAY['E','S']),
('00000000-0001-0000-0000-000000000000', 'Sosiologi',               'FISIP',                            ARRAY['S','I']),
('00000000-0001-0000-0000-000000000000', 'Ilmu Hubungan Internasional', 'FISIP',                        ARRAY['E','S']),
('00000000-0001-0000-0000-000000000000', 'Administrasi Publik',     'FISIP',                            ARRAY['C','E']),
('00000000-0001-0000-0000-000000000000', 'Administrasi Bisnis',     'FISIP',                            ARRAY['E','C']),
('00000000-0001-0000-0000-000000000000', 'Sastra Indonesia',        'Fakultas Ilmu Pengetahuan Budaya', ARRAY['A','S']),
('00000000-0001-0000-0000-000000000000', 'Sastra Inggris',          'Fakultas Ilmu Pengetahuan Budaya', ARRAY['A','S']),
('00000000-0001-0000-0000-000000000000', 'Filsafat',                'Fakultas Ilmu Pengetahuan Budaya', ARRAY['I','A']),
('00000000-0001-0000-0000-000000000000', 'Matematika',              'FMIPA',                            ARRAY['I','C']),
('00000000-0001-0000-0000-000000000000', 'Fisika',                  'FMIPA',                            ARRAY['I','R']),
('00000000-0001-0000-0000-000000000000', 'Kimia',                   'FMIPA',                            ARRAY['I','R']),
('00000000-0001-0000-0000-000000000000', 'Biologi',                 'FMIPA',                            ARRAY['I','R']),
('00000000-0001-0000-0000-000000000000', 'Statistika',              'FMIPA',                            ARRAY['C','I']),
('00000000-0001-0000-0000-000000000000', 'Geografi',                'FMIPA',                            ARRAY['R','I']),

-- ── ITB ──────────────────────────────────────────────────────────────────────
('00000000-0002-0000-0000-000000000000', 'Teknik Sipil',            'FTSL',   ARRAY['R','I']),
('00000000-0002-0000-0000-000000000000', 'Teknik Mesin',            'FTMD',   ARRAY['R','I']),
('00000000-0002-0000-0000-000000000000', 'Teknik Elektro',          'STEI',   ARRAY['R','I']),
('00000000-0002-0000-0000-000000000000', 'Teknik Kimia',            'FTI',    ARRAY['R','I']),
('00000000-0002-0000-0000-000000000000', 'Teknik Industri',         'FTI',    ARRAY['R','C']),
('00000000-0002-0000-0000-000000000000', 'Teknik Pertambangan',     'FTTM',   ARRAY['R','I']),
('00000000-0002-0000-0000-000000000000', 'Teknik Informatika',      'STEI',   ARRAY['I','C']),
('00000000-0002-0000-0000-000000000000', 'Sistem dan Teknologi Informasi', 'STEI', ARRAY['I','C']),
('00000000-0002-0000-0000-000000000000', 'Arsitektur',              'SAPPK',  ARRAY['R','A']),
('00000000-0002-0000-0000-000000000000', 'Perencanaan Wilayah dan Kota', 'SAPPK', ARRAY['R','E']),
('00000000-0002-0000-0000-000000000000', 'Desain Produk Industri',  'FSRD',   ARRAY['A','R']),
('00000000-0002-0000-0000-000000000000', 'Desain Interior',         'FSRD',   ARRAY['A','R']),
('00000000-0002-0000-0000-000000000000', 'Desain Komunikasi Visual','FSRD',   ARRAY['A','E']),
('00000000-0002-0000-0000-000000000000', 'Seni Rupa',               'FSRD',   ARRAY['A']),
('00000000-0002-0000-0000-000000000000', 'Kriya',                   'FSRD',   ARRAY['A','R']),
('00000000-0002-0000-0000-000000000000', 'Matematika',              'FMIPA',  ARRAY['I','C']),
('00000000-0002-0000-0000-000000000000', 'Fisika',                  'FMIPA',  ARRAY['I','R']),
('00000000-0002-0000-0000-000000000000', 'Kimia',                   'FMIPA',  ARRAY['I','R']),
('00000000-0002-0000-0000-000000000000', 'Biologi',                 'FMIPA',  ARRAY['I','R']),
('00000000-0002-0000-0000-000000000000', 'Astronomi',               'FMIPA',  ARRAY['I','R']),
('00000000-0002-0000-0000-000000000000', 'Manajemen',               'SBM',    ARRAY['E','C']),
('00000000-0002-0000-0000-000000000000', 'Kewirausahaan',           'SBM',    ARRAY['E','A']),

-- ── UGM ──────────────────────────────────────────────────────────────────────
('00000000-0010-0000-0000-000000000000', 'Kedokteran',              'FK',     ARRAY['I','S']),
('00000000-0010-0000-0000-000000000000', 'Kedokteran Gigi',         'FKG',    ARRAY['I','S']),
('00000000-0010-0000-0000-000000000000', 'Farmasi',                 'FF',     ARRAY['I','C']),
('00000000-0010-0000-0000-000000000000', 'Keperawatan',             'FK',     ARRAY['S','I']),
('00000000-0010-0000-0000-000000000000', 'Kedokteran Hewan',        'FKH',    ARRAY['I','R']),
('00000000-0010-0000-0000-000000000000', 'Teknik Sipil',            'FT',     ARRAY['R','I']),
('00000000-0010-0000-0000-000000000000', 'Teknik Mesin',            'FT',     ARRAY['R','I']),
('00000000-0010-0000-0000-000000000000', 'Teknik Elektro',          'FT',     ARRAY['R','I']),
('00000000-0010-0000-0000-000000000000', 'Teknik Kimia',            'FT',     ARRAY['R','I']),
('00000000-0010-0000-0000-000000000000', 'Teknik Industri',         'FT',     ARRAY['R','C']),
('00000000-0010-0000-0000-000000000000', 'Teknik Informatika',      'FT',     ARRAY['I','C']),
('00000000-0010-0000-0000-000000000000', 'Arsitektur',              'FT',     ARRAY['R','A']),
('00000000-0010-0000-0000-000000000000', 'Hukum',                   'FH',     ARRAY['E','C']),
('00000000-0010-0000-0000-000000000000', 'Ilmu Ekonomi',            'FEB',    ARRAY['I','C']),
('00000000-0010-0000-0000-000000000000', 'Akuntansi',               'FEB',    ARRAY['C','I']),
('00000000-0010-0000-0000-000000000000', 'Manajemen',               'FEB',    ARRAY['E','C']),
('00000000-0010-0000-0000-000000000000', 'Psikologi',               'Fak. Psikologi', ARRAY['S','I']),
('00000000-0010-0000-0000-000000000000', 'Ilmu Komunikasi',         'FISIPOL', ARRAY['S','A']),
('00000000-0010-0000-0000-000000000000', 'Ilmu Politik',            'FISIPOL', ARRAY['E','S']),
('00000000-0010-0000-0000-000000000000', 'Sosiologi',               'FISIPOL', ARRAY['S','I']),
('00000000-0010-0000-0000-000000000000', 'Ilmu Hubungan Internasional', 'FISIPOL', ARRAY['E','S']),
('00000000-0010-0000-0000-000000000000', 'Administrasi Publik',     'FISIPOL', ARRAY['C','E']),
('00000000-0010-0000-0000-000000000000', 'Sastra Indonesia',        'FIB',    ARRAY['A','S']),
('00000000-0010-0000-0000-000000000000', 'Sastra Inggris',          'FIB',    ARRAY['A','S']),
('00000000-0010-0000-0000-000000000000', 'Filsafat',                'FIB',    ARRAY['I','A']),
('00000000-0010-0000-0000-000000000000', 'Ilmu Sejarah',            'FIB',    ARRAY['I','A']),
('00000000-0010-0000-0000-000000000000', 'Matematika',              'FMIPA',  ARRAY['I','C']),
('00000000-0010-0000-0000-000000000000', 'Fisika',                  'FMIPA',  ARRAY['I','R']),
('00000000-0010-0000-0000-000000000000', 'Kimia',                   'FMIPA',  ARRAY['I','R']),
('00000000-0010-0000-0000-000000000000', 'Biologi',                 'FMIPA',  ARRAY['I','R']),
('00000000-0010-0000-0000-000000000000', 'Statistika',              'FMIPA',  ARRAY['C','I']),
('00000000-0010-0000-0000-000000000000', 'Ilmu Komputer',           'FMIPA',  ARRAY['I','C']),
('00000000-0010-0000-0000-000000000000', 'Agroteknologi',           'Fak. Pertanian', ARRAY['R','I']),
('00000000-0010-0000-0000-000000000000', 'Agribisnis',              'Fak. Pertanian', ARRAY['E','R']),
('00000000-0010-0000-0000-000000000000', 'Kehutanan',               'Fak. Kehutanan', ARRAY['R','I']),
('00000000-0010-0000-0000-000000000000', 'Teknologi Pangan',        'FATETA', ARRAY['I','R']),

-- ── ITS ──────────────────────────────────────────────────────────────────────
('00000000-0015-0000-0000-000000000000', 'Teknik Sipil',            'FTSP',   ARRAY['R','I']),
('00000000-0015-0000-0000-000000000000', 'Teknik Mesin',            'FTI',    ARRAY['R','I']),
('00000000-0015-0000-0000-000000000000', 'Teknik Elektro',          'FTEIC',  ARRAY['R','I']),
('00000000-0015-0000-0000-000000000000', 'Teknik Kimia',            'FTI',    ARRAY['R','I']),
('00000000-0015-0000-0000-000000000000', 'Teknik Industri',         'FTI',    ARRAY['R','C']),
('00000000-0015-0000-0000-000000000000', 'Teknik Informatika',      'FTEIC',  ARRAY['I','C']),
('00000000-0015-0000-0000-000000000000', 'Sistem Informasi',        'FTEIC',  ARRAY['I','C']),
('00000000-0015-0000-0000-000000000000', 'Teknik Perkapalan',       'FTK',    ARRAY['R','I']),
('00000000-0015-0000-0000-000000000000', 'Desain Produk Industri',  'FDSK',   ARRAY['A','R']),
('00000000-0015-0000-0000-000000000000', 'Desain Interior',         'FDSK',   ARRAY['A','R']),
('00000000-0015-0000-0000-000000000000', 'Desain Komunikasi Visual','FDSK',   ARRAY['A','E']),
('00000000-0015-0000-0000-000000000000', 'Arsitektur',              'FADP',   ARRAY['R','A']),
('00000000-0015-0000-0000-000000000000', 'Matematika',              'FSAD',   ARRAY['I','C']),
('00000000-0015-0000-0000-000000000000', 'Fisika',                  'FSAD',   ARRAY['I','R']),
('00000000-0015-0000-0000-000000000000', 'Statistika',              'FSAD',   ARRAY['C','I']),
('00000000-0015-0000-0000-000000000000', 'Manajemen Bisnis',        'SBM',    ARRAY['E','C']),

-- ── Unair ─────────────────────────────────────────────────────────────────────
('00000000-0016-0000-0000-000000000000', 'Kedokteran',              'FK',     ARRAY['I','S']),
('00000000-0016-0000-0000-000000000000', 'Farmasi',                 'FF',     ARRAY['I','C']),
('00000000-0016-0000-0000-000000000000', 'Keperawatan',             'FKp',    ARRAY['S','I']),
('00000000-0016-0000-0000-000000000000', 'Psikologi',               'Fak. Psikologi', ARRAY['S','I']),
('00000000-0016-0000-0000-000000000000', 'Hukum',                   'FH',     ARRAY['E','C']),
('00000000-0016-0000-0000-000000000000', 'Ilmu Ekonomi',            'FEB',    ARRAY['I','C']),
('00000000-0016-0000-0000-000000000000', 'Akuntansi',               'FEB',    ARRAY['C','I']),
('00000000-0016-0000-0000-000000000000', 'Manajemen',               'FEB',    ARRAY['E','C']),
('00000000-0016-0000-0000-000000000000', 'Ilmu Komunikasi',         'FISIP',  ARRAY['S','A']),
('00000000-0016-0000-0000-000000000000', 'Ilmu Politik',            'FISIP',  ARRAY['E','S']),
('00000000-0016-0000-0000-000000000000', 'Administrasi Publik',     'FISIP',  ARRAY['C','E']),
('00000000-0016-0000-0000-000000000000', 'Ilmu Komputer',           'FST',    ARRAY['I','C']),
('00000000-0016-0000-0000-000000000000', 'Biologi',                 'FST',    ARRAY['I','R']),
('00000000-0016-0000-0000-000000000000', 'Kimia',                   'FST',    ARRAY['I','R']),
('00000000-0016-0000-0000-000000000000', 'Matematika',              'FST',    ARRAY['I','C']),
('00000000-0016-0000-0000-000000000000', 'Statistika',              'FST',    ARRAY['C','I']),

-- ── IPB ──────────────────────────────────────────────────────────────────────
('00000000-0004-0000-0000-000000000000', 'Agroteknologi',           'Faperta', ARRAY['R','I']),
('00000000-0004-0000-0000-000000000000', 'Agribisnis',              'FEM',    ARRAY['E','R']),
('00000000-0004-0000-0000-000000000000', 'Ilmu & Teknologi Pangan', 'FATETA', ARRAY['I','R']),
('00000000-0004-0000-0000-000000000000', 'Teknologi Industri Pertanian', 'FATETA', ARRAY['R','I']),
('00000000-0004-0000-0000-000000000000', 'Kehutanan',               'Fahutan', ARRAY['R','I']),
('00000000-0004-0000-0000-000000000000', 'Ilmu Kelautan',           'FPIK',   ARRAY['R','I']),
('00000000-0004-0000-0000-000000000000', 'Peternakan',              'FAPET',  ARRAY['R','I']),
('00000000-0004-0000-0000-000000000000', 'Kedokteran Hewan',        'FKH',    ARRAY['I','R']),
('00000000-0004-0000-0000-000000000000', 'Matematika',              'FMIPA',  ARRAY['I','C']),
('00000000-0004-0000-0000-000000000000', 'Statistika & Sains Data', 'FMIPA',  ARRAY['C','I']),
('00000000-0004-0000-0000-000000000000', 'Ilmu Komputer',           'FMIPA',  ARRAY['I','C']),
('00000000-0004-0000-0000-000000000000', 'Manajemen',               'FEM',    ARRAY['E','C']),
('00000000-0004-0000-0000-000000000000', 'Komunikasi & Pengembangan Masyarakat', 'FEMA', ARRAY['S','E']),
('00000000-0004-0000-0000-000000000000', 'Gizi Masyarakat',         'FEMA',   ARRAY['I','S']),

-- ── Binus ─────────────────────────────────────────────────────────────────────
('00000000-0006-0000-0000-000000000000', 'Teknik Informatika',      'School of Computing', ARRAY['I','C']),
('00000000-0006-0000-0000-000000000000', 'Sistem Informasi',        'School of Computing', ARRAY['I','C']),
('00000000-0006-0000-0000-000000000000', 'Ilmu Komputer',           'School of Computing', ARRAY['I','C']),
('00000000-0006-0000-0000-000000000000', 'Data Science',            'School of Computing', ARRAY['I','C']),
('00000000-0006-0000-0000-000000000000', 'Cyber Security',          'School of Computing', ARRAY['I','C']),
('00000000-0006-0000-0000-000000000000', 'Kecerdasan Buatan',       'School of Computing', ARRAY['I','C']),
('00000000-0006-0000-0000-000000000000', 'Teknik Industri',         'School of Engineering', ARRAY['R','C']),
('00000000-0006-0000-0000-000000000000', 'Teknik Elektro',          'School of Engineering', ARRAY['R','I']),
('00000000-0006-0000-0000-000000000000', 'Desain Komunikasi Visual','School of Design',    ARRAY['A','E']),
('00000000-0006-0000-0000-000000000000', 'Desain Interior',         'School of Design',    ARRAY['A','R']),
('00000000-0006-0000-0000-000000000000', 'Film',                    'School of Design',    ARRAY['A','E']),
('00000000-0006-0000-0000-000000000000', 'Animasi',                 'School of Design',    ARRAY['A','I']),
('00000000-0006-0000-0000-000000000000', 'Game Application & Technology', 'School of Design', ARRAY['A','I']),
('00000000-0006-0000-0000-000000000000', 'Manajemen',               'School of Business',  ARRAY['E','C']),
('00000000-0006-0000-0000-000000000000', 'Akuntansi',               'School of Business',  ARRAY['C','I']),
('00000000-0006-0000-0000-000000000000', 'Kewirausahaan',           'School of Business',  ARRAY['E','A']),
('00000000-0006-0000-0000-000000000000', 'Pemasaran',               'School of Business',  ARRAY['E','S']),
('00000000-0006-0000-0000-000000000000', 'Ilmu Komunikasi',         'School of Communication', ARRAY['S','A']),
('00000000-0006-0000-0000-000000000000', 'Psikologi',               'School of Psychology', ARRAY['S','I']),
('00000000-0006-0000-0000-000000000000', 'Hukum',                   'School of Law',       ARRAY['E','C']),

-- ── Telkom University ─────────────────────────────────────────────────────────
('00000000-0019-0000-0000-000000000000', 'Teknik Informatika',      'FIF',    ARRAY['I','C']),
('00000000-0019-0000-0000-000000000000', 'Sistem Informasi',        'FIF',    ARRAY['I','C']),
('00000000-0019-0000-0000-000000000000', 'Teknik Telekomunikasi',   'FTE',    ARRAY['R','I']),
('00000000-0019-0000-0000-000000000000', 'Teknik Elektro',          'FTE',    ARRAY['R','I']),
('00000000-0019-0000-0000-000000000000', 'Teknik Komputer',         'FTE',    ARRAY['R','I']),
('00000000-0019-0000-0000-000000000000', 'Data Science',            'FIF',    ARRAY['I','C']),
('00000000-0019-0000-0000-000000000000', 'Cyber Security',          'FIF',    ARRAY['I','C']),
('00000000-0019-0000-0000-000000000000', 'Desain Komunikasi Visual','FID',    ARRAY['A','E']),
('00000000-0019-0000-0000-000000000000', 'Desain Produk',           'FID',    ARRAY['A','R']),
('00000000-0019-0000-0000-000000000000', 'Ilmu Komunikasi',         'FKB',    ARRAY['S','A']),
('00000000-0019-0000-0000-000000000000', 'Manajemen',               'FEB',    ARRAY['E','C']),
('00000000-0019-0000-0000-000000000000', 'Akuntansi',               'FEB',    ARRAY['C','I']),
('00000000-0019-0000-0000-000000000000', 'Kewirausahaan',           'FEB',    ARRAY['E','A']),
('00000000-0019-0000-0000-000000000000', 'Digital Business',        'FEB',    ARRAY['E','I']),
('00000000-0019-0000-0000-000000000000', 'Teknik Industri',         'FIT',    ARRAY['R','C']),

-- ── ISI Yogyakarta ────────────────────────────────────────────────────────────
('00000000-0014-0000-0000-000000000000', 'Seni Murni',              'FSR',    ARRAY['A']),
('00000000-0014-0000-0000-000000000000', 'Desain Interior',         'FSR',    ARRAY['A','R']),
('00000000-0014-0000-0000-000000000000', 'Desain Komunikasi Visual','FSR',    ARRAY['A','E']),
('00000000-0014-0000-0000-000000000000', 'Kriya',                   'FSR',    ARRAY['A','R']),
('00000000-0014-0000-0000-000000000000', 'Musik',                   'FSP',    ARRAY['A']),
('00000000-0014-0000-0000-000000000000', 'Tari',                    'FSP',    ARRAY['A','S']),
('00000000-0014-0000-0000-000000000000', 'Teater',                  'FSP',    ARRAY['A','S']),
('00000000-0014-0000-0000-000000000000', 'Pedalangan',              'FSP',    ARRAY['A','S']),
('00000000-0014-0000-0000-000000000000', 'Fotografi',               'FSMR',   ARRAY['A','R']),
('00000000-0014-0000-0000-000000000000', 'Film & Televisi',         'FSMR',   ARRAY['A','E']),
('00000000-0014-0000-0000-000000000000', 'Animasi',                 'FSMR',   ARRAY['A','I'])

on conflict do nothing;
