package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.danjdt.pdfviewer.PdfViewer
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.InvoiceActivityPdfMainBinding
import com.nithra.invoice_generator_tool.pdfviewer.interfaces.OnErrorListener
import com.nithra.invoice_generator_tool.pdfviewer.interfaces.OnPageChangedListener
import com.nithra.invoice_generator_tool.pdfviewer.utils.PdfPageQuality
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class InvoiceFilePdfViewActivity : AppCompatActivity(), OnPageChangedListener, OnErrorListener {
    private val REQUEST_CODE_LOAD = 367
    private lateinit var binding: InvoiceActivityPdfMainBinding
    var jathagam = ""
    var pdf_path = ""
    var PDFjathagamName = ""
    var DataFrom = 0
    var pdfName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = InvoiceActivityPdfMainBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(binding.root)

        val pdf_path =intent.getStringExtra("pdf_path")
        PDFjathagamName = "" + intent.getStringExtra("InvoicePdfName")
        DataFrom = intent.getIntExtra("DataFrom", 0)

        setSupportActionBar(binding.toolbar)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
         pdfName = getFileNameFromUrl(jathagam)
        println("pdfName == $pdfName")
        println("pdfName == $pdf_path")
        InvoiceUtils.loadingProgress(this@InvoiceFilePdfViewActivity,InvoiceUtils.messageLoading,false).show()

        val pdfFile = File(pdf_path)
        if (pdfFile.exists()) {
            InvoiceUtils.loadingDialog.dismiss()
            val uri = Uri.fromFile(pdfFile)
            PdfViewer.Builder(binding.rootView, lifecycleScope)
                .setMaxZoom(3f)
                .setZoomEnabled(true)
                .quality(PdfPageQuality.QUALITY_1080)
                .build()
                .load(uri.toString())
        } else {
            Log.e("PDF Error", "File not found")
        }

        binding.rootView.setOnClickListener {

        }
    }
    fun getFileNameFromUrl(url: String): String {
        return url.substringAfterLast("/")
    }
    /*

        override fun onBackPressed() {
            super.onBackPressed()
            val resultIntent = Intent()
            setResult(1, resultIntent) // Set custom result code as 1
            finish()
        }
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOAD && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.rootView.removeAllViews()
                PdfViewer.Builder(binding.rootView, lifecycleScope)
                    .setMaxZoom(3f)
                    .setZoomEnabled(true)
                    .quality(PdfPageQuality.QUALITY_1080)
                    .setOnErrorListener(this)
                    .setOnPageChangedListener(this)
                    .setRenderDispatcher(Dispatchers.Default)
                    .build()
                    .load(uri)
            }
        }
    }

    override fun onPageChanged(page: Int, total: Int) {
        //   binding.tvCounter.text = getString(R.string.pdf_page_counter, page, total)
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
        if (InvoiceUtils.loadingDialog.isShowing){
            InvoiceUtils.loadingDialog.dismiss()
        }
        binding.tvCounter.text = "" + page + "/" + total
    }

    override fun onFileLoadError(e: Exception) {
        //Handle error ...
        if (InvoiceUtils.loadingDialog.isShowing){
            InvoiceUtils.loadingDialog.dismiss()
        }
        println("execption onFileLoad == ${e.toString()}")
        e.printStackTrace()
    }

    override fun onAttachViewError(e: Exception) {
        //Handle error ...
        if (InvoiceUtils.loadingDialog.isShowing){
            InvoiceUtils.loadingDialog.dismiss()
        }
        println("execption onAttach == ${e.toString()}")
        e.printStackTrace()
    }

    override fun onPdfRendererError(e: Exception) {
        //Handle error ...
        if (InvoiceUtils.loadingDialog.isShowing){
            InvoiceUtils.loadingDialog.dismiss()
        }
        println("execption onPdfRenderer == ${e.toString()}")
        e.printStackTrace()
    }

    private fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            packageName,
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "நித்ரா ஜாதகம் PDF")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    }

    private fun sharePdf(context: Context, url: String) {
        val folder = File("${context.filesDir}/Aanmeega/pdf")
        if (!folder.exists()) folder.mkdirs()

        val fileName = "" + pdfName  // ✅ Ensure correct file extension
        val file = File(folder, fileName)

        CoroutineScope(Dispatchers.IO).launch {
            val isDownloaded = downloadShareFile(url, file)
            withContext(Dispatchers.Main) {
                if (isDownloaded) {
                    if (InvoiceUtils.loadingDialog.isShowing){
                        InvoiceUtils.loadingDialog.dismiss()
                    }
                    sharePdfFile(context, file)
                } else {
                    //  Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun downloadShareFile(fileUrl: String, file: File): Boolean {
        return try {
            val url = URL(fileUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(file)

            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            connection.disconnect()

            true // ✅ Return success
        } catch (e: Exception) {
            e.printStackTrace()
            false // ❌ Return failure
        }
    }

}
