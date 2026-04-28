-- ─────────────────────────────────────────────────────────────────────────────
-- Tentuin — RIASEC Questions
-- Jalankan di: Supabase Dashboard → SQL Editor
-- ─────────────────────────────────────────────────────────────────────────────

-- ─── 1. Tabel Questions ──────────────────────────────────────────────────────
create table if not exists public.questions (
  id            uuid    default gen_random_uuid() primary key,
  order_number  integer not null unique,
  text          text    not null,
  category      text    not null check (category in (
                  'realistic','investigative','artistic',
                  'social','enterprising','conventional'
                )),
  is_active     boolean default true,
  created_at    timestamptz default now()
);

alter table public.questions enable row level security;
create policy "Anyone can view questions"
  on public.questions for select using (true);


-- ─── 2. Seed: 60 Soal RIASEC ─────────────────────────────────────────────────
-- Urutan dikocok merata: R-I-A-S-E-C × 10 putaran
-- Setiap soal adalah pernyataan yang dirating 1–5
-- (1 = Sangat Tidak Sesuai, 5 = Sangat Sesuai)

insert into public.questions (order_number, category, text) values

-- Putaran 1
(1,  'realistic',     'Saya senang memperbaiki peralatan atau barang yang rusak'),
(2,  'investigative', 'Saya menikmati memecahkan soal matematika atau logika yang menantang'),
(3,  'artistic',      'Saya suka menggambar, melukis, atau membuat desain'),
(4,  'social',        'Saya senang membantu teman yang sedang menghadapi masalah'),
(5,  'enterprising',  'Saya suka menjadi pemimpin atau ketua dalam suatu kelompok'),
(6,  'conventional',  'Saya suka membuat jadwal dan mengikutinya dengan disiplin'),

-- Putaran 2
(7,  'realistic',     'Saya lebih nyaman bekerja di luar ruangan daripada di dalam kantor'),
(8,  'investigative', 'Saya suka membaca tentang penemuan ilmiah atau teknologi terbaru'),
(9,  'artistic',      'Saya menikmati menulis cerita, puisi, atau konten kreatif'),
(10, 'social',        'Saya menikmati mengajar atau menjelaskan sesuatu kepada orang lain'),
(11, 'enterprising',  'Saya tertarik dengan dunia bisnis dan kewirausahaan'),
(12, 'conventional',  'Saya merasa nyaman bekerja dengan angka, tabel, atau data'),

-- Putaran 3
(13, 'realistic',     'Saya tertarik dengan cara kerja mesin, kendaraan, atau alat teknik'),
(14, 'investigative', 'Saya selalu ingin tahu bagaimana sesuatu bekerja secara mendalam'),
(15, 'artistic',      'Saya suka mendengarkan atau memainkan musik'),
(16, 'social',        'Saya merasa puas ketika orang lain berhasil karena bantuan saya'),
(17, 'enterprising',  'Saya tidak takut berbicara atau presentasi di depan banyak orang'),
(18, 'conventional',  'Saya lebih suka pekerjaan dengan instruksi yang jelas dan terstruktur'),

-- Putaran 4
(19, 'realistic',     'Saya senang membuat atau membangun sesuatu dengan tangan sendiri'),
(20, 'investigative', 'Saya tertarik melakukan penelitian atau eksperimen untuk menemukan jawaban'),
(21, 'artistic',      'Saya lebih nyaman bekerja tanpa aturan yang terlalu kaku'),
(22, 'social',        'Saya mudah bergaul dan membuat teman baru di lingkungan baru'),
(23, 'enterprising',  'Saya suka meyakinkan orang lain dengan argumen atau ide saya'),
(24, 'conventional',  'Saya senang merapikan dan mengorganisir file atau dokumen'),

