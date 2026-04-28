import { useEffect, useRef } from 'react'
import * as Linking from 'expo-linking'
import { Stack, useRouter } from 'expo-router'
import * as SplashScreen from 'expo-splash-screen'
import { StatusBar } from 'expo-status-bar'
import {
  useFonts,
  PlusJakartaSans_400Regular,
  PlusJakartaSans_500Medium,
  PlusJakartaSans_600SemiBold,
  PlusJakartaSans_700Bold,
  PlusJakartaSans_800ExtraBold,
} from '@expo-google-fonts/plus-jakarta-sans'
import { supabase } from '@tentuin/supabase'
import { useAuthStore } from '../stores/authStore'
import { getProfile } from '@tentuin/supabase'
import { OfflineBanner } from '../components/OfflineBanner'

SplashScreen.preventAutoHideAsync()

export default function RootLayout() {
  const router = useRouter()
  const { setSession, setProfile, setLoading, setInitialized } = useAuthStore()
  const initDoneRef = useRef(false)
  const handlingCallbackRef = useRef(false)

  const [fontsLoaded] = useFonts({
    PlusJakartaSans_400Regular,
    PlusJakartaSans_500Medium,
    PlusJakartaSans_600SemiBold,
    PlusJakartaSans_700Bold,
    PlusJakartaSans_800ExtraBold,
  })

  useEffect(() => {
    const handleAuthUrl = async (url: string) => {
      const parsed = Linking.parse(url)
      const normalizedPath = (parsed.path ?? '')
        .replace(/^--\//, '')
        .replace(/^\/+/, '')
        .replace(/\/+$/, '')
      const normalizedHost = (parsed.hostname ?? '').replace(/^\/+|\/+$/g, '')
      const parsedPath = normalizedHost && normalizedPath
        ? `${normalizedHost}/${normalizedPath}`
        : normalizedPath || normalizedHost
      const codeValue = parsed.queryParams?.code
      const code = Array.isArray(codeValue) ? codeValue[0] : codeValue
      const queryAccessTokenValue = parsed.queryParams?.access_token
      const queryRefreshTokenValue = parsed.queryParams?.refresh_token
      const queryAccessToken = Array.isArray(queryAccessTokenValue)
        ? queryAccessTokenValue[0]
        : queryAccessTokenValue
      const queryRefreshToken = Array.isArray(queryRefreshTokenValue)
        ? queryRefreshTokenValue[0]
        : queryRefreshTokenValue
      const hash = url.includes('#') ? url.split('#')[1] : ''
      const hashParams = new URLSearchParams(hash)
      const accessToken = hashParams.get('access_token') ?? queryAccessToken
      const refreshToken = hashParams.get('refresh_token') ?? queryRefreshToken
      const isAuthCallbackPath =
        parsedPath === 'auth/callback' ||
        parsedPath === 'callback' ||
        (normalizedHost === 'auth' && normalizedPath === 'callback')

      console.log('[Auth] deep link path:', parsedPath, '| has code:', Boolean(code), '| has access token:', Boolean(accessToken))

      if ((!isAuthCallbackPath && !code && !accessToken) || handlingCallbackRef.current) {
        return
      }

      handlingCallbackRef.current = true

      try {
        if (accessToken && refreshToken) {
          const { data, error } = await supabase.auth.setSession({
            access_token: accessToken,
            refresh_token: refreshToken,
          })

          if (error) {
            console.error('[Auth] setSession error:', error.message)
            router.replace('/(auth)/login')
            return
          }

          if (data.session) {
            setSession(data.session)

            try {
              const profile = await getProfile(data.session.user.id)
              setProfile(profile)
            } catch (_error) {
              setProfile(null)
            }
          }

          router.replace('/(tabs)/home')
          return
        }

        if (code) {
          const { data, error } = await supabase.auth.exchangeCodeForSession(code)
          if (error) {
            console.error('[Auth] exchangeCodeForSession error:', error.message)
            router.replace('/(auth)/login')
            return
          }

          if (data.session) {
            setSession(data.session)

            try {
              const profile = await getProfile(data.session.user.id)
              setProfile(profile)
            } catch (_error) {
              setProfile(null)
            }
          }

          router.replace('/(tabs)/home')
          return
        }

        router.replace('/(auth)/login')
      } finally {
        handlingCallbackRef.current = false
      }
    }

    const initSession = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession()
        setSession(session)

        if (session?.user) {
          try {
            const profile = await getProfile(session.user.id)
            setProfile(profile)
            console.log('[Auth] Profile loaded successfully')
          } catch (error) {
            console.error('[Auth] Failed to fetch profile:', error)
            setProfile(null)
          }
        }
      } catch (error) {
        console.error('[Auth] Error init session:', error)
      } finally {
        setLoading(false)
        setInitialized(true)
        initDoneRef.current = true
      }
    }

    initSession()

    Linking.getInitialURL().then((url) => {
      if (url) {
        console.log('[Auth] initial url:', url)
        handleAuthUrl(url)
      }
    })

    const urlSubscription = Linking.addEventListener('url', ({ url }) => {
      console.log('[Auth] url event received')
      handleAuthUrl(url)
    })

    const { data: { subscription } } = supabase.auth.onAuthStateChange(
      async (event, session) => {
        console.log('[Auth] event:', event, '| user:', session?.user?.email, '| initDone:', initDoneRef.current)
        setSession(session)

        if (session?.user) {
          try {
            const profile = await getProfile(session.user.id)
            setProfile(profile)
          } catch (_error) {
            console.error('[Auth] Failed to get profile:', _error)
            setProfile(null)
          }
        } else {
          setProfile(null)
        }

        // Hanya navigate saat SIGNED_IN (login baru) atau SIGNED_OUT
        // TOKEN_REFRESHED & SESSION_UPDATED jangan trigger redirect — user mungkin sedang di tengah flow lain
        if (initDoneRef.current) {
          if (event === 'SIGNED_IN' && session?.user) {
            console.log('[Auth] Redirecting to home after SIGNED_IN')
            try {
              router.replace('/(tabs)/home')
            } catch (e) {
              console.error('[Auth] Navigation error:', e)
            }
          } else if (event === 'SIGNED_OUT') {
            console.log('[Auth] User signed out')
            router.replace('/(auth)/login')
          }
        }
      }
    )

    return () => {
      urlSubscription.remove()
      subscription.unsubscribe()
    }
  }, [])

  useEffect(() => {
    if (fontsLoaded) SplashScreen.hideAsync()
  }, [fontsLoaded])

  if (!fontsLoaded) return null

  return (
    <>
      <StatusBar style="auto" />
      <OfflineBanner />
      <Stack screenOptions={{ headerShown: false }}>
        <Stack.Screen name="index" />
        <Stack.Screen name="(onboarding)" />
        <Stack.Screen name="(auth)" />
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="auth/callback" />
        <Stack.Screen name="test-session" />
        <Stack.Screen name="test-result" />
        <Stack.Screen name="test-history" />
        <Stack.Screen name="edit-profile" />
        <Stack.Screen name="favorites-majors" />
        <Stack.Screen name="favorites-campus" />
        <Stack.Screen name="university-detail" />
        <Stack.Screen name="major-detail" />
      </Stack>
    </>
  )
}
