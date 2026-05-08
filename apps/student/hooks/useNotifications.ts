import { useCallback, useEffect, useRef, useState } from 'react'
import { useRouter } from 'expo-router'
import * as Notifications from 'expo-notifications'
import { useAuthStore } from '../stores/authStore'
import {
  registerForPushNotifications,
  scheduleTestReminder,
  clearBadge,
  NOTIF_PERMISSION_ASKED,
} from '../utils/notifications'
import { savePushToken } from '@tentuin/supabase'
import type { ToastNotification } from '../components/NotificationToast'
import AsyncStorage from '@react-native-async-storage/async-storage'

// ─── useNotifications ─────────────────────────────────────────────────────────
/**
 * Hook utama untuk inisialisasi notifikasi.
 * Dipanggil sekali di RootLayout.
 *
 * Returns:
 *   toasts       — daftar toast yang sedang tampil
 *   dismissToast — hapus toast berdasarkan id
 */
export function useNotifications() {
  const router  = useRouter()
  const session = useAuthStore(s => s.session)

  const [toasts, setToasts] = useState<ToastNotification[]>([])

  const foregroundSub = useRef<Notifications.Subscription | null>(null)
  const responseSub   = useRef<Notifications.Subscription | null>(null)

  // ── Helper: buang toast berdasarkan id ──────────────────────────────────────
  const dismissToast = useCallback((id: string) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  // ── Foreground notification listener ────────────────────────────────────────
  // Tampilkan in-app toast saat notifikasi datang ketika app terbuka
  useEffect(() => {
    foregroundSub.current = Notifications.addNotificationReceivedListener(notification => {
      const { title, body, data } = notification.request.content
      if (!title) return

      const id = notification.request.identifier

      // Navigasi saat toast di-tap
      const onPress = () => {
        const screen = (data as Record<string, string>)?.screen
        if (screen === 'test')    router.push('/(tabs)/test')
        else if (screen === 'home') router.push('/(tabs)/home')
        else if (screen === 'profile') router.push('/(tabs)/profile')
      }

      setToasts(prev => [
        // Maks 3 toast sekaligus, hapus yang paling lama jika penuh
        ...(prev.length >= 3 ? prev.slice(1) : prev),
        { id, title, body: body ?? '', onPress },
      ])
    })

    return () => {
      foregroundSub.current?.remove()
    }
  }, [router])

  // ── Notification tap (background/killed state) ───────────────────────────────
  // Saat user tap notifikasi di notification tray → deep link ke screen
  useEffect(() => {
    responseSub.current = Notifications.addNotificationResponseReceivedListener(response => {
      const data   = response.notification.request.content.data as Record<string, string>
      const screen = data?.screen

      clearBadge()

      if (screen === 'test')       router.push('/(tabs)/test')
      else if (screen === 'home')  router.push('/(tabs)/home')
      else if (screen === 'profile') router.push('/(tabs)/profile')
    })

    // Handle notifikasi yang membuka app dari killed state
    Notifications.getLastNotificationResponseAsync().then(response => {
      if (!response) return
      const data   = response.notification.request.content.data as Record<string, string>
      const screen = data?.screen
      clearBadge()
      if (screen === 'test')      router.push('/(tabs)/test')
      else if (screen === 'home') router.push('/(tabs)/home')
    })

    return () => {
      responseSub.current?.remove()
    }
  }, [router])

  // ── Register push token saat user login ────────────────────────────────────
  useEffect(() => {
    if (!session?.user?.id || !session?.access_token) return

    const uid   = session.user.id
    const token = session.access_token

    const registerToken = async () => {
      try {
        const pushToken = await registerForPushNotifications()
        if (pushToken) {
          await savePushToken(uid, pushToken, token)
          // Jadwalkan reminder setelah dapat permission (jika belum pernah test)
          await scheduleTestReminder()
        }
      } catch (err) {
        console.warn('[useNotifications] Token registration failed:', err)
      }
    }

    registerToken()
  }, [session?.user?.id])

  // ── Schedule reminder untuk guest user (tanpa login) ───────────────────────
  // Muncul 3 hari setelah install pertama kali, tapi hanya jika permission sudah di-ask
  useEffect(() => {
    const scheduleGuestReminder = async () => {
      // Hanya jadwalkan jika user sudah pernah di-ask permission (supaya tidak langsung minta)
      const alreadyAsked = await AsyncStorage.getItem(NOTIF_PERMISSION_ASKED)
      if (!alreadyAsked) return

      await scheduleTestReminder()
    }

    // Jalankan hanya saat session null (guest)
    if (!session?.user?.id) {
      scheduleGuestReminder()
    }
  }, [session?.user?.id])

  return { toasts, dismissToast }
}
