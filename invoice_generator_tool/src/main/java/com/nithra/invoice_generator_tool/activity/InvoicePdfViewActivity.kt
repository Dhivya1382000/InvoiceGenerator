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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen.Companion.refreshRecentList
import com.nithra.invoice_generator_tool.databinding.InvoiceActivityPdfMainBinding
import com.nithra.invoice_generator_tool.pdfviewer.InvoicePdfViewer
import com.nithra.invoice_generator_tool.pdfviewer.interfaces.OnErrorListener
import com.nithra.invoice_generator_tool.pdfviewer.interfaces.OnPageChangedListener
import com.nithra.invoice_generator_tool.pdfviewer.utils.PdfPageQuality
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
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

class   InvoicePdfViewActivity : AppCompatActivity(), OnPageChangedListener, OnErrorListener {
    private val REQUEST_CODE_LOAD = 367
    private lateinit var binding: InvoiceActivityPdfMainBinding
    var jathagam = ""
    var pdf_path = ""
    var PDFjathagamName = ""
    var DataFrom = 0
    var pdfName = ""
    var InvoiceId = 0
    var preference = InvioceSharedPreference()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = InvoiceActivityPdfMainBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(binding.root)

        jathagam = "" + intent.getStringExtra("InvoicePdfLink")
        PDFjathagamName = "" + intent.getStringExtra("InvoicePdfName")
        DataFrom = intent.getIntExtra("DataFrom", 0)
        InvoiceId = intent.getIntExtra("INVOICE_EDIT_ID", 0)

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
        InvoiceUtils.loadingProgress(this@InvoicePdfViewActivity,InvoiceUtils.messageLoading,false).show()

        InvoicePdfViewer.Builder(binding.rootView, lifecycleScope)
            .setMaxZoom(3f)
            .setZoomEnabled(true)
            .quality(PdfPageQuality.QUALITY_1080)
            .setOnErrorListener(this)
            .setOnPageChangedListener(this)
            .setRenderDispatcher(Dispatchers.Default)
            .build()
            .load(jathagam)

        binding.rootView.setOnClickListener {

        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back press handling
               // Toast.makeText(this@InvoicePdfViewActivity, "Custom Back Pressed!", Toast.LENGTH_SHORT).show()
                refreshRecentList = true
                finish()
            }
        })

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


    fun downloadPdf(context: Context, fileUrl: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
                setTitle("Downloading $fileName")
                setDescription("Downloading PDF file...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/pdf")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            // Show "Download Started" indication
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()

            // Register BroadcastReceiver for download completion
            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        Toast.makeText(context, "Download Completed: $fileName", Toast.LENGTH_LONG)
                            .show()
                        context.unregisterReceiver(this) // Unregister receiver after download is complete
                    }
                }
            }

            context.registerReceiver(
                onCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )

        } catch (e: Exception) {
            e.printStackTrace()
            //  Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.invoice_pdf_menu, menu)
        val navEdit = menu!!.findItem(R.id.nav_edit)
        if (InvoiceId == 0){
            navEdit.setVisible(false)
        }else{
            navEdit.setVisible(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_pdf) {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoicePdfViewActivity)) {
                Toast.makeText(
                    this@InvoicePdfViewActivity, "தங்களின் இணையதள சேவையை சரிபார்க்க",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
               // val jathagaFileName = PDFjathagamName
                downloadPdf(
                    this@InvoicePdfViewActivity,
                    jathagam,
                    pdfName
                )
            }
            return true
        } else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }else if (item.itemId == R.id.nav_edit) {
            val intent = Intent(this@InvoicePdfViewActivity,InvoiceCreateFormActivity::class.java)
            intent.putExtra("INVOICE_EDIT_ID",InvoiceId)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOAD && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.rootView.removeAllViews()
                InvoicePdfViewer.Builder(binding.rootView, lifecycleScope)
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
