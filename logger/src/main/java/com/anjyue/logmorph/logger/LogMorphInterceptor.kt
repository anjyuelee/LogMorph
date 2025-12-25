package com.anjyue.logmorph.logger

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * LogMorphInterceptor 負責攔截 OkHttp 請求與回應，並進行以下處理：
 * 1. 印出請求與回應的詳細資訊 (Headers, Body等)。
 * 2. 自動美化 JSON 格式的 Body 內容。
 * 3. 根據傳入的 [replacements] Map 進行敏感字詞或特定文字的替換。
 *
 * @param replacements key 為想要被替換的文字，value 為替換後的文字。
 */
class LogMorphInterceptor(
    private val replacements: Map<String, String> = emptyMap()
) : Interceptor {

    companion object {
        private const val TAG = "LogMorph"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Log Request
        Log.d(TAG, "--> ${request.method} ${request.url}")
        request.headers.forEach { pair ->
            Log.d(TAG, "${pair.first}: ${pair.second}")
        }

        request.body?.let { requestBody ->
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            
            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            
            if (isPlaintext(buffer)) {
                val content = buffer.readString(charset)
                Log.d(TAG, "Request Body:\n${formatJson(replaceText(content))}")
            } else {
                Log.d(TAG, "Request Body: (binary ${requestBody.contentLength()}-byte body omitted)")
            }
        }
        Log.d(TAG, "--> END ${request.method}")

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.d(TAG, "<-- HTTP FAILED: $e")
            throw e
        }
        val tookMs = (System.nanoTime() - startNs) / 1e6

        // Log Response
        Log.d(TAG, "<-- ${response.code} ${response.message} ${response.request.url} (${tookMs}ms)")
        response.headers.forEach { pair ->
            Log.d(TAG, "${pair.first}: ${pair.second}")
        }

        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer

            val contentType = responseBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            if (isPlaintext(buffer)) {
                if (responseBody.contentLength() != 0L) {
                    val content = buffer.clone().readString(charset)
                    Log.d(TAG, "Response Body:\n${formatJson(replaceText(content))}")
                }
            } else {
                Log.d(TAG, "Response Body: (binary ${buffer.size}-byte body omitted)")
            }
        }
        Log.d(TAG, "<-- END HTTP")

        return response
    }

    private fun replaceText(input: String): String {
        var result = input
        replacements.forEach { (key, value) ->
            result = result.replace(key, value)
        }
        return result
    }

    private fun formatJson(json: String): String {
        val trimmedJson = json.trim()
        return try {
            if (trimmedJson.startsWith("{")) {
                JSONObject(trimmedJson).toString(4)
            } else if (trimmedJson.startsWith("[")) {
                JSONArray(trimmedJson).toString(4)
            } else {
                json
            }
        } catch (e: Exception) {
            json
        }
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: Exception) {
            return false // Truncated UTF-8 sequence.
        }
    }
}
