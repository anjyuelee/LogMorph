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

        // 全局同步鎖，確保不同線程的日誌不會交錯
        private val logLock = Any()
    }

    /**
     * 日誌緩衝區，用於收集完整的日誌內容後一次性輸出
     */
    private class LogBuffer {
        private val buffer = StringBuilder()

        fun append(line: String) {
            buffer.append(line).append('\n')
        }

        fun flush(tag: String, logLevel: LogLevel) {
            val content = buffer.toString()
            // 移除最後一個換行符
            val finalContent = if (content.endsWith('\n')) {
                content.substring(0, content.length - 1)
            } else {
                content
            }

            when (logLevel) {
                LogLevel.VERBOSE -> Log.v(tag, finalContent)
                LogLevel.DEBUG -> Log.d(tag, finalContent)
                LogLevel.INFO -> Log.i(tag, finalContent)
                LogLevel.WARN -> Log.w(tag, finalContent)
                LogLevel.ERROR -> Log.e(tag, finalContent)
            }
        }
    }

    private fun logDivider(buffer: LogBuffer, isTop: Boolean = false, isBottom: Boolean = false, isMiddle: Boolean = false) {
        when {
            isTop -> buffer.append("$TOP_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER")
            isBottom -> buffer.append("$BOTTOM_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER")
            isMiddle -> buffer.append("$MIDDLE_CORNER$SINGLE_DIVIDER$SINGLE_DIVIDER")
            else -> buffer.append(SIDE_DIVIDER)
        }
    }

    private fun logLine(buffer: LogBuffer, message: String) {
        message.split('\n').forEach { line ->
            buffer.append("$SIDE_DIVIDER$line")
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 創建日誌緩衝區收集請求日誌
        val requestLogBuffer = LogBuffer()

        // Log Request with border
        logDivider(requestLogBuffer, isTop = true)
        val displayUrl = replaceText(request.url.toString())
        logLine(requestLogBuffer, "REQUEST")
        logDivider(requestLogBuffer, isMiddle = true)
        logLine(requestLogBuffer, "Method: ${request.method}")
        logLine(requestLogBuffer, "URL: $displayUrl")

        if (request.headers.size > 0) {
            logDivider(requestLogBuffer, isMiddle = true)
            logLine(requestLogBuffer, "Headers:")
            request.headers.forEach { pair ->
                logLine(requestLogBuffer, "  ${pair.first}: ${pair.second}")
            }
        }

        request.body?.let { requestBody ->
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            
            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            
            logDivider(requestLogBuffer, isMiddle = true)
            if (isPlaintext(buffer)) {
                val content = buffer.readString(charset)
                logLine(requestLogBuffer, "Request Body:")
                logLine(requestLogBuffer, formatJson(replaceText(content)))
            } else {
                logLine(requestLogBuffer, "Request Body: (binary ${requestBody.contentLength()}-byte body omitted)")
            }
        }
        logDivider(requestLogBuffer, isBottom = true)

        // 同步輸出請求日誌
        synchronized(logLock) {
            requestLogBuffer.flush(tag, logLevel)
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            val errorLogBuffer = LogBuffer()
            logDivider(errorLogBuffer, isTop = true)
            logLine(errorLogBuffer, "HTTP FAILED: $e")
            logDivider(errorLogBuffer, isBottom = true)

            // 同步輸出錯誤日誌
            synchronized(logLock) {
                errorLogBuffer.flush(tag, logLevel)
            }
            throw e
        }
        val tookMs = (System.nanoTime() - startNs) / 1e6

        // 創建日誌緩衝區收集響應日誌
        val responseLogBuffer = LogBuffer()

        // Log Response with border
        logDivider(responseLogBuffer, isTop = true)
        val displayResponseUrl = replaceText(response.request.url.toString())
        logLine(responseLogBuffer, "RESPONSE")
        logDivider(responseLogBuffer, isMiddle = true)
        logLine(responseLogBuffer, "URL: $displayResponseUrl")
        logLine(responseLogBuffer, "Status Code: ${response.code} ${response.message}")
        logLine(responseLogBuffer, "Duration: ${tookMs}ms")

        if (response.headers.size > 0) {
            logDivider(responseLogBuffer, isMiddle = true)
            logLine(responseLogBuffer, "Headers:")
            response.headers.forEach { pair ->
                logLine(responseLogBuffer, "  ${pair.first}: ${pair.second}")
            }
        }

        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer

            val contentType = responseBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            logDivider(responseLogBuffer, isMiddle = true)
            if (isPlaintext(buffer)) {
                if (responseBody.contentLength() != 0L) {
                    val content = buffer.clone().readString(charset)
                    logLine(responseLogBuffer, "Response Body:")
                    logLine(responseLogBuffer, formatJson(replaceText(content)))
                } else {
                    logLine(responseLogBuffer, "Response Body: (empty)")
                }
            } else {
                logLine(responseLogBuffer, "Response Body: (binary ${buffer.size}-byte body omitted)")
            }
        }
        logDivider(responseLogBuffer, isBottom = true)

        // 同步輸出響應日誌
        synchronized(logLock) {
            responseLogBuffer.flush(tag, logLevel)
        }

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
