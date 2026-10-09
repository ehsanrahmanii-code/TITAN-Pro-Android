package com.titan.pro

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingPanel: LinearLayout
    private lateinit var loadingText: TextView
    private val io = Executors.newSingleThreadExecutor()
    private val ui = Handler(Looper.getMainLooper())
    private var engineStarted = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        loadingPanel = findViewById(R.id.loadingPanel)
        loadingText = findViewById(R.id.loadingText)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.userAgentString = settings.userAgentString + " TITAN-Pro-Full/75"

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                loadingPanel.visibility = View.GONE
            }
        }

        startFullEngine()
    }

    private fun startFullEngine() {
        loadingPanel.visibility = View.VISIBLE
        loadingText.text = getString(R.string.loading_engine)
        io.execute {
            try {
                val py = Python.getInstance()
                val boot = py.getModule("titan_bootstrap")
                if (!engineStarted) {
                    boot.callAttr("start_titan", filesDir.absolutePath)
                    engineStarted = true
                }
                ui.post { loadingText.text = getString(R.string.waiting_engine) }
                val ready = boot.callAttr("wait_ready", "http://127.0.0.1:5000/health", 120.0).toBoolean()
                ui.post {
                    if (ready) {
                        webView.loadUrl("http://127.0.0.1:5000/")
                    } else {
                        loadingText.text = getString(R.string.engine_timeout)
                        // fallback embedded dashboard
                        webView.loadUrl("file:///android_asset/index.html")
                    }
                }
            } catch (e: Exception) {
                ui.post {
                    loadingText.text = getString(R.string.engine_error, e.message ?: "?")
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                    webView.loadUrl("file:///android_asset/index.html")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reload -> {
                webView.reload()
                true
            }
            R.id.action_about -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.about_full)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
