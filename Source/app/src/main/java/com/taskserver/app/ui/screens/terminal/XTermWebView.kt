package com.taskserver.app.ui.screens.terminal

import android.annotation.SuppressLint
import android.util.Base64
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun XTermWebView(
    modifier: Modifier = Modifier,
    viewModel: TerminalViewModel,
    onTerminalReady: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(isReady) {
        if (isReady) {
            viewModel.rawOutputFlow.collect { chunk ->
                val base64 = Base64.encodeToString(chunk, Base64.NO_WRAP)
                webViewRef?.post {
                    webViewRef?.evaluateJavascript("window.writeBase64ToTerminal('$base64');", null)
                }
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportZoom(false)
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }

                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()

                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun onData(data: String) {
                        viewModel.sendRawInteractiveBytes(data.toByteArray(Charsets.UTF_8))
                    }

                    @JavascriptInterface
                    fun onResize(cols: Int, rows: Int) {
                        viewModel.resizeTerminal(cols, rows)
                    }

                    @JavascriptInterface
                    fun onTerminalReady(cols: Int, rows: Int) {
                        viewModel.resizeTerminal(cols, rows)
                        viewModel.setTerminalReady()
                        webViewRef?.post {
                            isReady = true
                        }
                        onTerminalReady()
                    }
                }, "AndroidBridge")

                loadUrl("file:///android_asset/index.html")
                webViewRef = this
            }
        },
        onRelease = {
            webViewRef = null
            it.destroy()
        }
    )
}
