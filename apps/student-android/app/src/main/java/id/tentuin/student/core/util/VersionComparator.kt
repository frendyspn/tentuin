package id.tentuin.student.core.util

object VersionComparator {
    fun isLessThan(a: String, b: String): Boolean {
        val partsA = a.trim().split(".").map { it.toIntOrNull() ?: 0 }
        val partsB = b.trim().split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(partsA.size, partsB.size)) {
            val x = partsA.getOrElse(i) { 0 }
            val y = partsB.getOrElse(i) { 0 }
            if (x < y) return true
            if (x > y) return false
        }
        return false
    }
}
