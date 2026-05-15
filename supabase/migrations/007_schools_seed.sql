-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — Schools Seed Data (Jakarta, Bandung, Surabaya)
-- Jalankan di: Supabase Dashboard → SQL Editor
-- ─────────────────────────────────────────────────────────────────────────────

insert into public.schools (name, npsn, city, province, address, total_students) values

-- ── Jakarta ──────────────────────────────────────────────────────────────────
('SMA Negeri 8 Jakarta',           '20100001', 'Jakarta Selatan', 'DKI Jakarta', 'Jl. Taman Bukit Duri Tanjakan No.1, Tebet',          1180),
('SMA Negeri 28 Jakarta',          '20100002', 'Jakarta Selatan', 'DKI Jakarta', 'Jl. Raya Ragunan, Pasar Minggu',                     950),
('SMA Negeri 70 Jakarta',          '20100003', 'Jakarta Selatan', 'DKI Jakarta', 'Jl. Bulungan Blok C No.1, Kebayoran Baru',         1340),
('SMA Negeri 81 Jakarta',          '20100004', 'Jakarta Timur',   'DKI Jakarta', 'Jl. Kartika Eka Paksi, Komp. Kodam Jaya',           1090),
('SMA Negeri 1 Jakarta',           '20100005', 'Jakarta Pusat',   'DKI Jakarta', 'Jl. Budi Utomo No.7, Sawah Besar',                   860),
('SMA Negeri 78 Jakarta',          '20100006', 'Jakarta Barat',   'DKI Jakarta', 'Jl. Bhakti IV/1, Kemanggisan, Palmerah',           1020),
('SMA Labschool Kebayoran',        '20100007', 'Jakarta Selatan', 'DKI Jakarta', 'Jl. K.H. Ahmad Dahlan No.14, Kramat Pela',          720),
('SMA Kanisius Jakarta',           '20100008', 'Jakarta Pusat',   'DKI Jakarta', 'Jl. Menteng Raya No.64, Menteng',                    640),

-- ── Bandung ──────────────────────────────────────────────────────────────────
('SMA Negeri 3 Bandung',           '20200001', 'Bandung',         'Jawa Barat',  'Jl. Belitung No.8, Sumur Bandung',                  1050),
('SMA Negeri 5 Bandung',           '20200002', 'Bandung',         'Jawa Barat',  'Jl. Belitung No.8, Sumur Bandung',                  1080),
('SMA Negeri 2 Bandung',           '20200003', 'Bandung',         'Jawa Barat',  'Jl. Cihampelas No.173, Cipaganti',                  1240),
('SMA Negeri 8 Bandung',           '20200004', 'Bandung',         'Jawa Barat',  'Jl. Solontongan No.3, Buahbatu',                     980),
('SMAK BPK Penabur Holis',         '20200005', 'Bandung',         'Jawa Barat',  'Jl. Holis No.488, Cijerah',                          560),
('SMA Taruna Bakti',               '20200006', 'Bandung',         'Jawa Barat',  'Jl. R.E. Martadinata No.52, Citarum',                480),

-- ── Surabaya ─────────────────────────────────────────────────────────────────
('SMA Negeri 5 Surabaya',          '20300001', 'Surabaya',        'Jawa Timur',  'Jl. Kusuma Bangsa No.21, Genteng',                  1110),
('SMA Negeri 2 Surabaya',          '20300002', 'Surabaya',        'Jawa Timur',  'Jl. Wijaya Kusuma No.48, Genteng',                  1090),
('SMA Negeri 1 Surabaya',          '20300003', 'Surabaya',        'Jawa Timur',  'Jl. Wijaya Kusuma No.48, Genteng',                  1030),
('SMA Negeri 6 Surabaya',          '20300004', 'Surabaya',        'Jawa Timur',  'Jl. Gubernur Suryo No.11, Embong Kaliasin',         920),
('SMA Kristen Petra 1 Surabaya',   '20300005', 'Surabaya',        'Jawa Timur',  'Jl. H.R. Muhammad No.808, Pradah Kalikendal',       780)

on conflict (npsn) do nothing;
