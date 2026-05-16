package com.breathy.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resumeWithException

/**
 * Handles profile image uploads to Firebase Storage with compression,
 * progress tracking, cancellation support, and automatic retries.
 *
 * Images are uploaded to: `profileImages/{userId}.jpg`
 *
 * Features:
 * - Compresses images to quality 80 JPEG, max 1024px on longest side
 * - Corrects EXIF orientation before upload
 * - Reports upload progress via [ProgressCallback]
 * - Supports cooperative cancellation via [UploadHandle]
 * - Retries up to [MAX_RETRIES] times on transient failures
 * - 30-second timeout per attempt
 */
class ImageUploader(
    private val storage: FirebaseStorage
) {

    companion object {
        /** Maximum dimension (width or height) for the compressed image. */
        private const val MAX_DIMENSION = 1024

        /** JPEG compression quality (0–100). */
        private const val JPEG_QUALITY = 80

        /** Maximum number of retry attempts per upload. */
        private const val MAX_RETRIES = 3

        /** Upload timeout in milliseconds. */
        private const val UPLOAD_TIMEOUT_MS = 30_000L

        /** Firebase Storage path template for profile images. */
        private const val STORAGE_PATH = "profileImages/%s.jpg"

        /** Maximum file size for a profile image (10 MB before compression). */
        private const val MAX_SOURCE_FILE_SIZE_BYTES = 10L * 1024L * 1024L
    }

    /**
     * Callback interface for receiving upload progress updates.
     */
    interface ProgressCallback {
        /**
         * Called periodically as bytes are transferred.
         *
         * @param bytesTransferred Number of bytes uploaded so far.
         * @param totalBytes       Total number of bytes to upload.
         * @param percentage       Upload progress as a value between 0.0 and 100.0.
         */
        fun onProgress(bytesTransferred: Long, totalBytes: Long, percentage: Double)
    }

    /**
     * Handle for an in-progress upload. Call [cancel] to abort.
     */
    class UploadHandle {
        @Volatile
        var isCancelled: Boolean = false
            private set

        fun cancel() {
            isCancelled = true
        }
    }

    /**
     * Result of a successful image upload.
     *
     * @property downloadUrl The publicly accessible download URL from Firebase Storage.
     * @property storagePath The Firebase Storage path where the image was saved.
     * @property compressedSizeBytes The size of the compressed image in bytes.
     */
    data class UploadResult(
        val downloadUrl: String,
        val storagePath: String,
        val compressedSizeBytes: Long
    )

    /**
     * Upload a profile image for the given user.
     *
     * The image is compressed to a maximum of [MAX_DIMENSION]px on the longest
     * side, converted to JPEG at quality [JPEG_QUALITY], and uploaded to
     * `profileImages/{userId}.jpg`. EXIF orientation is corrected before upload.
     *
     * @param context   Android context for content resolver access.
     * @param userId    The user's Firebase Auth UID — used as the storage key.
     * @param imageUri  Content URI of the source image.
     * @param callback  Optional progress callback.
     * @param handle    Optional upload handle for cancellation support.
     * @return [UploadResult] on success, or `null` on failure or cancellation.
     */
    suspend fun uploadProfileImage(
        context: Context,
        userId: String,
        imageUri: Uri,
        callback: ProgressCallback? = null,
        handle: UploadHandle? = null
    ): UploadResult? = withContext(Dispatchers.IO) {
        val storagePath = STORAGE_PATH.format(userId)
        val storageRef = storage.reference.child(storagePath)

        // ── Step 1: Validate source ────────────────────────────────────────
        val sourceSize = try {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                stream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            Timber.e(e, "Failed to read source image URI")
            return@withContext null
        }

        if (sourceSize > MAX_SOURCE_FILE_SIZE_BYTES) {
            Timber.w("Source image too large: %d bytes (max %d)", sourceSize, MAX_SOURCE_FILE_SIZE_BYTES)
            return@withContext null
        }

        // ── Step 2: Compress image ─────────────────────────────────────────
        val compressedFile = try {
            compressImage(context, imageUri)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to compress profile image")
            return@withContext null
        }

        if (handle?.isCancelled == true) {
            Timber.d("Upload cancelled before starting")
            compressedFile.delete()
            return@withContext null
        }

        val compressedBytes = compressedFile.readBytes()

        // ── Step 3: Upload with retries ────────────────────────────────────
        var lastException: Exception? = null
        var attempt = 0

        while (attempt < MAX_RETRIES) {
            attempt++
            if (handle?.isCancelled == true) {
                Timber.d("Upload cancelled on attempt %d", attempt)
                compressedFile.delete()
                return@withContext null
            }

            try {
                val result = withTimeoutOrNull(UPLOAD_TIMEOUT_MS) {
                    uploadBytes(
                        storageRef = storageRef,
                        bytes = compressedBytes,
                        callback = callback,
                        handle = handle
                    )
                }

                if (result != null) {
                    compressedFile.delete()
                    Timber.d(
                        "Profile image uploaded: path=%s, size=%d bytes, attempts=%d",
                        storagePath, compressedBytes.size, attempt
                    )
                    return@withContext result
                } else {
                    Timber.w("Upload timeout on attempt %d/%d for %s", attempt, MAX_RETRIES, storagePath)
                    lastException = Exception("Upload timed out after ${UPLOAD_TIMEOUT_MS}ms")
                }
            } catch (e: CancellationException) {
                compressedFile.delete()
                throw e
            } catch (e: Exception) {
                lastException = e
                Timber.w(
                    e,
                    "Upload attempt %d/%d failed for %s",
                    attempt, MAX_RETRIES, storagePath
                )
            }

            // Exponential backoff before retry: 500ms, 1000ms, 2000ms
            if (attempt < MAX_RETRIES) {
                val backoffMs = 500L * (1L shl (attempt - 1))
                try {
                    kotlinx.coroutines.delay(backoffMs)
                } catch (e: CancellationException) {
                    compressedFile.delete()
                    throw e
                }
            }
        }

        // All retries exhausted
        compressedFile.delete()
        Timber.e(lastException, "All %d upload attempts failed for %s", MAX_RETRIES, storagePath)
        return@withContext null
    }

    /**
     * Perform the actual byte upload to Firebase Storage with progress tracking.
     */
    private suspend fun uploadBytes(
        storageRef: com.google.firebase.storage.StorageReference,
        bytes: ByteArray,
        callback: ProgressCallback?,
        handle: UploadHandle?
    ): UploadResult = suspendCancellableCoroutine { continuation ->

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .setCustomMetadata("uploadedBy", "breathy_app")
            .build()

        val uploadTask = storageRef.putBytes(bytes, metadata)

        // Progress listener
        uploadTask.addOnProgressListener { snapshot ->
            val transferred = snapshot.bytesTransferred
            val total = snapshot.totalByteCount
            val percentage = if (total > 0) (transferred.toDouble() / total) * 100.0 else 0.0
            callback?.onProgress(transferred, total, percentage)
        }

        // Success → get download URL
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl
                .addOnSuccessListener { uri ->
                    if (continuation.isActive) {
                        continuation.resume(
                            UploadResult(
                                downloadUrl = uri.toString(),
                                storagePath = storageRef.path,
                                compressedSizeBytes = taskSnapshot.metadata?.sizeBytes?.toLong()
                                    ?: bytes.size.toLong()
                            )
                        ) {}
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
        }

        // Failure
        uploadTask.addOnFailureListener { e ->
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }

        // Cancellation support
        continuation.invokeOnCancellation {
            handle?.cancel()
            uploadTask.cancel()
        }
    }

    /**
     * Compress the source image:
     * 1. Decode with inSampleSize to reduce memory footprint
     * 2. Scale to max 1024px on the longest side
     * 3. Correct EXIF orientation
     * 4. Write as JPEG at quality 80
     *
     * @return A [File] containing the compressed JPEG.
     */
    private fun compressImage(context: Context, imageUri: Uri): File {
        // ── Decode bounds first ─────────────────────────────────────────────
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(imageUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        } ?: throw IllegalStateException("Cannot open image URI: $imageUri")

        // ── Calculate inSampleSize ──────────────────────────────────────────
        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        // ── Decode the sampled bitmap ───────────────────────────────────────
        val sampledBitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalStateException("Cannot decode image from URI: $imageUri")

        // ── Scale to exact max dimension ────────────────────────────────────
        val scaledBitmap = scaleBitmap(sampledBitmap, MAX_DIMENSION)
        if (scaledBitmap != sampledBitmap) {
            sampledBitmap.recycle()
        }

        // ── Correct EXIF orientation ────────────────────────────────────────
        val orientedBitmap = correctExifOrientation(context, imageUri, scaledBitmap)
        if (orientedBitmap != scaledBitmap) {
            scaledBitmap.recycle()
        }

        // ── Compress to JPEG ────────────────────────────────────────────────
        val outputFile = File.createTempFile("profile_upload_", ".jpg", context.cacheDir)
        ByteArrayOutputStream().use { baos ->
            val compressed = orientedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
            if (!compressed) {
                throw IllegalStateException("Bitmap compression failed")
            }
            FileOutputStream(outputFile).use { fos ->
                fos.write(baos.toByteArray())
            }
        }
        orientedBitmap.recycle()

        return outputFile
    }

    /**
     * Calculate an appropriate [BitmapFactory.Options.inSampleSize] value
     * that is a power of two and results in an image no larger than
     * [maxDimension] on its longest side.
     */
    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var inSampleSize = 1
        val longerSide = maxOf(width, height)

        if (longerSide > maxDimension) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            // Calculate the largest inSampleSize that is a power of 2 and keeps
            // both dimensions larger than the target max dimension
            while ((halfWidth / inSampleSize) >= maxDimension &&
                (halfHeight / inSampleSize) >= maxDimension
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Scale a bitmap so that its longest side is at most [maxDimension] pixels.
     * Returns the original bitmap if no scaling is needed.
     */
    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val longerSide = maxOf(width, height)

        if (longerSide <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / longerSide.toFloat()
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Read the EXIF orientation tag from the source image and rotate the
     * bitmap accordingly so that it displays correctly after upload.
     *
     * Returns the original bitmap if no rotation is needed or if EXIF
     * data is unavailable.
     */
    private fun correctExifOrientation(
        context: Context,
        imageUri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        return try {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> return bitmap // No rotation needed
                }

                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
            } ?: bitmap
        } catch (e: Exception) {
            Timber.w(e, "Failed to read EXIF orientation, using bitmap as-is")
            bitmap
        }
    }

    /**
     * Delete the profile image for the given user from Firebase Storage.
     *
     * @return `true` if deletion succeeded or the file did not exist, `false` on error.
     */
    suspend fun deleteProfileImage(userId: String): Boolean {
        return try {
            val storagePath = STORAGE_PATH.format(userId)
            storage.reference.child(storagePath).delete().await()
            Timber.d("Profile image deleted: %s", storagePath)
            true
        } catch (e: Exception) {
            // Firebase throws StorageException with code OBJECT_NOT_FOUND if file doesn't exist
            val errorCode = (e as? com.google.firebase.storage.StorageException)?.errorCode
            if (errorCode == com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND) {
                Timber.d("Profile image does not exist, nothing to delete")
                true
            } else {
                Timber.e(e, "Failed to delete profile image for user %s", userId)
                false
            }
        }
    }
}
