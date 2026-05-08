package id.tentuin.student.ui.theme

import androidx.compose.ui.graphics.Color

val Primary      = Color(0xFF6C63FF)
val PrimaryLight = Color(0xFFEEEDFF)
val PrimaryDark  = Color(0xFF4F46E5)

val Background   = Color(0xFFF7F8FA)
val Surface      = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF3F4F6)

val TextPrimary  = Color(0xFF111827)
val TextSub      = Color(0xFF374151)
val TextMuted    = Color(0xFF9CA3AF)

val Border       = Color(0xFFE5E7EB)
val Success      = Color(0xFF10B981)
val Error        = Color(0xFFEF4444)
val Warning      = Color(0xFFF59E0B)

// RIASEC colors
val RiasecR      = Color(0xFFF97316) // Realistic — orange
val RiasecI      = Color(0xFF3B82F6) // Investigative — blue
val RiasecA      = Color(0xFFEC4899) // Artistic — pink
val RiasecS      = Color(0xFF10B981) // Social — green
val RiasecE      = Color(0xFFF59E0B) // Enterprising — amber
val RiasecC      = Color(0xFF5C59F8) // Conventional — indigo

// Partner tiers
val PartnerGold  = Color(0xFFF59E0B)
val PartnerBlue  = Color(0xFF6C63FF)

fun riasecColor(code: String) = when (code.uppercase()) {
    "R" -> RiasecR
    "I" -> RiasecI
    "A" -> RiasecA
    "S" -> RiasecS
    "E" -> RiasecE
    "C" -> RiasecC
    else -> Primary
}
