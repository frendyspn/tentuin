package id.tentuin.agent.core.util

import java.text.NumberFormat
import java.util.Locale

fun Int.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(this).replace("Rp", "Rp ").replace(",00", "")
}
