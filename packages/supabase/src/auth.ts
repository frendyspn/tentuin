import { supabase } from './client'

// ─── Email / Password ────────────────────────────────────────────────────────

export const signUpWithEmail = async (
  email: string,
  password: string,
  fullName: string
) => {
  const { data, error } = await supabase.auth.signUp({
    email,
    password,
    options: {
      data: { full_name: fullName },
    },
  })
  if (error) throw error
  return data
}

export const signInWithEmail = async (email: string, password: string) => {
  const { data, error } = await supabase.auth.signInWithPassword({
    email,
    password,
  })
  if (error) throw error
  return data
}

// ─── Google OAuth ────────────────────────────────────────────────────────────

export const signInWithGoogle = async (redirectTo: string) => {
  const { data, error } = await supabase.auth.signInWithOAuth({
    provider: 'google',
    options: {
      redirectTo,
      skipBrowserRedirect: true,
    },
  })
  if (error) throw error
  return data.url
}

// ─── Session ─────────────────────────────────────────────────────────────────

export const getSession = async () => {
  const { data, error } = await supabase.auth.getSession()
  if (error) throw error
  return data.session
}

export const signOut = async () => {
  const { error } = await supabase.auth.signOut()
  if (error) throw error
}

// ─── Profile ─────────────────────────────────────────────────────────────────

export const getProfile = async (userId: string) => {
  const { data, error } = await supabase
    .from('profiles')
    .select('*')
    .eq('id', userId)
    .single()
  if (error) throw error
  return data
}

export const updateProfile = async (
  userId: string,
  updates: {
    full_name?: string
    school_name?: string
    city?: string
    birth_year?: number
    avatar_url?: string
    has_completed_onboarding?: boolean
  }
) => {
  const { data, error } = await supabase
    .from('profiles')
    .update({ ...updates, updated_at: new Date().toISOString() })
    .eq('id', userId)
    .select()
    .single()
  if (error) throw error
  return data
}
