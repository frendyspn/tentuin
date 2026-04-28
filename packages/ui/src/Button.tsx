import React from 'react'
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  type PressableProps,
  type StyleProp,
  type TextStyle,
  type ViewStyle,
} from 'react-native'
import { colors, fonts } from '@tentuin/config'

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'
type ButtonSize = 'sm' | 'md' | 'lg'

interface ButtonProps extends PressableProps {
  label: string
  variant?: ButtonVariant
  size?: ButtonSize
  isLoading?: boolean
  leftIcon?: React.ReactNode
  rightIcon?: React.ReactNode
  style?: StyleProp<ViewStyle>
  textStyle?: StyleProp<TextStyle>
  fullWidth?: boolean
}

export const Button: React.FC<ButtonProps> = ({
  label,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  leftIcon,
  rightIcon,
  style,
  textStyle,
  disabled,
  fullWidth = false,
  ...props
}) => {
  const isDisabled = disabled || isLoading

  return (
    <Pressable
      style={({ pressed }) => [
        styles.base,
        styles[`variant_${variant}`],
        styles[`size_${size}`],
        fullWidth && styles.fullWidth,
        pressed && !isDisabled && styles.pressed,
        isDisabled && styles.disabled,
        style,
      ]}
      disabled={isDisabled}
      {...props}
    >
      {isLoading ? (
        <ActivityIndicator
          size="small"
          color={variant === 'primary' || variant === 'secondary' ? colors.white : colors.primary}
        />
      ) : (
        <>
          {leftIcon}
          <Text style={[
            styles.text,
            styles[`text_${variant}`],
            styles[`textSize_${size}`],
            textStyle,
          ]}>
            {label}
          </Text>
          {rightIcon}
        </>
      )}
    </Pressable>
  )
}

const styles = StyleSheet.create({
  base: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 100,
    gap: 8,
    alignSelf: 'flex-start',
  },
  fullWidth: {
    alignSelf: 'stretch',
  },

  // ─── Variants ─────────────────────────────────────────────
  variant_primary: {
    backgroundColor: colors.primary,
    shadowColor: colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 12,
    elevation: 4,
  },
  variant_secondary: {
    backgroundColor: colors.secondary,
    shadowColor: colors.secondary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 12,
    elevation: 4,
  },
  variant_outline: {
    backgroundColor: colors.transparent,
    borderWidth: 1.5,
    borderColor: colors.primary,
  },
  variant_ghost: {
    backgroundColor: colors.transparent,
  },
  variant_danger: {
    backgroundColor: colors.errorLight,
  },

  // ─── Sizes ────────────────────────────────────────────────
  size_sm: { paddingHorizontal: 16, paddingVertical: 9,  minHeight: 36 },
  size_md: { paddingHorizontal: 24, paddingVertical: 14, minHeight: 48 },
  size_lg: { paddingHorizontal: 32, paddingVertical: 17, minHeight: 56 },

  // ─── States ───────────────────────────────────────────────
  pressed:  { opacity: 0.82, transform: [{ scale: 0.97 }] },
  disabled: { opacity: 0.45, shadowOpacity: 0 },

  // ─── Text Base ────────────────────────────────────────────
  text: {
    fontFamily: fonts.bold,
    letterSpacing: 0.1,
  },
  text_primary:   { color: colors.white },
  text_secondary: { color: colors.white },
  text_outline:   { color: colors.primary },
  text_ghost:     { color: colors.primary },
  text_danger:    { color: colors.error },

  textSize_sm: { fontSize: 13 },
  textSize_md: { fontSize: 15 },
  textSize_lg: { fontSize: 16 },
})
