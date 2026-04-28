import { useEffect } from 'react'
import * as Linking from 'expo-linking'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import { useRouter, useLocalSearchParams } from 'expo-router'
import { colors } from '@tentuin/config'
import { supabase } from '@tentuin/supabase'
import { getProfile } from '@tentuin/supabase'
import { useAuthStore } from '../../stores/authStore'

const parseAuthPayloadFromUrl = (url: string) => {
  const parsed = Linking.parse(url)
  const queryParams = parsed.queryParams ?? {}

  const codeValue = queryParams.code
  const code = Array.isArray(codeValue) ? codeValue[0] : codeValue

  const queryErrorValue = queryParams.error
  const queryError = Array.isArray(queryErrorValue) ? queryErrorValue[0] : queryErrorValue

  const queryErrorDescriptionValue = queryParams.error_description
  const queryErrorDescription = Array.isArray(queryErrorDescriptionValue)
    ? queryErrorDescriptionValue[0]
    : queryErrorDescriptionValue

  const queryAccessTokenValue = queryParams.access_token
  const queryAccessToken = Array.isArray(queryAccessTokenValue)
    ? queryAccessTokenValue[0]
    : queryAccessTokenValue

  const queryRefreshTokenValue = queryParams.refresh_token
  const queryRefreshToken = Array.isArray(queryRefreshTokenValue)
    ? queryRefreshTokenValue[0]
    : queryRefreshTokenValue

  const hash = url.includes('#') ? url.split('#')[1] : ''
  const hashParams = new URLSearchParams(hash)

  return {
    code,
    error: hashParams.get('error') ?? queryError,
    errorDescription: hashParams.get('error_description') ?? queryErrorDescription,
    accessToken: hashParams.get('access_token') ?? queryAccessToken,
    refreshToken: hashParams.get('refresh_token') ?? queryRefreshToken,
  }
}

export default function AuthCallbackScreen() {
  const router = useRouter()
  const { setSession, setProfile } = useAuthStore()
  const params = useLocalSearchParams<{ code?: string; error?: string; error_description?: string }>()

  useEffect(() => {
    let isMounted = true

    const redirectToLogin = (message: string) => {
      if (!isMounted) return
      router.replace({
        pathname: '/(auth)/login',
        params: { oauth_error: message },
      })
    }

    const completeCallback = async () => {
      console.log('[AuthCallback] params:', JSON.stringify(params))

      const initialUrl = await Linking.getInitialURL()
      const payloadFromUrl = initialUrl ? parseAuthPayloadFromUrl(initialUrl) : null
      const code = typeof params.code === 'string' ? params.code : payloadFromUrl?.code
      const error = typeof params.error === 'string' ? params.error : payloadFromUrl?.error
      const errorDescription = typeof params.error_description === 'string'
        ? params.error_description
        : payloadFromUrl?.errorDescription
      const accessToken = payloadFromUrl?.accessToken
      const refreshToken = payloadFromUrl?.refreshToken

      if (error) {
        redirectToLogin(errorDescription || 'Login Google gagal. Periksa konfigurasi OAuth.')
        return
      }

      try {
        if (accessToken && refreshToken) {
          const { error: setSessionError } = await supabase.auth.setSession({
            access_token: accessToken,
            refresh_token: refreshToken,
          })
          if (setSessionError) {
            console.error('[AuthCallback] setSession error:', setSessionError.message)
          }
        } else if (code) {
          const { error: exchangeError } = await supabase.auth.exchangeCodeForSession(code)
          if (exchangeError) {
            console.error('[AuthCallback] exchangeCodeForSession error:', exchangeError.message)
          }
        }

        const { data: { session } } = await supabase.auth.getSession()
        if (session) {
          setSession(session)
          try {
            const profile = await getProfile(session.user.id)
            setProfile(profile)
          } catch (_error) {
            setProfile(null)
          }

          if (isMounted) {
            router.replace('/(tabs)/home')
          }
          return
        }

        redirectToLogin('Login Google tidak menemukan sesi. Coba lagi.')
      } catch (error) {
        console.error('[AuthCallback] unexpected error:', error)
        redirectToLogin('Login Google gagal diproses. Coba lagi.')
      }
    }

    completeCallback()

    return () => {
      isMounted = false
    }
  }, [params, router, setProfile, setSession])

  return (
    <View style={styles.container}>
      <ActivityIndicator size="large" color={colors.primary} />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.background,
  },
})
