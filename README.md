# LogMorph

[![](https://jitpack.io/v/anjyuelee/LogMorph.svg)](https://jitpack.io/#anjyuelee/LogMorph)

LogMorph 是一個 OkHttp 攔截器，提供：
- 🎨 自動美化 JSON 格式的 Log
- 🔒 敏感資料遮蔽功能
- 📋 完整的 Request/Response 資訊

## 安裝

### Step 1: 加入 JitPack repository

在 `settings.gradle.kts` 中：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: 加入依賴

在 app 模組的 `build.gradle.kts` 中：

```kotlin
dependencies {
    implementation("com.github.anjyuelee:LogMorph:1.0.0")
}
```

## 使用方式

### 基本用法

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(LogMorphInterceptor())
    .build()
```

### 敏感資料遮蔽

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(LogMorphInterceptor(
        replacements = mapOf(
            "password" to "***",
            "token" to "***",
            "敏感字" to "已遮蔽"
        )
    ))
    .build()
```

## 功能特色

- ✅ 自動偵測並美化 JSON 格式（縮排 4 空格）
- ✅ 顯示 Request/Response Headers
- ✅ 顯示請求耗時
- ✅ 支援敏感字詞替換
- ✅ 自動處理二進位內容

## License

Apache License 2.0

