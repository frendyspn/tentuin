import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { hapticLight, hapticSelection } from '../../../utils/haptics'
import { ExploreUniListSkeleton, ExploreMajorGridSkeleton } from '../../../components/Skeleton'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import {
  getUniversities,
  searchUniversities,
  supabaseUrl,
  supabaseAnonKey,
  type UniversityRow,
  type MajorRow,
} from '@tentuin/supabase'

// ─── Constants ───────────────────────────────────────────────────────────────

const TYPE_FILTERS = ['Semua', 'Negeri', 'Swasta']

const RIASEC_CATEGORIES = [
  { label: 'Semua',         code: null  },
  { label: 'Realistis',     code: 'R'   },
  { label: 'Investigatif',  code: 'I'   },
  { label: 'Artistik',      code: 'A'   },
  { label: 'Sosial',        code: 'S'   },
  { label: 'Enterprising',  code: 'E'   },
  { label: 'Konvensional',  code: 'C'   },
]

const UNIVERSITY_PAGE_SIZE = 12
const MAJOR_PAGE_SIZE = 12

// ─── Helpers ─────────────────────────────────────────────────────────────────

const getMajorStyle = (name: string): { icon: any; bg: string; color: string } => {
  const n = name.toLowerCase()
  if (n.includes('informatika') || n.includes('komputer') || n.includes('sistem informasi'))
    return { icon: 'desktop-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('teknik') && !n.includes('informatika'))
    return { icon: 'construct-outline', bg: '#F0F9FF', color: '#3B82F6' }
  if (n.includes('psikologi'))
    return { icon: 'sparkles-outline', bg: '#EEF2FF', color: colors.primary }
  if (n.includes('desain') || n.includes('seni') || n.includes('dkv'))
    return { icon: 'color-palette-outline', bg: '#FFF0F6', color: '#EC4899' }
  if (n.includes('arsitektur'))
    return { icon: 'business-outline', bg: '#FFF0F6', color: '#EC4899' }
  if (n.includes('manajemen') || n.includes('bisnis'))
    return { icon: 'trending-up-outline', bg: '#FFFBEB', color: '#F59E0B' }
  if (n.includes('akuntansi') || n.includes('keuangan') || n.includes('ekonomi'))
    return { icon: 'cash-outline', bg: '#FFFBEB', color: '#F59E0B' }
  if (n.includes('kedokteran') || n.includes('medis'))
    return { icon: 'medkit-outline', bg: '#FFF0F0', color: '#EF4444' }
  if (n.includes('farmasi') || n.includes('apoteker'))
    return { icon: 'flask-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('hukum'))
    return { icon: 'scale-outline', bg: '#EEF2FF', color: colors.primary }
  if (n.includes('komunikasi') || n.includes('jurnalis'))
    return { icon: 'megaphone-outline', bg: '#FFF0F6', color: '#EC4899' }
  if (n.includes('biologi') || n.includes('kimia') || n.includes('fisika') || n.includes('matematika'))
    return { icon: 'flask-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('pertanian') || n.includes('agro') || n.includes('peternakan') || n.includes('perikanan'))
    return { icon: 'leaf-outline', bg: '#F0FDF4', color: '#10B981' }
  if (n.includes('musik') || n.includes('teater') || n.includes('film'))
    return { icon: 'musical-notes-outline', bg: '#FFF0F6', color: '#EC4899' }
  return { icon: 'school-outline', bg: '#EEF2FF', color: colors.primary }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

function UniversityCard({ item, onPress }: { item: UniversityRow; onPress: () => void }) {
  const initial = item.short_name?.[0]?.toUpperCase() ?? item.name[0]?.toUpperCase() ?? '?'
  return (
    <Pressable
      style={({ pressed }) => [styles.uniCard, pressed && { opacity: 0.88 }]}
      onPress={onPress}
    >
      {/* Logo placeholder */}
      <View style={styles.uniLogo}>
        <Text style={styles.uniLogoText}>{initial}</Text>
      </View>

      <View style={styles.uniBody}>
        <View style={styles.uniNameRow}>
          <Text style={styles.uniName} numberOfLines={1}>{item.name}</Text>
          {item.is_partner && (
            <View style={[
              styles.partnerBadge,
              item.partner_tier === 'premium' && styles.partnerBadgePremium,
            ]}>
              <Ionicons
                name={item.partner_tier === 'premium' ? 'star' : 'checkmark-circle'}
                size={10}
                color={item.partner_tier === 'premium' ? '#F59E0B' : colors.primary}
              />
              <Text style={[
                styles.partnerText,
                item.partner_tier === 'premium' && styles.partnerTextPremium,
              ]}>
                {item.partner_tier === 'premium' ? 'Premium' : 'Partner'}
              </Text>
            </View>
          )}
        </View>

        <Text style={styles.uniShort}>{item.short_name}</Text>

        <View style={styles.uniMeta}>
          <View style={[
            styles.typeBadge,
            item.type === 'negeri' ? styles.typeBadgeNegeri : styles.typeBadgeSwasta,
          ]}>
            <Text style={[
              styles.typeText,
              item.type === 'negeri' ? styles.typeTextNegeri : styles.typeTextSwasta,
            ]}>
              PT{item.type === 'negeri' ? 'N' : 'S'}
            </Text>
          </View>
          <Ionicons name="location-outline" size={11} color={colors.textMuted} />
          <Text style={styles.uniCity} numberOfLines={1}>{item.city}</Text>
        </View>
      </View>

      <Ionicons name="chevron-forward" size={16} color={colors.gray[300]} />
    </Pressable>
  )
}

function MajorCard({ item, onPress }: { item: MajorRow; onPress: () => void }) {
  const { icon, bg, color } = getMajorStyle(item.name)
  return (
    <Pressable
      style={({ pressed }) => [styles.majorCard, pressed && { opacity: 0.88 }]}
      onPress={onPress}
    >
      <View style={[styles.majorIcon, { backgroundColor: bg }]}>
        <Ionicons name={icon} size={26} color={color} />
      </View>
      <Text style={styles.majorName} numberOfLines={2}>{item.name}</Text>
      <View style={styles.majorRiasec}>
        {item.riasec_codes.slice(0, 3).map((code) => (
          <View key={code} style={styles.riasecChip}>
            <Text style={styles.riasecText}>{code}</Text>
          </View>
        ))}
      </View>
    </Pressable>
  )
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

export default function ExploreScreen() {
  const router = useRouter()
  const [activeTab, setActiveTab] = useState<'universitas' | 'jurusan'>('universitas')
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState('Semua')
  const [riasecCode, setRiasecCode] = useState<string | null>(null)

  // Universitas state
  const [universities, setUniversities] = useState<UniversityRow[]>([])
  const [loadingUni, setLoadingUni] = useState(true)
  const [searchResults, setSearchResults] = useState<UniversityRow[] | null>(null)
  const [searching, setSearching] = useState(false)
  const [visibleUniCount, setVisibleUniCount] = useState(UNIVERSITY_PAGE_SIZE)

  // Jurusan state
  const [majors, setMajors] = useState<MajorRow[]>([])
  const [loadingMajors, setLoadingMajors] = useState(true)
  const [visibleMajorCount, setVisibleMajorCount] = useState(MAJOR_PAGE_SIZE)

  // Load universities
  useEffect(() => {
    getUniversities()
      .then(setUniversities)
      .catch(console.error)
      .finally(() => setLoadingUni(false))
  }, [])

  // Load majors
  useEffect(() => {
    const fetchMajors = async () => {
      try {
        const res = await fetch(
          `${supabaseUrl}/rest/v1/majors?is_active=eq.true&order=name.asc&select=id,name,faculty,riasec_codes,university_id`,
          { headers: { apikey: supabaseAnonKey, Authorization: `Bearer ${supabaseAnonKey}` } }
        )
        if (!res.ok) throw new Error(`${res.status}`)
        setMajors(await res.json())
      } catch (e) {
        console.error('loadMajors', e)
      } finally {
        setLoadingMajors(false)
      }
    }
    fetchMajors()
  }, [])

  // Debounced university search
  useEffect(() => {
    if (activeTab !== 'universitas') return
    if (!search.trim()) { setSearchResults(null); return }
    const t = setTimeout(async () => {
      setSearching(true)
      try { setSearchResults(await searchUniversities(search.trim())) }
      catch (e) { console.error(e) }
      finally { setSearching(false) }
    }, 400)
    return () => clearTimeout(t)
  }, [search, activeTab])

  // Clear search results when switching tabs
  const handleTabChange = useCallback((tab: 'universitas' | 'jurusan') => {
    hapticSelection()
    setActiveTab(tab)
    setSearch('')
    setSearchResults(null)
    setTypeFilter('Semua')
    setRiasecCode(null)
  }, [])

  // Filtered universities
  const displayedUniversities = useMemo(() => {
    const source = searchResults ?? universities
    if (typeFilter === 'Semua') return source
    const t = typeFilter.toLowerCase() as 'negeri' | 'swasta'
    return source.filter(u => u.type === t)
  }, [searchResults, universities, typeFilter])

  // Unique majors deduplicated by name
  const uniqueMajors = useMemo(() => {
    const seen = new Set<string>()
    const result: MajorRow[] = []
    for (const m of majors) {
      const key = m.name.toLowerCase().trim()
      if (!seen.has(key)) {
        seen.add(key)
        result.push(m)
      }
    }
    return result
  }, [majors])

  const filteredMajors = useMemo(() => {
    return uniqueMajors.filter(m => {
      const matchSearch = !search.trim() || m.name.toLowerCase().includes(search.toLowerCase())
      const matchRiasec = !riasecCode || m.riasec_codes.includes(riasecCode)
      return matchSearch && matchRiasec
    })
  }, [uniqueMajors, search, riasecCode])

  const visibleUniversities = useMemo(
    () => displayedUniversities.slice(0, visibleUniCount),
    [displayedUniversities, visibleUniCount]
  )

  const visibleMajors = useMemo(
    () => filteredMajors.slice(0, visibleMajorCount),
    [filteredMajors, visibleMajorCount]
  )

  const hasMoreUniversities = visibleUniCount < displayedUniversities.length
  const hasMoreMajors = visibleMajorCount < filteredMajors.length

  useEffect(() => {
    setVisibleUniCount(UNIVERSITY_PAGE_SIZE)
  }, [searchResults, universities, typeFilter, activeTab])

  useEffect(() => {
    setVisibleMajorCount(MAJOR_PAGE_SIZE)
  }, [uniqueMajors, search, riasecCode, activeTab])

  const loadMoreUniversities = useCallback(() => {
    if (loadingUni || searching || !hasMoreUniversities) return
    setVisibleUniCount((prev) => Math.min(prev + UNIVERSITY_PAGE_SIZE, displayedUniversities.length))
  }, [displayedUniversities.length, hasMoreUniversities, loadingUni, searching])

  const loadMoreMajors = useCallback(() => {
    if (loadingMajors || !hasMoreMajors) return
    setVisibleMajorCount((prev) => Math.min(prev + MAJOR_PAGE_SIZE, filteredMajors.length))
  }, [filteredMajors.length, hasMoreMajors, loadingMajors])

  const renderLoadMoreFooter = (show: boolean) => {
    if (!show) return null
    return (
      <View style={styles.loadMoreFooter}>
        <ActivityIndicator size="small" color={colors.primary} />
        <Text style={styles.loadMoreText}>Memuat data berikutnya...</Text>
      </View>
    )
  }

  // ── Render ────────────────────────────────────────────────────────────────

  const listHeader = useMemo(() => (
    <View style={styles.stickyTop}>
      {/* Title row */}
      <View style={styles.topRow}>
        <Text style={styles.title}>Jelajah</Text>
        <View style={styles.countBadge}>
          <Text style={styles.countText}>
            {activeTab === 'universitas'
              ? `${displayedUniversities.length} kampus`
              : `${filteredMajors.length} jurusan`}
          </Text>
        </View>
      </View>

      {/* Tab switcher */}
      <View style={styles.tabSwitcher}>
        <Pressable
          style={[styles.tabBtn, activeTab === 'universitas' && styles.tabBtnActive]}
          onPress={() => handleTabChange('universitas')}
        >
          <Ionicons
            name="business-outline"
            size={15}
            color={activeTab === 'universitas' ? colors.white : colors.textSub}
          />
          <Text style={[styles.tabBtnText, activeTab === 'universitas' && styles.tabBtnTextActive]}>
            Universitas
          </Text>
        </Pressable>
        <Pressable
          style={[styles.tabBtn, activeTab === 'jurusan' && styles.tabBtnActive]}
          onPress={() => handleTabChange('jurusan')}
        >
          <Ionicons
            name="book-outline"
            size={15}
            color={activeTab === 'jurusan' ? colors.white : colors.textSub}
          />
          <Text style={[styles.tabBtnText, activeTab === 'jurusan' && styles.tabBtnTextActive]}>
            Jurusan
          </Text>
        </Pressable>
      </View>

      {/* Search bar */}
      <View style={styles.searchBar}>
        <Ionicons name="search-outline" size={18} color={colors.textMuted} />
        <TextInput
          style={styles.searchInput}
          placeholder={activeTab === 'universitas' ? 'Cari universitas atau kota...' : 'Cari jurusan...'}
          placeholderTextColor={colors.textMuted}
          value={search}
          onChangeText={setSearch}
          returnKeyType="search"
        />
        {(search.length > 0 || searching) && (
          <Pressable onPress={() => { setSearch(''); setSearchResults(null) }} hitSlop={8}>
            {searching
              ? <ActivityIndicator size="small" color={colors.primary} />
              : <Ionicons name="close-circle" size={18} color={colors.textMuted} />
            }
          </Pressable>
        )}
      </View>

      {/* Filter chips */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        style={styles.chipsScroll}
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={{ paddingHorizontal: 24, gap: 8 }}
      >
        {activeTab === 'universitas'
          ? TYPE_FILTERS.map(f => (
              <Pressable
                key={f}
                style={[styles.chip, typeFilter === f && styles.chipActive]}
                onPress={() => { hapticLight(); setTypeFilter(f) }}
              >
                <Text style={[styles.chipText, typeFilter === f && styles.chipTextActive]}>{f}</Text>
              </Pressable>
            ))
          : RIASEC_CATEGORIES.map(c => (
              <Pressable
                key={c.label}
                style={[styles.chip, riasecCode === c.code && styles.chipActive]}
                onPress={() => { hapticLight(); setRiasecCode(c.code) }}
              >
                {c.code && (
                  <View style={[styles.chipCode, riasecCode === c.code && styles.chipCodeActive]}>
                    <Text style={[styles.chipCodeText, riasecCode === c.code && styles.chipCodeTextActive]}>
                      {c.code}
                    </Text>
                  </View>
                )}
                <Text style={[styles.chipText, riasecCode === c.code && styles.chipTextActive]}>
                  {c.label}
                </Text>
              </Pressable>
            ))
        }
      </ScrollView>
    </View>
  ), [
    activeTab,
    displayedUniversities.length,
    filteredMajors.length,
    search,
    searching,
    handleTabChange,
    typeFilter,
    riasecCode,
  ])

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      {/* Header selalu di atas — tidak ikut scroll, tab switch tidak menyebabkan remount */}
      {listHeader}

      {/* Universitas list — selalu mounted, disembunyikan kalau bukan tab aktif */}
      <View style={[styles.listContainer, activeTab !== 'universitas' && styles.hidden]}>
        <FlatList
          data={loadingUni ? [] : visibleUniversities}
          keyExtractor={item => item.id}
          renderItem={({ item }) => (
            <UniversityCard
              item={item}
              onPress={() => router.push(`/university-detail?id=${item.id}`)}
            />
          )}
          initialNumToRender={UNIVERSITY_PAGE_SIZE}
          maxToRenderPerBatch={UNIVERSITY_PAGE_SIZE}
          windowSize={7}
          onEndReached={loadMoreUniversities}
          onEndReachedThreshold={0.35}
          ListFooterComponent={renderLoadMoreFooter(!loadingUni && hasMoreUniversities)}
          contentContainerStyle={styles.listContent}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="none"
          showsVerticalScrollIndicator={false}
          ListEmptyComponent={
            <View style={styles.empty}>
              {loadingUni
                ? <ExploreUniListSkeleton />
                : <>
                    <Ionicons name="business-outline" size={40} color={colors.gray[300]} />
                    <Text style={styles.emptyText}>Tidak ada kampus ditemukan</Text>
                  </>
              }
            </View>
          }
        />
      </View>

      {/* Jurusan list — selalu mounted, disembunyikan kalau bukan tab aktif */}
      <View style={[styles.listContainer, activeTab !== 'jurusan' && styles.hidden]}>
        <FlatList
          data={loadingMajors ? [] : visibleMajors}
          keyExtractor={item => item.id}
          numColumns={2}
          columnWrapperStyle={styles.majorRow}
          renderItem={({ item }) => (
            <MajorCard
              item={item}
              onPress={() => router.push(`/major-detail?name=${encodeURIComponent(item.name)}&codes=${item.riasec_codes.join(',')}`)}
            />
          )}
          initialNumToRender={MAJOR_PAGE_SIZE}
          maxToRenderPerBatch={MAJOR_PAGE_SIZE}
          windowSize={7}
          onEndReached={loadMoreMajors}
          onEndReachedThreshold={0.35}
          ListFooterComponent={renderLoadMoreFooter(!loadingMajors && hasMoreMajors)}
          contentContainerStyle={styles.listContent}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="none"
          showsVerticalScrollIndicator={false}
          ListEmptyComponent={
            <View style={styles.empty}>
              {loadingMajors
                ? <ExploreMajorGridSkeleton />
                : <>
                    <Ionicons name="book-outline" size={40} color={colors.gray[300]} />
                    <Text style={styles.emptyText}>Jurusan tidak ditemukan</Text>
                  </>
            }
            </View>
          }
        />
      </View>
    </SafeAreaView>
  )
}

// ─── Styles ──────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  listContainer: { flex: 1 },
  hidden: { display: 'none' },
  listContent: { paddingBottom: 48 },

  stickyTop: {
    backgroundColor: colors.background,
    paddingTop: 16,
    paddingBottom: 12,
    gap: 12,
  },
  topRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingHorizontal: 24 },
  title: { fontSize: 26, fontFamily: fonts.extraBold, color: colors.text, letterSpacing: -0.6 },
  countBadge: { backgroundColor: colors.primaryMuted, paddingHorizontal: 10, paddingVertical: 4, borderRadius: 100 },
  countText: { fontSize: 12, fontFamily: fonts.semiBold, color: colors.primary },

  tabSwitcher: {
    flexDirection: 'row',
    marginHorizontal: 24,
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 4,
    borderWidth: 1,
    borderColor: colors.border,
  },
  tabBtn: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6, paddingVertical: 9, borderRadius: 9 },
  tabBtnActive: { backgroundColor: colors.primary },
  tabBtnText: { fontSize: 14, fontFamily: fonts.semiBold, color: colors.textSub },
  tabBtnTextActive: { color: colors.white },

  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: 24,
    backgroundColor: colors.surface,
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 2,
  },
  searchInput: { flex: 1, fontSize: 15, fontFamily: fonts.regular, color: colors.text },

  chipsScroll: { marginHorizontal: -24 },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 100,
    backgroundColor: colors.surface,
    borderWidth: 1.5,
    borderColor: colors.border,
  },
  chipActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  chipText: { fontSize: 13, fontFamily: fonts.semiBold, color: colors.textSub },
  chipTextActive: { color: colors.white },
  chipCode: {
    width: 18, height: 18, borderRadius: 5,
    backgroundColor: colors.primaryMuted,
    alignItems: 'center', justifyContent: 'center',
  },
  chipCodeActive: { backgroundColor: 'rgba(255,255,255,0.25)' },
  chipCodeText: { fontSize: 10, fontFamily: fonts.bold, color: colors.primary },
  chipCodeTextActive: { color: colors.white },

  // University card
  uniCard: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: 24,
    marginTop: 10,
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 16,
    gap: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 2,
  },
  uniLogo: {
    width: 52, height: 52, borderRadius: 12,
    backgroundColor: colors.primaryLight,
    alignItems: 'center', justifyContent: 'center',
    flexShrink: 0,
  },
  uniLogoText: { fontSize: 22, fontFamily: fonts.extraBold, color: colors.primary },
  uniBody: { flex: 1, gap: 4 },
  uniNameRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  uniName: { flex: 1, fontSize: 14, fontFamily: fonts.bold, color: colors.text },
  uniShort: { fontSize: 12, fontFamily: fonts.medium, color: colors.textSub },
  uniMeta: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 2 },
  uniCity: { flex: 1, fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },
  typeBadge: { paddingHorizontal: 7, paddingVertical: 2, borderRadius: 5 },
  typeBadgeNegeri: { backgroundColor: '#EEF2FF' },
  typeBadgeSwasta: { backgroundColor: '#FFF3CD' },
  typeText: { fontSize: 10, fontFamily: fonts.bold },
  typeTextNegeri: { color: colors.primary },
  typeTextSwasta: { color: '#D97706' },
  partnerBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 3,
    paddingHorizontal: 7, paddingVertical: 3, borderRadius: 6,
    backgroundColor: colors.primaryMuted,
  },
  partnerBadgePremium: { backgroundColor: '#FFFBEB' },
  partnerText: { fontSize: 10, fontFamily: fonts.bold, color: colors.primary },
  partnerTextPremium: { color: '#F59E0B' },

  // Major card (2-col grid)
  majorRow: { paddingHorizontal: 24, gap: 12 },
  majorCard: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 16,
    marginTop: 10,
    gap: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 2,
  },
  majorIcon: { width: 50, height: 50, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  majorName: { fontSize: 13, fontFamily: fonts.bold, color: colors.text, lineHeight: 19 },
  majorRiasec: { flexDirection: 'row', gap: 4 },
  riasecChip: {
    width: 20, height: 20, borderRadius: 5,
    backgroundColor: colors.primaryMuted,
    alignItems: 'center', justifyContent: 'center',
  },
  riasecText: { fontSize: 10, fontFamily: fonts.bold, color: colors.primary },

  empty: { alignItems: 'center', paddingVertical: 64, gap: 12, paddingHorizontal: 24 },
  emptyText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub },
  loadMoreFooter: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingTop: 14,
    paddingBottom: 20,
  },
  loadMoreText: {
    fontSize: 13,
    fontFamily: fonts.medium,
    color: colors.textMuted,
  },
})
