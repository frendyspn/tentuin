import React, { useEffect, useRef } from 'react'
import { Animated, ScrollView, StyleSheet, View } from 'react-native'
import { colors } from '@tentuin/config'

// ─── Base pulse hook ──────────────────────────────────────────────────────────

function usePulse(delay = 0) {
  const anim = useRef(new Animated.Value(1)).current
  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(anim, { toValue: 0.35, duration: 750, delay, useNativeDriver: true }),
        Animated.timing(anim, { toValue: 1,    duration: 750,        useNativeDriver: true }),
      ]),
    )
    loop.start()
    return () => loop.stop()
  }, [])
  return anim
}

// ─── Primitive ────────────────────────────────────────────────────────────────

interface BoxProps {
  width?:        number | string
  height:        number
  borderRadius?: number
  delay?:        number
  style?:        object
}

export function SkeletonBox({ width = '100%', height, borderRadius = 8, delay = 0, style }: BoxProps) {
  const opacity = usePulse(delay)
  return (
    <Animated.View
      style={[
        { width: width as any, height, borderRadius, backgroundColor: colors.border },
        { opacity },
        style,
      ]}
    />
  )
}

// ─── Home — university card (horizontal carousel) ─────────────────────────────

export function HomeUniCardSkeleton({ delay = 0 }: { delay?: number }) {
  return (
    <View style={homeStyles.card}>
      <SkeletonBox width={48} height={48} borderRadius={12} delay={delay} />
      <SkeletonBox width="55%" height={11} borderRadius={6} delay={delay + 60} />
      <SkeletonBox width="90%" height={10} borderRadius={6} delay={delay + 120} />
      <SkeletonBox width="90%" height={10} borderRadius={6} delay={delay + 130} />
      <SkeletonBox width="50%" height={18} borderRadius={6} delay={delay + 180} />
    </View>
  )
}

const homeStyles = StyleSheet.create({
  card: {
    width: 150, gap: 7,
    backgroundColor: colors.surface,
    borderRadius: 14, padding: 16,
  },
})

export function HomeUniRowSkeleton() {
  return (
    <ScrollView
      horizontal
      scrollEnabled={false}
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={{ paddingLeft: 24, paddingRight: 12, gap: 12 }}
    >
      {[0, 80, 160, 240].map((delay) => (
        <HomeUniCardSkeleton key={delay} delay={delay} />
      ))}
    </ScrollView>
  )
}

// ─── Explore — university list card ───────────────────────────────────────────

export function ExploreUniCardSkeleton({ delay = 0 }: { delay?: number }) {
  return (
    <View style={exploreStyles.uniCard}>
      {/* Logo */}
      <SkeletonBox width={52} height={52} borderRadius={12} delay={delay} style={{ flexShrink: 0 }} />
      {/* Body */}
      <View style={{ flex: 1, gap: 7 }}>
        <SkeletonBox height={14} width="70%" borderRadius={7} delay={delay + 60} />
        <SkeletonBox height={11} width="45%" borderRadius={6} delay={delay + 100} />
        <SkeletonBox height={18} width="30%" borderRadius={9} delay={delay + 140} />
      </View>
    </View>
  )
}

const exploreStyles = StyleSheet.create({
  uniCard: {
    flexDirection: 'row', alignItems: 'center', gap: 14,
    backgroundColor: colors.surface, borderRadius: 14, padding: 16,
  },
  majorCard: {
    backgroundColor: colors.surface, borderRadius: 14,
    padding: 14, gap: 8, alignItems: 'center',
  },
})

export function ExploreUniListSkeleton() {
  return (
    <View style={{ gap: 10, paddingHorizontal: 16, paddingTop: 8 }}>
      {[0, 80, 160, 200, 240].map((delay) => (
        <ExploreUniCardSkeleton key={delay} delay={delay} />
      ))}
    </View>
  )
}

// ─── Explore — major grid card (2 column) ─────────────────────────────────────

export function ExploreMajorCardSkeleton({ delay = 0 }: { delay?: number }) {
  return (
    <View style={[exploreStyles.majorCard, { width: '48%' }]}>
      <SkeletonBox width={44} height={44} borderRadius={12} delay={delay} />
      <SkeletonBox width="80%" height={12} borderRadius={6} delay={delay + 60} />
      <SkeletonBox width="55%" height={10} borderRadius={6} delay={delay + 100} />
    </View>
  )
}

export function ExploreMajorGridSkeleton() {
  return (
    <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 10, paddingHorizontal: 16, paddingTop: 8, justifyContent: 'space-between' }}>
      {[0, 60, 120, 180, 240, 300].map((delay) => (
        <ExploreMajorCardSkeleton key={delay} delay={delay} />
      ))}
    </View>
  )
}
