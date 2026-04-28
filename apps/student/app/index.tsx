import { Redirect } from 'expo-router'
import { useEffect, useState } from 'react'
import { View, ActivityIndicator, Text } from 'react-native'
import { useAuthStore } from '../stores/authStore'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { colors } from '@tentuin/config'

const ONBOARDING_KEY = 'tentuin_onboarding_done'

export default function Index() {
  const { isInitialized, session } = useAuthStore()
  const [onboardingDone, setOnboardingDone] = useState<boolean | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let resolved = false

    const checkOnboarding = async () => {
      try {
        const value = await AsyncStorage.getItem(ONBOARDING_KEY)
        resolved = true
        setOnboardingDone(value === 'true')
      } catch (e) {
        console.error('[Index] Error checking onboarding:', e)
        resolved = true
        setOnboardingDone(false)
        setError('Failed to load app state')
      }
    }

    // Fallback kalau AsyncStorage hang (sangat jarang, tapi bisa terjadi di emulator)
    const timeoutId = setTimeout(() => {
      if (!resolved) {
        console.warn('[Index] Onboarding check timeout, defaulting to false')
        resolved = true
        setOnboardingDone(false)
      }
    }, 3000)

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
