package com.example.mesgaging.cloud

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class CloudUploadResult(
    val success: Boolean,
    val directDownloadUrl: String? = null,
    val webPageUrl: String? = null,
    val fileName: String = "",
    val fileSizeFormatted: String = "",
    val errorMessage: String? = null
)

class CloudStorageManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        fileName: String
    ): CloudUploadResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val fileSize = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            val sizeFormatted = formatFileSize(fileSize)

            val requestBody = object : RequestBody() {
                override fun contentType() = mimeType.toMediaTypeOrNull()
                override fun contentLength() = fileSize

                override fun writeTo(sink: BufferedSink) {
                    val stream: InputStream = contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Could not open input stream for $uri")
                    stream.source().use { source ->
                        sink.writeAll(source)
                    }
                }
            }

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, requestBody)
                .build()

            val request = Request.Builder()
                .url("https://tmpfiles.org/api/v1/upload")
                .post(multipartBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext CloudUploadResult(
                    success = false,
                    fileName = fileName,
                    errorMessage = "Upload failed with HTTP ${response.code}: $responseBody"
                )
            }

            val json = JSONObject(responseBody)
            val status = json.optString("status", "")
            if (status == "success") {
                val data = json.getJSONObject("data")
                val pageUrl = data.getString("url")
                // Convert https://tmpfiles.org/KEY/NAME to direct download https://tmpfiles.org/dl/KEY/NAME
                val directUrl = if (pageUrl.contains("tmpfiles.org/") && !pageUrl.contains("tmpfiles.org/dl/")) {
                    pageUrl.replace("tmpfiles.org/", "tmpfiles.org/dl/")
                } else {
                    pageUrl
                }

                CloudUploadResult(
                    success = true,
                    directDownloadUrl = directUrl,
                    webPageUrl = pageUrl,
                    fileName = fileName,
                    fileSizeFormatted = sizeFormatted
                )
            } else {
                val errorMsg = json.optString("message", "Unknown error from cloud storage provider")
                CloudUploadResult(
                    success = false,
                    fileName = fileName,
                    errorMessage = errorMsg
                )
            }
        } catch (e: Exception) {
            CloudUploadResult(
                success = false,
                fileName = fileName,
                errorMessage = e.localizedMessage ?: "Failed to upload to cloud"
            )
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Cloud File"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$bytes B"
        }
    }
}
