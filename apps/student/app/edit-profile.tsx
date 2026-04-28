import React, { useState } from 'react'
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { Input, Button } from '@tentuin/ui'
import { supabase } from '@tentuin/supabase'
import { useAuthStore } from '../stores/authStore'
import { hapticSuccess, hapticMedium } from '../utils/haptics'

export default function EditProfileScreen() {
  const router = useRouter()
  const { profile, session, setProfile } = useAuthStore()

  const [form, setForm] = useState({
    full_name:   profile?.full_name   ?? '',
    school_name: profile?.school_name ?? '',
    city:        profile?.city        ?? '',
  })
  const [saving, setSaving] = useState(false)

  const handleSave = async () => {
    if (!session?.user?.id) return
    hapticMedium()
    setSaving(true)
    try {
      const { data, error } = await supabase
        .from('profiles')
        .update({
          full_name:   form.full_name.trim()   || null,
          school_name: form.school_name.trim() || null,
          city:        form.city.trim()        || null,
          updated_at:  new Date().toISOString(),
        })
        .eq('id', session.user.id)
        .select()
        .single()

      if (error) throw error
      setProfile(data)
      hapticSuccess()
      Alert.alert('Berhasil', 'Profil berhasil diperbarui.')
      router.back()
    } catch (err: any) {
      Alert.alert('Gagal', err?.message ?? 'Coba lagi.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <SafeAreaView style={styles.safe} edges={['top', 'bottom']}>
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        {/* Header */}
        <View style={styles.header}>
          <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
            <Ionicons name="arrow-back" size={22} color={colors.text} />
          </Pressable>
          <Text style={styles.title}>Edit Profil</Text>
          <View style={{ width: 36 }} />
        </View>

        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          {/* Avatar */}
          <View style={styles.avatarWrap}>
            <View style={styles.avatar}>
              <Text style={styles.avatarText}>
                {form.full_name?.[0]?.toUpperCase() ?? session?.user?.email?.[0]?.toUpperCase() ?? '?'}
              </Text>
            </View>
            <Text style={styles.avatarHint}>Foto profil belum tersedia</Text>
          </View>

          {/* Form */}
          <View style={styles.form}>
            <Input
              label="Nama Lengkap"
              placeholder="Nama kamu"
              value={form.full_name}
              onChangeText={(v) => setForm({ ...form, full_name: v })}
            />
            <Input
              label="Nama Sekolah"
              placeholder="SMA / SMK / MA-mu"
              value={form.school_name}
              onChangeText={(v) => setForm({ ...form, school_name: v })}
            />
            <Input
              label="Kota"
              placeholder="Kota tempat tinggal"
              value={form.city}
              onChangeText={(v) => setForm({ ...form, city: v })}
            />
          </View>

          {/* Email (read-only) */}
          <View style={styles.emailRow}>
            <Ionicons name="mail-outline" size={16} color={colors.textMuted} />
            <Text style={styles.emailText}>{session?.user?.email}</Text>
            <View style={styles.emailBadge}>
              <Text style={styles.emailBadgeText}>Tidak dapat diubah</Text>
            </View>
          </View>

          <Button
            label="Simpan Perubahan"
            variant="primary"
            size="lg"
            onPress={handleSave}
            isLoading={saving}
            fullWidth
            style={styles.saveBtn}
          />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },

  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 4,
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  backBtn: {
    width: 36, height: 36, borderRadius: 10,
    backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center',
  },
  title: { fontSize: 20, fontFamily: fonts.bold, color: colors.text },

  content: { padding: 24, gap: 20, paddingBottom: 48 },

  avatarWrap: { alignItems: 'center', gap: 8, paddingVertical: 8 },
  avatar: {
    width: 88, height: 88, borderRadius: 44,
    backgroundColor: colors.primaryLight,
    alignItems: 'center', justifyContent: 'center',
  },
  avatarText: { fontSize: 36, fontFamily: fonts.extraBold, color: colors.primary },
  avatarHint: { fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },

  form: { gap: 16 },

  emailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    backgroundColor: colors.surface,
    borderRadius: 10,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
  },
  emailText: { flex: 1, fontSize: 14, fontFamily: fonts.regular, color: colors.textSub },
  emailBadge: {
    backgroundColor: colors.gray[100],
    paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6,
  },
  emailBadgeText: { fontSize: 10, fontFamily: fonts.medium, color: colors.textMuted },

  saveBtn: { marginTop: 4 },
})
