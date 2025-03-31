package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceWebviewLoadBinding

class InvoiceWebviewLoadActivity : AppCompatActivity() {
    lateinit var binding : ActivityInvoiceWebviewLoadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     //   setContentView(R.layout.activity_invoice_webview_load)
        binding = ActivityInvoiceWebviewLoadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable JavaScript (if needed)
        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true

        // Ensure links open in the WebView itself
        binding.webView.webViewClient = WebViewClient()

        // ✅ Load a URL
        binding.webView.loadUrl("https://www.nithra.mobi/privacy.php")


    }
}