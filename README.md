# LogMorph

<div align="center">

[![](https://jitpack.io/v/anjyuelee/LogMorph.svg)](https://jitpack.io/#anjyuelee/LogMorph)

一個強大的 Android OkHttp 網路請求日誌攔截器，提供美化的 JSON 格式輸出和敏感資訊遮罩功能。

</div>

---

## ✨ 功能特色

- 📝 **美化的日誌輸出** - 使用邊框和縮排格式化請求和回應日誌
- 🎨 **自動 JSON 格式化** - 自動美化 JSON 格式的請求和回應內容
- 🔒 **敏感資訊遮罩** - 可自訂替換規則，保護敏感資料
- ⚙️ **自訂日誌等級** - 支援 VERBOSE、DEBUG、INFO、WARN、ERROR
- 🏷️ **自訂 Log Tag** - 可自訂 Log Tag，方便過濾和識別不同的網路請求
- 🧵 **線程安全** - 確保多線程環境下每個完整的日誌不會被其他線程的日誌插入
- 🚀 **輕量且易用** - 簡單整合，無需複雜設定

---

## 📦 安裝

### 步驟 1：在專案根目錄的 `settings.gradle.kts` 中新增 JitPack 倉庫

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 步驟 2：在模組的 `build.gradle.kts` 中新增依賴

```kotlin
dependencies {
    implementation("com.github.anjyuelee:LogMorph:v1.0.0")
}
```

> 💡 請將 `v1.0.0` 替換為 [![](https://jitpack.io/v/anjyuelee/LogMorph.svg)](https://jitpack.io/#anjyuelee/LogMorph) 上顯示的最新版本

---

## 🚀 使用方式

LogMorph 使用 **Builder 模式** 來建立攔截器，提供靈活且易讀的 API。

### Builder 方法說明

| 方法 | 參數 | 說明 |
|------|------|------|
| `addReplacement(key, value)` | key: String, value: String | 新增單一替換規則 |
| `setReplacements(map)` | map: Map<String, String> | 批次設定替換規則 |
| `setLogLevel(level)` | level: LogLevel | 設定日誌等級 |
| `setTag(tag)` | tag: String | 設定 Log Tag |
| `setLogContent(content)` | content: LogContent | 設定顯示內容類型 |
| `setReplaceUrlOnly(enabled)` | enabled: Boolean | 設定是否只替換 URL 中的內容（預設：false） |
| `build()` | - | 建立 LogMorphInterceptor 實例 |

### 基本用法

使用 Builder 模式建立 LogMorphInterceptor：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .build()
    )
    .build()

val request = Request.Builder()
    .url("https://api.example.com/users")
    .build()

client.newCall(request).execute()
```

### 使用敏感資訊遮罩

保護 API 金鑰、Token 等敏感資訊：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .addReplacement("api_key", "***")
            .addReplacement("password", "***")
            .addReplacement("token", "***")
            .build()
    )
    .build()
```

或使用 `setReplacements` 批次設定：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .setReplacements(mapOf(
                "api_key" to "***",
                "password" to "***",
                "token" to "***"
            ))
            .build()
    )
    .build()
```

### 自訂日誌等級

根據需求設定不同的日誌等級：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .setLogLevel(LogLevel.INFO)
            .build()
    )
    .build()
```

可用的日誌等級：
- `LogLevel.VERBOSE`
- `LogLevel.DEBUG` (預設)
- `LogLevel.INFO`
- `LogLevel.WARN`
- `LogLevel.ERROR`

### 自訂 Log Tag

自訂 Log Tag 方便在 Logcat 中過濾和識別不同的網路請求：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .setTag("MyAPI")
            .build()
    )
    .build()
```

或結合其他參數一起使用：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .addReplacement("token", "***")
            .setLogLevel(LogLevel.DEBUG)
            .setTag("UserAPI")
            .setLogContent(LogContent.BODY_ONLY)
            .build()
    )
    .build()
```

### 控制顯示內容

使用 `setLogContent` 方法控制要顯示的日誌內容：

```kotlin
// 顯示所有內容 (Headers + Body) - 預設值
val clientAll = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .setLogContent(LogContent.ALL)
            .build()
    )
    .build()

// 只顯示 Headers
val clientHeadersOnly = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .setLogContent(LogContent.HEADERS_ONLY)
            .build()
    )
    .build()

// 只顯示 Body
val clientBodyOnly = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .setLogContent(LogContent.BODY_ONLY)
            .build()
    )
    .build()

// 只顯示基本資訊 (Method, URL, Status Code, Duration)
val clientBasic = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .setLogContent(LogContent.BASIC)
            .build()
    )
    .build()
```

可用的內容顯示模式：
- `LogContent.ALL` (預設)：顯示完整資訊 (Headers + Body)
- `LogContent.HEADERS_ONLY`：只顯示 Headers，不顯示 Body
- `LogContent.BODY_ONLY`：只顯示 Body，不顯示 Headers
- `LogContent.BASIC`：只顯示基本資訊 (請求方法、URL、狀態碼、耗時)

#### 使用場景建議

- **LogContent.ALL**：開發除錯階段，需要完整的請求資訊
- **LogContent.HEADERS_ONLY**：需要驗證認證、內容類型等 Header 資訊
- **LogContent.BODY_ONLY**：專注於資料內容，不關心 Headers
- **LogContent.BASIC**：生產環境或效能敏感場景，只記錄基本資訊

### 只替換 URL 中的敏感資訊

如果你只想遮罩 URL 中的敏感資訊，但保留 Body 中的原始內容，可以使用 `setReplaceUrlOnly(true)`：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .addReplacement("api_key", "***")
            .addReplacement("token", "***")
            .setReplaceUrlOnly(true)  // 只替換 URL，不替換 Body
            .build()
    )
    .build()
