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
                    makeRequest()
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Test Network Request")
        }
    }
}

fun makeRequest() {
    try {
        val client = OkHttpClient.Builder()
            // 在這裡使用自定義的 Interceptor
            // 使用 mapOf 定義要替換的文字，例如將 "origin" 替換成 "***"
            .addInterceptor(LogMorphInterceptor(mapOf("origin" to "***")))
            .build()

        val request = Request.Builder()
            .url("https://httpbin.org/get")
            .build()

        client.newCall(request).execute().use { response ->
            // Request 會被攔截並在 Logcat 印出美化後的 Log
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
