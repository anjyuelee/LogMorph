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

### 基本用法

最簡單的方式，直接加入攔截器：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(LogMorphInterceptor())
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
        LogMorphInterceptor(
            replacements = mapOf(
                "api_key" to "***",
                "password" to "***",
                "token" to "***"
            )
        )
    )
    .build()
```

### 自訂日誌等級

根據需求設定不同的日誌等級：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor(
            logLevel = LogLevel.INFO
        )
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
        LogMorphInterceptor(
            tag = "MyAPI"
        )
    )
    .build()
```

或結合其他參數一起使用：

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor(
            replacements = mapOf("token" to "***"),
            logLevel = LogLevel.DEBUG,
            tag = "UserAPI"
        )
    )
    .build()
```
- `LogLevel.DEBUG` (預設)
- `LogLevel.INFO`
- `LogLevel.WARN`
- `LogLevel.ERROR`

### 完整範例

結合所有功能的完整範例：

```kotlin
import com.anjyue.logmorph.logger.LogMorphInterceptor
import com.anjyue.logmorph.logger.LogLevel
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class NetworkClient {
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(
            LogMorphInterceptor(
                replacements = mapOf(
                    "authorization" to "***",
                    "api_key" to "***",
                    "password" to "***"
                ),
                logLevel = LogLevel.DEBUG,
                tag = "NetworkClient"
            )
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

### 結合 Retrofit 使用

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(
        LogMorphInterceptor(
            replacements = mapOf("api_key" to "***"),
            logLevel = LogLevel.DEBUG
        )
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
            addInterceptor(LogMorphInterceptor())
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

