import React, { useCallback, useEffect, useState } from 'react'
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter, useFocusEffect } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'
import {
  getBookmarkedUniversities,
  deleteUniversityBookmark,
  type UniversityBookmark,
  type UniversityRow,
} from '@tentuin/supabase'
import { useAuthStore } from '../stores/authStore'
import { hapticWarning } from '../utils/haptics'

type BookmarkWithUniversity = UniversityBookmark & { universities: UniversityRow }

export default function FavoritesCampusScreen() {
  const router = useRouter()
  const { session } = useAuthStore()

  const [bookmarks, setBookmarks] = useState<BookmarkWithUniversity[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (!session?.user?.id) { setLoading(false); return }
    setLoading(true)
    setError(null)
    try {
      const data = await getBookmarkedUniversities(session.user.id, session.access_token)
      setBookmarks(data as BookmarkWithUniversity[])
    } catch (e: any) {
      setError(e?.message ?? 'Gagal memuat data')
    } finally {
      setLoading(false)
    }
  }, [session?.user?.id])

  // Reload whenever screen comes into focus (e.g. after bookmarking from detail)
  useFocusEffect(useCallback(() => { load() }, [load]))

  const handleRemove = (item: BookmarkWithUniversity) => {
    Alert.alert(
      'Hapus Favorit',
      `Hapus ${item.universities?.name ?? 'kampus ini'} dari favorit?`,
      [
        { text: 'Batal', style: 'cancel' },
        {
          text: 'Hapus', style: 'destructive',
          onPress: async () => {
            hapticWarning()
            try {
              await deleteUniversityBookmark(session!.user.id, item.university_id, session!.access_token)
              setBookmarks(prev => prev.filter(b => b.university_id !== item.university_id))
            } catch (e: any) {
              Alert.alert('Gagal', e?.message ?? 'Coba lagi.')
            }
          },
        },
      ]
    )
  }

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={22} color={colors.text} />
        </Pressable>
        <Text style={styles.title}>Kampus Favorit</Text>
        <View style={{ width: 36 }} />
      </View>

      {loading ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : error ? (
        <View style={styles.centered}>
          <Ionicons name="alert-circle-outline" size={40} color={colors.error} />
          <Text style={styles.errorText}>{error}</Text>
          <Pressable style={styles.retryBtn} onPress={load}>
            <Text style={styles.retryText}>Coba Lagi</Text>
          </Pressable>
        </View>
      ) : bookmarks.length === 0 ? (
        <View style={styles.empty}>
          <View style={styles.emptyIcon}>
            <Ionicons name="business-outline" size={40} color="#10B981" />
          </View>
          <Text style={styles.emptyTitle}>Belum Ada Kampus Favorit</Text>
          <Text style={styles.emptyDesc}>
            Bookmark universitas dari halaman detail kampus atau hasil test RIASEC.
          </Text>
          <Pressable
            style={styles.exploreBtn}
            onPress={() => router.replace('/(tabs)/explore')}
          >
            <Ionicons name="search-outline" size={16} color={colors.white} />
            <Text style={styles.exploreBtnText}>Jelajahi Kampus</Text>
          </Pressable>
        </View>
      ) : (
        <FlatList
          data={bookmarks}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          ListHeaderComponent={
            <Text style={styles.listHeader}>{bookmarks.length} kampus tersimpan</Text>
          }
          renderItem={({ item }) => {
            const uni = item.universities
            if (!uni) return null
            const initial = uni.short_name?.[0]?.toUpperCase() ?? uni.name[0]?.toUpperCase() ?? '?'
            return (
              <Pressable
                style={({ pressed }) => [styles.card, pressed && { opacity: 0.88 }]}
                onPress={() => router.push(`/university-detail?id=${uni.id}`)}
              >
                {/* Logo */}
                <View style={styles.cardLogo}>
                  <Text style={styles.cardLogoText}>{initial}</Text>
                </View>

                {/* Info */}
                <View style={styles.cardBody}>
                  <View style={styles.cardNameRow}>
                    <Text style={styles.cardName} numberOfLines={1}>{uni.name}</Text>
                    {uni.is_partner && (
                      <View style={[
                        styles.partnerBadge,
                        uni.partner_tier === 'premium' && styles.partnerBadgePremium,
                      ]}>
                        <Ionicons
                          name={uni.partner_tier === 'premium' ? 'star' : 'checkmark-circle'}
                          size={9}
                          color={uni.partner_tier === 'premium' ? '#F59E0B' : colors.primary}
                        />
                        <Text style={[
                          styles.partnerText,
                          uni.partner_tier === 'premium' && styles.partnerTextPremium,
                        ]}>
                          {uni.partner_tier === 'premium' ? 'Premium' : 'Partner'}
                        </Text>
                      </View>
                    )}
                  </View>
                  <Text style={styles.cardShort}>{uni.short_name}</Text>
                  <View style={styles.cardMeta}>
                    <View style={[
                      styles.typeBadge,
                      uni.type === 'negeri' ? styles.typeBadgeNegeri : styles.typeBadgeSwasta,
                    ]}>
                      <Text style={[
                        styles.typeText,
                        uni.type === 'negeri' ? styles.typeTextNegeri : styles.typeTextSwasta,
                      ]}>
                        PT{uni.type === 'negeri' ? 'N' : 'S'}
                      </Text>
                    </View>
                    <Ionicons name="location-outline" size={11} color={colors.textMuted} />
                    <Text style={styles.cardCity} numberOfLines={1}>{uni.city}</Text>
                  </View>
                </View>

                {/* Remove bookmark */}
                <Pressable
                  style={styles.removeBtn}
                  onPress={() => handleRemove(item)}
                  hitSlop={8}
                >
                  <Ionicons name="bookmark" size={18} color={colors.primary} />
                </Pressable>
              </Pressable>
            )
          }}
        />
      )}
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  centered: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 16, padding: 24 },

  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 20, paddingTop: 4, paddingBottom: 12,
    borderBottomWidth: 1, borderBottomColor: colors.border,
  },
  backBtn: { width: 36, height: 36, borderRadius: 10, backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: 20, fontFamily: fonts.bold, color: colors.text },

  listContent: { padding: 20, paddingBottom: 48, gap: 10 },
  listHeader: { fontSize: 13, fontFamily: fonts.medium, color: colors.textMuted, marginBottom: 4 },

  card: {
    flexDirection: 'row', alignItems: 'center', gap: 14,
    backgroundColor: colors.surface, borderRadius: 14, padding: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
  },
  cardLogo: {
    width: 52, height: 52, borderRadius: 12,
    backgroundColor: colors.primaryLight, alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  cardLogoText: { fontSize: 22, fontFamily: fonts.extraBold, color: colors.primary },
  cardBody: { flex: 1, gap: 3 },
  cardNameRow: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  cardName: { flex: 1, fontSize: 14, fontFamily: fonts.bold, color: colors.text },
  cardShort: { fontSize: 12, fontFamily: fonts.medium, color: colors.textSub },
  cardMeta: { flexDirection: 'row', alignItems: 'center', gap: 5, marginTop: 2 },
  cardCity: { flex: 1, fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },

  typeBadge: { paddingHorizontal: 7, paddingVertical: 2, borderRadius: 5 },
  typeBadgeNegeri: { backgroundColor: '#EEF2FF' },
  typeBadgeSwasta: { backgroundColor: '#FFF3CD' },
  typeText: { fontSize: 10, fontFamily: fonts.bold },
  typeTextNegeri: { color: colors.primary },
  typeTextSwasta: { color: '#D97706' },

  partnerBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, paddingHorizontal: 7, paddingVertical: 3, borderRadius: 6, backgroundColor: colors.primaryMuted },
  partnerBadgePremium: { backgroundColor: '#FFFBEB' },
  partnerText: { fontSize: 10, fontFamily: fonts.bold, color: colors.primary },
  partnerTextPremium: { color: '#F59E0B' },

  removeBtn: { padding: 6 },

  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32, gap: 12 },
  emptyIcon: { width: 80, height: 80, borderRadius: 20, backgroundColor: '#ECFDF5', alignItems: 'center', justifyContent: 'center', marginBottom: 4 },
  emptyTitle: { fontSize: 18, fontFamily: fonts.bold, color: colors.text },
  emptyDesc: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub, textAlign: 'center', lineHeight: 22 },
  exploreBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    paddingHorizontal: 24, paddingVertical: 12,
    backgroundColor: '#10B981', borderRadius: 12, marginTop: 8,
  },
  exploreBtnText: { fontSize: 15, fontFamily: fonts.semiBold, color: colors.white },

  errorText: { fontSize: 15, fontFamily: fonts.medium, color: colors.textSub, textAlign: 'center' },
  retryBtn: { paddingHorizontal: 24, paddingVertical: 10, backgroundColor: colors.primaryLight, borderRadius: 10 },
  retryText: { fontSize: 14, fontFamily: fonts.semiBold, color: colors.primary },
})