-- Putaran 5
(25, 'realistic',     'Saya lebih suka aktivitas fisik daripada duduk membaca dalam waktu lama'),
(26, 'investigative', 'Saya lebih suka mencari tahu sendiri daripada langsung bertanya kepada orang lain'),
(27, 'artistic',      'Saya tertarik dengan dunia film, fotografi, atau seni visual'),
(28, 'social',        'Saya tertarik dengan isu-isu sosial dan permasalahan di masyarakat'),
(29, 'enterprising',  'Saya menikmati kompetisi dan suka menjadi yang terbaik'),
(30, 'conventional',  'Saya sangat teliti dan jarang membuat kesalahan dalam pekerjaan detail'),

-- Putaran 6
(31, 'realistic',     'Saya merasa puas ketika pekerjaan saya menghasilkan sesuatu yang nyata dan terlihat'),
(32, 'investigative', 'Saya menikmati menganalisis data dan mencari pola atau tren di dalamnya'),
(33, 'artistic',      'Saya mengekspresikan diri lebih baik melalui karya seni daripada kata-kata'),
(34, 'social',        'Saya lebih suka bekerja dalam tim dibanding bekerja sendiri'),
(35, 'enterprising',  'Saya selalu punya ide untuk memulai atau meningkatkan sesuatu'),
(36, 'conventional',  'Saya tertarik dengan bidang akuntansi, administrasi, atau manajemen data'),

-- Putaran 7
(37, 'realistic',     'Saya tertarik dengan bidang otomotif, elektronik, atau konstruksi bangunan'),
(38, 'investigative', 'Saya suka mengajukan pertanyaan dan mencari jawabannya secara sistematis'),
(39, 'artistic',      'Saya suka menciptakan sesuatu yang baru dan orisinal, bukan meniru'),
(40, 'social',        'Saya senang mendengarkan cerita dan masalah yang diceritakan orang lain'),
(41, 'enterprising',  'Saya bermimpi memiliki bisnis atau usaha sendiri suatu hari nanti'),
(42, 'conventional',  'Saya merasa tidak nyaman jika pekerjaan tidak terorganisir dengan baik'),

-- Putaran 8
(43, 'realistic',     'Saya lebih suka petunjuk praktis yang langsung bisa diterapkan'),
(44, 'investigative', 'Saya tertarik dengan ilmu seperti biologi, kimia, fisika, atau astronomi'),
(45, 'artistic',      'Saya merasa berenergi saat bekerja di lingkungan yang kreatif dan dinamis'),
(46, 'social',        'Saya tertarik dengan profesi seperti guru, konselor, dokter, atau pekerja sosial'),
(47, 'enterprising',  'Saya merasa bersemangat saat ada target atau tantangan yang harus dicapai'),
(48, 'conventional',  'Saya suka mengikuti prosedur yang sudah ada daripada membuat cara sendiri'),

-- Putaran 9
(49, 'realistic',     'Saya merasa nyaman berinteraksi dengan hewan atau bekerja di alam terbuka'),
(50, 'investigative', 'Saya lebih suka berpikir mandiri dan menemukan solusi yang belum pernah ada'),
(51, 'artistic',      'Saya tertarik dengan fashion, desain interior, atau arsitektur'),
(52, 'social',        'Saya mudah memahami dan merasakan apa yang dirasakan orang lain'),
(53, 'enterprising',  'Saya tertarik dengan strategi dan cara memenangkan persaingan'),
(54, 'conventional',  'Saya menikmati pekerjaan yang hasilnya bisa diukur dan dievaluasi dengan jelas'),

-- Putaran 10
(55, 'realistic',     'Saya suka olahraga atau kegiatan yang melibatkan kemampuan fisik'),
(56, 'investigative', 'Saya menikmati berdiskusi tentang topik-topik kompleks dan abstrak'),
(57, 'artistic',      'Saya sering menggunakan imajinasi dan intuisi dalam mengambil keputusan'),
(58, 'social',        'Saya suka kegiatan sukarela atau kegiatan sosial untuk membantu sesama'),
(59, 'enterprising',  'Saya pandai membujuk, memotivasi, dan menggerakkan orang lain'),
(60, 'conventional',  'Saya pandai mengelola waktu dan memprioritaskan tugas dengan baik')

on conflict (order_number) do nothing;