```

**範例說明：**

假設發送以下請求：
```
URL: https://api.example.com/data?api_key=secret123&token=abc456
Body: { "api_key": "secret123", "token": "abc456" }
```

使用 `setReplaceUrlOnly(false)` （預設）：
```
URL: https://api.example.com/data?api_key [***]=secret123&token [***]=abc456
Body: { "api_key [***]": "secret123", "token [***]": "abc456" }
```

使用 `setReplaceUrlOnly(true)`：
```
URL: https://api.example.com/data?api_key [***]=secret123&token [***]=abc456
Body: { "api_key": "secret123", "token": "abc456" }  // Body 保持原樣
```

**使用場景：**
- 保護 URL 參數中的敏感資訊（如 API Key、Token）
- 需要完整查看 Response Body 內容進行除錯
- URL 和 Body 的敏感度不同，需要差異化處理

### 完整範例

結合所有功能的完整範例：

```kotlin
import com.anjyue.logmorph.logger.LogMorphInterceptor
import com.anjyue.logmorph.logger.LogLevel
import com.anjyue.logmorph.logger.LogContent
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class NetworkClient {
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(
            LogMorphInterceptor.Builder()
                .addReplacement("authorization", "***")
                .addReplacement("api_key", "***")
                .addReplacement("password", "***")
                .setLogLevel(LogLevel.DEBUG)
                .setTag("NetworkClient")
                .setLogContent(LogContent.ALL)
                .build()
        )
        .build()
    
    fun getUser(userId: String) {
        val request = Request.Builder()
            .url("https://api.example.com/users/$userId")
            .header("Authorization", "Bearer your_token_here")
            .build()
        
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                println("Success: ${response.body?.string()}")
            }
        }
    }
    
    fun createUser(name: String, email: String) {
        val json = """
            {
                "name": "$name",
                "email": "$email",
                "password": "secret123"
            }
        """.trimIndent()
        
        val body = json.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("https://api.example.com/users")
            .post(body)
            .header("Content-Type", "application/json")
            .build()
        
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                println("User created: ${response.body?.string()}")
            }
        }
    }
}
```

---

## 📱 在 Android 應用中使用

### Kotlin Coroutines 範例

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchData() = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .addInterceptor(LogMorphInterceptor())
        .build()
    
    val request = Request.Builder()
        .url("https://jsonplaceholder.typicode.com/posts/1")
        .build()
    
    client.newCall(request).execute().use { response ->
        response.body?.string()
    }
}
```

