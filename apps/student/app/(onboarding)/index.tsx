import React, { useRef, useState } from 'react'
import {
  Dimensions,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
  type ViewToken,
} from 'react-native'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { onboardingSlides, colors, fonts } from '@tentuin/config'
import { OnboardingSlide } from '../../components/onboarding/OnboardingSlide'
import type { OnboardingSlide as SlideType } from '@tentuin/types'

const { width } = Dimensions.get('window')
const ONBOARDING_KEY = 'tentuin_onboarding_done'

export default function OnboardingScreen() {
  const router = useRouter()
  const flatListRef = useRef<FlatList>(null)
  const [activeIndex, setActiveIndex] = useState(0)

  const isLast = activeIndex === onboardingSlides.length - 1

  const onViewableItemsChanged = useRef(
    ({ viewableItems }: { viewableItems: ViewToken[] }) => {
      const idx = viewableItems[0]?.index
      if (idx != null) setActiveIndex(idx)
    }
  ).current

  const goNext = () => {
    if (isLast) finish()
    else flatListRef.current?.scrollToIndex({ index: activeIndex + 1, animated: true })
  }

  const finish = async () => {
    await AsyncStorage.setItem(ONBOARDING_KEY, 'true')
    router.replace('/(tabs)/home')
  }

  return (
    <View style={styles.root}>

      {/* Skip */}
      {!isLast && (
        <Pressable style={styles.skipBtn} onPress={finish}>
          <Text style={styles.skipText}>Lewati</Text>
        </Pressable>
      )}

      {/* Slides */}
      <FlatList<SlideType>
        ref={flatListRef}
        data={onboardingSlides}
        renderItem={({ item }) => <OnboardingSlide slide={item} />}
        keyExtractor={(item) => String(item.id)}
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        onViewableItemsChanged={onViewableItemsChanged}
        viewabilityConfig={{ viewAreaCoveragePercentThreshold: 50 }}
      />

      {/* Footer */}
      <View style={styles.footer}>

        {/* Dots */}
        <View style={styles.dots}>
          {onboardingSlides.map((_, i) => (
            <View
              key={i}
              style={[styles.dot, i === activeIndex ? styles.dotActive : styles.dotInactive]}
            />
          ))}
        </View>

        {/* CTA Button */}
        <Pressable
          style={({ pressed }) => [styles.ctaBtn, pressed && { opacity: 0.88, transform: [{ scale: 0.97 }] }]}
          onPress={goNext}
        >
          <Text style={styles.ctaText}>{isLast ? 'Mulai Sekarang' : 'Lanjut'}</Text>
          <Ionicons
            name={isLast ? 'rocket-outline' : 'arrow-forward'}
            size={18}
            color={colors.white}
          />
        </Pressable>

      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.background,
  },

  skipBtn: {
    position: 'absolute',
    top: 56,
    right: 24,
    zIndex: 10,
    paddingHorizontal: 16,
    paddingVertical: 8,
    backgroundColor: colors.surface,
    borderRadius: 100,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 2,
  },
  skipText: {
    fontSize: 13,
    fontFamily: fonts.semiBold,
    color: colors.textSub,
  },

  footer: {
    backgroundColor: colors.surface,
    paddingHorizontal: 28,
    paddingTop: 28,
    paddingBottom: 44,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    gap: 24,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.06,
    shadowRadius: 16,
    elevation: 10,
  },

  dots: {
    flexDirection: 'row',
    gap: 6,
    alignItems: 'center',
  },
  dot: {
    height: 6,
    borderRadius: 3,
  },
  dotActive: {
    width: 28,
    backgroundColor: colors.primary,
  },
  dotInactive: {
    width: 6,
    backgroundColor: colors.gray[200],
  },

  ctaBtn: {
    width: width - 56,
    backgroundColor: colors.primary,
    borderRadius: 14,
    paddingVertical: 18,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    shadowColor: colors.primary,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 16,
    elevation: 6,
  },
  ctaText: {
    fontSize: 16,
    fontFamily: fonts.bold,
    color: colors.white,
    letterSpacing: 0.2,
  },
})
