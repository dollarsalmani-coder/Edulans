package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.ObjectAnalysisResult
import com.example.model.StudyLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

enum class ShareExportOption {
    FULL_PACKAGE,
    SOLUTION_IMAGE,
    TEXT_NOTES
}

object StudyShareHelper {

    private const val SHARED_EXPORTS_DIR = "shared_exports"
    private const val MAX_CACHE_AGE_MS = 60 * 60 * 1000L

    fun formatEducationalText(
        study: ObjectAnalysisResult,
        language: StudyLanguage
    ): String = buildString {
        appendLine("📘 ${study.objectTitle} (${study.scientificOrTechnicalName})")
        appendLine("Category: ${study.category} · Language: ${language.nativeName} (${language.englishName})")
        appendLine()
        appendLine("Summary:")
        appendLine(study.summary)

        if (study.structures.isNotEmpty()) {
            appendLine()
            appendLine("🧩 Structural Breakdown:")
            study.structures.forEach { part ->
                appendLine("${part.layerOrder}. ${part.name} [${part.materialOrRole}]")
                appendLine("   ${part.functionDescription}")
            }
        }

        if (study.facts.isNotEmpty()) {
            appendLine()
            appendLine("💡 Educational Facts:")
            study.facts.forEachIndexed { index, fact ->
                appendLine("${index + 1}. [${fact.discipline}] ${fact.title}")
                appendLine("   ${fact.detail}")
            }
        }

        if (study.solutions.isNotEmpty()) {
            appendLine()
            appendLine("🛠️ Practical Solutions & Care:")
            study.solutions.forEachIndexed { index, sol ->
                appendLine("${index + 1}. ${sol.problemTitle}")
                appendLine("   Steps: ${sol.solutionSteps}")
                appendLine("   Care Tip: ${sol.careOrSustainabilityTip}")
            }
        }

        study.quiz?.let { quiz ->
            appendLine()
            appendLine("❓ Quick Check: ${quiz.question}")
            val answer = quiz.options.getOrNull(quiz.correctIndex)
            if (answer != null) {
                appendLine("Answer: $answer")
            }
            appendLine("Note: ${quiz.explanation}")
        }

        appendLine()
        append("Exported from EduLens")
    }.trim()

    fun formatCompactSummaryCaption(
        study: ObjectAnalysisResult,
        language: StudyLanguage
    ): String = buildString {
        appendLine("${study.objectTitle} (${study.scientificOrTechnicalName}) — ${language.nativeName}")
        appendLine(study.summary)
        study.solutions.firstOrNull()?.let { firstSol ->
            appendLine()
            appendLine("Solution · ${firstSol.problemTitle}: ${firstSol.solutionSteps}")
        }
        append("Shared via EduLens")
    }.trim()

    suspend fun prepareShareImageUris(
        context: Context,
        study: ObjectAnalysisResult,
        language: StudyLanguage,
        option: ShareExportOption
    ): List<Uri> = withContext(Dispatchers.IO) {
        if (option == ShareExportOption.TEXT_NOTES) {
            return@withContext emptyList()
        }

        val exportDir = File(context.cacheDir, SHARED_EXPORTS_DIR).apply { mkdirs() }
        pruneOldExports(exportDir)

        val safeSlug = study.objectTitle
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "study" }
            .take(24)
        val stamp = System.currentTimeMillis()
        val authority = "${context.packageName}.fileprovider"

        val uris = mutableListOf<Uri>()

        // Always render the Solution & Educational Facts Card PNG for image exports
        val cardFile = File(exportDir, "${safeSlug}_solution_card_$stamp.png")
        renderSolutionCardPng(context, study, language, cardFile)
        if (cardFile.exists() && cardFile.length() > 0L) {
            uris += FileProvider.getUriForFile(context, authority, cardFile)
        }

        // In FULL_PACKAGE mode, also include the original/sample object image
        if (option == ShareExportOption.FULL_PACKAGE) {
            val photoFile = File(exportDir, "${safeSlug}_photo_$stamp.jpg")
            if (exportStudyPhotoJpeg(context, study.imageSource, photoFile)) {
                uris += FileProvider.getUriForFile(context, authority, photoFile)
            }
        }

