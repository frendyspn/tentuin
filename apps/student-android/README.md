# Tentuin Native (Kotlin)

Project ini adalah baseline Android native untuk membandingkan performa dan maintenance dengan app Expo di `apps/student`.

## Fokus

- Satu layar eksplorasi yang memakai data Supabase yang sama.
- Filter universitas dan jurusan.
- Search universitas dengan debounce.
- List native berbasis `RecyclerView`.

## Cara pakai

1. Buka folder `apps/student-android` di Android Studio.
2. Sinkronkan Gradle.
3. Jika key Supabase berubah, update `gradle.properties`.
4. Jalankan di emulator atau device Android.

## Catatan

- Versi ini sengaja dibuat sebagai pembanding native baseline, bukan port penuh semua layar Expo.
- Kalau kamu mau, saya bisa lanjut lengkapi route detail seperti detail universitas dan jurusan juga.
