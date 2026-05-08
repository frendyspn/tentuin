package id.tentuin.student.ui.component.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.core.app.ShareCompat
import java.io.File

/**
 * Menyimpan Bitmap ke cache directory dan membuka Android share sheet.
 * Menggunakan FileProvider agar URI dapat diakses oleh app lain.
 */
object ShareHelper {

    /**
     * Share gambar + teks pendek lewat Android share sheet.
     *
     * @param context  Context (bisa dari Activity atau Composable via LocalContext)
     * @param bitmap   Bitmap yang akan dibagikan (hasil dari ShareCardGenerator)
     * @param text     Teks pendek yang ikut dibagikan (caption / caption story)
     */
    fun shareImage(context: Context, bitmap: Bitmap, text: String) {
        // 1. Simpan bitmap ke cache/share/tentuin_result.png
        val shareDir = File(context.cacheDir, "share").also { it.mkdirs() }
        val imageFile = File(shareDir, "tentuin_result.png")

        imageFile.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // 2. Buat URI via FileProvider (aman untuk dibagikan ke app lain)
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        // 3. Buka Android share sheet
        val shareIntent = ShareCompat.IntentBuilder(context)
            .setType("image/png")
            .addStream(imageUri)
            .setText(text)
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(shareIntent)
    }
}
