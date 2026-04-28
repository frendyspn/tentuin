// ─── User & Auth ────────────────────────────────────────────────────────────

export type UserRole = 'student' | 'university' | 'admin'

export interface Profile {
  id: string
  full_name: string | null
  school_name: string | null
  city: string | null
  birth_year: number | null
  avatar_url: string | null
  has_completed_onboarding: boolean
  created_at: string
  updated_at: string
}

// ─── RIASEC Test ────────────────────────────────────────────────────────────

export type RiasecCategory =
  | 'realistic'
  | 'investigative'
  | 'artistic'
  | 'social'
  | 'enterprising'
  | 'conventional'

export interface Question {
  id: string
  test_id: string
  order_number: number
  question_text: string
  category: RiasecCategory
}

export interface RiasecScores {
  realistic: number
  investigative: number
  artistic: number
  social: number
  enterprising: number
  conventional: number
}

export interface TestResult {
  id: string
  user_id: string
  test_id: string
  scores: RiasecScores
  riasec_code: string       // e.g. "SAI", "RIE"
  recommended_majors: RecommendedMajor[]
  completed_at: string
}

// ─── Jurusan & Universitas ───────────────────────────────────────────────────

export interface Major {
  id: string
  name: string
  category: RiasecCategory
  description: string
  career_prospects: string[]
  riasec_match: string[]    // kode RIASEC yang cocok, e.g. ["SAI", "SIA"]
}

export interface University {
  id: string
  name: string
  city: string
  province: string
  logo_url: string | null
  cover_url: string | null
  description: string | null
  accreditation: string | null
  website_url: string | null
  is_partner: boolean
  partner_tier: 'basic' | 'partner' | 'premium' | null
  majors: string[]          // array of major IDs
  created_at: string
}

export interface RecommendedMajor {
  major_id: string
  major_name: string
  match_score: number       // 0-100
  universities: string[]    // array of university IDs
}

// ─── Artikel / Konten ────────────────────────────────────────────────────────

export interface Article {
  id: string
  title: string
  slug: string
  excerpt: string
  content: string
  cover_url: string | null
  category: 'tips' | 'story' | 'news' | 'career'
  author: string
  published_at: string
}

// ─── Onboarding ─────────────────────────────────────────────────────────────

export interface OnboardingSlide {
  id: number
  title: string
  description: string
  illustration: string      // image require path atau URL
  backgroundColor: string
}
