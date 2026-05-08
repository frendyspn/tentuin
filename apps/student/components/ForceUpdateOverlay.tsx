import React, { useEffect, useRef } from 'react'
import {
  Animated,
  BackHandler,
  Linking,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { hapticMedium } from '../utils/haptics'

interface Props {
  currentVersion: string
  minVersion:     string
  storeUrl:       string
}

export function ForceUpdateOverlay({ currentVersion, minVersion, storeUrl }: Props) {
  const fadeAnim = useRef(new Animated.Value(0)).current
  const scaleAnim = useRef(new Animated.Value(0.88)).current

  // Blokir tombol back Android
  useEffect(() => {
    if (Platform.OS !== 'android') return
    const sub = BackHandler.addEventListener('hardwareBackPress', () => true)
    return () => sub.remove()
  }, [])

  // Entrance animation
  useEffect(() => {
    Animated.parallel([
      Animated.timing(fadeAnim, { toValue: 1, duration: 300, useNativeDriver: true }),
      Animated.spring(scaleAnim, { toValue: 1, damping: 16, stiffness: 180, useNativeDriver: true }),
    ]).start()
  }, [])

  const handleUpdate = async () => {
    hapticMedium()
    try {
      const canOpen = await Linking.canOpenURL(storeUrl)
      if (canOpen) Linking.openURL(storeUrl)
    } catch {
      // fallback — buka Play Store / App Store langsung
      const fallback = Platform.OS === 'ios'
        ? 'https://apps.apple.com'
        : 'https://play.google.com/store'
      Linking.openURL(fallback)
    }
  }

  return (
    <Animated.View style={[styles.backdrop, { opacity: fadeAnim }]}>
      <Animated.View style={[styles.card, { transform: [{ scale: scaleAnim }] }]}>

        {/* Icon */}
        <View style={styles.iconWrap}>
          <Ionicons name="arrow-up-circle" size={52} color={colors.primary} />
        </View>

        {/* Copy */}
        <Text style={styles.title}>Update Diperlukan</Text>
        <Text style={styles.desc}>
          Versi aplikasi kamu sudah terlalu lama. Update sekarang untuk mendapatkan
          pengalaman terbaik dan fitur terbaru.
        </Text>

        {/* Version info */}
        <View style={styles.versionRow}>
          <View style={styles.versionBadge}>
            <Text style={styles.versionLabel}>Versi kamu</Text>
            <Text style={styles.versionNum}>{currentVersion}</Text>
          </View>
          <Ionicons name="arrow-forward" size={16} color={colors.textMuted} />
          <View style={[styles.versionBadge, styles.versionBadgeNew]}>
            <Text style={styles.versionLabel}>Minimal</Text>
            <Text style={[styles.versionNum, { color: colors.primary }]}>{minVersion}</Text>
          </View>
        </View>

        {/* CTA */}
        <Pressable
          style={({ pressed }) => [styles.btn, pressed && { opacity: 0.88, transform: [{ scale: 0.97 }] }]}
          onPress={handleUpdate}
        >
          <Ionicons name="cloud-download-outline" size={20} color={colors.white} />
          <Text style={styles.btnText}>Update Sekarang</Text>
        </Pressable>

        <Text style={styles.note}>
          {Platform.OS === 'ios' ? 'App Store' : 'Google Play Store'}
        </Text>
      </Animated.View>
    </Animated.View>
  )
}

const styles = StyleSheet.create({
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.65)',
    alignItems:      'center',
    justifyContent:  'center',
    zIndex:          9999,
    padding:         28,
  },
  card: {
    width:           '100%',
    backgroundColor: colors.surface,
    borderRadius:    24,
    padding:         28,
    alignItems:      'center',
    gap:             14,
    shadowColor:     '#000',
    shadowOffset:    { width: 0, height: 8 },
    shadowOpacity:   0.18,
    shadowRadius:    24,
    elevation:       12,
  },

  iconWrap: {
    width:           88,
    height:          88,
    borderRadius:    22,
    backgroundColor: colors.primaryLight,
    alignItems:      'center',
    justifyContent:  'center',
    marginBottom:    4,
  },

  title: {
    fontSize:      24,
    fontFamily:    fonts.extraBold,
    color:         colors.text,
    letterSpacing: -0.6,
    textAlign:     'center',
  },
  desc: {
    fontSize:   14,
    fontFamily: fonts.regular,
    color:      colors.textSub,
    textAlign:  'center',
    lineHeight: 22,
  },

  versionRow: {
    flexDirection:  'row',
    alignItems:     'center',
    gap:            12,
    marginVertical: 4,
  },
  versionBadge: {
    alignItems:      'center',
    gap:             3,
    backgroundColor: colors.gray[100],
    paddingHorizontal: 16,
    paddingVertical:   10,
    borderRadius:    12,
  },
  versionBadgeNew: {
    backgroundColor: colors.primaryMuted,
  },
  versionLabel: {
    fontSize:   10,
    fontFamily: fonts.medium,
    color:      colors.textMuted,
    textTransform: 'uppercase',
    letterSpacing:  0.6,
  },
  versionNum: {
    fontSize:   16,
    fontFamily: fonts.extraBold,
    color:      colors.text,
  },

  btn: {
    width:           '100%',
    flexDirection:   'row',
    alignItems:      'center',
    justifyContent:  'center',
    gap:             10,
    backgroundColor: colors.primary,
    borderRadius:    14,
    paddingVertical: 16,
    shadowColor:     colors.primary,
    shadowOffset:    { width: 0, height: 6 },
    shadowOpacity:   0.3,
    shadowRadius:    12,
    elevation:       6,
  },
  btnText: {
    fontSize:   16,
    fontFamily: fonts.bold,
    color:      colors.white,
  },

  note: {
    fontSize:   12,
    fontFamily: fonts.regular,
    color:      colors.textMuted,
    marginTop:  -4,
  },
})