        uris
    }

    fun buildRawShareIntent(
        context: Context,
        study: ObjectAnalysisResult,
        language: StudyLanguage,
        imageUris: List<Uri>,
        option: ShareExportOption
    ): Intent {
        val shareText = when (option) {
            ShareExportOption.SOLUTION_IMAGE -> formatCompactSummaryCaption(study, language)
            ShareExportOption.FULL_PACKAGE,
            ShareExportOption.TEXT_NOTES -> formatEducationalText(study, language)
        }
        val subject = "EduLens Study Guide: ${study.objectTitle}"

        return when {
            imageUris.isEmpty() -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
            }

            imageUris.size == 1 -> {
                val uri = imageUris.first()
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newUri(context.contentResolver, subject, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            else -> {
                val uriArrayList = ArrayList(imageUris)
                val clip = ClipData.newUri(context.contentResolver, subject, uriArrayList.first())
                for (i in 1 until uriArrayList.size) {
                    clip.addItem(ClipData.Item(uriArrayList[i]))
                }
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArrayList)
                    clipData = clip
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
        }
    }

    fun buildShareChooserIntent(
        context: Context,
        study: ObjectAnalysisResult,
        language: StudyLanguage,
        imageUris: List<Uri>,
        option: ShareExportOption,
        chooserTitle: String
    ): Intent {
        val rawIntent = buildRawShareIntent(context, study, language, imageUris, option)
        return Intent.createChooser(rawIntent, chooserTitle).apply {
            if (imageUris.isNotEmpty()) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    private fun pruneOldExports(exportDir: File) {
        val cutoff = System.currentTimeMillis() - MAX_CACHE_AGE_MS
        exportDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                runCatching { file.delete() }
            }
        }
    }

    private fun exportStudyPhotoJpeg(
        context: Context,
        imageSource: String?,
        destination: File
    ): Boolean {
        val bitmap = loadStudyBitmap(context, imageSource, maxDimension = 1080) ?: return false
        return try {
            FileOutputStream(destination).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            true
        } catch (_: Exception) {
            false
        } finally {
            bitmap.recycle()
        }
    }

    private fun loadStudyBitmap(
        context: Context,
        imageSource: String?,
        maxDimension: Int
    ): Bitmap? {
        val rawBitmap = when {
            imageSource == null -> BitmapFactory.decodeResource(context.resources, R.drawable.img_hero_banner)
            imageSource.startsWith("drawable://") -> {
                val resId = when (imageSource.removePrefix("drawable://")) {
                    "img_sample_leaf" -> R.drawable.img_sample_leaf
                    "img_sample_watch" -> R.drawable.img_sample_watch
                    "img_sample_solar" -> R.drawable.img_sample_solar
                    else -> R.drawable.img_hero_banner
                }
                BitmapFactory.decodeResource(context.resources, resId)
            }
            else -> {
                val file = File(imageSource)
                if (!file.exists()) {
                    BitmapFactory.decodeResource(context.resources, R.drawable.img_hero_banner)
                } else {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, opts)
                    var sample = 1
                    val largest = max(opts.outWidth, opts.outHeight)
                    while (largest / sample > maxDimension * 2) {
                        sample *= 2
                    }
                    BitmapFactory.decodeFile(
                        file.absolutePath,
                        BitmapFactory.Options().apply { inSampleSize = sample }
                    )
                }
            }
        } ?: return null

        if (rawBitmap.width <= maxDimension && rawBitmap.height <= maxDimension) {
            return rawBitmap
        }
        val ratio = rawBitmap.width.toFloat() / rawBitmap.height.toFloat()
        val w = if (rawBitmap.width >= rawBitmap.height) maxDimension else max(1, (maxDimension * ratio).toInt())
        val h = if (rawBitmap.height > rawBitmap.width) maxDimension else max(1, (maxDimension / ratio).toInt())
        val scaled = Bitmap.createScaledBitmap(rawBitmap, w, h, true)
        if (scaled !== rawBitmap) rawBitmap.recycle()
        return scaled
    }

    private fun renderSolutionCardPng(
        context: Context,
        study: ObjectAnalysisResult,
        language: StudyLanguage,
        destination: File
    ) {
        val width = 1080
        val pad = 56
        val contentWidth = width - pad * 2

        val bgColor = Color.parseColor("#F6FAF8")
        val tealHeader = Color.parseColor("#0F5C58")
        val cardBg = Color.WHITE
        val cardBorder = Color.parseColor("#D5E3DF")
        val textPrimary = Color.parseColor("#132422")
        val textMuted = Color.parseColor("#4C635F")
        val accentAmber = Color.parseColor("#C76B1E")
        val greenTipBg = Color.parseColor("#E1F5EB")
        val greenTipText = Color.parseColor("#136F45")

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBECE8")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val sectionHeadingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tealHeader
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val itemTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimary
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textMuted
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val tipPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = greenTipText
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerTitleLayout = staticLayout(study.objectTitle, titlePaint, contentWidth)
        val headerSubLayout = staticLayout(
            "${study.category} · ${study.scientificOrTechnicalName} · ${language.nativeName}",
            subtitlePaint,
            contentWidth
        )

        val thumbSize = 210
        val summaryLayout = staticLayout(
            study.summary,
            bodyPaint,
            contentWidth - thumbSize - 64
        )
        val summaryCardHeight = max(thumbSize, summaryLayout.height) + 48

        val factsToDraw = study.facts.take(3)
        val factLayouts = factsToDraw.map { fact ->
            val tLayout = staticLayout("[${fact.discipline}] ${fact.title}", itemTitlePaint, contentWidth - 48)
            val bLayout = staticLayout(fact.detail, bodyPaint, contentWidth - 48)
            tLayout to bLayout
        }

        val solutionsToDraw = study.solutions.take(2)
        val solutionLayouts = solutionsToDraw.map { sol ->
            val tLayout = staticLayout(sol.problemTitle, itemTitlePaint, contentWidth - 48)
            val sLayout = staticLayout(sol.solutionSteps, bodyPaint, contentWidth - 48)
            val tipLayout = staticLayout("Tip: ${sol.careOrSustainabilityTip}", tipPaint, contentWidth - 80)
            Triple(tLayout, sLayout, tipLayout)
        }

        val headerHeight = 44 + headerTitleLayout.height + 12 + headerSubLayout.height + 36
        var totalHeight = headerHeight + 32 + summaryCardHeight + 32

        if (factLayouts.isNotEmpty()) {
            totalHeight += 42
            factLayouts.forEach { (t, b) ->
                totalHeight += 36 + t.height + 8 + b.height + 14
            }
            totalHeight += 14
        }

        if (solutionLayouts.isNotEmpty()) {
            totalHeight += 42
            solutionLayouts.forEach { (t, s, tip) ->
                totalHeight += 36 + t.height + 8 + s.height + 16 + tip.height + 24 + 14
            }
        }

        totalHeight += 88

        val bitmap = Bitmap.createBitmap(width, totalHeight.coerceIn(900, 3200), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(bgColor)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = cardBorder
        }

        // Header block
        fillPaint.color = tealHeader
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), fillPaint)

        var y = 44f
        drawLayout(canvas, headerTitleLayout, pad.toFloat(), y)
        y += headerTitleLayout.height + 12f
        drawLayout(canvas, headerSubLayout, pad.toFloat(), y)

        // Summary + thumbnail card
        y = headerHeight + 32f
        val summaryRect = RectF(pad.toFloat(), y, (width - pad).toFloat(), y + summaryCardHeight)
        fillPaint.color = cardBg
        canvas.drawRoundRect(summaryRect, 28f, 28f, fillPaint)
        canvas.drawRoundRect(summaryRect, 28f, 28f, strokePaint)

        val thumbRect = RectF(
            (pad + 24).toFloat(),
            y + 24f,
            (pad + 24 + thumbSize).toFloat(),
            y + 24f + thumbSize
        )
        val photoBitmap = loadStudyBitmap(context, study.imageSource, maxDimension = 420)
        if (photoBitmap != null) {
            drawCenterCroppedRoundedBitmap(canvas, photoBitmap, thumbRect, 20f)
            photoBitmap.recycle()
        } else {
            fillPaint.color = cardBorder
            canvas.drawRoundRect(thumbRect, 20f, 20f, fillPaint)
        }

        drawLayout(canvas, summaryLayout, thumbRect.right + 24f, y + 24f)
        y = summaryRect.bottom + 32f

        // Educational Facts section
        if (factLayouts.isNotEmpty()) {
            fillPaint.color = accentAmber
            canvas.drawCircle((pad + 10).toFloat(), y + 14f, 8f, fillPaint)
            canvas.drawText("EDUCATIONAL FACTS", (pad + 28).toFloat(), y + 22f, sectionHeadingPaint)
            y += 42f

            factLayouts.forEach { (tLayout, bLayout) ->
                val cardH = 36f + tLayout.height + 8f + bLayout.height
                val rect = RectF(pad.toFloat(), y, (width - pad).toFloat(), y + cardH)
                fillPaint.color = cardBg
                canvas.drawRoundRect(rect, 22f, 22f, fillPaint)
                canvas.drawRoundRect(rect, 22f, 22f, strokePaint)

                drawLayout(canvas, tLayout, (pad + 24).toFloat(), y + 18f)
                drawLayout(canvas, bLayout, (pad + 24).toFloat(), y + 18f + tLayout.height + 8f)
                y = rect.bottom + 14f
            }
            y += 14f
        }

        // Practical Solutions section
        if (solutionLayouts.isNotEmpty()) {
            fillPaint.color = tealHeader
            canvas.drawCircle((pad + 10).toFloat(), y + 14f, 8f, fillPaint)
            canvas.drawText("PRACTICAL SOLUTIONS & CARE", (pad + 28).toFloat(), y + 22f, sectionHeadingPaint)
            y += 42f

            solutionLayouts.forEach { (tLayout, sLayout, tipLayout) ->
                val tipBoxH = tipLayout.height + 24f
                val cardH = 36f + tLayout.height + 8f + sLayout.height + 16f + tipBoxH
                val rect = RectF(pad.toFloat(), y, (width - pad).toFloat(), y + cardH)
                fillPaint.color = cardBg
                canvas.drawRoundRect(rect, 22f, 22f, fillPaint)
                canvas.drawRoundRect(rect, 22f, 22f, strokePaint)

                var innerY = y + 18f
                drawLayout(canvas, tLayout, (pad + 24).toFloat(), innerY)
                innerY += tLayout.height + 8f
                drawLayout(canvas, sLayout, (pad + 24).toFloat(), innerY)
                innerY += sLayout.height + 16f

                val tipRect = RectF((pad + 24).toFloat(), innerY, (width - pad - 24).toFloat(), innerY + tipBoxH)
                fillPaint.color = greenTipBg
                canvas.drawRoundRect(tipRect, 14f, 14f, fillPaint)
                drawLayout(canvas, tipLayout, tipRect.left + 16f, tipRect.top + 12f)

                y = rect.bottom + 14f
            }
        }

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textMuted
            textSize = 22f
        }
        canvas.drawText(
            "EduLens · Structures, Facts & Solutions (${study.structures.size} parts · ${study.facts.size} facts · ${study.solutions.size} solutions)",
            pad.toFloat(),
            (bitmap.height - 28).toFloat(),
            footerPaint
        )

        FileOutputStream(destination).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        bitmap.recycle()
    }

    private fun staticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        val safeWidth = max(120, width)
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.12f)
            .setIncludePad(false)
            .build()
    }

    private fun drawLayout(canvas: Canvas, layout: StaticLayout, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawCenterCroppedRoundedBitmap(
        canvas: Canvas,
        bitmap: Bitmap,
        dest: RectF,
        cornerRadius: Float
    ) {
        val clipPath = Path().apply {
            addRoundRect(dest, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)

        val srcAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val dstAspect = dest.width() / dest.height()
        val srcRect = if (srcAspect > dstAspect) {
            val cropW = (bitmap.height * dstAspect).toInt()
            val left = (bitmap.width - cropW) / 2
            Rect(left, 0, left + cropW, bitmap.height)
        } else {
            val cropH = (bitmap.width / dstAspect).toInt()
            val top = (bitmap.height - cropH) / 2
            Rect(0, top, bitmap.width, top + cropH)
        }
        canvas.drawBitmap(bitmap, srcRect, dest, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        canvas.restore()
    }
}
