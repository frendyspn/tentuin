import { supabase, supabaseUrl, supabaseAnonKey } from './client'

export type Question = {
  id: string
  order_number: number
  text: string
  category: 'realistic' | 'investigative' | 'artistic' | 'social' | 'enterprising' | 'conventional'
  is_active: boolean
}

export type RiasecScores = {
  realistic: number
  investigative: number
  artistic: number
  social: number
  enterprising: number
  conventional: number
}

/**
 * Fetch active questions ordered by order_number.
 * Uses direct REST fetch to avoid Supabase JS-client hanging issues on Android.
 */
export const getQuestions = async (): Promise<Question[]> => {
  const url =
    `${supabaseUrl}/rest/v1/questions` +
    `?is_active=eq.true&order=order_number.asc&select=id,order_number,text,category,is_active`

  const res = await fetch(url, {
    method: 'GET',
    headers: {
      apikey: supabaseAnonKey,
      Authorization: `Bearer ${supabaseAnonKey}`,
      'Content-Type': 'application/json',
    },
  })

  if (!res.ok) {
    const body = await res.text()
    throw new Error(`getQuestions failed (${res.status}): ${body}`)
  }

  return res.json() as Promise<Question[]>
}

export const saveTestResult = async (
  userId: string,
  scores: RiasecScores,
  riasecCode: string,
  accessToken?: string,
) => {
  console.log('[Supabase] saveTestResult: starting for user', userId)
  
  // Try REST API directly
  try {
    console.log('[Supabase] saveTestResult: using REST API...')
    const restUrl = `${supabase.supabaseUrl}/rest/v1/test_results`
    
    const body = {
      user_id: userId,
      scores: scores,
      riasec_code: riasecCode,
    }
    
    console.log('[Supabase] saveTestResult: body:', JSON.stringify(body))
    
    const headers: Record<string, string> = {
      'apikey': supabaseAnonKey,
      'Content-Type': 'application/json',
      'Prefer': 'return=representation',
    }
    
    // Add auth token if provided
    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`
      console.log('[Supabase] saveTestResult: using auth token')
    }
    
    const response = await Promise.race([
      fetch(restUrl, {
        method: 'POST',
        headers,
        body: JSON.stringify(body),
      }),
      new Promise<Response>((_, reject) =>
        setTimeout(() => reject(new Error('fetch timeout')), 5000)
      ),
    ]) as Response
    
    console.log('[Supabase] saveTestResult: response status:', response.status)
    
    if (!response.ok) {
      const errorText = await response.text()
      console.error('[Supabase] saveTestResult: error response:', errorText)
      throw new Error(`${response.status}: ${errorText}`)
    }
    
    const data = await response.json()
    console.log('[Supabase] saveTestResult: success, id:', data?.[0]?.id ?? data?.id)
    return data?.[0] ?? data
  } catch (err: any) {
    console.error('[Supabase] saveTestResult: error:', err?.message)
    throw err
  }
}

export const getTestHistory = async (userId: string, accessToken?: string) => {
  console.log('[Supabase] getTestHistory: starting for user', userId)
  
  // Try REST API directly like getQuestions
  try {
    console.log('[Supabase] getTestHistory: trying REST API...')
    const restUrl = `${supabase.supabaseUrl}/rest/v1/test_results?user_id=eq.${userId}&order=completed_at.desc`
    console.log('[Supabase] getTestHistory: URL:', restUrl)
    
    const headers: Record<string, string> = {
      'apikey': supabaseAnonKey,
      'Content-Type': 'application/json',
    }
    
    // If we have a token, use it
    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`
      console.log('[Supabase] getTestHistory: using provided auth token')
    }
    
    const response = await Promise.race([
      fetch(restUrl, {
        method: 'GET',
        headers,
      }),
      new Promise<Response>((_, reject) =>
        setTimeout(() => reject(new Error('fetch timeout')), 5000)
      ),
    ]) as Response
    
    console.log('[Supabase] getTestHistory: response status:', response.status)
    
    if (!response.ok) {
      const errorText = await response.text()
      console.error('[Supabase] getTestHistory: error response:', errorText)
      throw new Error(`${response.status}: ${errorText}`)
    }
    
    const data = await response.json()
    console.log('[Supabase] getTestHistory: success, got', data.length, 'results')
    return data
  } catch (err: any) {
    console.error('[Supabase] getTestHistory: REST API error:', err?.message)
    throw err
  }
}
