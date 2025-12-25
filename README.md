# LogMorph

LogMorph 是一個 OkHttp 的攔截器 (Interceptor) 函式庫，專門用於美化 JSON Log 並支援敏感資料遮蔽。

## 專案結構

本專案包含兩個模組：
1. **app**: 範例應用程式，展示如何使用 `LogMorphInterceptor`。
2. **logger**: Android Library 模組，包含 `LogMorphInterceptor` 的核心邏輯。

## 如何發佈到 Maven

本專案支援發佈至自定義的 Maven Repository。請在專案根目錄的 `gradle.properties` (或全域 `~/.gradle/gradle.properties`) 設定以下變數：

```properties
myMavenRepoUrl=https://your.maven.repo/url
myMavenRepoUsername=your_username
myMavenRepoPassword=your_password
```

### 執行發佈

設定完成後，執行以下指令將 Library 發佈至指定的 Repository：

```bash
./gradlew :logger:publishReleasePublicationToMyRepoRepository
```

(若未設定 Url，預設會發佈到 `logger/build/repo` 資料夾中)

### 發佈到本地 Maven Local

若僅供本機測試，仍可使用：

```bash
./gradlew :logger:publishReleasePublicationToMavenLocal
```

## 使用方式

### 加入相依性

發佈成功後，在其他專案中加入：

```kotlin
dependencies {
    implementation("com.anjyue.logmorph:logger:1.0.0")
}
```

### 初始化

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(LogMorphInterceptor(mapOf("敏感字" to "***")))
    .build()
```
