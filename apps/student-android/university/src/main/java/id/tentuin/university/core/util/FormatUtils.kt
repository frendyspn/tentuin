package id.tentuin.university.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Int.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

private val ISO_FMT: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

fun nowIso(): String = ISO_FMT.format(Date())

private fun parseSupabaseTs(iso: String?): Date? {
    if (iso.isNullOrBlank()) return null
    val cleaned = iso.replace(Regex("\\.(\\d{3})\\d*"), ".$1")
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    )
    for (p in patterns) {
        runCatching {
            SimpleDateFormat(p, Locale.US).parse(cleaned)
        }.getOrNull()?.let { return it }
    }
    return null
}

fun formatDateId(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    val date = parseSupabaseTs(iso) ?: return iso
    return SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(date)
}

fun formatDateTimeId(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    val date = parseSupabaseTs(iso) ?: return iso
    return SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID")).format(date)
}

fun relativeTime(iso: String?): String {
    val date = parseSupabaseTs(iso) ?: return "-"
    val diffMs = System.currentTimeMillis() - date.time
    val mins = diffMs / 60_000
    return when {
        mins < 1 -> "baru saja"
        mins < 60 -> "$mins menit lalu"
        mins < 1440 -> "${mins / 60} jam lalu"
        mins < 10080 -> "${mins / 1440} hari lalu"
        else -> formatDateId(iso)
    }
}
