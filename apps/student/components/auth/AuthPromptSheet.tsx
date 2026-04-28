import React from 'react'
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { Button } from '@tentuin/ui'

interface Props {
  visible: boolean
  onClose: () => void
  message?: string
}

export const AuthPromptSheet: React.FC<Props> = ({
  visible,
  onClose,
  message = 'Daftar gratis untuk mulai test dan simpan hasilmu.',
}) => {
  const router = useRouter()

  const goRegister = () => { onClose(); router.push('/(auth)/register') }
  const goLogin    = () => { onClose(); router.push('/(auth)/login') }

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose} />
      <View style={styles.sheet}>

        {/* Handle */}
        <View style={styles.handle} />

        {/* Content */}
        <View style={styles.body}>
          <View style={styles.iconBadge}>
            <Ionicons name="ribbon" size={32} color={colors.primary} />
          </View>
          <Text style={styles.title}>Yuk, Tentuin Jurusanmu!</Text>
          <Text style={styles.message}>{message}</Text>
        </View>

        {/* Buttons */}
        <View style={styles.actions}>
          <Button
            label="Daftar Gratis"
            variant="primary"
            size="lg"
            onPress={goRegister}
            fullWidth
          />
          <Button
            label="Sudah Punya Akun — Masuk"
            variant="outline"
            size="md"
            onPress={goLogin}
            fullWidth
          />
        </View>

        {/* Skip */}
        <Pressable onPress={onClose} style={styles.skipBtn} hitSlop={12}>
          <Text style={styles.skipText}>Nanti saja</Text>
        </Pressable>

      </View>
    </Modal>
  )
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(17,24,39,0.5)',
  },
  sheet: {
    backgroundColor: colors.surface,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    paddingTop: 12,
    paddingHorizontal: 24,
    paddingBottom: 44,
  },
  handle: {
    width: 36,
    height: 4,
    borderRadius: 2,
    backgroundColor: colors.gray[200],
    alignSelf: 'center',
    marginBottom: 24,
  },
  body: {
    alignItems: 'center',
    gap: 10,
    marginBottom: 24,
  },
  iconBadge: {
    width: 72,
    height: 72,
    borderRadius: 18,
    backgroundColor: colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 4,
  },
  title: {
    fontSize: 22,
    fontFamily: fonts.extraBold,
    color: colors.text,
    textAlign: 'center',
    letterSpacing: -0.5,
  },
  message: {
    fontSize: 15,
    fontFamily: fonts.regular,
    color: colors.textSub,
    textAlign: 'center',
    lineHeight: 23,
  },
  actions: { gap: 10, marginBottom: 16 },
  skipBtn: { alignSelf: 'center', paddingVertical: 8 },
  skipText: { fontSize: 14, fontFamily: fonts.medium, color: colors.textMuted },
})
