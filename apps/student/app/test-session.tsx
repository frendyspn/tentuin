import React, { useCallback, useEffect, useRef, useState } from 'react'
import {
  ActivityIndicator,
  Alert,
  Animated,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { getQuestions, type RiasecScores } from '@tentuin/supabase'
import { useTestStore } from '../stores/testStore'
import { hapticSelection, hapticSuccess } from '../utils/haptics'

const CATEGORY_COLORS: Record<string, string> = {
  realistic:     colors.riasec.realistic,
  investigative: colors.riasec.investigative,
  artistic:      colors.riasec.artistic,
  social:        colors.riasec.social,
  enterprising:  colors.riasec.enterprising,
  conventional:  colors.riasec.conventional,
}

const ANSWER_OPTIONS = [
  { value: 1, short: '1' },
  { value: 2, short: '2' },
  { value: 3, short: '3' },
  { value: 4, short: '4' },
  { value: 5, short: '5' },
]

function computeRiasecCode(scores: RiasecScores): string {
  const codeMap: Record<string, string> = {
    realistic: 'R', investigative: 'I', artistic: 'A',
    social: 'S', enterprising: 'E', conventional: 'C',
  }
  return (Object.entries(scores) as [keyof RiasecScores, number][])
    .sort((a, b) => b[1] - a[1])
    .slice(0, 3)
    .map(([k]) => codeMap[k])
    .join('')
}

export default function TestSessionScreen() {
  const router = useRouter()

  const {
    questions, answers, currentIndex,
    setQuestions, setAnswer, setCurrentIndex, startTest, resetTest,
  } = useTestStore()

  const [loading, setLoading] = useState(questions.length === 0)

  const progressAnim = useRef(new Animated.Value(
    questions.length > 0 ? (currentIndex + 1) / questions.length : 0
  )).current
  const slideAnim = useRef(new Animated.Value(0)).current

  // Load questions only if not already in store
  useEffect(() => {
    if (questions.length > 0) {
      setLoading(false)
      return
    }
    getQuestions()
      .then((data) => {
        setQuestions(data)
        startTest()
      })
      .catch((err) => {
        Alert.alert('Gagal Memuat', `${err?.message ?? 'Coba lagi.'}`, [
          { text: 'Kembali', onPress: () => router.back() },
        ])
      })
      .finally(() => setLoading(false))
  }, [])

  const total = questions.length

  // Sync progress bar when currentIndex changes
  useEffect(() => {
    if (total === 0) return
    Animated.timing(progressAnim, {
      toValue: (currentIndex + 1) / total,
      duration: 300,
      useNativeDriver: false,
    }).start()
  }, [currentIndex, total])

  const animateSlide = useCallback(
    (direction: 'next' | 'prev', cb: () => void) => {
      const toValue = direction === 'next' ? -20 : 20
      slideAnim.setValue(toValue)
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 120,
        useNativeDriver: true,
      }).start()
      cb()
    },
    [slideAnim],
  )

  const handleAnswer = useCallback(
    (value: number) => {
      const question = questions[currentIndex]
      if (!question) return

      hapticSelection()
      setAnswer(question.id, value)

      if (currentIndex + 1 < total) {
        animateSlide('next', () => setCurrentIndex(currentIndex + 1))
      } else {
        hapticSuccess()
        // Compute scores from store answers (include this answer)
        const finalAnswers = { ...answers, [question.id]: value }
        const raw: RiasecScores = {
          realistic: 0, investigative: 0, artistic: 0,
          social: 0, enterprising: 0, conventional: 0,
        }
        questions.forEach((q) => {
          raw[q.category] += finalAnswers[q.id] ?? 0
        })
        const maxPerCategory = 10 * 5
        const normalized: RiasecScores = {
          realistic:     Math.round((raw.realistic     / maxPerCategory) * 100),
          investigative: Math.round((raw.investigative / maxPerCategory) * 100),
          artistic:      Math.round((raw.artistic      / maxPerCategory) * 100),
          social:        Math.round((raw.social        / maxPerCategory) * 100),
          enterprising:  Math.round((raw.enterprising  / maxPerCategory) * 100),
          conventional:  Math.round((raw.conventional  / maxPerCategory) * 100),
        }
        resetTest()
        router.replace({
          pathname: '/test-result',
          params: { scores: JSON.stringify(normalized), riasecCode: computeRiasecCode(normalized) },
        })
      }
    },
    [questions, currentIndex, total, answers, animateSlide, setAnswer, setCurrentIndex, resetTest, router],
  )

  const handleBack = () => {
    if (currentIndex === 0) {
      Alert.alert('Keluar Test?', 'Progress kamu akan tersimpan — bisa dilanjutkan nanti.', [
        { text: 'Batal' },
        { text: 'Keluar', style: 'destructive', onPress: () => router.back() },
      ])
    } else {
      animateSlide('prev', () => setCurrentIndex(currentIndex - 1))
    }
  }

  // ── Loading ───────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <View style={styles.loadingWrap}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.loadingText}>Memuat soal...</Text>
      </View>
    )
  }

  if (total === 0) {
    return (
      <View style={styles.loadingWrap}>
        <Text style={styles.loadingText}>Soal tidak tersedia.</Text>
        <Pressable onPress={() => router.back()} style={styles.retryBtn}>
          <Text style={styles.retryText}>Kembali</Text>
        </Pressable>
      </View>
    )
  }

  const question    = questions[currentIndex]
  const accentColor = CATEGORY_COLORS[question.category] ?? colors.primary
  const selectedVal = answers[question.id]

  // How many have been answered so far (for resume indicator)
  const answeredCount = Object.keys(answers).length

  return (
    <SafeAreaView style={styles.safe} edges={['top', 'bottom']}>

      {/* ── Header ── */}
      <View style={styles.header}>
        <Pressable onPress={handleBack} hitSlop={12} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={22} color={colors.text} />
        </Pressable>
        <View style={styles.counterWrap}>
          <Text style={styles.counter}>{currentIndex + 1}</Text>
          <Text style={styles.counterTotal}> / {total}</Text>
        </View>
        {/* Resume badge — shown if user came back mid-test */}
        {answeredCount > 0 && answeredCount < total && (
          <View style={styles.resumeBadge}>
            <Ionicons name="bookmark" size={11} color={colors.primary} />
            <Text style={styles.resumeText}>Lanjutan</Text>
          </View>
        )}
        {answeredCount === 0 && <View style={{ width: 72 }} />}
      </View>

      {/* ── Progress bar ── */}
      <View style={styles.progressTrack}>
        <Animated.View
          style={[
            styles.progressFill,
            {
              backgroundColor: accentColor,
              width: progressAnim.interpolate({
                inputRange: [0, 1],
                outputRange: ['0%', '100%'],
              }),
            },
          ]}
        />
      </View>

      {/* ── Category chip ── */}
      <View style={styles.chipRow}>
        <View style={[styles.chip, { backgroundColor: accentColor + '22' }]}>
          <View style={[styles.chipDot, { backgroundColor: accentColor }]} />
          <Text style={[styles.chipText, { color: accentColor }]}>
            {question.category.charAt(0).toUpperCase() + question.category.slice(1)}
          </Text>
        </View>
      </View>

      {/* ── Question text ── */}
      <Animated.View
        style={[styles.questionWrap, { transform: [{ translateX: slideAnim }] }]}
      >
        <Text style={styles.questionText}>{question.text}</Text>
        <Text style={styles.scaleHint}>Seberapa sesuai pernyataan ini dengan dirimu?</Text>
      </Animated.View>

      {/* ── Answer buttons ── */}
      <View style={styles.answersWrap}>
        <View style={styles.scaleLabels}>
          <Text style={styles.scaleLabel}>Tidak{'\n'}Sesuai</Text>
          <Text style={[styles.scaleLabel, { textAlign: 'right' }]}>Sangat{'\n'}Sesuai</Text>
        </View>

        <View style={styles.optionsRow}>
          {ANSWER_OPTIONS.map((opt) => {
            const isSelected = selectedVal === opt.value
            const size = 44 + (opt.value - 1) * 6   // 44 → 68
            return (
              <Pressable
                key={opt.value}
                onPress={() => handleAnswer(opt.value)}
                style={({ pressed }) => [
                  styles.optionBtn,
                  {
                    width:  size,
                    height: size,
                    borderRadius: size / 2,
                    backgroundColor: isSelected ? accentColor : colors.surface,
                    borderColor:     isSelected ? accentColor : colors.borderMedium,
                    opacity: pressed ? 0.75 : 1,
                    transform: pressed ? [{ scale: 0.92 }] : [],
                  },
                ]}
              >
                <Text style={[styles.optionNum, { color: isSelected ? colors.white : colors.textSub }]}>
                  {opt.short}
                </Text>
              </Pressable>
            )
          })}
        </View>
      </View>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },

  loadingWrap: {
    flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12, backgroundColor: colors.background,
  },
  loadingText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub },
  retryBtn: {
    marginTop: 8, paddingHorizontal: 24, paddingVertical: 10,
    backgroundColor: colors.primaryLight, borderRadius: 100,
  },
  retryText: { fontSize: 14, fontFamily: fonts.semiBold, color: colors.primary },

  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 4,
    paddingBottom: 6,
  },
  backBtn: {
    width: 36, height: 36, borderRadius: 10,
    backgroundColor: colors.surface,
    alignItems: 'center', justifyContent: 'center',
  },
  counterWrap: { flexDirection: 'row', alignItems: 'baseline' },
  counter:      { fontSize: 20, fontFamily: fonts.extraBold, color: colors.text },
  counterTotal: { fontSize: 14, fontFamily: fonts.regular, color: colors.textMuted },

  resumeBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    backgroundColor: colors.primaryMuted,
    paddingHorizontal: 10, paddingVertical: 4, borderRadius: 100,
  },
  resumeText: { fontSize: 11, fontFamily: fonts.semiBold, color: colors.primary },

  progressTrack: {
    height: 5, backgroundColor: colors.border,
    marginHorizontal: 20, marginVertical: 3, borderRadius: 100, overflow: 'hidden',
  },
  progressFill: { height: '100%', borderRadius: 100 },

  chipRow: { paddingHorizontal: 20, marginTop: 8, marginBottom: 2 },
  chip: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    paddingHorizontal: 12, paddingVertical: 5, borderRadius: 100, alignSelf: 'flex-start',
  },
  chipDot:  { width: 7, height: 7, borderRadius: 4 },
  chipText: { fontSize: 12, fontFamily: fonts.semiBold, letterSpacing: 0.3 },

  questionWrap: {
    paddingHorizontal: 24, paddingTop: 4, paddingBottom: 4, gap: 4,
  },
  questionText: {
    fontSize: 19, fontFamily: fonts.bold, color: colors.text, lineHeight: 26, letterSpacing: -0.3,
  },
  scaleHint: { fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },

  answersWrap: { paddingHorizontal: 20, paddingTop: 8, paddingBottom: 12, gap: 6 },
  scaleLabels: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 2 },
  scaleLabel: { fontSize: 10, fontFamily: fonts.medium, color: colors.textMuted, lineHeight: 14 },

  optionsRow: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6,
  },
  optionBtn: {
    alignItems: 'center', justifyContent: 'center',
    borderWidth: 1.5,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06, shadowRadius: 6, elevation: 2,
  },
  optionNum: { fontSize: 14, fontFamily: fonts.bold },
})
