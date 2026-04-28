import * as Haptics from 'expo-haptics'

/** Tap ringan — navigasi, chip filter, tab switch */
export const hapticLight = () =>
  Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light)

/** Tap sedang — tombol utama, submit form */
export const hapticMedium = () =>
  Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium)

/** Sukses — bookmark tersimpan, profil disimpan, test selesai */
export const hapticSuccess = () =>
  Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success)

/** Warning — hapus bookmark, sign out */
export const hapticWarning = () =>
  Haptics.notificationAsync(Haptics.NotificationFeedbackType.Warning)

/** Selection — jawab soal test, ganti pilihan */
export const hapticSelection = () =>
  Haptics.selectionAsync()
