package id.tentuin.student.ui.component.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import id.tentuin.student.core.util.riasecTypeDescription
import id.tentuin.student.core.util.riasecTypeName
import id.tentuin.student.data.model.RiasecScores

/**
 * Menghasilkan share card RIASEC sebagai Bitmap 1080×1920 (rasio 9:16)
 * menggunakan Android Canvas API. Cocok untuk dibagikan ke IG Story, WA, TikTok.
 *
 * Dipanggil di background thread (Dispatchers.Default) karena operasi CPU-intensive.
 */
object ShareCardGenerator {

    private const val WIDTH  = 1080
    private const val HEIGHT = 1920
    private const val PAD    = 80f   // horizontal padding

    // Brand colors (sesuai Color.kt)
    private val COLOR_PRIMARY   = Color.parseColor("#6C63FF")
    private val COLOR_PRIMARY_LIGHT = Color.parseColor("#F3F2FF")
    private val COLOR_TEXT_PRIMARY  = Color.parseColor("#111827")
    private val COLOR_TEXT_SUB      = Color.parseColor("#374151")
    private val COLOR_TEXT_MUTED    = Color.parseColor("#9CA3AF")
    private val COLOR_BORDER        = Color.parseColor("#E5E7EB")
    private val COLOR_SURFACE       = Color.WHITE

    // RIASEC colors
    private fun riasecColor(code: String): Int = when (code.uppercase()) {
        "R" -> Color.parseColor("#F97316")  // orange
        "I" -> Color.parseColor("#3B82F6")  // blue
        "A" -> Color.parseColor("#EC4899")  // pink
        "S" -> Color.parseColor("#10B981")  // green
        "E" -> Color.parseColor("#F59E0B")  // amber
        "C" -> Color.parseColor("#5C59F8")  // indigo
        else -> COLOR_PRIMARY
    }

    fun generate(context: Context, riasecCode: String, scores: RiasecScores): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint  = Paint(Paint.ANTI_ALIAS_FLAG)

        drawBackground(canvas, paint)
        drawDecorations(canvas, paint)
        drawBrand(canvas, paint)
        drawRiasecSection(canvas, paint, riasecCode)
        drawDivider(canvas, paint, 960f)
        drawScoreSection(canvas, paint, scores, riasecCode)
        drawFooter(canvas, paint)

