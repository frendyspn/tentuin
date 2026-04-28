import React from 'react'
import { Dimensions, StyleSheet, Text, View } from 'react-native'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import type { OnboardingSlide as SlideType } from '@tentuin/types'

const { width } = Dimensions.get('window')

const SLIDE_CONFIG: Record<string, {
  bg: string
  card: string
  icon: keyof typeof Ionicons.glyphMap
  iconColor: string
}> = {
  'onboarding-1': { bg: '#EEF2FF', card: '#5C59F8', icon: 'sparkles-outline',     iconColor: '#fff' },
  'onboarding-2': { bg: '#FFF0F6', card: '#FF6B6B', icon: 'compass-outline',       iconColor: '#fff' },
  'onboarding-3': { bg: '#ECFDF5', card: '#10B981', icon: 'rocket-outline',         iconColor: '#fff' },
}

interface Props {
  slide: SlideType
}

export const OnboardingSlide: React.FC<Props> = ({ slide }) => {
  const theme = SLIDE_CONFIG[slide.illustration] ?? SLIDE_CONFIG['onboarding-1']

  return (
    <View style={[styles.container, { backgroundColor: theme.bg }]}>

      {/* Illustration */}
      <View style={styles.illustrationArea}>
        <View style={[styles.circle, { backgroundColor: theme.card + '18' }]} />
        <View style={[styles.circleSmall, { backgroundColor: theme.card + '28' }]} />

        <View style={[styles.iconCard, { backgroundColor: theme.card }]}>
          <Ionicons name={theme.icon} size={60} color={theme.iconColor} />
        </View>

        <View style={styles.badge}>
          <Ionicons name="checkmark-circle" size={14} color={colors.primary} />
          <Text style={styles.badgeText}>Gratis selamanya</Text>
        </View>
      </View>

      {/* Text */}
      <View style={styles.textArea}>
        <Text style={styles.title}>{slide.title}</Text>
        <Text style={styles.description}>{slide.description}</Text>
      </View>

    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    width,
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
  },

  illustrationArea: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
  },
  circle: {
    position: 'absolute',
    width: 260,
    height: 260,
    borderRadius: 130,
  },
  circleSmall: {
    position: 'absolute',
    width: 180,
    height: 180,
    borderRadius: 90,
  },
  iconCard: {
    width: 130,
    height: 130,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.15,
    shadowRadius: 24,
    elevation: 10,
  },
  badge: {
    position: 'absolute',
    bottom: 30,
    right: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingHorizontal: 14,
    paddingVertical: 8,
    backgroundColor: colors.surface,
    borderRadius: 100,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 12,
    elevation: 4,
  },
  badgeText: {
    fontSize: 12,
    fontFamily: fonts.semiBold,
    color: colors.text,
  },

  textArea: {
    paddingBottom: 24,
    gap: 12,
    alignItems: 'center',
    width: '100%',
  },
  title: {
    fontSize: 30,
    fontFamily: fonts.extraBold,
    color: colors.text,
    textAlign: 'center',
    letterSpacing: -0.8,
    lineHeight: 36,
  },
  description: {
    fontSize: 16,
    fontFamily: fonts.regular,
    color: colors.textSub,
    textAlign: 'center',
    lineHeight: 26,
  },
})