### Compose UI 整合範例

```kotlin
@Composable
fun NetworkTestScreen() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        val data = fetchData()
                        withContext(Dispatchers.Main) {
                            result = data ?: "No data"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        ) {
            Text("執行網路請求")
        }
        
        Text(
            text = result,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
```

---

## 📊 日誌輸出範例

### 請求日誌

```
╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
║ REQUEST
╟────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
║ Method: POST
║ URL: https://api.example.com/users
╟────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
║ Headers:
║   Content-Type: application/json
║   Authorization: Bearer your_token_here
╟────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
║ Request Body:
║ {
║     "name": "John Doe",
║     "email": "john@example.com",
║     "password [***]": "secret123"
║ }
╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
```

### 回應日誌

```
╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
║ RESPONSE
╟────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
║ URL: https://api.example.com/users
║ Status Code: 200 OK
║ Duration: 1234.56ms
╟────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
║ Headers:
║   Content-Type: application/json
║   Cache-Control: no-cache
╟────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
║ Response Body:
║ {
║     "id": 123,
║     "name": "John Doe",
║     "email": "john@example.com",
║     "created_at": "2025-12-25T10:30:00Z"
║ }
╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
```

---

## 🔧 進階設定

### 線程安全

LogMorphInterceptor 已實現線程安全機制，確保在多線程環境下（如並發的網路請求），每個完整的請求-響應日誌區塊都能完整呈現，不會被其他線程的日誌插入。

內部實現：
- 使用全局同步鎖 (`synchronized`) 保護日誌輸出過程
- 將完整的日誌內容先收集到緩衝區，再一次性輸出
- 每個 HTTP 請求和響應分別作為獨立的日誌區塊輸出

這確保了即使在高併發的情況下，你也能清晰地看到每個請求的完整資訊：

```kotlin
// 即使同時發起多個請求，每個請求的日誌都能完整呈現
val client = OkHttpClient.Builder()
    .addInterceptor(LogMorphInterceptor())
    .build()

// 並發執行多個請求
coroutineScope {
    launch { client.newCall(request1).execute() }
    launch { client.newCall(request2).execute() }
    launch { client.newCall(request3).execute() }
}
// 輸出的日誌不會交錯混亂
```

### 結合 Retrofit 使用

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor.Builder()
            .addReplacement("api_key", "***")
            .setLogLevel(LogLevel.DEBUG)
            .build()
    )
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

### 僅在 Debug 模式啟用

```kotlin
val client = OkHttpClient.Builder()
    .apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(
                LogMorphInterceptor.Builder()
                    .build()
            )
        }
    }
    .build()
```

---

## 📝 參數說明

### LogMorphInterceptor 建構參數

| 參數 | 類型 | 預設值 | 說明 |
|------|------|--------|------|
| `replacements` | `Map<String, String>` | `emptyMap()` | 設定需要遮罩的敏感資訊，Key 為原始文字，Value 為替換後的文字 |
| `logLevel` | `LogLevel` | `LogLevel.DEBUG` | 設定日誌輸出等級 |
| `tag` | `String` | `"LogMorph"` | 自訂的 Log Tag，方便在 Logcat 中過濾 |
| `logContent` | `LogContent` | `LogContent.ALL` | 控制顯示的內容類型 (ALL/HEADERS_ONLY/BODY_ONLY/BASIC) |
| `replaceUrlOnly` | `Boolean` | `false` | 是否只替換 URL 中的內容，不替換 Body |

### LogLevel 列舉

| 等級 | 說明 |
|------|------|
| `VERBOSE` | 詳細模式，輸出所有資訊 |
| `DEBUG` | 除錯模式（預設） |
| `INFO` | 資訊模式 |
| `WARN` | 警告模式 |
| `ERROR` | 錯誤模式 |

---

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request！

---

## 📄 授權

本專案採用 MIT 授權條款。

---

## 💬 聯絡方式

如有任何問題或建議，歡迎透過 [GitHub Issues](https://github.com/anjyuelee/LogMorph/issues) 與我聯繫。

---

<div align="center">

**如果這個專案對你有幫助，請給個 ⭐️ Star 支持一下！**

</div>

