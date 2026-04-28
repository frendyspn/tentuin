import { create } from 'zustand'
import type { Session, User } from '@supabase/supabase-js'
import type { Profile } from '@tentuin/types'

interface AuthState {
  // ─── State ──────────────────────────────────────────────
  user: User | null
  session: Session | null
  profile: Profile | null
  isLoading: boolean
  isInitialized: boolean

  // ─── Computed ───────────────────────────────────────────
  isGuest: () => boolean
  isLoggedIn: () => boolean

  // ─── Actions ────────────────────────────────────────────
  setSession: (session: Session | null) => void
  setProfile: (profile: Profile | null) => void
  setLoading: (loading: boolean) => void
  setInitialized: (initialized: boolean) => void
  reset: () => void
}

const initialState = {
  user: null,
  session: null,
  profile: null,
  isLoading: true,
  isInitialized: false,
}

export const useAuthStore = create<AuthState>((set, get) => ({
  ...initialState,

  // ─── Computed ───────────────────────────────────────────
  isGuest: () => get().user === null,
  isLoggedIn: () => get().user !== null,

  // ─── Actions ────────────────────────────────────────────
  setSession: (session) =>
    set({
      session,
      user: session?.user ?? null,
    }),

  setProfile: (profile) => set({ profile }),

  setLoading: (isLoading) => set({ isLoading }),

  setInitialized: (isInitialized) => set({ isInitialized }),

  reset: () => set({ ...initialState, isLoading: false, isInitialized: true }),
}))
