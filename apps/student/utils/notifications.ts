import { Platform } from 'react-native'
import * as Notifications from 'expo-notifications'
import AsyncStorage from '@react-native-async-storage/async-storage'

// ─── AsyncStorage Keys ────────────────────────────────────────────────────────
export const NOTIF_REMINDER_ID_KEY   = 'tentuin_reminder_notif_id'
export const NOTIF_FIRST_TEST_KEY    = 'tentuin_first_test_done'
export const NOTIF_PERMISSION_ASKED  = 'tentuin_notif_permission_asked'

// ─── Foreground Handler ───────────────────────────────────────────────────────
/**
 * Konfigurasi bagaimana notifikasi ditampilkan saat app di foreground.
 * Harus dipanggil SEBELUM app mount (di _layout useEffect awal).
 * Kita matikan shouldShowAlert karena kita pakai custom in-app toast.
 */
export function setupNotificationHandler(): void {
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldShowAlert: false,   // kita handle sendiri dengan NotificationToast
      shouldPlaySound: true,
      shouldSetBadge: true,
    }),
  })
}

// ─── Android Channels ─────────────────────────────────────────────────────────
export async function setupNotificationChannels(): Promise<void> {
  if (Platform.OS !== 'android') return

  await Notifications.setNotificationChannelAsync('default', {
    name: 'Umum',
    importance: Notifications.AndroidImportance.DEFAULT,
    vibrationPattern: [0, 250, 250, 250],
    lightColor: '#6C63FF',
  })

  await Notifications.setNotificationChannelAsync('reminders', {
    name: 'Pengingat Test',
    description: 'Notifikasi pengingat untuk menyelesaikan test RIASEC',
    importance: Notifications.AndroidImportance.HIGH,
    vibrationPattern: [0, 250, 250, 250],
    lightColor: '#6C63FF',
    sound: 'default',
  })
}

// ─── Permission & Token ───────────────────────────────────────────────────────
/**
 * Minta izin notifikasi dan kembalikan Expo push token.
 * Return null jika izin ditolak atau error.
 */
export async function registerForPushNotifications(): Promise<string | null> {
  try {
    await setupNotificationChannels()

    const { status: existingStatus } = await Notifications.getPermissionsAsync()
    let finalStatus = existingStatus

    if (existingStatus !== 'granted') {
      await AsyncStorage.setItem(NOTIF_PERMISSION_ASKED, 'true')
      const { status } = await Notifications.requestPermissionsAsync()
      finalStatus = status
    }

    if (finalStatus !== 'granted') {
      console.log('[Notifications] Permission denied')
      return null
    }

    const tokenData = await Notifications.getExpoPushTokenAsync({
      projectId: '5650f07b-f22c-48d9-a5ec-52b9ad61e5f5',
    })

    console.log('[Notifications] Push token:', tokenData.data)
    return tokenData.data
  } catch (err) {
    // Bisa terjadi di emulator atau saat tidak ada koneksi
    console.warn('[Notifications] registerForPushNotifications failed:', err)
    return null
  }
}

/**
 * Cek status permission notifikasi saat ini.
 */
export async function getNotificationPermission(): Promise<Notifications.PermissionStatus> {
  const { status } = await Notifications.getPermissionsAsync()
  return status
}

// ─── Local Reminder ───────────────────────────────────────────────────────────
/**
 * Jadwalkan notifikasi pengingat 3 hari dari sekarang.
 * Dipanggil setelah user pertama kali grant permission.
 * Tidak akan dijadwalkan ulang jika user sudah menyelesaikan test.
 */
export async function scheduleTestReminder(): Promise<void> {
  try {
    // Tidak perlu reminder jika test sudah pernah dilakukan
    const testDone = await AsyncStorage.getItem(NOTIF_FIRST_TEST_KEY)
    if (testDone) return

    // Batalkan yang lama dulu (jika ada)
    await cancelTestReminder()

    const THREE_DAYS_SECONDS = 3 * 24 * 60 * 60

    const id = await Notifications.scheduleNotificationAsync({
      content: {
        title: '🎯 Sudah siap pilih jurusan?',
        body: 'Yuk selesaikan test RIASEC dan temukan jurusan kuliah yang paling cocok untukmu!',
        data: { screen: 'test' },
        sound: true,
        categoryIdentifier: 'reminder',
      },
      trigger: {
        seconds: THREE_DAYS_SECONDS,
        repeats: false,
        channelId: 'reminders',
      } as Notifications.TimeIntervalTriggerInput,
    })

    await AsyncStorage.setItem(NOTIF_REMINDER_ID_KEY, id)
    console.log('[Notifications] Test reminder scheduled, id:', id)
  } catch (err) {
    console.warn('[Notifications] scheduleTestReminder failed:', err)
  }
}

/**
 * Batalkan notifikasi pengingat yang sudah dijadwalkan.
 * Dipanggil setelah user berhasil menyelesaikan test pertama.
 */
export async function cancelTestReminder(): Promise<void> {
  try {
    const id = await AsyncStorage.getItem(NOTIF_REMINDER_ID_KEY)
    if (!id) return
    await Notifications.cancelScheduledNotificationAsync(id)
    await AsyncStorage.removeItem(NOTIF_REMINDER_ID_KEY)
    console.log('[Notifications] Test reminder cancelled')
  } catch (err) {
    console.warn('[Notifications] cancelTestReminder failed:', err)
  }
}

/**
 * Tandai bahwa user sudah selesai test pertama.
 * Ini mencegah reminder dijadwalkan ulang.
 */
export async function markFirstTestDone(): Promise<void> {
  await AsyncStorage.setItem(NOTIF_FIRST_TEST_KEY, 'true')
  await cancelTestReminder()
}

// ─── Badge ────────────────────────────────────────────────────────────────────
export async function clearBadge(): Promise<void> {
  try {
    await Notifications.setBadgeCountAsync(0)
  } catch (_) {
    // iOS only, ignore on Android
  }
}
