export type University = {
  id: string
  name: string
  shortName: string
  city: string
  province: string
  type: 'negeri' | 'swasta'
  majors: string[]
}

export const UNIVERSITIES: University[] = [
  // ─────────────────────────────────────────────────────────────────────────
  // JAWA BARAT & DKI JAKARTA
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'ui',
    name: 'Universitas Indonesia',
    shortName: 'UI',
    city: 'Depok',
    province: 'Jawa Barat',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kesehatan Masyarakat',
      'Ilmu Gizi', 'Kebidanan', 'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro',
      'Teknik Kimia', 'Teknik Metalurgi & Material', 'Teknik Perkapalan', 'Teknik Komputer',
      'Teknik Industri', 'Arsitektur', 'Ilmu Komputer', 'Sistem Informasi',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen', 'Ilmu Administrasi Fiskal',
      'Ilmu Administrasi Negara', 'Ilmu Administrasi Niaga', 'Psikologi',
      'Sosiologi', 'Ilmu Politik', 'Ilmu Komunikasi', 'Kriminologi',
      'Ilmu Hubungan Internasional', 'Ilmu Kesejahteraan Sosial',
      'Sastra Indonesia', 'Sastra Inggris', 'Sastra Jepang', 'Sastra Arab',
      'Sastra Belanda', 'Sastra China', 'Sastra Perancis', 'Sastra Jerman',
      'Sastra Rusia', 'Ilmu Perpustakaan', 'Arkeologi', 'Ilmu Sejarah', 'Filsafat',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Geografi', 'Geofisika',
      'Ilmu Kelautan', 'Meteorologi', 'Statistika',
    ],
  },
  {
    id: 'itb',
    name: 'Institut Teknologi Bandung',
    shortName: 'ITB',
    city: 'Bandung',
    province: 'Jawa Barat',
    type: 'negeri',
    majors: [
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Teknik Industri', 'Teknik Pertambangan', 'Teknik Perminyakan',
      'Teknik Geodesi & Geomatika', 'Teknik Geofisika', 'Teknik Metalurgi',
      'Teknik Informatika', 'Sistem dan Teknologi Informasi',
      'Arsitektur', 'Perencanaan Wilayah dan Kota', 'Desain Produk Industri',
      'Desain Interior', 'Desain Komunikasi Visual',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Astronomi',
      'Manajemen', 'Kewirausahaan', 'Sains dan Teknologi Farmasi',
      'Teknik Dirgantara', 'Teknik Biomedis',
      'Kriya', 'Seni Rupa', 'Seni Musik',
    ],
  },
  {
    id: 'unpad',
    name: 'Universitas Padjadjaran',
    shortName: 'Unpad',
    city: 'Bandung',
    province: 'Jawa Barat',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kebidanan',
      'Kesehatan Masyarakat', 'Ilmu Gizi',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen', 'Bisnis Digital',
      'Ilmu Komunikasi', 'Hubungan Masyarakat', 'Jurnalistik', 'Televisi & Film',
      'Ilmu Politik', 'Administrasi Publik', 'Sosiologi', 'Antropologi',
      'Psikologi', 'Ilmu Hubungan Internasional', 'Ilmu Kesejahteraan Sosial',
      'Sastra Indonesia', 'Sastra Inggris', 'Sastra Jepang', 'Sastra Sunda',
      'Sastra Arab', 'Sastra Perancis', 'Sastra Jerman', 'Sastra Rusia',
      'Ilmu Perpustakaan', 'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Statistika',
      'Ilmu Kelautan', 'Oseanografi', 'Geologi', 'Geofisika',
      'Agroteknologi', 'Agribisnis', 'Teknologi Pangan', 'Peternakan', 'Perikanan',
      'Ilmu Komputer', 'Sistem Informasi',
    ],
  },
  {
    id: 'binus',
    name: 'Universitas Bina Nusantara',
    shortName: 'Binus',
    city: 'Jakarta',
    province: 'DKI Jakarta',
    type: 'swasta',
    majors: [
      'Ilmu Komputer', 'Teknik Informatika', 'Sistem Informasi', 'Sistem Komputer',
      'Teknik Elektro', 'Teknik Industri', 'Arsitektur',
      'Desain Komunikasi Visual', 'Desain Interior', 'Film', 'Animasi', 'Game Application & Technology',
      'Manajemen', 'Akuntansi', 'Keuangan', 'Bisnis Internasional', 'Pemasaran',
      'Manajemen Perhotelan', 'Pariwisata',
      'Ilmu Komunikasi', 'Jurnalistik', 'Public Relations',
      'Psikologi', 'Hukum',
      'Chinese', 'Sastra Inggris', 'Sastra Jepang',
      'Matematika', 'Statistika',
      'Cyber Security', 'Data Science', 'Kewirausahaan',
    ],
  },
  {
    id: 'trisakti',
    name: 'Universitas Trisakti',
    shortName: 'Trisakti',
    city: 'Jakarta',
    province: 'DKI Jakarta',
    type: 'swasta',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Farmasi',
      'Hukum', 'Manajemen', 'Akuntansi', 'Ekonomi Pembangunan',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Industri', 'Teknik Kimia',
      'Arsitektur', 'Perencanaan Wilayah & Kota',
      'Desain Produk', 'Desain Interior', 'Desain Komunikasi Visual',
      'Ilmu Komunikasi', 'Hubungan Internasional',
      'Teknologi Pangan', 'Manajemen Sumber Daya Perairan',
    ],
  },
  {
    id: 'untar',
    name: 'Universitas Tarumanagara',
    shortName: 'Untar',
    city: 'Jakarta',
    province: 'DKI Jakarta',
    type: 'swasta',
    majors: [
      'Kedokteran', 'Keperawatan', 'Farmasi', 'Fisioterapi',
      'Hukum', 'Manajemen', 'Akuntansi', 'Kewirausahaan',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Industri',
      'Arsitektur', 'Desain Interior', 'Desain Komunikasi Visual',
      'Ilmu Komunikasi', 'Psikologi', 'Ilmu Komputer', 'Sistem Informasi',
      'Teknologi Pangan', 'Sastra Inggris', 'Sastra China',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // JAWA TENGAH & D.I. YOGYAKARTA
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'ugm',
    name: 'Universitas Gadjah Mada',
    shortName: 'UGM',
    city: 'Yogyakarta',
    province: 'D.I. Yogyakarta',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kebidanan',
      'Kesehatan Masyarakat', 'Ilmu Gizi', 'Kedokteran Hewan',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Teknik Industri', 'Teknik Geodesi', 'Teknik Geologi', 'Teknik Nuklir',
      'Teknik Fisika', 'Teknik Informatika', 'Teknologi Informasi',
      'Arsitektur', 'Perencanaan Wilayah & Kota',
      'Ilmu Politik', 'Sosiologi', 'Administrasi Publik', 'Administrasi Bisnis',
      'Psikologi', 'Ilmu Komunikasi', 'Ilmu Hubungan Internasional', 'Kriminologi',
      'Ilmu Kesejahteraan Sosial', 'Manajemen & Kebijakan Publik',
      'Sastra Indonesia', 'Sastra Inggris', 'Sastra Arab', 'Sastra Jawa',
      'Sastra Jepang', 'Sastra Perancis', 'Ilmu Sejarah', 'Arkeologi', 'Filsafat',
      'Ilmu Perpustakaan', 'Antropologi Budaya',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Statistika', 'Ilmu Komputer',
      'Elektronika & Instrumentasi', 'Geofisika',
      'Agroteknologi', 'Agribisnis', 'Teknologi Pangan', 'Teknologi Industri Pertanian',
      'Peternakan', 'Ilmu Tanah', 'Proteksi Tanaman', 'Kehutanan',
      'Manajemen Sumber Daya Perikanan', 'Ilmu Kelautan',
    ],
  },
  {
    id: 'undip',
    name: 'Universitas Diponegoro',
    shortName: 'Undip',
    city: 'Semarang',
    province: 'Jawa Tengah',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kebidanan',
      'Kesehatan Masyarakat', 'Ilmu Gizi',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen', 'Bisnis Internasional',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Teknik Industri', 'Teknik Perkapalan', 'Teknik Lingkungan', 'Teknik Geologi',
      'Teknik Informatika', 'Sistem Komputer', 'Sistem Informasi',
      'Arsitektur', 'Perencanaan Wilayah & Kota',
      'Ilmu Politik', 'Administrasi Publik', 'Administrasi Bisnis',
      'Sosiologi', 'Ilmu Komunikasi', 'Ilmu Hubungan Internasional', 'Psikologi',
      'Sastra Indonesia', 'Sastra Inggris', 'Ilmu Sejarah', 'Ilmu Perpustakaan',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Statistika',
      'Oseanografi', 'Ilmu Kelautan', 'Manajemen Sumber Daya Perairan',
      'Agroteknologi', 'Agribisnis', 'Peternakan', 'Teknologi Pangan',
    ],
  },
  {
    id: 'unes',
    name: 'Universitas Negeri Semarang',
    shortName: 'Unnes',
    city: 'Semarang',
    province: 'Jawa Tengah',
    type: 'negeri',
    majors: [
      'Pendidikan Matematika', 'Pendidikan Fisika', 'Pendidikan Kimia', 'Pendidikan Biologi',
      'Pendidikan IPA', 'Pendidikan Teknik Informatika',
      'Pendidikan Bahasa Indonesia', 'Pendidikan Bahasa Inggris', 'Pendidikan Bahasa Jawa',
      'Pendidikan Sejarah', 'Pendidikan Geografi', 'Pendidikan Ekonomi',
      'Pendidikan Pancasila & Kewarganegaraan', 'Pendidikan Sosiologi & Antropologi',
      'Pendidikan Jasmani', 'Pendidikan Kepelatihan Olahraga',
      'Bimbingan & Konseling', 'Pendidikan Luar Biasa', 'Pendidikan Luar Sekolah',
      'Psikologi', 'Pendidikan Guru Sekolah Dasar',
      'Manajemen', 'Akuntansi', 'Ekonomi Pembangunan',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Ilmu Hukum', 'Sosiologi',
      'Desain Komunikasi Visual', 'Seni Rupa', 'Seni Tari', 'Seni Drama, Tari & Musik',
    ],
  },
  {
    id: 'uii',
    name: 'Universitas Islam Indonesia',
    shortName: 'UII',
    city: 'Yogyakarta',
    province: 'D.I. Yogyakarta',
    type: 'swasta',
    majors: [
      'Hukum', 'Ekonomi Islam', 'Manajemen', 'Akuntansi',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Teknik Industri', 'Teknik Lingkungan', 'Teknik Informatika', 'Sistem Informasi',
      'Arsitektur', 'Farmasi', 'Psikologi',
      'Ilmu Komunikasi', 'Hubungan Internasional', 'Ilmu Pemerintahan',
      'Matematika', 'Statistika', 'Kedokteran',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // JAWA TIMUR
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'its',
    name: 'Institut Teknologi Sepuluh Nopember',
    shortName: 'ITS',
    city: 'Surabaya',
    province: 'Jawa Timur',
    type: 'negeri',
    majors: [
      'Teknik Sipil', 'Teknik Lingkungan', 'Teknik Mesin', 'Teknik Material & Metalurgi',
      'Teknik Elektro', 'Teknik Biomedik', 'Teknik Komputer', 'Teknik Informatika',
      'Teknologi Informasi', 'Sistem Informasi', 'Teknik Kimia', 'Teknik Industri',
      'Teknik Fisika', 'Teknik Geomatika', 'Teknik Perkapalan', 'Teknik Sistem Perkapalan',
      'Teknik Kelautan', 'Ilmu Kelautan', 'Oceanografi',
      'Desain Produk Industri', 'Desain Interior', 'Desain Komunikasi Visual',
      'Arsitektur', 'Perencanaan Wilayah & Kota',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Statistika', 'Aktuaria',
      'Manajemen Bisnis', 'Manajemen Teknologi',
    ],
  },
  {
    id: 'unair',
    name: 'Universitas Airlangga',
    shortName: 'Unair',
    city: 'Surabaya',
    province: 'Jawa Timur',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kebidanan',
      'Kesehatan Masyarakat', 'Ilmu Gizi', 'Kedokteran Hewan',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen', 'Ekonomi Islam',
      'Ilmu Komunikasi', 'Ilmu Informasi & Perpustakaan', 'Sosiologi',
      'Psikologi', 'Ilmu Politik', 'Administrasi Publik', 'Administrasi Bisnis',
      'Ilmu Hubungan Internasional', 'Kriminologi',
      'Sastra Indonesia', 'Sastra Inggris', 'Ilmu Sejarah', 'Bahasa & Sastra Indonesia',
      'Ilmu Komputer', 'Sistem Informasi', 'Teknologi Informasi',
      'Biologi', 'Kimia', 'Fisika', 'Matematika', 'Statistika',
      'Budidaya Perairan', 'Manajemen Sumber Daya Perairan',
      'Agroteknologi', 'Agribisnis',
    ],
  },
  {
    id: 'ub',
    name: 'Universitas Brawijaya',
    shortName: 'UB',
    city: 'Malang',
    province: 'Jawa Timur',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Keperawatan', 'Farmasi', 'Kebidanan', 'Kedokteran Gigi',
      'Kesehatan Masyarakat', 'Ilmu Gizi', 'Kedokteran Hewan',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen', 'Bisnis Internasional',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Pengairan', 'Teknik Kimia',
      'Teknik Industri', 'Teknik Informatika', 'Sistem Informasi', 'Teknologi Informasi',
      'Arsitektur', 'Perencanaan Wilayah & Kota',
      'Ilmu Politik', 'Ilmu Pemerintahan', 'Sosiologi', 'Ilmu Komunikasi', 'Psikologi',
      'Ilmu Hubungan Internasional', 'Administrasi Publik', 'Administrasi Bisnis',
      'Sastra Inggris', 'Bahasa & Sastra Jepang', 'Bahasa & Sastra Indonesia',
      'Biologi', 'Kimia', 'Fisika', 'Matematika', 'Statistika',
      'Ilmu Kelautan', 'Manajemen Sumber Daya Perairan', 'Budidaya Perairan',
      'Agroteknologi', 'Agribisnis', 'Teknologi Hasil Pertanian', 'Teknologi Pangan',
      'Peternakan', 'Kehutanan', 'Agroekoteknologi',
    ],
  },
  {
    id: 'um',
    name: 'Universitas Negeri Malang',
    shortName: 'UM',
    city: 'Malang',
    province: 'Jawa Timur',
    type: 'negeri',
    majors: [
      'Pendidikan Matematika', 'Pendidikan Fisika', 'Pendidikan Kimia', 'Pendidikan Biologi',
      'Pendidikan Bahasa Indonesia', 'Pendidikan Bahasa Inggris', 'Pendidikan Bahasa Arab',
      'Pendidikan Bahasa Jepang', 'Pendidikan Bahasa Jerman', 'Pendidikan Bahasa Mandarin',
      'Pendidikan Sejarah', 'Pendidikan Geografi', 'Pendidikan Ekonomi', 'Pendidikan Akuntansi',
      'Pendidikan Pancasila & Kewarganegaraan', 'Pendidikan Jasmani',
      'Bimbingan & Konseling', 'Psikologi', 'Pendidikan Guru Sekolah Dasar',
      'Teknologi Pendidikan', 'Pendidikan Luar Biasa',
      'Manajemen', 'Akuntansi', 'Ekonomi Pembangunan',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Informatika',
      'Ilmu Komputer', 'Sistem Informasi',
      'Desain Komunikasi Visual', 'Seni Rupa',
      'Ilmu Hukum', 'Sosiologi', 'Geografi',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // BOGOR & SEKITARNYA
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'ipb',
    name: 'Institut Pertanian Bogor',
    shortName: 'IPB',
    city: 'Bogor',
    province: 'Jawa Barat',
    type: 'negeri',
    majors: [
      'Agronomi & Hortikultura', 'Proteksi Tanaman', 'Ilmu Tanah', 'Agroekoteknologi',
      'Ilmu & Teknologi Pangan', 'Teknologi Industri Pertanian', 'Gizi Masyarakat',
      'Agribisnis', 'Ekonomi Pertanian & Sumber Daya', 'Ekonomi Sumber Daya & Lingkungan',
      'Manajemen', 'Bisnis',
      'Peternakan', 'Nutrisi & Teknologi Pakan',
      'Teknologi Hasil Hutan', 'Silvikultur', 'Manajemen Hutan', 'Konservasi Sumber Daya Hutan',
      'Teknologi & Manajemen Perikanan Budidaya', 'Manajemen Sumber Daya Perairan',
      'Teknologi Hasil Perairan', 'Ilmu Kelautan', 'Teknologi & Manajemen Perikanan Tangkap',
      'Kedokteran Hewan', 'Teknik Sipil & Lingkungan', 'Teknik Mesin & Biosistem',
      'Teknik Pertanian & Biosistem', 'Teknologi Hasil Ternak',
      'Statistika & Sains Data', 'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Biokimia',
      'Ilmu Komputer', 'Meteorologi Terapan', 'Komunikasi & Pengembangan Masyarakat',
      'Ekologi Manusia',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // BANDUNG (SWASTA)
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'telkom',
    name: 'Telkom University',
    shortName: 'Tel-U',
    city: 'Bandung',
    province: 'Jawa Barat',
    type: 'swasta',
    majors: [
      'Teknik Elektro', 'Teknik Telekomunikasi', 'Teknik Komputer', 'Teknik Informatika',
      'Sistem Informasi', 'Teknologi Informasi', 'Ilmu Komputasi',
      'Data Science', 'Cyber Security', 'Kecerdasan Buatan',
      'Teknik Industri', 'Teknik Logistik',
      'Desain Komunikasi Visual', 'Desain Produk', 'Kriya Tekstil',
      'Ilmu Komunikasi', 'Administrasi Bisnis', 'Manajemen', 'Akuntansi',
      'Digital Business', 'Kewirausahaan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // SULAWESI SELATAN
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'unhas',
    name: 'Universitas Hasanuddin',
    shortName: 'Unhas',
    city: 'Makassar',
    province: 'Sulawesi Selatan',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kebidanan',
      'Kesehatan Masyarakat', 'Ilmu Gizi',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Pertambangan',
      'Teknik Geologi', 'Teknik Kelautan', 'Teknik Industri', 'Teknik Informatika',
      'Arsitektur', 'Perencanaan Wilayah & Kota',
      'Ilmu Komunikasi', 'Ilmu Politik', 'Administrasi Publik', 'Sosiologi',
      'Ilmu Hubungan Internasional', 'Antropologi',
      'Sastra Indonesia', 'Sastra Inggris', 'Sastra Arab', 'Ilmu Sejarah', 'Arkeologi',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Statistika', 'Ilmu Komputer',
      'Agribisnis', 'Agroteknologi', 'Kehutanan', 'Perikanan', 'Ilmu Kelautan',
      'Peternakan', 'Kedokteran Hewan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // SUMATERA UTARA
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'usu',
    name: 'Universitas Sumatera Utara',
    shortName: 'USU',
    city: 'Medan',
    province: 'Sumatera Utara',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kebidanan',
      'Kesehatan Masyarakat', 'Ilmu Gizi',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Teknik Industri', 'Teknik Lingkungan', 'Teknik Informatika',
      'Arsitektur',
      'Ilmu Politik', 'Administrasi Publik', 'Sosiologi', 'Ilmu Komunikasi', 'Psikologi',
      'Antropologi', 'Ilmu Kesejahteraan Sosial',
      'Sastra Indonesia', 'Sastra Inggris', 'Sastra Arab', 'Sastra Jepang', 'Ilmu Sejarah',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Statistika', 'Ilmu Komputer', 'Geofisika',
      'Agroteknologi', 'Agribisnis', 'Kehutanan', 'Peternakan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // SUMATERA SELATAN
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'unsri',
    name: 'Universitas Sriwijaya',
    shortName: 'Unsri',
    city: 'Palembang',
    province: 'Sumatera Selatan',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Keperawatan', 'Kesehatan Masyarakat', 'Ilmu Gizi', 'Farmasi',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Teknik Pertambangan', 'Teknik Geologi', 'Teknik Lingkungan', 'Teknik Informatika',
      'Ilmu Komunikasi', 'Ilmu Politik', 'Administrasi Publik', 'Sosiologi',
      'Sastra Indonesia', 'Sastra Inggris', 'Sastra Arab',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Ilmu Komputer',
      'Agroteknologi', 'Agribisnis', 'Peternakan', 'Perikanan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // KALIMANTAN TIMUR
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'unmul',
    name: 'Universitas Mulawarman',
    shortName: 'Unmul',
    city: 'Samarinda',
    province: 'Kalimantan Timur',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Keperawatan', 'Farmasi', 'Kesehatan Masyarakat',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Pertambangan',
      'Teknik Geologi', 'Teknik Kimia', 'Teknik Industri', 'Teknik Informatika',
      'Arsitektur',
      'Ilmu Politik', 'Administrasi Publik', 'Ilmu Komunikasi', 'Sosiologi',
      'Sastra Indonesia', 'Sastra Inggris', 'Ilmu Sejarah',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Ilmu Komputer', 'Statistika',
      'Kehutanan', 'Agroteknologi', 'Agribisnis', 'Perikanan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // NUSA TENGGARA BARAT
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'unram',
    name: 'Universitas Mataram',
    shortName: 'Unram',
    city: 'Mataram',
    province: 'Nusa Tenggara Barat',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Keperawatan', 'Kesehatan Masyarakat', 'Farmasi',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Industri', 'Teknik Informatika',
      'Arsitektur',
      'Ilmu Politik', 'Administrasi Publik', 'Sosiologi',
      'Sastra Inggris', 'Sastra Indonesia', 'Ilmu Sejarah',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Ilmu Komputer',
      'Agroteknologi', 'Agribisnis', 'Peternakan', 'Kehutanan', 'Perikanan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // BALI
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'udayana',
    name: 'Universitas Udayana',
    shortName: 'Unud',
    city: 'Denpasar',
    province: 'Bali',
    type: 'negeri',
    majors: [
      'Kedokteran', 'Kedokteran Gigi', 'Keperawatan', 'Farmasi', 'Kebidanan',
      'Kesehatan Masyarakat', 'Ilmu Gizi', 'Kedokteran Hewan',
      'Hukum', 'Ilmu Ekonomi', 'Akuntansi', 'Manajemen',
      'Teknik Sipil', 'Teknik Mesin', 'Teknik Elektro', 'Teknik Kimia',
      'Teknik Informatika', 'Arsitektur',
      'Ilmu Politik', 'Sosiologi', 'Ilmu Komunikasi', 'Administrasi Publik', 'Psikologi',
      'Ilmu Hubungan Internasional',
      'Sastra Indonesia', 'Sastra Inggris', 'Sastra Jepang', 'Arkeologi', 'Ilmu Sejarah',
      'Pariwisata', 'Destinasi Pariwisata', 'Industri Perjalanan Wisata',
      'Matematika', 'Fisika', 'Kimia', 'Biologi', 'Ilmu Komputer', 'Statistika',
      'Agroteknologi', 'Agribisnis', 'Peternakan', 'Perikanan', 'Ilmu Kelautan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // ISLAM / KHUSUS
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'paramadina',
    name: 'Universitas Paramadina',
    shortName: 'Paramadina',
    city: 'Jakarta',
    province: 'DKI Jakarta',
    type: 'swasta',
    majors: [
      'Manajemen', 'Akuntansi', 'Bisnis Digital', 'Kewirausahaan',
      'Ilmu Komunikasi', 'Public Relations', 'Jurnalistik',
      'Desain Komunikasi Visual', 'Arsitektur',
      'Teknik Informatika', 'Sistem Informasi',
      'Hubungan Internasional', 'Ilmu Politik',
      'Psikologi', 'Hukum',
    ],
  },
  {
    id: 'ipdn',
    name: 'Institut Pemerintahan Dalam Negeri',
    shortName: 'IPDN',
    city: 'Jatinangor',
    province: 'Jawa Barat',
    type: 'negeri',
    majors: [
      'Administrasi Pemerintahan Daerah', 'Kebijakan Publik', 'Pembangunan Ekonomi & Pemberdayaan Masyarakat',
      'Manajemen Keamanan & Keselamatan Publik', 'Keuangan Publik',
      'Hukum Administrasi Pemerintahan', 'Teknologi Rekayasa Informasi Pemerintahan',
    ],
  },

  // ─────────────────────────────────────────────────────────────────────────
  // SENI & DESAIN KHUSUS
  // ─────────────────────────────────────────────────────────────────────────
  {
    id: 'isi_jogja',
    name: 'Institut Seni Indonesia Yogyakarta',
    shortName: 'ISI Yogyakarta',
    city: 'Yogyakarta',
    province: 'D.I. Yogyakarta',
    type: 'negeri',
    majors: [
      'Seni Murni', 'Seni Lukis', 'Seni Patung', 'Seni Grafis',
      'Desain Interior', 'Desain Komunikasi Visual', 'Batik & Fashion',
      'Kriya Kayu', 'Kriya Kulit', 'Kriya Logam', 'Kriya Keramik', 'Kriya Tekstil',
      'Musik', 'Etnomusikologi', 'Tari', 'Teater', 'Pedalangan',
      'Fotografi', 'Film & Televisi', 'Animasi',
      'Pendidikan Seni Rupa', 'Pendidikan Seni Pertunjukan',
    ],
  },
  {
    id: 'ikj',
    name: 'Institut Kesenian Jakarta',
    shortName: 'IKJ',
    city: 'Jakarta',
    province: 'DKI Jakarta',
    type: 'swasta',
    majors: [
      'Film & Televisi', 'Animasi', 'Fotografi',
      'Seni Rupa', 'Desain Komunikasi Visual',
      'Musik', 'Tari', 'Teater',
      'Seni Pertunjukan', 'Pendidikan Seni',
    ],
  },
]

// ─── Helper: cari universitas berdasarkan nama jurusan ──────────────────────

export function findUniversitiesForMajor(majorName: string): University[] {
  const lower = majorName.toLowerCase()
  return UNIVERSITIES.filter((u) =>
    u.majors.some((m) => m.toLowerCase().includes(lower))
  )
}

// ─── Helper: cari universitas per kota/provinsi ──────────────────────────────

export function findUniversitiesByCity(city: string): University[] {
  return UNIVERSITIES.filter((u) => u.city.toLowerCase() === city.toLowerCase())
}

// ─── Helper: semua jurusan unik (lintas universitas) ─────────────────────────

export function getAllMajors(): string[] {
  const set = new Set<string>()
  UNIVERSITIES.forEach((u) => u.majors.forEach((m) => set.add(m)))
  return Array.from(set).sort()
}
