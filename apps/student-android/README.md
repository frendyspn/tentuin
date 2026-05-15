# Tentuin Native (Kotlin) — Multi-module Android

Root project ini berisi 5 module Android native untuk seluruh app Tentuin.

## Module

| Module        | Package id                | Audience                                |
|---------------|---------------------------|------------------------------------------|
| `:app`        | `id.tentuin.student`      | Siswa SMA (RIASEC test, eksplor kampus)  |
| `:agent`      | `id.tentuin.agent`        | Agen Tentuin (claim, komisi, withdrawal) |
| `:admin`      | `id.tentuin.admin`        | Internal super admin                      |
| `:school-pic` | `id.tentuin.schoolpic`    | PIC sekolah (lihat siswa, komisi)         |
| `:university` | `id.tentuin.university`   | Universitas (kuota, prospek, follow-up)   |

## Cara pakai

1. Buka folder `apps/student-android` di Android Studio.
2. Sinkronkan Gradle.
3. Jika key Supabase berubah, update `gradle.properties`.
4. Pilih run configuration sesuai module yang mau dijalankan.

## Catatan

- Project React Native lama (`apps/student`) sudah dihapus per 2026-05-15 — semua app sekarang native Kotlin/Compose.
- Shared backend: Supabase di `supabase/` (root repo).
- Migrations: lihat `supabase/migrations/`.
