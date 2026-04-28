import React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { SafeAreaView } from 'react-native-safe-area-context'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'

export default function FavoritesMajorsScreen() {
  const router = useRouter()
  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <View style={styles.header}>
        <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={22} color={colors.text} />
        </Pressable>
        <Text style={styles.title}>Jurusan Favorit</Text>
        <View style={{ width: 36 }} />
      </View>
      <View style={styles.empty}>
        <View style={styles.emptyIcon}>
          <Ionicons name="heart-outline" size={40} color="#EC4899" />
        </View>
        <Text style={styles.emptyTitle}>Belum Ada Jurusan Favorit</Text>
        <Text style={styles.emptyDesc}>
          Fitur ini segera hadir. Kamu akan bisa menyimpan jurusan yang menarik minatmu.
        </Text>
      </View>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: 20, paddingTop: 4, paddingBottom: 12,
    borderBottomWidth: 1, borderBottomColor: colors.border,
  },
  backBtn: { width: 36, height: 36, borderRadius: 10, backgroundColor: colors.surface, alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: 20, fontFamily: fonts.bold, color: colors.text },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32, gap: 12 },
  emptyIcon: { width: 80, height: 80, borderRadius: 20, backgroundColor: '#FFF0F6', alignItems: 'center', justifyContent: 'center', marginBottom: 4 },
  emptyTitle: { fontSize: 18, fontFamily: fonts.bold, color: colors.text },
  emptyDesc: { fontSize: 14, fontFamily: fonts.regular, color: colors.textSub, textAlign: 'center', lineHeight: 22 },
})