        return bitmap
    }

    // ── Background ────────────────────────────────────────────────────────

    private fun drawBackground(canvas: Canvas, paint: Paint) {
        val gradient = LinearGradient(
            0f, 0f, 0f, HEIGHT.toFloat(),
            COLOR_SURFACE, COLOR_PRIMARY_LIGHT,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
        paint.shader = null
    }

    // ── Dekorasi lingkaran samar ───────────────────────────────────────────

    private fun drawDecorations(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL

        // Lingkaran besar kanan atas
        paint.color = Color.argb(20, Color.red(COLOR_PRIMARY), Color.green(COLOR_PRIMARY), Color.blue(COLOR_PRIMARY))
        canvas.drawCircle(WIDTH.toFloat(), 0f, 420f, paint)

        // Lingkaran kecil kiri bawah
        paint.color = Color.argb(15, Color.red(COLOR_PRIMARY), Color.green(COLOR_PRIMARY), Color.blue(COLOR_PRIMARY))
        canvas.drawCircle(0f, HEIGHT.toFloat(), 280f, paint)

        // Lingkaran sedang tengah kanan bawah
        paint.color = Color.argb(10, Color.red(COLOR_PRIMARY), Color.green(COLOR_PRIMARY), Color.blue(COLOR_PRIMARY))
        canvas.drawCircle(WIDTH * 0.85f, HEIGHT * 0.6f, 180f, paint)
    }

    // ── Brand Header ──────────────────────────────────────────────────────

    private fun drawBrand(canvas: Canvas, paint: Paint) {
        // Brand name "tentuin"
        paint.style      = Paint.Style.FILL
        paint.color      = COLOR_PRIMARY
        paint.typeface   = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize   = 96f
        paint.textAlign  = Paint.Align.LEFT
        canvas.drawText("tentuin", PAD, 200f, paint)

        // Tagline
        paint.color    = COLOR_TEXT_MUTED
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 36f
        canvas.drawText("Kenali Dirimu, Tentukan Jalanmu ✦", PAD, 270f, paint)

        // Garis bawah header
        drawDivider(canvas, paint, 330f)
    }

    // ── Seksi RIASEC Code + Nama + Deskripsi ─────────────────────────────

    private fun drawRiasecSection(canvas: Canvas, paint: Paint, riasecCode: String) {
        val codes = riasecCode.map { it.toString() }

        // Label "Tipe Kepribadian Dominanmu"
        paint.color    = COLOR_TEXT_MUTED
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 36f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Tipe Kepribadian Dominanmu", WIDTH / 2f, 410f, paint)

        // 3 chip besar RIASEC — centered
        val chipSize = 196f
        val chipGap  = 28f
        val totalChipWidth = codes.size * chipSize + (codes.size - 1) * chipGap
        val chipStartX = (WIDTH - totalChipWidth) / 2f
        val chipStartY = 450f

        codes.forEachIndexed { index, code ->
            val cx = chipStartX + index * (chipSize + chipGap)
            drawBigRiasecChip(canvas, paint, code, cx, chipStartY, chipSize)
        }

        // Nama-nama tipe kepribadian (bergabung dengan "·")
        val typeNames = codes.joinToString("  ·  ") { riasecTypeName(it) }
        paint.color    = COLOR_TEXT_SUB
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 38f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(typeNames, WIDTH / 2f, 790f, paint)

        // Deskripsi singkat tipe dominan (maks 100 karakter)
        val dominantCode = codes.firstOrNull() ?: "R"
        val fullDesc = riasecTypeDescription(dominantCode)
        val shortDesc = if (fullDesc.length > 105) "${fullDesc.take(105)}..." else fullDesc

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = COLOR_TEXT_MUTED
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
        }
        val contentWidth = (WIDTH - PAD * 2).toInt()
        val layout = StaticLayout.Builder
            .obtain(shortDesc, 0, shortDesc.length, textPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(6f, 1f)
            .build()

        canvas.save()
        canvas.translate(PAD, 840f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawBigRiasecChip(canvas: Canvas, paint: Paint, code: String, x: Float, y: Float, size: Float) {
        val color  = riasecColor(code)
        val radius = 40f

        // Background chip (warna tipe, transparan)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(35, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(RectF(x, y, x + size, y + size), radius, radius, paint)

        // Border chip
        paint.style       = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color       = Color.argb(160, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(RectF(x, y, x + size, y + size), radius, radius, paint)

        // Huruf besar di tengah chip
        paint.style     = Paint.Style.FILL
        paint.color     = color
        paint.typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize  = size * 0.52f
        paint.textAlign = Paint.Align.CENTER
        val textY = y + size / 2 - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(code.uppercase(), x + size / 2, textY, paint)
    }

    // ── Seksi Score Bars ──────────────────────────────────────────────────

    private fun drawScoreSection(canvas: Canvas, paint: Paint, scores: RiasecScores, riasecCode: String) {
        // Judul seksi
        paint.style     = Paint.Style.FILL
        paint.color     = COLOR_TEXT_PRIMARY
        paint.typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize  = 44f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Skor RIASEC", PAD, 1030f, paint)

        // 6 bar rows
        val scoreEntries = listOf(
            Triple("R", "Realistis",    scores.realistic),
            Triple("I", "Investigatif", scores.investigative),
            Triple("A", "Artistik",     scores.artistic),
            Triple("S", "Sosial",       scores.social),
            Triple("E", "Enterprising", scores.enterprising),
            Triple("C", "Konvensional", scores.conventional),
        )
        val dominantCodes = riasecCode.map { it.toString() }.toSet()
        val rowHeight = 108f
        val startY    = 1080f

        scoreEntries.forEachIndexed { index, (code, _, score) ->
            val rowY = startY + index * rowHeight
            val isDominant = code in dominantCodes
            drawScoreRow(canvas, paint, code, score, rowY, isDominant)
        }
    }

    private fun drawScoreRow(canvas: Canvas, paint: Paint, code: String, score: Int, y: Float, isDominant: Boolean) {
        val color      = riasecColor(code)
        val chipSize   = 72f
        val chipRadius = 20f
        val barStartX  = PAD + chipSize + 24f
        val barEndX    = WIDTH - PAD - 120f
        val barWidth   = barEndX - barStartX
        val barHeight  = 36f
        val barY       = y + (chipSize - barHeight) / 2

        // Small chip
        paint.style = Paint.Style.FILL
        val chipAlpha = if (isDominant) 50 else 25
        paint.color = Color.argb(chipAlpha, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(RectF(PAD, y, PAD + chipSize, y + chipSize), chipRadius, chipRadius, paint)

        paint.style       = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color       = Color.argb(if (isDominant) 200 else 100, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(RectF(PAD, y, PAD + chipSize, y + chipSize), chipRadius, chipRadius, paint)

        paint.style     = Paint.Style.FILL
        paint.color     = color
        paint.typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize  = if (isDominant) 38f else 34f
        paint.textAlign = Paint.Align.CENTER
        val textY = y + chipSize / 2 - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(code, PAD + chipSize / 2, textY, paint)

        // Bar background
        paint.color = Color.argb(20, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(RectF(barStartX, barY, barEndX, barY + barHeight), barHeight / 2, barHeight / 2, paint)

        // Bar fill
        val fillWidth = (barWidth * score / 100f).coerceAtLeast(barHeight) // min fill untuk visibility
        val fillColor = if (isDominant) color else Color.argb(160, Color.red(color), Color.green(color), Color.blue(color))
        paint.color = fillColor
        canvas.drawRoundRect(RectF(barStartX, barY, barStartX + fillWidth, barY + barHeight), barHeight / 2, barHeight / 2, paint)

        // Score percentage text
        paint.color     = if (isDominant) COLOR_TEXT_PRIMARY else COLOR_TEXT_MUTED
        paint.typeface  = Typeface.create(Typeface.SANS_SERIF, if (isDominant) Typeface.BOLD else Typeface.NORMAL)
        paint.textSize  = if (isDominant) 36f else 32f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$score%", WIDTH - PAD, textY, paint)
    }

    // ── Footer ────────────────────────────────────────────────────────────

    private fun drawFooter(canvas: Canvas, paint: Paint) {
        drawDivider(canvas, paint, 1752f)

        paint.style     = Paint.Style.FILL
        paint.color     = COLOR_TEXT_MUTED
        paint.typeface  = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize  = 34f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Temukan jurusanmu di Tentuin!", PAD, 1810f, paint)

        paint.color    = COLOR_PRIMARY
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 50f
        canvas.drawText("tentuin.id/tes  →", PAD, 1876f, paint)
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private fun drawDivider(canvas: Canvas, paint: Paint, y: Float) {
        paint.style       = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color       = COLOR_BORDER
        canvas.drawLine(PAD, y, WIDTH - PAD, y, paint)
        paint.style = Paint.Style.FILL
    }
}
