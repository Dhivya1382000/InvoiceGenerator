//package com.danjdt.pdfviewer
package com.nithra.invoice_generator_tool.pdfviewer

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.RawRes
import com.nithra.invoice_generator_tool.pdfviewer.decoder.FileLoader
import com.nithra.invoice_generator_tool.pdfviewer.interfaces.OnErrorListener
import com.nithra.invoice_generator_tool.pdfviewer.interfaces.OnPageChangedListener
import com.nithra.invoice_generator_tool.pdfviewer.interfaces.PdfViewController
import com.nithra.invoice_generator_tool.pdfviewer.utils.PdfPageQuality
import com.nithra.invoice_generator_tool.pdfviewer.view.PdfViewControllerImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

class InvoicePdfViewer private constructor(
    pdfViewController: PdfViewController,
    rootView: ViewGroup,
    private var scope: CoroutineScope,
    private val errorListener: OnErrorListener? = null
) : PdfViewController by pdfViewController {

    private val context: Context by lazy { rootView.context }

    init {
        try {
            rootView.addView(getView())
        } catch (e: IOException) {
            errorListener?.onAttachViewError(e)
        }
    }

    private fun display(file: File) {
        try {
            setup(file)
        } catch (e: Exception) {
            when (e) {
                is IOException, is SecurityException -> errorListener?.onPdfRendererError(e)
                else -> errorListener?.onAttachViewError(e)
            }
        }
    }

    fun load(file: File) {
        display(file)
    }

    fun load(uri: Uri) {
        scope.launch(Dispatchers.Main.immediate) {
            runCatching {
                FileLoader.loadFile(context, uri)
            }.onFailure {
                errorListener?.onFileLoadError(Exception())
            }.onSuccess {
                display(it)
            }
        }
    }

    fun load(@RawRes resId: Int) {
        scope.launch(Dispatchers.Main.immediate) {
            runCatching {
                FileLoader.loadFile(context, resId)
            }.onFailure {
                errorListener?.onFileLoadError(Exception())
            }.onSuccess {
                display(it)
            }
        }
    }

    fun load(input: InputStream) {
        scope.launch(Dispatchers.Main.immediate) {
            runCatching {
                FileLoader.loadFile(context, input)
            }.onFailure {
                errorListener?.onFileLoadError(Exception())
            }.onSuccess {
                display(it)
            }
        }
    }

    fun load(url: String) {
        scope.launch(Dispatchers.Main.immediate) {
            runCatching {
                FileLoader.loadFile(context, url)
            }.onFailure {
                errorListener?.onFileLoadError(Exception())
            }.onSuccess {
                display(it)
            }
        }
    }

    class Builder(
        private val rootView: ViewGroup,
        private val scope: CoroutineScope,
    ) {

        private val context: Context = rootView.context

        private var pdfViewController: PdfViewController = PdfViewControllerImpl(context, scope)

        private var quality: PdfPageQuality = PdfPageQuality.QUALITY_1080

        private var maxZoom: Float = 3f

        private var isZoomEnabled: Boolean = true

        private var onPageChangedListener: OnPageChangedListener? = null

        private var onErrorListener: OnErrorListener? = null

        private var dispatcher: CoroutineDispatcher = Dispatchers.IO

        fun controller(controller: PdfViewController): Builder {
            this.pdfViewController = controller
            return this
        }

        fun setZoomEnabled(isEnabled: Boolean): Builder {
            this.isZoomEnabled = isEnabled
            return this
        }

        fun setMaxZoom(maxZoom: Float): Builder {
            this.maxZoom = maxZoom
            return this
        }

        fun setOnPageChangedListener(onPageChangedListener: OnPageChangedListener): Builder {
            this.onPageChangedListener = onPageChangedListener
            return this
        }

        fun quality(quality: PdfPageQuality): Builder {
            this.quality = quality
            return this
        }

        fun setOnErrorListener(onErrorListener: OnErrorListener): Builder {
            this.onErrorListener = onErrorListener
            return this
        }

        fun setRenderDispatcher(dispatcher: CoroutineDispatcher): Builder {
            this.dispatcher = dispatcher
            return this
        }

        fun build(): InvoicePdfViewer {
            val pdfViewer = InvoicePdfViewer(pdfViewController, rootView, scope, onErrorListener)
            pdfViewController.setQuality(quality)
            pdfViewController.setZoomEnabled(isZoomEnabled)
            pdfViewController.setMaxZoom(maxZoom)
            pdfViewController.setOnPageChangedListener(onPageChangedListener)
            pdfViewController.setDispatcher(dispatcher)
            return pdfViewer
        }
    }
}