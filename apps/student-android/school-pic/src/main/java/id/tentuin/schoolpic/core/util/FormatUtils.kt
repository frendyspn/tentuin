package id.tentuin.schoolpic.core.util

import java.text.NumberFormat
import java.util.Locale

fun Long.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}

fun Int.toRupiah(): String = this.toLong().toRupiah()

private val MONTHS_ID = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
    "Jul", "Agu", "Sep", "Okt", "Nov", "Des",
)

fun monthShortId(month: Int): String =
    if (month in 1..12) MONTHS_ID[month - 1] else month.toString()

fun monthYearId(month: Int, year: Int): String = "${monthShortId(month)} $year"
