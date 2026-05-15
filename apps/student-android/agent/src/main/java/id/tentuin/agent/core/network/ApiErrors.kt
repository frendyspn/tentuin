package id.tentuin.agent.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private data class SupabaseError(
    val error:              String? = null,
    @Suppress("PropertyName")
    val error_description:  String? = null,
    val msg:                String? = null,
    val message:            String? = null,
    val code:               String? = null,
    val details:            String? = null,
    val hint:               String? = null,
)

private fun parseSupabaseError(t: HttpException): SupabaseError? = runCatching {
    val body = t.response()?.errorBody()?.string() ?: return@runCatching null
    Gson().fromJson(body, SupabaseError::class.java)
}.getOrNull()

private fun describe(e: SupabaseError?): String =
    (e?.error_description ?: e?.msg ?: e?.message ?: "").lowercase()

/** Pesan ramah untuk error login/register/auth flow. */
fun friendlyAuthError(t: Throwable): String = when (t) {
    is HttpException -> {
        val parsed = parseSupabaseError(t)
        val desc = describe(parsed)
        when {
            t.code() == 400 && (desc.contains("invalid login credentials") ||
                                desc.contains("invalid grant") ||
                                parsed?.error == "invalid_grant") ->
                "Email atau password salah."

            t.code() == 400 && desc.contains("email not confirmed") ->
                "Email belum diverifikasi."

            t.code() == 400 && desc.contains("password") ->
                "Password minimal 6 karakter."

            t.code() == 400 && desc.contains("email") ->
                "Format email tidak valid."

            t.code() == 401 ->
                "Akun tidak ditemukan atau sesi berakhir. Silakan login ulang."

            t.code() == 422 && (desc.contains("user already registered") ||
                                desc.contains("already registered") ||
                                desc.contains("duplicate")) ->
                "Email sudah terdaftar. Silakan masuk atau gunakan email lain."

            t.code() == 422 ->
                "Data tidak valid: ${parsed?.error_description ?: parsed?.msg ?: "periksa input"}"

            t.code() == 429 ->
                "Terlalu banyak percobaan. Coba lagi beberapa menit."

            t.code() in 500..599 ->
                "Server sedang bermasalah. Coba lagi beberapa saat."

            else ->
                parsed?.error_description ?: parsed?.msg ?: parsed?.message ?: "Gagal memproses (HTTP ${t.code()})."
        }
    }

    is UnknownHostException -> "Tidak ada koneksi internet."
    is SocketTimeoutException -> "Koneksi timeout. Coba lagi."
    is IOException -> "Tidak dapat terhubung ke server. Periksa internet."
    else -> t.message ?: "Terjadi kesalahan."
}

/** Pesan ramah untuk error API biasa (REST PostgREST). */
fun friendlyApiError(t: Throwable, default: String = "Gagal memuat data."): String = when (t) {
    is HttpException -> {
        val parsed = parseSupabaseError(t)
        val pgCode = parsed?.code  // 23503, 23505, dst.
        val msg    = parsed?.message.orEmpty().lowercase()
        val det    = parsed?.details.orEmpty().lowercase()

        when {
            // Foreign key violation (parent row missing)
            pgCode == "23503" && (msg.contains("agent") || det.contains("agents")) ->
                "Akun agen Anda belum terdaftar di sistem. Coba logout lalu login ulang."

            pgCode == "23503" ->
                "Data referensi tidak ditemukan: ${parsed?.details ?: parsed?.message ?: "cek data"}"

            // Unique constraint violation
            pgCode == "23505" -> "Data sudah ada / sudah diklaim."

            // Check constraint violation
            pgCode == "23514" -> "Nilai data tidak valid: ${parsed?.message ?: "cek input"}"

            t.code() == 401 -> "Sesi Anda berakhir. Silakan login ulang."
            t.code() == 403 -> parsed?.message ?: "Anda tidak punya akses untuk operasi ini."
            t.code() == 404 -> "Data tidak ditemukan."
            t.code() == 409 -> parsed?.message ?: "Data sudah ada atau bentrok."
            t.code() == 429 -> "Terlalu banyak permintaan. Tunggu sebentar."
            t.code() in 500..599 -> "Server bermasalah. Coba lagi beberapa saat."
            else -> parsed?.message ?: parsed?.error_description ?: "$default (HTTP ${t.code()})"
        }
    }
    is UnknownHostException -> "Tidak ada koneksi internet."
    is SocketTimeoutException -> "Koneksi timeout. Coba lagi."
    is IOException -> "Tidak dapat terhubung ke server."
    else -> t.message ?: default
}
