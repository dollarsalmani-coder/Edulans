package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

data class CompressedImageResult(
    val localFilePath: String,
    val compressedKb: Int,
    val originalKb: Int,
    val savingsPercent: Int
)

object ImageCompressor {

    fun createCameraCaptureUri(context: Context): Pair<Uri, File> {
        val cacheDir = File(context.cacheDir, "camera_temp").apply { mkdirs() }
        val photoFile = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        return uri to photoFile
    }

    suspend fun compressAndStoreImage(
        context: Context,
        sourceUri: Uri,
        lowBandwidthMode: Boolean
    ): CompressedImageResult? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val originalBytes = resolver.openInputStream(sourceUri)?.use { it.readBytes() }
            ?: return@withContext null
        compressAndStoreBytes(
            context = context,
            originalBytes = originalBytes,
            rotationDegrees = 0,
            lowBandwidthMode = lowBandwidthMode
        )
    }

    suspend fun compressAndStoreBytes(
        context: Context,
        originalBytes: ByteArray,
        rotationDegrees: Int,
        lowBandwidthMode: Boolean
    ): CompressedImageResult? = withContext(Dispatchers.IO) {
        if (originalBytes.isEmpty()) return@withContext null
        val originalKb = max(1, (originalBytes.size / 1024.0).roundToInt())

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size, boundsOptions)

        val maxDimension = if (lowBandwidthMode) 480 else 1024
        val quality = if (lowBandwidthMode) 45 else 80

        val sampleSize = calculateInSampleSize(
            width = boundsOptions.outWidth,
            height = boundsOptions.outHeight,
            reqMax = maxDimension
        )

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decodedBitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size, decodeOptions)
            ?: return@withContext null

        val rotatedBitmap = if (rotationDegrees % 360 != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(
                decodedBitmap,
                0,
                0,
                decodedBitmap.width,
                decodedBitmap.height,
                matrix,
                true
            ).also {
                if (it !== decodedBitmap) decodedBitmap.recycle()
            }
        } else {
            decodedBitmap
        }

        val scaledBitmap = scaleDownIfNeeded(rotatedBitmap, maxDimension)
        if (scaledBitmap !== rotatedBitmap) {
            rotatedBitmap.recycle()
        }

        val outStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
        scaledBitmap.recycle()
        val compressedBytes = outStream.toByteArray()

        val targetDir = File(context.filesDir, "study_images").apply { mkdirs() }
        val targetFile = File(targetDir, "obj_${UUID.randomUUID().toString().take(10)}.jpg")
        FileOutputStream(targetFile).use { it.write(compressedBytes) }

        val compressedKb = max(1, (compressedBytes.size / 1024.0).roundToInt())
        val savings = if (originalKb > compressedKb) {
            (((originalKb - compressedKb).toFloat() / originalKb.toFloat()) * 100f).roundToInt()
        } else {
            0
        }

        CompressedImageResult(
            localFilePath = targetFile.absolutePath,
            compressedKb = compressedKb,
            originalKb = originalKb,
            savingsPercent = savings
        )
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqMax: Int): Int {
        var inSampleSize = 1
        val largest = max(width, height)
        if (largest > reqMax) {
            val halfLargest = largest / 2
            while (halfLargest / inSampleSize >= reqMax) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (width >= height) {
            targetWidth = maxDimension
            targetHeight = max(1, (maxDimension / ratio).roundToInt())
        } else {
            targetHeight = maxDimension
            targetWidth = max(1, (maxDimension * ratio).roundToInt())
        }
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
