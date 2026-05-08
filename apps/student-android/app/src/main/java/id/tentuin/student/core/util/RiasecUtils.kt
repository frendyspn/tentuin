package id.tentuin.student.core.util

import id.tentuin.student.data.model.Question
import id.tentuin.student.data.model.RiasecScores
import kotlin.math.roundToInt

private val RIASEC_ORDER = listOf(
    "realistic", "investigative", "artistic", "social", "enterprising", "conventional"
)
private val RIASEC_LETTER = mapOf(
    "realistic"     to "R",
    "investigative" to "I",
    "artistic"      to "A",
    "social"        to "S",
    "enterprising"  to "E",
    "conventional"  to "C",
)

fun computeScores(questions: List<Question>, answers: Map<String, Int>): RiasecScores {
    val questionsPerCategory = 10
    val maxRaw = questionsPerCategory * 5

    val raw = mutableMapOf(
        "realistic"     to 0,
        "investigative" to 0,
        "artistic"      to 0,
        "social"        to 0,
        "enterprising"  to 0,
        "conventional"  to 0,
    )
    questions.forEach { q ->
        val rating = answers[q.id] ?: 0
        raw[q.category] = (raw[q.category] ?: 0) + rating
    }
    return RiasecScores(
        realistic     = ((raw["realistic"]!!     / maxRaw.toFloat()) * 100).roundToInt(),
        investigative = ((raw["investigative"]!! / maxRaw.toFloat()) * 100).roundToInt(),
        artistic      = ((raw["artistic"]!!      / maxRaw.toFloat()) * 100).roundToInt(),
        social        = ((raw["social"]!!        / maxRaw.toFloat()) * 100).roundToInt(),
        enterprising  = ((raw["enterprising"]!!  / maxRaw.toFloat()) * 100).roundToInt(),
        conventional  = ((raw["conventional"]!!  / maxRaw.toFloat()) * 100).roundToInt(),
    )
}

fun computeRiasecCode(scores: RiasecScores): String {
    return scores.toMap()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { RIASEC_ORDER.indexOf(it.key) }
        )
        .take(3)
        .joinToString("") { RIASEC_LETTER[it.key] ?: "" }
}

fun RiasecScores.toMap(): Map<String, Int> = mapOf(
    "realistic"     to realistic,
    "investigative" to investigative,
    "artistic"      to artistic,
    "social"        to social,
    "enterprising"  to enterprising,
    "conventional"  to conventional,
)

fun riasecTypeName(code: String): String = when (code) {
    "R" -> "Realistis"
    "I" -> "Investigatif"
    "A" -> "Artistik"
    "S" -> "Sosial"
    "E" -> "Enterprising"
    "C" -> "Konvensional"
    else -> code
}

fun riasecTypeDescription(code: String): String = when (code) {
    "R" -> "Kamu suka bekerja dengan tangan, alat, mesin, atau di luar ruangan. Kamu praktis, mekanis, dan suka hal-hal konkret."
    "I" -> "Kamu suka berpikir, menganalisis, dan memecahkan masalah. Kamu intelektual, penasaran, dan suka penelitian."
    "A" -> "Kamu kreatif dan ekspresif. Kamu suka seni, musik, menulis, atau desain. Kamu imajinatif dan orisinal."
    "S" -> "Kamu suka bekerja dengan orang lain. Kamu peduli, suka membantu, mengajar, atau merawat orang lain."
    "E" -> "Kamu suka memimpin, meyakinkan, dan memengaruhi orang lain. Kamu ambisius dan berorientasi pada tujuan."
    "C" -> "Kamu suka pekerjaan yang teratur, sistematis, dan detail. Kamu rapi, teliti, dan menyukai prosedur yang jelas."
    else -> ""
}
