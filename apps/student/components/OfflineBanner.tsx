import React, { useEffect, useRef, useState } from 'react'
import { Animated, StyleSheet, Text, View } from 'react-native'
import NetInfo from '@react-native-community/netinfo'
import { Ionicons } from '@expo/vector-icons'
import { fonts } from '@tentuin/config'

export function OfflineBanner() {
  const [isOffline, setIsOffline] = useState(false)
  const [showBack, setShowBack] = useState(false)
  const translateY = useRef(new Animated.Value(-60)).current

  useEffect(() => {
    const unsub = NetInfo.addEventListener(state => {
      const offline = !(state.isConnected && state.isInternetReachable !== false)
      setIsOffline(prev => {
        if (!prev && offline) {
          // baru offline
          setShowBack(false)
          Animated.spring(translateY, { toValue: 0, useNativeDriver: true, tension: 80 }).start()
        } else if (prev && !offline) {
          // kembali online
          setShowBack(true)
          // tahan sebentar lalu slide up
          setTimeout(() => {
            Animated.timing(translateY, { toValue: -60, duration: 400, useNativeDriver: true }).start(
              () => setShowBack(false)
            )
          }, 1800)
        }
        return offline
      })
    })
    return unsub
  }, [])

  if (!isOffline && !showBack) return null

  return (
    <Animated.View style={[styles.banner, { transform: [{ translateY }] }, showBack && styles.bannerOnline]}>
      <Ionicons
        name={showBack ? 'wifi-outline' : 'cloud-offline-outline'}
        size={15}
        color="#fff"
      />
      <Text style={styles.text}>
        {showBack ? 'Koneksi kembali' : 'Tidak ada koneksi internet'}
      </Text>
    </Animated.View>
  )
}

const styles = StyleSheet.create({
  banner: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    zIndex: 999,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 7,
    backgroundColor: '#EF4444',
    paddingVertical: 10,
    paddingHorizontal: 16,
  },
  bannerOnline: {
    backgroundColor: '#10B981',
  },
  text: {
    fontSize: 13,
    fontFamily: fonts.semiBold,
    color: '#fff',
  },
})
