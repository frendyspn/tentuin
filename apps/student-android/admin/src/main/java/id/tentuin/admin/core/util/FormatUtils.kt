package id.tentuin.admin.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun Int.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

fun Long.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val displayFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID"))

fun String?.toDisplayDate(): String {
    if (this.isNullOrBlank()) return "-"
    return try {
        val cleaned = substringBefore('.').substringBefore('+').substringBefore('Z')
        val date = isoParser.parse(cleaned) ?: return "-"
        displayFormat.format(date)
    } catch (e: Exception) {
        "-"
    }
}

fun String?.toRelativeTime(): String {
    if (this.isNullOrBlank()) return "-"
    return try {
        val cleaned = substringBefore('.').substringBefore('+').substringBefore('Z')
        val date = isoParser.parse(cleaned) ?: return "-"
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "baru saja"
            diff < TimeUnit.HOURS.toMillis(1)   -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} menit lalu"
            diff < TimeUnit.DAYS.toMillis(1)    -> "${TimeUnit.MILLISECONDS.toHours(diff)} jam lalu"
            diff < TimeUnit.DAYS.toMillis(7)    -> "${TimeUnit.MILLISECONDS.toDays(diff)} hari lalu"
            else                                 -> displayFormat.format(date)
        }
    } catch (e: Exception) {
        "-"
    }
}
