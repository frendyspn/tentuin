-- ─── Migration 005: Push Token ───────────────────────────────────────────────
-- Menyimpan Expo push token per user untuk server-side push notifications

ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS push_token text;

-- Index untuk memudahkan broadcast ke semua token
CREATE INDEX IF NOT EXISTS idx_profiles_push_token
  ON public.profiles (push_token)
  WHERE push_token IS NOT NULL;

-- RLS: user hanya bisa update push_token milik sendiri
-- (Policy "Users can update own profile" sudah cover ini karena update seluruh row)
