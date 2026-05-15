package id.tentuin.agent.core.util

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

/** ISO-8601 UTC timestamp seperti "2026-03-06T11:29:10.123Z" — diterima Postgres timestamptz. */
fun nowIso(): String = ISO_FMT.format(Date())

private fun parseSupabaseTs(iso: String?): Date? {
    if (iso.isNullOrBlank()) return null
    val cleaned = iso.replace(Regex("\\.(\\d{3})\\d*"), ".$1") // truncate µs → ms
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

/** Format ISO timestamp Supabase → "5 Apr 2026". Fallback ke string asli kalau gagal. */
fun formatDateId(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    val date = parseSupabaseTs(iso) ?: return iso
    return SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(date)
}

/** Sisa hari hingga tanggal target (dari sekarang). null kalau parse gagal. */
fun daysUntil(iso: String?): Long? {
    val date = parseSupabaseTs(iso) ?: return null
    val diff = date.time - System.currentTimeMillis()
    return if (diff <= 0) 0L else diff / (1000L * 60 * 60 * 24)
}
