import { Stack } from 'expo-router'
import { colors } from '@tentuin/config'

export default function AuthLayout() {
  return (
    <Stack
      screenOptions={{
        headerShown: true,
        headerTransparent: true,
        headerTitle: '',
        headerTintColor: colors.primary,
        animation: 'slide_from_right',
      }}
    />
  )
}
