import { useState, useCallback } from 'react'
import { useAuthStore } from '../stores/authStore'

/**
 * Hook untuk memproteksi aksi yang butuh auth.
 *
 * Usage:
 * const { requireAuth, showPrompt, closePrompt } = useRequireAuth()
 *
 * <Button onPress={() => requireAuth(() => router.push('/test'))} />
 * <AuthPromptSheet visible={showPrompt} onClose={closePrompt} />
 */
export const useRequireAuth = () => {
  const isGuest = useAuthStore((s) => s.isGuest())
  const [showPrompt, setShowPrompt] = useState(false)

  const requireAuth = useCallback(
    (callback: () => void) => {
      if (isGuest) {
        setShowPrompt(true)
        return
      }
      callback()
    },
    [isGuest]
  )

  const closePrompt = useCallback(() => {
    setShowPrompt(false)
  }, [])

  return {
    requireAuth,
    showPrompt,
    closePrompt,
    isGuest,
  }
}
