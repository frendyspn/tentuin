import { useState } from 'react'
import * as WebBrowser from 'expo-web-browser'
import * as Linking from 'expo-linking'
import { Platform } from 'react-native'
import { signInWithGoogle as getGoogleUrl } from '@tentuin/supabase'

WebBrowser.maybeCompleteAuthSession()

export const useGoogleAuth = () => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const signInWithGoogle = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const redirectUrl = Linking.createURL('auth/callback')
      console.log('[Google Auth] redirectUrl:', redirectUrl)

      const url = await getGoogleUrl(redirectUrl)
      console.log('[Google Auth] oauth url:', url)

      if (!url) throw new Error('URL OAuth tidak tersedia. Aktifkan Google provider di Supabase.')

      if (Platform.OS === 'android') {
        await Linking.openURL(url)
        setIsLoading(false)
        return
      }

      const result = await WebBrowser.openAuthSessionAsync(url, redirectUrl)
      console.log('[Google Auth] result type:', result.type)

      setIsLoading(false)
    } catch (err: any) {
      console.error('[Google Auth] error:', err?.message)
      setError(err?.message ?? 'Login Google gagal, coba lagi.')
      setIsLoading(false)
    }
  }

  return { signInWithGoogle, isLoading, error }
}
