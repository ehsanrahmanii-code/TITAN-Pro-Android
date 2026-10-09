package com.titan.pro

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingPanel: LinearLayout
    private lateinit var loadingText: TextView

    private val prefs by lazy { getSharedPreferences("titan_pro", Context.MODE_PRIVATE) }

    /** empty = use built-in offline dashboard from assets */
    private var serverUrl: String
        get() = prefs.getString("server_url", "") ?: ""
        set(value) = prefs.edit().putString("server_url", value.trim().trimEnd('/')).apply()

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
        settings.setSupportZoom(false)
        settings.mediaPlaybackRequiresUserGesture = false
        settings.userAgentString = settings.userAgentString + " TITAN-Pro-Android/75"

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                loadingPanel.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                loadingPanel.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    // fallback to embedded dashboard
                    loadEmbedded()
                }
            }
        }

        loadDashboard()
    }

    private fun loadEmbedded() {
        loadingText.setText(R.string.loading)
        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun loadDashboard() {
        loadingPanel.visibility = View.VISIBLE
        loadingText.setText(R.string.loading)
        val remote = serverUrl
        if (remote.isBlank()) {
            loadEmbedded()
        } else {
            webView.loadUrl(remote)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reload -> {
                loadDashboard()
                true
            }
            R.id.action_server -> {
                showServerDialog()
                true
            }
            R.id.action_about -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.about_body)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showServerDialog() {
        val input = EditText(this).apply {
            setText(if (serverUrl.isBlank()) "" else serverUrl)
            hint = getString(R.string.server_hint)
            setPadding(48, 32, 48, 32)
            setTextColor(0xFFE8ECFF.toInt())
            setHintTextColor(0xFF8E98C9.toInt())
        }
        val container = FrameLayout(this).apply {
            addView(input)
            setPadding(24, 8, 24, 0)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.server_dialog_title)
            .setMessage("خالی بگذارید تا داشبورد داخلی استفاده شود.")
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val url = input.text.toString().trim()
                if (url.isEmpty() || url.startsWith("http://") || url.startsWith("https://")) {
                    serverUrl = url
                    loadDashboard()
                } else {
                    Toast.makeText(this, R.string.server_hint, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
