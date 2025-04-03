package com.nithra.invoice_generator_tool.pdfviewer.interfaces

import android.view.View
import com.nithra.invoice_generator_tool.pdfviewer.utils.PdfPageQuality
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

interface PdfViewController {

    fun getView(): View

    fun setup(file: File)

    fun setZoomEnabled(isZoomEnabled: Boolean)

    fun setMaxZoom(maxZoom: Float)

    fun setQuality(quality: PdfPageQuality)

    fun setOnPageChangedListener(onPageChangedListener: OnPageChangedListener?)

    fun goToPosition(position: Int)

    fun setDispatcher(dispatcher: CoroutineDispatcher)
}