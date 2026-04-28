import React, { useEffect, useState } from 'react'
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { useLocalSearchParams, useRouter } from 'expo-router'
import { Ionicons, AntDesign } from '@expo/vector-icons'
import { signInWithEmail } from '@tentuin/supabase'
import { colors, fonts } from '@tentuin/config'
import { Button, Input } from '@tentuin/ui'
import { useGoogleAuth } from '../../hooks/useGoogleAuth'
import { useAuthStore } from '../../stores/authStore'
import { hapticMedium, hapticLight } from '../../utils/haptics'

export default function LoginScreen() {
  const router = useRouter()
  const params = useLocalSearchParams<{ oauth_error?: string }>()
  const user = useAuthStore((state) => state.user)
  const [form, setForm] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isLoading, setIsLoading] = useState(false)
  const { signInWithGoogle, isLoading: isGoogleLoading, error: googleError } = useGoogleAuth()

  useEffect(() => {
    if (user) {
      router.replace('/(tabs)/home')
    }
  }, [router, user])

  useEffect(() => {
    const oauthError = params.oauth_error
    if (typeof oauthError === 'string' && oauthError.trim().length > 0) {
      setErrors((prev) => ({ ...prev, google: oauthError }))
    }
  }, [params.oauth_error])

  const validate = () => {
    const e: Record<string, string> = {}
    if (!form.email.trim()) e.email = 'Email wajib diisi'
    if (!form.password)     e.password = 'Password wajib diisi'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleLogin = async () => {
    if (!validate()) return
    hapticMedium()
    setIsLoading(true)
    try {
      await signInWithEmail(form.email, form.password)
      router.replace('/(tabs)/home')
    } catch {
      setErrors({ general: 'Email atau password salah. Coba lagi.' })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView
        contentContainerStyle={styles.scroll}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >

        {/* Header */}
        <View style={styles.header}>
          <View style={styles.iconBadge}>
            <Ionicons name="hand-left-outline" size={26} color={colors.primary} />
          </View>
          <Text style={styles.title}>Selamat Datang!</Text>
          <Text style={styles.subtitle}>Masuk untuk lanjutkan perjalananmu</Text>
        </View>

        {/* Error banner */}
        {errors.general && (
          <View style={styles.errorBanner}>
            <Ionicons name="warning-outline" size={16} color={colors.error} />
            <Text style={styles.errorBannerText}>{errors.general}</Text>
          </View>
        )}

        {/* Form */}
        <View style={styles.form}>
          <Input
            label="Email"
            placeholder="email@contoh.com"
            value={form.email}
            onChangeText={v => setForm({ ...form, email: v })}
            error={errors.email}
            keyboardType="email-address"
          />
          <Input
            label="Password"
            placeholder="Password kamu"
            value={form.password}
            onChangeText={v => setForm({ ...form, password: v })}
            error={errors.password}
            isPassword
          />
        </View>

        {/* CTA */}
        <Button
          label="Masuk"
          variant="primary"
          size="lg"
          onPress={handleLogin}
          isLoading={isLoading}
          fullWidth
          style={styles.ctaBtn}
        />

        {/* Divider */}
        <View style={styles.divider}>
          <View style={styles.dividerLine} />
          <Text style={styles.dividerText}>atau lanjut dengan</Text>
          <View style={styles.dividerLine} />
        </View>

        {/* Google Sign In */}
        {(googleError ?? errors.google) && (
          <View style={styles.errorBanner}>
            <Ionicons name="warning-outline" size={16} color={colors.error} />
            <Text style={styles.errorBannerText}>{googleError ?? errors.google}</Text>
          </View>
        )}
        <Pressable
          style={({ pressed }) => [styles.googleBtn, pressed && { opacity: 0.85 }]}
          onPress={() => { hapticLight(); signInWithGoogle() }}
          disabled={isGoogleLoading}
        >
          {isGoogleLoading
            ? <Ionicons name="sync-outline" size={20} color={colors.text} />
            : <AntDesign name="google" size={20} color="#EA4335" />
          }
          <Text style={styles.googleBtnText}>Masuk dengan Google</Text>
        </Pressable>

        {/* Register link */}
        <View style={styles.footer}>
          <Text style={styles.footerText}>Belum punya akun?</Text>
          <Pressable onPress={() => router.push('/(auth)/register')} hitSlop={8}>
            <Text style={styles.footerLink}> Daftar Gratis</Text>
          </Pressable>
        </View>

      </ScrollView>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.background },

  scroll: {
    flexGrow: 1,
    paddingHorizontal: 24,
    paddingTop: 100,
    paddingBottom: 48,
  },

  header: {
    marginBottom: 36,
    gap: 10,
  },
  iconBadge: {
    width: 56,
    height: 56,
    borderRadius: 14,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 4,
  },
  title: {
    fontSize: 32,
    fontFamily: fonts.extraBold,
    color: colors.text,
    letterSpacing: -1,
  },
  subtitle: {
    fontSize: 16,
    fontFamily: fonts.regular,
    color: colors.textSub,
    lineHeight: 24,
  },

  errorBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    backgroundColor: colors.errorLight,
    borderRadius: 10,
    padding: 14,
    marginBottom: 20,
  },
  errorBannerText: { fontSize: 14, fontFamily: fonts.medium, color: colors.error, flex: 1 },

  form: { gap: 16, marginBottom: 28 },

  ctaBtn: { marginBottom: 24 },

  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 24,
  },
  dividerLine: { flex: 1, height: 1, backgroundColor: colors.border },
  dividerText: { fontSize: 13, fontFamily: fonts.medium, color: colors.textMuted },

  googleBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
    backgroundColor: colors.surface,
    borderRadius: 100,
    borderWidth: 1.5,
    borderColor: colors.border,
    paddingVertical: 15,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 2,
  },
  googleBtnText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.text },

  footer: { flexDirection: 'row', justifyContent: 'center', alignItems: 'center' },
  footerText: { fontSize: 15, fontFamily: fonts.regular, color: colors.textSub },
  footerLink: { fontSize: 15, fontFamily: fonts.bold, color: colors.primary },
})
