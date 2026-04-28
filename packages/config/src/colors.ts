export const colors = {
  // ─── Brand ────────────────────────────────────────────────
  primary: '#5C59F8',
  primaryDark: '#3B38D4',
  primaryLight: '#EEEEFF',
  primaryMuted: '#F4F4FF',

  secondary: '#FF6B6B',
  secondaryLight: '#FFF0F0',

  // ─── Background & Surface ─────────────────────────────────
  background: '#F7F8FA',
  surface: '#FFFFFF',
  surfaceRaised: '#FFFFFF',

  // ─── Text ─────────────────────────────────────────────────
  text: '#111827',
  textSub: '#6B7280',
  textMuted: '#9CA3AF',
  textDisabled: '#D1D5DB',
  white: '#FFFFFF',

  // ─── Border ───────────────────────────────────────────────
  border: '#F0F0F5',
  borderMedium: '#E5E7EB',

  // ─── Semantic ─────────────────────────────────────────────
  success: '#10B981',
  successLight: '#ECFDF5',
  error: '#EF4444',
  errorLight: '#FEF2F2',
  warning: '#F59E0B',
  warningLight: '#FFFBEB',

  // ─── Gray Scale ───────────────────────────────────────────
  gray: {
    50:  '#F9FAFB',
    100: '#F3F4F6',
    200: '#E5E7EB',
    300: '#D1D5DB',
    400: '#9CA3AF',
    500: '#6B7280',
    600: '#4B5563',
    700: '#374151',
    800: '#1F2937',
    900: '#111827',
  },

  // ─── RIASEC ───────────────────────────────────────────────
  riasec: {
    realistic:    '#F97316',
    investigative:'#3B82F6',
    artistic:     '#EC4899',
    social:       '#10B981',
    enterprising: '#F59E0B',
    conventional: '#5C59F8',
  },

  // ─── Onboarding ───────────────────────────────────────────
  onboarding: {
    slide1: '#EEF2FF',
    slide2: '#FFF0F6',
    slide3: '#ECFDF5',
  },

  transparent: 'transparent',
} as const

export type Colors = typeof colors
