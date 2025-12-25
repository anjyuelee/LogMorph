package com.anjyue.logmorph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anjyue.logmorph.logger.LogMorphInterceptor
import com.anjyue.logmorph.ui.theme.LogMorphTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LogMorphTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Hello $name!")
        
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    makeRequestAll()
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("顯示全部 (ALL)")
        }

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    makeRequestHeadersOnly()
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("只顯示 Headers")
        }

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    makeRequestBodyOnly()
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("只顯示 Body")
        }

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    makeRequestBasic()
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("基本資訊 (BASIC)")
        }
    }
}

fun makeRequestAll() {
    try {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                LogMorphInterceptor.Builder()
                    .addReplacement("origin", "***")
                    .setTag("HttpBin_ALL")
                    .setLogContent(com.anjyue.logmorph.logger.LogContent.ALL)
                    .build()
            )
            .build()

        val request = Request.Builder()
            .url("https://httpbin.org/get")
            .build()

        client.newCall(request).execute().use { _ ->
            // 顯示完整資訊 (Headers + Body)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun makeRequestHeadersOnly() {
    try {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                LogMorphInterceptor.Builder()
                    .addReplacement("origin", "***")
                    .setTag("HttpBin_HeadersOnly")
                    .setLogContent(com.anjyue.logmorph.logger.LogContent.HEADERS_ONLY)
                    .build()
            )
            .build()

        val request = Request.Builder()
            .url("https://httpbin.org/get")
            .build()

        client.newCall(request).execute().use { _ ->
            // 只顯示 Headers，不顯示 Body
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun makeRequestBodyOnly() {
    try {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                LogMorphInterceptor.Builder()
                    .addReplacement("origin", "***")
                    .setTag("HttpBin_BodyOnly")
                    .setLogContent(com.anjyue.logmorph.logger.LogContent.BODY_ONLY)
                    .build()
            )
            .build()

        val request = Request.Builder()
            .url("https://httpbin.org/get")
            .build()

        client.newCall(request).execute().use { _ ->
            // 只顯示 Body，不顯示 Headers
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun makeRequestBasic() {
    try {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                LogMorphInterceptor.Builder()
                    .addReplacement("origin", "***")
                    .setTag("HttpBin_Basic")
                    .setLogContent(com.anjyue.logmorph.logger.LogContent.BASIC)
                    .build()
            )
            .build()

        val request = Request.Builder()
            .url("https://httpbin.org/get")
            .build()

        client.newCall(request).execute().use { _ ->
            // 只顯示基本資訊 (Method, URL, Status Code, Duration)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LogMorphTheme {
        Greeting("Android")
    }
}
