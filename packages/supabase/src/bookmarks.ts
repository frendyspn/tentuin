import { supabase, supabaseUrl, supabaseAnonKey } from './client'
import { NetworkError, SessionExpiredError } from './universities'

// ─── Types ────────────────────────────────────────────────────────────────────

export type UniversityBookmark = {
  id: string
  user_id: string
  university_id: string
  major_names: string[]  // Array of major names user is interested in
  created_at: string
  updated_at: string
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

const restFetch = async <T>(
  path: string,
  method: string = 'GET',
  body?: unknown,
  token?: string,
): Promise<T> => {
  const url = `${supabaseUrl}/rest/v1/${path}`

  let res: Response
  try {
    res = await fetch(url, {
      method,
      headers: {
        apikey: supabaseAnonKey,
        Authorization: `Bearer ${token ?? supabaseAnonKey}`,
        'Content-Type': 'application/json',
      },
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new NetworkError()
  }

  if (res.status === 401) {
    await supabase.auth.signOut()
    throw new SessionExpiredError()
  }

  const text = await res.text()
  if (!res.ok) {
    throw new Error(`[${res.status}] ${text || 'No response body'}`)
  }
  if (!text) return null as T

  try {
    return JSON.parse(text)
  } catch {
    throw new Error(`[JSON Parse Error] ${text?.slice(0, 100) || 'Empty response'}`)
  }
}

// ─── Queries ──────────────────────────────────────────────────────────────────

/**
 * Save atau update bookmark universitas
 * Jika sudah ada, update major_names; jika belum ada, insert
 */
export const saveUniversityBookmark = async (
  userId: string,
  universityId: string,
  majorNames: string[],
  token?: string,
): Promise<UniversityBookmark> => {
  // First check if bookmark already exists
  const existing = await checkUniversityBookmark(userId, universityId, token)
  
  if (existing) {
    // Update existing bookmark with new major names
    console.log('[saveUniversityBookmark] Updating existing bookmark')
    return restFetch<UniversityBookmark>(
      `university_bookmarks?user_id=eq.${userId}&university_id=eq.${universityId}`,
      'PATCH',
      {
        major_names: majorNames,
      },
      token,
    )
  }
  
  // Insert new bookmark
  console.log('[saveUniversityBookmark] Creating new bookmark')
  return restFetch<UniversityBookmark>(
    'university_bookmarks',
    'POST',
    {
      user_id: userId,
      university_id: universityId,
      major_names: majorNames,
    },
    token,
  )
}

/**
 * Get semua bookmark user
 */
export const getUserUniversityBookmarks = async (
  userId: string,
  token?: string,
): Promise<UniversityBookmark[]> => {
  return restFetch<UniversityBookmark[]>(
    `university_bookmarks?user_id=eq.${userId}&order=created_at.desc`,
    'GET',
    undefined,
    token,
  )
}

/**
 * Delete bookmark
 */
export const deleteUniversityBookmark = async (
  userId: string,
  universityId: string,
  token?: string,
): Promise<void> => {
  await restFetch<void>(
    `university_bookmarks?user_id=eq.${userId}&university_id=eq.${universityId}`,
    'DELETE',
    undefined,
    token,
  )
}

/**
 * Get semua bookmark user beserta data universitas (join)
 */
export const getBookmarkedUniversities = async (
  userId: string,
  token?: string,
): Promise<(UniversityBookmark & { universities: import('./universities').UniversityRow })[]> => {
  return restFetch<(UniversityBookmark & { universities: import('./universities').UniversityRow })[]>(
    `university_bookmarks?user_id=eq.${userId}&select=*,universities(*)&order=created_at.desc`,
    'GET',
    undefined,
    token,
  )
}

/**
 * Check if university is bookmarked by user
 */
export const checkUniversityBookmark = async (
  userId: string,
  universityId: string,
  token?: string,
): Promise<UniversityBookmark | null> => {
  const results = await restFetch<UniversityBookmark[]>(
    `university_bookmarks?user_id=eq.${userId}&university_id=eq.${universityId}&select=*`,
    'GET',
    undefined,
    token,
  )
  return results[0] ?? null
}
