package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.IOException


class InvoicePdfPrinter(private val printAttributes: PrintAttributes) {

    fun generate(
        printAdapter: PrintDocumentAdapter,
        path: File,
        onPdfGenerated: (File) -> Unit
    ) {

        if (path.exists()) {
            path.delete()
        }

        try {
            printAdapter.onLayout(null, printAttributes, null, object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                    printAdapter.onWrite(
                        arrayOf(PageRange.ALL_PAGES),
                        getOutputFile(path),
                        CancellationSignal(),
                        object : PrintDocumentAdapter.WriteResultCallback() {
                            override fun onWriteFinished(pages: Array<PageRange>) {
                                if (pages.isNotEmpty()) {
                                    val builder = PrintDocumentInfo.Builder(path.toString())
                                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                        .setPageCount(info.pageCount)
                                        .build()
                                    onPdfGenerated(path)
                                }


                                super.onWriteFinished(pages)



                            }

                        })
                }
            }, null)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getOutputFile(file: File): ParcelFileDescriptor? {
        val fileDirectory = file.parentFile
        if (fileDirectory != null && !fileDirectory.exists()) {
            fileDirectory.mkdirs()
        }
        try {
            file.createNewFile()
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
        } catch (e: IOException) {
            Log.e("TAG", "Failed to open ParcelFileDescriptor", e)
            throw e
        }
        return null
    }

    interface OnPdfGeneratedListener {
        fun onPdfGenerated(pdfFilePath: File)
    }

}