import type { OnboardingSlide } from '@tentuin/types'
import { colors } from './colors'

export const onboardingSlides: OnboardingSlide[] = [
  {
    id: 1,
    title: 'Kenali Dirimu',
    description:
      'Setiap orang punya kepribadian unik. Temukan siapa dirimu lewat test psikologi yang sudah terbukti secara ilmiah.',
    illustration: 'onboarding-1',
    backgroundColor: colors.onboarding.slide1,
  },
  {
    id: 2,
    title: 'Temukan Jurusanmu',
    description:
      'Dapatkan rekomendasi jurusan kuliah yang paling cocok dengan kepribadian dan minatmu. Bukan sekadar tebak-tebakan.',
    illustration: 'onboarding-2',
    backgroundColor: colors.onboarding.slide2,
  },
  {
    id: 3,
    title: 'Mulai Perjalananmu',
    description:
      'Jelajahi universitas dan jurusan kapan saja, gratis tanpa perlu daftar. Siap mulai test? Daftar hanya butuh 1 menit.',
    illustration: 'onboarding-3',
    backgroundColor: colors.onboarding.slide3,
  },
]
