import React, { useState } from 'react'
import {
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
  type StyleProp,
  type TextInputProps,
  type ViewStyle,
} from 'react-native'
import { Ionicons } from '@expo/vector-icons'
import { colors, fonts } from '@tentuin/config'

interface InputProps extends TextInputProps {
  label?: string
  error?: string
  hint?: string
  leftIcon?: React.ReactNode
  rightIcon?: React.ReactNode
  containerStyle?: StyleProp<ViewStyle>
  isPassword?: boolean
}

export const Input: React.FC<InputProps> = ({
  label,
  error,
  hint,
  leftIcon,
  rightIcon,
  containerStyle,
  isPassword = false,
  secureTextEntry,
  ...props
}) => {
  const [isFocused, setIsFocused] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const isSecure = isPassword ? !showPassword : secureTextEntry

  return (
    <View style={[styles.container, containerStyle]}>
      {label && <Text style={styles.label}>{label}</Text>}

      <View style={[
        styles.wrapper,
        isFocused && styles.wrapperFocused,
        !!error && styles.wrapperError,
      ]}>
        {leftIcon && <View style={styles.iconLeft}>{leftIcon}</View>}

        <TextInput
          style={[
            styles.input,
            leftIcon && styles.inputPaddingLeft,
            (rightIcon || isPassword) && styles.inputPaddingRight,
          ]}
          placeholderTextColor={colors.textDisabled}
          secureTextEntry={isSecure}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          autoCapitalize="none"
          autoCorrect={false}
          {...props}
        />

        {isPassword && (
          <Pressable style={styles.iconRight} onPress={() => setShowPassword(v => !v)} hitSlop={8}>
            <Ionicons
              name={showPassword ? 'eye-off-outline' : 'eye-outline'}
              size={20}
              color={colors.textMuted}
            />
          </Pressable>
        )}
        {rightIcon && !isPassword && (
          <View style={styles.iconRight}>{rightIcon}</View>
        )}
      </View>

      {error && (
        <View style={styles.feedbackRow}>
          <Ionicons name="alert-circle-outline" size={13} color={colors.error} />
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}
      {hint && !error && <Text style={styles.hint}>{hint}</Text>}
    </View>
  )
}

const styles = StyleSheet.create({
  container: { gap: 8 },

  label: {
    fontSize: 13,
    fontFamily: fonts.semiBold,
    color: colors.text,
    letterSpacing: 0.1,
  },

  wrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.gray[50],
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: colors.border,
    minHeight: 56,
  },
  wrapperFocused: {
    borderColor: colors.primary,
    backgroundColor: colors.primaryMuted,
  },
  wrapperError: {
    borderColor: colors.error,
    backgroundColor: colors.errorLight,
  },

  input: {
    flex: 1,
    paddingHorizontal: 18,
    paddingVertical: 16,
    fontSize: 15,
    fontFamily: fonts.regular,
    color: colors.text,
  },
  inputPaddingLeft:  { paddingLeft: 8 },
  inputPaddingRight: { paddingRight: 8 },

  iconLeft:  { paddingLeft: 16 },
  iconRight: { paddingRight: 16 },

  feedbackRow: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  errorText: { fontSize: 12, fontFamily: fonts.medium, color: colors.error },
  hint:      { fontSize: 12, fontFamily: fonts.regular, color: colors.textMuted },
})
