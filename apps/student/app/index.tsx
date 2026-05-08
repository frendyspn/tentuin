import { Redirect } from 'expo-router'
import { useEffect, useState } from 'react'
import { View, ActivityIndicator, Text } from 'react-native'
import { useAuthStore } from '../stores/authStore'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { colors } from '@tentuin/config'

const ONBOARDING_KEY = 'tentuin_onboarding_done'
const INIT_TIMEOUT = 5000 // 5 seconds timeout

export default function Index() {
  const { isInitialized, session } = useAuthStore()
  const [onboardingDone, setOnboardingDone] = useState<boolean | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const checkOnboarding = async () => {
      try {
        const value = await AsyncStorage.getItem(ONBOARDING_KEY)
        setOnboardingDone(value === 'true')
      } catch (e) {
        console.error('[Index] Error checking onboarding:', e)
        setOnboardingDone(false)
        setError('Failed to load app state')
      }
    }

    const timeoutId = setTimeout(() => {
      if (onboardingDone === null) {
        console.warn('[Index] Onboarding check timeout, defaulting to false')
        setOnboardingDone(false)
        setError('Loading timeout - defaulting to home')
      }
    }, INIT_TIMEOUT)

    checkOnboarding()

    return () => clearTimeout(timeoutId)
  }, [])

  // Tunggu sampai session & onboarding status keduanya siap
  if (!isInitialized || onboardingDone === null) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.background }}>
        <ActivityIndicator size="large" color={colors.primary} />
        {error && <Text style={{ marginTop: 16, color: colors.textMuted, fontSize: 12 }}>{error}</Text>}
      </View>
    )
  }

  // Belum pernah onboarding → tampilkan onboarding
  if (!onboardingDone) return <Redirect href="/(onboarding)" />

  // Sudah onboarding → langsung ke home (guest atau logged-in)
  return <Redirect href="/(tabs)/home" />
}
