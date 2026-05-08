import React, { useCallback, useEffect, useRef } from 'react'
import { Animated, Pressable, StyleSheet, Text, View } from 'react-native'
import { useSafeAreaInsets } from 'react-native-safe-area-context'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'

// ─── Types ────────────────────────────────────────────────────────────────────

export interface ToastNotification {
  id: string
  title: string
  body: string
  onPress?: () => void
}

interface NotificationToastProps {
  notification: ToastNotification
  onDismiss: (id: string) => void
}

// ─── Single Toast Item ────────────────────────────────────────────────────────

export function NotificationToast({ notification, onDismiss }: NotificationToastProps) {
  const insets      = useSafeAreaInsets()
  const translateY  = useRef(new Animated.Value(-120)).current
  const opacity     = useRef(new Animated.Value(0)).current
  const dismissed   = useRef(false)

  const dismiss = useCallback(() => {
    if (dismissed.current) return
    dismissed.current = true
    Animated.parallel([
      Animated.timing(translateY, { toValue: -120, duration: 250, useNativeDriver: true }),
      Animated.timing(opacity,    { toValue: 0,    duration: 200, useNativeDriver: true }),
    ]).start(() => onDismiss(notification.id))
  }, [notification.id, onDismiss, translateY, opacity])

  useEffect(() => {
    // Slide in
    Animated.parallel([
      Animated.spring(translateY, {
        toValue: 0,
        useNativeDriver: true,
        tension: 75,
        friction: 10,
      }),
      Animated.timing(opacity, {
        toValue: 1,
        duration: 200,
        useNativeDriver: true,
      }),
    ]).start()

    // Auto-dismiss setelah 4 detik
    const timer = setTimeout(dismiss, 4000)
    return () => clearTimeout(timer)
  }, [dismiss, translateY, opacity])

  const handlePress = () => {
    notification.onPress?.()
    dismiss()
  }

  return (
    <Animated.View
      style={[
        styles.wrapper,
        { top: insets.top + 10, opacity, transform: [{ translateY }] },
      ]}
    >
      <Pressable
        style={({ pressed }) => [styles.toast, pressed && styles.toastPressed]}
        onPress={handlePress}
        android_ripple={{ color: 'rgba(108,99,255,0.08)' }}
      >
        {/* Icon */}
        <View style={styles.iconWrap}>
          <Ionicons name="notifications" size={18} color={colors.primary} />
        </View>

        {/* Text */}
        <View style={styles.textWrap}>
          <Text style={styles.title} numberOfLines={1}>{notification.title}</Text>
          <Text style={styles.body}  numberOfLines={2}>{notification.body}</Text>
        </View>

        {/* Close */}
        <Pressable onPress={dismiss} hitSlop={10} style={styles.closeBtn}>
          <Ionicons name="close" size={15} color={colors.textMuted} />
        </Pressable>
      </Pressable>
    </Animated.View>
  )
}

// ─── Styles ───────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  wrapper: {
    position:  'absolute',
    left:      16,
    right:     16,
    zIndex:    9998,
    // Shadow
    shadowColor:   '#000',
    shadowOffset:  { width: 0, height: 4 },
    shadowOpacity: 0.12,
    shadowRadius:  16,
    elevation:     8,
  },
  toast: {
    flexDirection:    'row',
    alignItems:       'center',
    backgroundColor:  colors.surface,
    borderRadius:     16,
    paddingHorizontal: 14,
    paddingVertical:   12,
    gap:              12,
    borderWidth:      1,
    borderColor:      colors.border,
  },
  toastPressed: {
    backgroundColor: colors.gray[50],
  },
  iconWrap: {
    width:           38,
    height:          38,
    borderRadius:    10,
    backgroundColor: colors.primaryMuted,
    alignItems:      'center',
    justifyContent:  'center',
    flexShrink:      0,
  },
  textWrap: {
    flex: 1,
    gap:  2,
  },
  title: {
    fontSize:    14,
    fontFamily:  fonts.semiBold,
    color:       colors.text,
    letterSpacing: -0.2,
  },
  body: {
    fontSize:   12,
    fontFamily: fonts.regular,
    color:      colors.textSub,
    lineHeight: 17,
  },
  closeBtn: {
    padding:   4,
    flexShrink: 0,
  },
})
