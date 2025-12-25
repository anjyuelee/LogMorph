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
 * Log 等級定義
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * LogMorphInterceptor 負責攔截 OkHttp 請求與回應，並進行以下處理：
 * 1. 印出請求與回應的詳細資訊 (Headers, Body等)。
 * 2. 自動美化 JSON 格式的 Body 內容。
 * 3. 根據傳入的 [replacements] Map 進行敏感字詞或特定文字的替換。
 *
 * @param replacements key 為想要被替換的文字，value 為替換後的文字。
 * @param logLevel 指定要使用的 Log 等級，預設為 DEBUG。
 * @param tag 自訂的 Log Tag，預設為 "LogMorph"。
 */
class LogMorphInterceptor(
    private val replacements: Map<String, String> = emptyMap(),
    private val logLevel: LogLevel = LogLevel.DEBUG,
    private val tag: String = DEFAULT_TAG
) : Interceptor {

    companion object {
        private const val DEFAULT_TAG = "LogMorph"
        private const val TOP_LEFT_CORNER = '╔'
        private const val BOTTOM_LEFT_CORNER = '╚'
        private const val DOUBLE_DIVIDER = "════════════════════════════════════════════════════════════════"
        private const val SINGLE_DIVIDER = "────────────────────────────────────────────────────────────────"
        private const val SIDE_DIVIDER = "║ "
        private const val MIDDLE_CORNER = '╟'
    }

    private fun log(message: String) {
        when (logLevel) {
            LogLevel.VERBOSE -> Log.v(tag, message)
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARN -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    }

    private fun logDivider(isTop: Boolean = false, isBottom: Boolean = false, isMiddle: Boolean = false) {
        when {
            isTop -> log("$TOP_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER")
            isBottom -> log("$BOTTOM_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER")
            isMiddle -> log("$MIDDLE_CORNER$SINGLE_DIVIDER$SINGLE_DIVIDER")
            else -> log(SIDE_DIVIDER)
        }
    }

    private fun logLine(message: String) {
        message.split('\n').forEach { line ->
            log("$SIDE_DIVIDER$line")
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Log Request with border
        logDivider(isTop = true)
        val displayUrl = replaceText(request.url.toString())
        logLine("REQUEST")
        logDivider(isMiddle = true)
        logLine("Method: ${request.method}")
        logLine("URL: $displayUrl")

        if (request.headers.size > 0) {
            logDivider(isMiddle = true)
            logLine("Headers:")
            request.headers.forEach { pair ->
                logLine("  ${pair.first}: ${pair.second}")
            }
        }

        request.body?.let { requestBody ->
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            
            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            
            logDivider(isMiddle = true)
            if (isPlaintext(buffer)) {
                val content = buffer.readString(charset)
                logLine("Request Body:")
                logLine(formatJson(replaceText(content)))
            } else {
                logLine("Request Body: (binary ${requestBody.contentLength()}-byte body omitted)")
            }
        }
        logDivider(isBottom = true)

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logDivider(isTop = true)
            logLine("HTTP FAILED: $e")
            logDivider(isBottom = true)
            throw e
        }
        val tookMs = (System.nanoTime() - startNs) / 1e6

        // Log Response with border
        logDivider(isTop = true)
        val displayResponseUrl = replaceText(response.request.url.toString())
        logLine("RESPONSE")
        logDivider(isMiddle = true)
        logLine("URL: $displayResponseUrl")
        logLine("Status Code: ${response.code} ${response.message}")
        logLine("Duration: ${tookMs}ms")

        if (response.headers.size > 0) {
            logDivider(isMiddle = true)
            logLine("Headers:")
            response.headers.forEach { pair ->
                logLine("  ${pair.first}: ${pair.second}")
            }
        }

        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer

            val contentType = responseBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            logDivider(isMiddle = true)
            if (isPlaintext(buffer)) {
                if (responseBody.contentLength() != 0L) {
                    val content = buffer.clone().readString(charset)
                    logLine("Response Body:")
                    logLine(formatJson(replaceText(content)))
                } else {
                    logLine("Response Body: (empty)")
                }
            } else {
                logLine("Response Body: (binary ${buffer.size}-byte body omitted)")
            }
        }
        logDivider(isBottom = true)

        return response
    }

    private fun replaceText(input: String): String {
        var result = input
        replacements.forEach { (key, value) ->
            result = result.replace(key, "$key [$value]")
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
        } catch (_: Exception) {
            json
        }
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            repeat(16) {
                if (prefix.exhausted()) {
                    return true
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (_: Exception) {
            return false // Truncated UTF-8 sequence.
        }
    }
}
