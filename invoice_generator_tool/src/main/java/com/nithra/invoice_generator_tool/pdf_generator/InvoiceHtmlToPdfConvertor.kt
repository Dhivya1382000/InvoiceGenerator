package com.nithra.invoice_generator_tool.pdf_generator

import android.content.Context
import android.os.Build
import android.print.InvoicePdfPrinter
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.annotation.UiThread
import java.io.File

class InvoiceHtmlToPdfConvertor(private val context: Context) {

    private var baseUrl: String? = null
    private var enableJavascript: Boolean? = null

    fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    fun setJavaScriptEnabled(flag: Boolean) {
        this.enableJavascript = flag
    }

    lateinit var htmlString: String
    @UiThread
    fun convert(
        pdfLocation: File,
        htmlString: String,
        onPdfGenerationFailed: PdfGenerationFailedCallback? = null,
        onPdfGenerated: PdfGeneratedCallback,
    ) {

        // maintain pdf generation status
        var pdfGenerationStarted = false
        try {

            // create new webview
            val pdfWebView = WebView(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pdfWebView.settings.safeBrowsingEnabled = false
            }

            // Code to use enableJavascript and set JavaScript enabled in WebView
            enableJavascript?.let { isEnabled ->
                pdfWebView.settings.javaScriptEnabled = isEnabled
            }

            /* // set webview enable/ disable javascript property
            enableJavascript?.let { pdfWebView.settings.javaScriptEnabled = it }
*/
// To set enableJavascript to true
            setJavaScriptEnabled(true)

            // job name
            val jobName = Math.random().toString()

            // generate pdf attributes and properties
            val attributes = getPrintAttributes()

            // generate print document adapter
            val printAdapter = getPrintAdapter(pdfWebView, jobName)

            pdfWebView.setOnLongClickListener(View.OnLongClickListener { true })
            pdfWebView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)

                    // some times progress provided by this method is wrong, that's why we are getting progress directly provided by WebView
                    val progress = pdfWebView.progress

                    // when page is fully loaded, start creating PDF
                    if (progress == 100 && !pdfGenerationStarted) {

                        // change the status of pdf generation
                        pdfGenerationStarted = true

                        // generate pdf
                        val pdfPrinter = InvoicePdfPrinter(attributes)
                        pdfPrinter.generate(printAdapter, pdfLocation, onPdfGenerated)
                    }
                }

            }
            this.htmlString =  htmlString.replace("<b>", "<b style='font-size: 30px;'>").replace("<th>", "<th style='padding: 15px;font-size: 20px'>")
                .replace("<td>", "<td style='padding: 15px;font-size: 30px'>")
            // load html in WebView when it's setup is completed
            pdfWebView.loadDataWithBaseURL(baseUrl, this.htmlString, "text/html", "utf-8", null)

        } catch (e: Exception) {
            onPdfGenerationFailed?.invoke(e)
        }
    }

    @SuppressWarnings("deprecation")
    private fun getPrintAdapter(pdfWebView: WebView, jobName: String): PrintDocumentAdapter {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pdfWebView.createPrintDocumentAdapter(jobName)
        } else {
            pdfWebView.createPrintDocumentAdapter()
        }
    }

    private fun getPrintAttributes(pdfPageSize: PrintAttributes.MediaSize = PrintAttributes.MediaSize.ISO_A4): PrintAttributes {
        return PrintAttributes.Builder().apply {
            setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            setMediaSize(pdfPageSize)
            setResolution(PrintAttributes.Resolution("pdf", Context.COMPANION_DEVICE_SERVICE, 600, 600))
            setMinMargins(PrintAttributes.Margins(10,30,10,10))

        }.build()
    }

}

private typealias PdfGeneratedCallback = (File) -> Unit
private typealias PdfGenerationFailedCallback = (Exception) -> Unit