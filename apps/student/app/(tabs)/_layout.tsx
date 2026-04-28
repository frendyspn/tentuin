import { Tabs } from 'expo-router'
import { StyleSheet, Text, View } from 'react-native'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'

interface TabIconProps {
  name: keyof typeof Ionicons.glyphMap
  outlineName: keyof typeof Ionicons.glyphMap
  label: string
  focused: boolean
}

function TabIcon({ name, outlineName, label, focused }: TabIconProps) {
  return (
    <View style={[styles.tabItem, focused && styles.tabItemActive]}>
      <Ionicons
        name={focused ? name : outlineName}
        size={22}
        color={focused ? colors.primary : colors.textMuted}
      />
      <Text style={[styles.label, focused && styles.labelActive]} numberOfLines={1}>{label}</Text>
    </View>
  )
}

export default function TabsLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: styles.tabBar,
        tabBarShowLabel: false,
      }}
    >
      <Tabs.Screen
        name="home/index"
        options={{
          tabBarIcon: ({ focused }) => (
            <TabIcon name="home" outlineName="home-outline" label="Beranda" focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="explore/index"
        options={{
          tabBarIcon: ({ focused }) => (
            <TabIcon name="search" outlineName="search-outline" label="Jelajah" focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="test/index"
        options={{
          tabBarIcon: ({ focused }) => (
            <TabIcon name="clipboard" outlineName="clipboard-outline" label="Test" focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="profile/index"
        options={{
          tabBarIcon: ({ focused }) => (
            <TabIcon name="person" outlineName="person-outline" label="Profil" focused={focused} />
          ),
        }}
      />
    </Tabs>
  )
}

const styles = StyleSheet.create({
  tabBar: {
    backgroundColor: colors.surface,
    borderTopWidth: 0,
    height: 88,
    paddingBottom: 22,
    paddingTop: 8,
    paddingHorizontal: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.06,
    shadowRadius: 20,
    elevation: 20,
  },
  tabItem: {
    alignItems: 'center',
    gap: 3,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 10,
    minWidth: 72,
    maxWidth: 90,
  },
  tabItemActive: {
    backgroundColor: colors.primaryMuted,
  },
  label: {
    fontSize: 10,
    fontFamily: fonts.medium,
    color: colors.textMuted,
  },
  labelActive: {
    color: colors.primary,
    fontFamily: fonts.bold,
  },
})
