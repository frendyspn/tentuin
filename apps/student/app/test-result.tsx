import React, { useEffect, useRef, useState } from 'react'
import {
  ActivityIndicator,
  Alert,
  Animated,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native'

import { SafeAreaView } from 'react-native-safe-area-context'
import AsyncStorage from '@react-native-async-storage/async-storage'
import { captureRef } from 'react-native-view-shot'
import * as Sharing from 'expo-sharing'
import { useLocalSearchParams, useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import { saveTestResult, getUniversitiesByRiasec, saveUniversityBookmark, deleteUniversityBookmark, type RiasecScores, type UniversityRow } from '@tentuin/supabase'
import { useAuthStore } from '../stores/authStore'
import { hapticLight, hapticMedium } from '../utils/haptics'
import { markFirstTestDone } from '../utils/notifications'

// ─── Static content ───────────────────────────────────────────────────────────

const CATEGORY_META: Record<
  string,
  { code: string; label: string; icon: keyof typeof import('@expo/vector-icons').Ionicons.glyphMap; color: string; bg: string; desc: string }
> = {
  realistic: {
    code: 'R', label: 'Realistic', icon: 'hammer-outline', color: colors.riasec.realistic, bg: '#FFF3E0',
    desc: 'Kamu suka bekerja dengan tangan, alat, mesin, atau di alam terbuka. Kamu praktis dan lebih suka hal nyata daripada abstrak.',
  },
  investigative: {
    code: 'I', label: 'Investigative', icon: 'search-outline', color: colors.riasec.investigative, bg: '#E3F2FD',
    desc: 'Kamu senang menganalisis, berpikir, meneliti, dan memecahkan masalah kompleks. Kamu penasaran dan suka ilmu pengetahuan.',
  },
  artistic: {
    code: 'A', label: 'Artistic', icon: 'color-palette-outline', color: colors.riasec.artistic, bg: '#FCE4EC',
    desc: 'Kamu kreatif, imajinatif, dan ekspresif. Kamu menyukai kebebasan berkarya dan tidak suka aturan yang terlalu kaku.',
  },
  social: {
    code: 'S', label: 'Social', icon: 'people-outline', color: colors.riasec.social, bg: '#E8F5E9',
    desc: 'Kamu suka membantu, mengajar, dan berinteraksi dengan orang lain. Kamu empatik dan peduli dengan lingkungan sekitar.',
  },
  enterprising: {
    code: 'E', label: 'Enterprising', icon: 'trending-up-outline', color: colors.riasec.enterprising, bg: '#FFFDE7',
    desc: 'Kamu suka memimpin, mempengaruhi, dan bersaing. Kamu ambisius dan tertarik dengan dunia bisnis & kepemimpinan.',
  },
  conventional: {
    code: 'C', label: 'Conventional', icon: 'document-outline', color: colors.riasec.conventional, bg: '#EDE7F6',
    desc: 'Kamu terorganisir, teliti, dan suka bekerja dengan data & prosedur. Kamu nyaman dengan struktur yang jelas.',
  },
}


const CODE_TO_KEY: Record<string, string> = {
  R: 'realistic', I: 'investigative', A: 'artistic',
  S: 'social', E: 'enterprising', C: 'conventional',
}

// 3–4 jurusan representatif per tipe dominan (untuk share card)
const RIASEC_MAJORS: Record<string, string[]> = {
  realistic:     ['Teknik Mesin', 'Teknik Sipil', 'Agroteknologi', 'Teknik Elektro'],
  investigative: ['Kedokteran', 'Teknik Informatika', 'Farmasi', 'Biologi'],
  artistic:      ['Desain Komunikasi Visual', 'Arsitektur', 'Seni Rupa', 'Film & Televisi'],
  social:        ['Psikologi', 'Ilmu Komunikasi', 'Pendidikan', 'Kesehatan Masyarakat'],
  enterprising:  ['Manajemen Bisnis', 'Hukum', 'Ilmu Politik', 'Hubungan Internasional'],
  conventional:  ['Akuntansi', 'Sistem Informasi', 'Statistika', 'Administrasi Bisnis'],
}

// ─── Score Card (compact) ─────────────────────────────────────────────────────

function ScoreCard({
  category,
  score,
  delay,
}: {
  category: string
  score: number
  delay: number
}) {
  const anim = useRef(new Animated.Value(0)).current
  const meta = CATEGORY_META[category]

  useEffect(() => {
    Animated.timing(anim, {
      toValue: score,
      duration: 700,
      delay,
      useNativeDriver: false,
    }).start()
  }, [])

  return (
    <View style={[cardStyles.card, { backgroundColor: meta.bg }]}>
      <Ionicons name={meta.icon as any} size={16} color={meta.color} />
      <Text style={[cardStyles.code, { color: meta.color }]}>{meta.code}</Text>
      <Animated.Text
        style={[
          cardStyles.score,
          {
            color: meta.color,
          },
        ]}
      >
        {anim.interpolate({
          inputRange: Array.from({length: 101}, (_, i) => i),
          outputRange: Array.from({length: 101}, (_, i) => String(i)),
        })}
      </Animated.Text>
      <Text style={[cardStyles.unit, { color: meta.color }]}>%</Text>
    </View>
  )
}

const cardStyles = StyleSheet.create({
  card: {
    width: '31%',
    borderRadius: 12,
    paddingHorizontal: 6,
    paddingVertical: 6,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 4,
  },
  code: { fontSize: 8, fontFamily: fonts.bold, textTransform: 'uppercase' },
  score: { fontSize: 14, fontFamily: fonts.extraBold },
  unit: { fontSize: 8, fontFamily: fonts.semiBold },
})

// ─── Main Screen ──────────────────────────────────────────────────────────────

export default function TestResultScreen() {
  const router  = useRouter()
  const params  = useLocalSearchParams<{ scores?: string; riasecCode?: string; isHistorical?: string }>()
  const session = useAuthStore((s) => s.session)
  const [saving, setSaving] = useState(false)
  const [saved,  setSaved]  = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [universities, setUniversities] = useState<(UniversityRow & { matching_majors: string[] })[]>([])
  const [uniLoading, setUniLoading] = useState(true)
  const [selectedMajors, setSelectedMajors] = useState<string[]>([])  // Max 3
  const [bookmarkedUnis, setBookmarkedUnis] = useState<Set<string>>(new Set())

  const isHistorical = params.isHistorical === 'true'
  const shareCardRef = useRef<View>(null)
  const [generatingImage, setGeneratingImage] = useState(false)

  // ── In-app rating ─────────────────────────────────────────────────────────
  // Tampil sekali saja, 2.5 detik setelah hasil test pertama tersimpan
  useEffect(() => {
    if (isHistorical || saving || !saved) return

    const maybeRequestReview = async () => {
      try {
        const alreadyPrompted = await AsyncStorage.getItem('tentuin_rating_prompted')
        if (alreadyPrompted) return

        // Cek native module tersedia sebelum import
        // (module belum ada kalau build belum di-rebuild setelah install expo-store-review)
        const { NativeModules } = await import('react-native')
        if (!NativeModules.ExpoStoreReview) {
          console.log('[Rating] ExpoStoreReview native module not available, skipping')
          return
        }

        // Dynamic import — supaya tidak crash di Expo Go (native module belum ada)
        const StoreReview = await import('expo-store-review')
        const available = await StoreReview.isAvailableAsync()
        if (!available) return

        // Beri waktu user melihat hasil dulu
        await new Promise<void>((resolve) => setTimeout(resolve, 2500))

        await StoreReview.requestReview()
        await AsyncStorage.setItem('tentuin_rating_prompted', 'true')
      } catch (err) {
        // Silent — jangan crash hanya karena rating gagal
        console.log('[Rating] Skipped:', err)
      }
    }

    maybeRequestReview()
  }, [saved, saving, isHistorical])

  const scores: RiasecScores = params.scores
    ? JSON.parse(params.scores)
    : { realistic: 0, investigative: 0, artistic: 0, social: 0, enterprising: 0, conventional: 0 }

  const riasecCode = params.riasecCode ?? 'RIA'

  // Top category meta
  const topKey  = CODE_TO_KEY[riasecCode[0]] ?? 'realistic'
  const topMeta = CATEGORY_META[topKey]

  // Get all unique majors from universities
  const allMajors = Array.from(new Set(universities.flatMap(u => u.matching_majors))).sort()

  // Toggle major selection (max 3)
  const toggleMajorSelection = (major: string) => {
    setSelectedMajors(prev => {
      if (prev.includes(major)) {
        return prev.filter(m => m !== major)
      } else if (prev.length < 3) {
        return [...prev, major]
      }
      return prev
    })
  }

  // Filter universities by selected majors (if any selected)
  const filteredUniversities = selectedMajors.length > 0
    ? universities.filter(u => 
        selectedMajors.some(m => u.matching_majors.includes(m))
      )
    : universities

  // Load bookmarks from AsyncStorage
  useEffect(() => {
    const loadBookmarks = async () => {
      try {
        const saved = await AsyncStorage.getItem('bookmarked_universities')
        if (saved) {
          setBookmarkedUnis(new Set(JSON.parse(saved)))
        }
      } catch (err) {
        console.error('[Bookmarks] Failed to load:', err)
      }
    }
    loadBookmarks()
  }, [])

  // Toggle bookmark
  const toggleBookmark = async (uniId: string) => {
    // If bookmarking, require selected majors
    if (!bookmarkedUnis.has(uniId) && selectedMajors.length === 0) {
      console.log('[Bookmarks] Please select at least 1 major to bookmark')
      // TODO: Show toast notification
      return
    }

    const updated = new Set(bookmarkedUnis)
    const isRemoving = updated.has(uniId)
    
    if (isRemoving) {
      updated.delete(uniId)
    } else {
      updated.add(uniId)
    }
    
    setBookmarkedUnis(updated)
    
    try {
      // Save to AsyncStorage
      await AsyncStorage.setItem('bookmarked_universities', JSON.stringify(Array.from(updated)))
      
      // Save to database if user is logged in
      if (session?.user?.id) {
        if (isRemoving) {
          console.log('[Bookmarks] Removing from database - user:', session.user.id, 'uni:', uniId)
          await deleteUniversityBookmark(session.user.id, uniId, session.access_token)
        } else {
          console.log('[Bookmarks] Saving to database - user:', session.user.id, 'uni:', uniId, 'majors:', selectedMajors)
          await saveUniversityBookmark(session.user.id, uniId, selectedMajors, session.access_token)
        }
      }
      
      console.log('[Bookmarks] Saved:', Array.from(updated))
    } catch (err) {
      console.error('[Bookmarks] Failed to save:', err)
      // Revert local state on error
      setBookmarkedUnis(bookmarkedUnis)
    }
  }

  // Fetch universitas berdasarkan kode RIASEC utama (kode pertama saja)
  useEffect(() => {
    const code = riasecCode[0]   // Hanya ambil kode pertama (misal: C dari CIS)
    setUniLoading(true)
    getUniversitiesByRiasec([code], 20)
      .then(setUniversities)
      .catch(() => {/* silent */})
      .finally(() => setUniLoading(false))
  }, [riasecCode])

  // Save to Supabase (skip if historical)
  useEffect(() => {
    if (isHistorical || !session?.user?.id || saved) return
    setSaving(true)
    setSaveError(null)
    console.log('[TestResult] Attempting to save results for user:', session.user.id)
    saveTestResult(session.user.id, scores, riasecCode, session.access_token)
      .then(() => {
        console.log('[TestResult] Results saved successfully')
        setSaved(true)
        // Cancel test reminder — user sudah selesai test pertama
        markFirstTestDone().catch(() => {})
      })
      .catch((err) => {
        const errorMsg = err?.message ?? 'Gagal menyimpan hasil'
        console.error('[TestResult] Failed to save:', errorMsg)
        setSaveError(errorMsg)
        setSaved(true) // Still show results even if save fails
      })
      .finally(() => setSaving(false))
  }, [isHistorical, session?.user?.id, session?.access_token, saved])

  const handleShareImage = async () => {
    hapticLight()
    setGeneratingImage(true)
    try {
      const uri = await captureRef(shareCardRef, {
        format:  'png',
        quality: 1,
        result:  'tmpfile',
        width:   SHARE_CARD_W * 2,   // 720 px — tajam untuk Instagram
        height:  SHARE_CARD_H * 2,
      })
      const canShare = await Sharing.isAvailableAsync()
      if (!canShare) {
        Alert.alert('Tidak Didukung', 'Perangkat ini tidak mendukung fitur berbagi.')
        return
      }
      await Sharing.shareAsync(uri, {
        mimeType:    'image/png',
        dialogTitle: 'Bagikan Hasil Test RIASEC',
        UTI:         'public.png',  // iOS
      })
    } catch (err) {
      console.error('[Share] Failed to capture / share:', err)
      Alert.alert('Gagal', 'Tidak bisa membuat gambar. Coba lagi.')
    } finally {
      setGeneratingImage(false)
    }
  }

  return (
    <SafeAreaView style={styles.safe} edges={['top', 'bottom']}>
      <ScrollView
        contentContainerStyle={styles.scroll}
        showsVerticalScrollIndicator={false}
      >
        {/* ── Hero badge ── */}
        <View style={styles.heroWrap}>
          <View style={[styles.heroBg, { backgroundColor: topMeta.bg }]}>
            <Ionicons name={topMeta.icon as any} size={40} color={topMeta.color} />
          </View>
          {/* <View style={styles.codeRow}>
            {riasecCode.split('').map((ch, i) => {
              const key  = CODE_TO_KEY[ch] ?? 'realistic'
              const meta = CATEGORY_META[key]
              return (
                <View key={i} style={[styles.codeBadge, { backgroundColor: meta.bg }]}>
                  <Text style={[styles.codeChar, { color: meta.color }]}>{ch}</Text>
                </View>
              )
            })}
          </View> */}
          <Text style={styles.heroTitle}>Tipe {topMeta.label}</Text>
          <Text style={styles.heroDesc}>{topMeta.desc}</Text>
          {isHistorical && (
            <View style={styles.historicalBadge}>
              <Ionicons name="time-outline" size={14} color={colors.primary} />
              <Text style={styles.historicalBadgeText}>Hasil Test Sebelumnya</Text>
            </View>
          )}
          {!isHistorical && saving && (
            <View style={styles.savingRow}>
              <ActivityIndicator size="small" color={colors.textMuted} />
              <Text style={styles.savingText}>Menyimpan hasil…</Text>
            </View>
          )}
          {!isHistorical && !saving && saved && !saveError && (
            <View style={styles.savingRow}>
              <Ionicons name="checkmark-circle" size={16} color={colors.success} />
              <Text style={[styles.savingText, { color: colors.success }]}>Tersimpan</Text>
            </View>
          )}
          {!isHistorical && saveError && (
            <View style={styles.errorRow}>
              <Ionicons name="alert-circle-outline" size={16} color={colors.error} />
              <Text style={[styles.errorText, { color: colors.error }]}>{saveError}</Text>
            </View>
          )}
        </View>

        {/* ── Score cards ── */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Skor Per Dimensi</Text>
          <View style={styles.scoreGrid}>
            {(Object.keys(scores) as (keyof RiasecScores)[]).map((cat, i) => (
              <ScoreCard
                key={cat}
                category={cat}
                score={scores[cat]}
                delay={i * 60}
              />
            ))}
          </View>
        </View>

        {/* ── Jurusan yang Cocok ── */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Jurusan yang Cocok</Text>
          <Text style={styles.sectionSub}>
            {allMajors.length > 3 
              ? `Pilih maksimal 3 jurusan (${selectedMajors.length}/3)`
              : `Pilih jurusan yang diminati`}
          </Text>
          {uniLoading ? (
            <ActivityIndicator color={colors.primary} style={{ marginTop: 12 }} />
          ) : allMajors.length === 0 ? (
            <Text style={styles.noUniText}>Tidak ada jurusan yang cocok.</Text>
          ) : (
            <View style={styles.majorFilterRow}>
              {allMajors.map((major) => {
                const isSelected = selectedMajors.includes(major)
                const isDisabled = !isSelected && selectedMajors.length >= 3
                return (
                  <Pressable
                    key={major}
                    onPress={() => !isDisabled && toggleMajorSelection(major)}
                    disabled={isDisabled}
                    style={[
                      styles.majorFilterChip,
                      isSelected && styles.majorFilterChipActive,
                      isDisabled && styles.majorFilterChipDisabled,
                    ]}
                  >
                    <Text
                      style={[
                        styles.majorFilterChipText,
                        isSelected && styles.majorFilterChipTextActive,
                        isDisabled && { opacity: 0.5 },
                      ]}
                    >
                      {major}
                    </Text>
                  </Pressable>
                )
              })}
            </View>
          )}
        </View>

        {/* ── Universitas dengan Jurusan Cocok ── */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Universitas Pilihan</Text>
          <Text style={styles.sectionSub}>
            {selectedMajors.length > 0
              ? `Universitas dengan jurusan: ${selectedMajors.join(', ')}`
              : `Semua universitas untuk tipe ${topMeta.label}`}
          </Text>

          {uniLoading ? (
            <ActivityIndicator color={colors.primary} style={{ marginTop: 12 }} />
          ) : filteredUniversities.length === 0 ? (
            <Text style={styles.noUniText}>
              {selectedMajors.length > 0
                ? `Tidak ada universitas dengan jurusan ${selectedMajors.join(', ')}`
                : 'Data universitas tidak tersedia.'}
            </Text>
          ) : (
            <View style={styles.uniList}>
              {filteredUniversities.map((uni) => (
                <View
                  key={uni.id}
                  style={[
                    styles.uniCard,
                    uni.is_partner && styles.uniCardPartner,
                    uni.partner_tier === 'premium' && styles.uniCardPremium,
                  ]}
                >
                  {/* Partner badge */}
                  {uni.is_partner && (
                    <View style={[
                      styles.partnerBadge,
                      uni.partner_tier === 'premium' && styles.partnerBadgePremium,
                    ]}>
                      <Ionicons
                        name={uni.partner_tier === 'premium' ? 'star' : 'checkmark-circle'}
                        size={10}
                        color={uni.partner_tier === 'premium' ? '#F59E0B' : colors.primary}
                      />
                      <Text style={[
                        styles.partnerBadgeText,
                        uni.partner_tier === 'premium' && { color: '#F59E0B' },
                      ]}>
                        {uni.partner_tier === 'premium' ? 'Partner Premium' : 'Partner Resmi'}
                      </Text>
                    </View>
                  )}

                  {/* Header: logo placeholder + nama */}
                  <View style={styles.uniCardHeader}>
                    <View style={[
                      styles.uniLogo,
                      uni.is_partner && { backgroundColor: colors.primaryLight },
                    ]}>
                      {uni.logo_url ? (
                        // TODO: <Image source={{ uri: uni.logo_url }} style={styles.uniLogoImg} />
                        <Text style={styles.uniLogoInitial}>{uni.short_name[0]}</Text>
                      ) : (
                        <Text style={styles.uniLogoInitial}>{uni.short_name[0]}</Text>
                      )}
                    </View>
                    <View style={{ flex: 1 }}>
                      <Text style={styles.uniCardName}>{uni.short_name}</Text>
                      <Text style={styles.uniCardFullName} numberOfLines={1}>{uni.name}</Text>
                      <Text style={styles.uniCardCity}>
                        <Ionicons name="location-outline" size={11} color={colors.textMuted} />
                        {' '}{uni.city}, {uni.province}
                      </Text>
                    </View>
                    <View style={styles.uniCardActions}>
                      <View style={[
                        styles.uniTypeBadge,
                        uni.type === 'negeri'
                          ? { backgroundColor: colors.successLight }
                          : { backgroundColor: colors.warningLight },
                      ]}>
                        <Text style={[
                          styles.uniTypeBadgeText,
                          { color: uni.type === 'negeri' ? colors.success : colors.warning },
                        ]}>
                          {uni.type === 'negeri' ? 'PTN' : 'PTS'}
                        </Text>
                      </View>
                      <Pressable
                        onPress={() => toggleBookmark(uni.id)}
                        style={styles.bookmarkBtn}
                      >
                        <Ionicons
                          name={bookmarkedUnis.has(uni.id) ? 'bookmark' : 'bookmark-outline'}
                          size={20}
                          color={bookmarkedUnis.has(uni.id) ? colors.primary : colors.textMuted}
                        />
                      </Pressable>
                    </View>
                  </View>
                </View>
              ))}
            </View>
          )}
        </View>

        {/* ── Actions ── */}
        <View style={styles.actions}>
          {/* Share image button */}
          <Pressable
            style={({ pressed }) => [
              styles.shareBtn,
              pressed && { opacity: 0.88 },
              generatingImage && { opacity: 0.6 },
            ]}
            onPress={handleShareImage}
            disabled={generatingImage}
          >
            {generatingImage
              ? <ActivityIndicator size="small" color={colors.primary} />
              : <Ionicons name="image-outline" size={18} color={colors.primary} />
            }
            <Text style={styles.shareBtnText}>
              {generatingImage ? 'Membuat gambar…' : 'Bagikan sebagai Gambar'}
            </Text>
          </Pressable>

          <Pressable
            style={({ pressed }) => [styles.primaryBtn, pressed && { opacity: 0.88 }]}
            onPress={() => { hapticMedium(); router.replace('/(tabs)/home') }}
          >
            <Ionicons name="home-outline" size={20} color={colors.white} />
            <Text style={styles.primaryBtnText}>Kembali ke Beranda</Text>
          </Pressable>
          <Pressable
            style={({ pressed }) => [styles.outlineBtn, pressed && { opacity: 0.7 }]}
            onPress={() => { hapticLight(); router.replace('/test-session') }}
          >
            <Ionicons name="refresh-outline" size={18} color={colors.primary} />
            <Text style={styles.outlineBtnText}>Ulangi Test</Text>
          </Pressable>
        </View>
      </ScrollView>
      {/* ── Off-screen share card (captured as PNG) ── */}
      <View
        ref={shareCardRef}
        collapsable={false}
        style={[shareCardStyles.card, {
          position:         'absolute',
          left:             -(SHARE_CARD_W + 100),
          top:              0,
          backgroundColor:  topMeta.bg,
        }]}
      >
        {/* Decorative circles */}
        <View style={[shareCardStyles.deco1, { backgroundColor: topMeta.color + '22' }]} />
        <View style={[shareCardStyles.deco2, { backgroundColor: topMeta.color + '14' }]} />

        {/* Brand */}
        <View style={shareCardStyles.brand}>
          <View style={[shareCardStyles.brandDot, { backgroundColor: topMeta.color }]} />
          <Text style={[shareCardStyles.brandName, { color: topMeta.color }]}>tentuin</Text>
        </View>

        {/* Result hero */}
        <View style={shareCardStyles.hero}>
          <View style={[shareCardStyles.heroIcon, { backgroundColor: topMeta.color + '28' }]}>
            <Ionicons name={topMeta.icon as any} size={52} color={topMeta.color} />
          </View>
          {/* Colored RIASEC code — each letter uses its category color */}
          <View style={shareCardStyles.codeRow}>
            {riasecCode.split('').map((ch, i) => {
              const key  = CODE_TO_KEY[ch] ?? 'realistic'
              const meta = CATEGORY_META[key]
              return (
                <Text key={i} style={[shareCardStyles.codeChar, { color: meta.color }]}>{ch}</Text>
              )
            })}
          </View>
          <Text style={shareCardStyles.heroTypeName}>Tipe {topMeta.label}</Text>
          <Text style={shareCardStyles.heroDesc} numberOfLines={2}>{topMeta.desc}</Text>

          {/* Jurusan rekomendasi */}
          <View style={shareCardStyles.majorsRow}>
            {(RIASEC_MAJORS[topKey] ?? []).map((m, i) => (
              <View
                key={i}
                style={[shareCardStyles.majorChip, {
                  backgroundColor: topMeta.color + '1A',
                  borderColor:     topMeta.color + '55',
                }]}
              >
                <Text style={[shareCardStyles.majorChipText, { color: topMeta.color }]}>{m}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* Divider */}
        <View style={[shareCardStyles.divider, { backgroundColor: topMeta.color + '35' }]} />

        {/* Score section */}
        <Text style={[shareCardStyles.sectionLabel, { color: topMeta.color }]}>SKOR RIASEC</Text>
        <View style={shareCardStyles.scoreList}>
          {(Object.entries(scores) as [keyof RiasecScores, number][]).map(([cat, score]) => {
            const m = CATEGORY_META[cat]
            return (
              <View key={cat} style={shareCardStyles.scoreRow}>
                <Text style={[shareCardStyles.scoreCode, { color: m.color }]}>{m.code}</Text>
                <View style={shareCardStyles.scoreTrack}>
                  <View style={[shareCardStyles.scoreFill, {
                    width:           `${score}%` as any,
                    backgroundColor: m.color,
                  }]} />
                </View>
                <Text style={shareCardStyles.scoreNum}>{score}%</Text>
              </View>
            )
          })}
        </View>

        {/* Footer watermark */}
        <View style={shareCardStyles.footer}>
          <Text style={[shareCardStyles.footerText, { color: topMeta.color + 'AA' }]}>
            tentuin.id  •  Temukan Jurusanmu
          </Text>
        </View>
      </View>
    </SafeAreaView>
  )
}

const SHARE_CARD_W = 360
const SHARE_CARD_H = 680

const shareCardStyles = StyleSheet.create({
  card: {
    width:        SHARE_CARD_W,
    height:       SHARE_CARD_H,
    borderRadius: 0,           // full-bleed, no radius → better for Instagram
    paddingHorizontal: 28,
    paddingTop:   28,
    paddingBottom: 20,
    overflow:     'hidden',
  },

  // Decorative background circles
  deco1: {
    position: 'absolute', width: 220, height: 220, borderRadius: 110,
    top: -60, right: -50,
  },
  deco2: {
    position: 'absolute', width: 180, height: 180, borderRadius: 90,
    bottom: 40, left: -50,
  },

  // Brand header
  brand: { flexDirection: 'row', alignItems: 'center', gap: 7, marginBottom: 24 },
  brandDot: { width: 9, height: 9, borderRadius: 5 },
  brandName: { fontSize: 16, fontFamily: fonts.bold, letterSpacing: 0.6 },

  // Hero
  hero: { alignItems: 'center', gap: 8, marginBottom: 20 },
  heroIcon: {
    width: 88, height: 88, borderRadius: 22,
    alignItems: 'center', justifyContent: 'center', marginBottom: 4,
  },
  codeRow: { flexDirection: 'row', gap: 2 },
  codeChar: { fontSize: 56, fontFamily: fonts.extraBold, letterSpacing: 2 },
  heroTypeName: { fontSize: 20, fontFamily: fonts.bold, color: colors.text, marginTop: 2 },
  heroDesc: {
    fontSize: 13, fontFamily: fonts.regular, color: colors.textSub,
    textAlign: 'center', lineHeight: 20, paddingHorizontal: 6,
  },

  majorsRow: {
    flexDirection: 'row', flexWrap: 'wrap',
    justifyContent: 'center', gap: 6, marginTop: 4,
  },
  majorChip: {
    paddingHorizontal: 10, paddingVertical: 4,
    borderRadius: 20, borderWidth: 1,
  },
  majorChipText: { fontSize: 11, fontFamily: fonts.semiBold },

  // Divider
  divider: { height: 1, marginBottom: 16 },

  // Score section
  sectionLabel: {
    fontSize: 10, fontFamily: fonts.bold, letterSpacing: 1.8,
    textTransform: 'uppercase', marginBottom: 10,
  },
  scoreList: { gap: 8 },
  scoreRow:  { flexDirection: 'row', alignItems: 'center', gap: 10 },
  scoreCode: { fontSize: 12, fontFamily: fonts.bold, width: 14 },
  scoreTrack: {
    flex: 1, height: 7, backgroundColor: '#00000012',
    borderRadius: 4, overflow: 'hidden',
  },
  scoreFill: { height: '100%', borderRadius: 4 },
  scoreNum: {
    fontSize: 11, fontFamily: fonts.semiBold, color: colors.textMuted,
    width: 34, textAlign: 'right',
  },

  // Footer
  footer: { position: 'absolute', bottom: 20, left: 0, right: 0, alignItems: 'center' },
  footerText: { fontSize: 11, fontFamily: fonts.medium, letterSpacing: 0.4 },
})

const styles = StyleSheet.create({
  safe:   { flex: 1, backgroundColor: colors.background },
  scroll: { padding: 24, gap: 28, paddingBottom: 40 },

  // Hero
  heroWrap: { alignItems: 'center', gap: 14 },
  heroBg: {
    width: 100,
    height: 100,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  codeRow:  { flexDirection: 'row', gap: 10 },
  codeBadge: {
    width: 48,
    height: 48,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  codeChar: { fontSize: 22, fontFamily: fonts.extraBold },
  heroTitle: { fontSize: 28, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.8 },
  heroDesc:  {
    fontSize: 15,
    fontFamily: fonts.regular,
    color: colors.textSub,
    textAlign: 'center',
    lineHeight: 23,
    maxWidth: 320,
  },
  savingRow: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  savingText: { fontSize: 13, fontFamily: fonts.medium, color: colors.textMuted },
  errorRow: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  errorText: { fontSize: 13, fontFamily: fonts.medium },
  historicalBadge: { flexDirection: 'row', alignItems: 'center', gap: 6, backgroundColor: colors.primaryLight, paddingHorizontal: 12, paddingVertical: 8, borderRadius: 12 },
  historicalBadgeText: { fontSize: 13, fontFamily: fonts.semiBold, color: colors.primary },

  // Section
  section: { gap: 14 },
  sectionTitle: {
    fontSize: 13,
    fontFamily: fonts.bold,
    color: colors.textSub,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  sectionSub: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub, marginTop: -6 },

  // Score cards
  scoreGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, justifyContent: 'space-between' },

  noUniText: { fontSize: 13, fontFamily: fonts.regular, color: colors.textMuted, fontStyle: 'italic' },

  // University list
  uniList: { gap: 10 },

  uniCard: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 14,
    gap: 10,
    borderWidth: 1.5,
    borderColor: colors.border,
  },
  uniCardPartner: {
    borderColor: colors.primary + '55',
    backgroundColor: colors.primaryMuted,
  },
  uniCardPremium: {
    borderColor: '#F59E0B' + '66',
    backgroundColor: '#FFFBEB',
  },

  partnerBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    backgroundColor: colors.primaryLight,
    paddingHorizontal: 8, paddingVertical: 3,
    borderRadius: 100, alignSelf: 'flex-start',
  },
  partnerBadgePremium: { backgroundColor: '#FEF3C7' },
  partnerBadgeText: {
    fontSize: 10, fontFamily: fonts.bold, color: colors.primary, letterSpacing: 0.3,
  },

  uniCardHeader: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  uniLogo: {
    width: 44, height: 44, borderRadius: 12,
    backgroundColor: colors.gray[100],
    alignItems: 'center', justifyContent: 'center',
  },
  uniLogoInitial: { fontSize: 18, fontFamily: fonts.extraBold, color: colors.primary },
  uniCardName:     { fontSize: 15, fontFamily: fonts.bold, color: colors.text },
  uniCardFullName: { fontSize: 11, fontFamily: fonts.regular, color: colors.textSub, marginTop: 1 },
  uniCardCity:     { fontSize: 11, fontFamily: fonts.regular, color: colors.textMuted, marginTop: 2 },

  uniTypeBadge: {
    paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6, alignSelf: 'flex-start',
  },
  uniTypeBadgeText: { fontSize: 10, fontFamily: fonts.bold },

  uniCardActions: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  bookmarkBtn: { padding: 4 },

  matchingMajors: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  majorChip: {
    backgroundColor: colors.primaryLight,
    paddingHorizontal: 10, paddingVertical: 6,
    borderRadius: 20,
  },
  majorChipText: { fontSize: 12, fontFamily: fonts.semiBold, color: colors.primary },

  majorFilterRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 12 },
  majorFilterChip: {
    paddingHorizontal: 12, paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1.5,
    borderColor: colors.border,
    backgroundColor: colors.surface,
  },
  majorFilterChipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  majorFilterChipDisabled: {
    backgroundColor: colors.gray[100],
    borderColor: colors.border,
    opacity: 0.5,
  },
  majorFilterChipText: { fontSize: 12, fontFamily: fonts.semiBold, color: colors.text },
  majorFilterChipTextActive: { color: colors.white },

  // Actions
  actions: { gap: 12 },
  primaryBtn: {
    backgroundColor: colors.primary,
    borderRadius: 14,
    paddingVertical: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    shadowColor: colors.primary,
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.25,
    shadowRadius: 12,
    elevation: 5,
  },
  primaryBtnText: { fontSize: 16, fontFamily: fonts.bold, color: colors.white },
  shareBtn: {
    borderRadius: 14,
    paddingVertical: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    backgroundColor: colors.surface,
    borderWidth: 1.5,
    borderColor: colors.border,
  },
  shareBtnText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.primary },
  outlineBtn: {
    borderRadius: 14,
    paddingVertical: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    borderWidth: 1.5,
    borderColor: colors.primary,
  },
  outlineBtnText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.primary },
})
