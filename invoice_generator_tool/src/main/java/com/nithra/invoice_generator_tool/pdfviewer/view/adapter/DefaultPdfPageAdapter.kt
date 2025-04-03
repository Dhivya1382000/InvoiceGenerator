package com.nithra.invoice_generator_tool.pdfviewer.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.nithra.invoice_generator_tool.databinding.InvoicePdfPageBinding
import com.nithra.invoice_generator_tool.pdfviewer.utils.PdfPageQuality
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.File

class DefaultPdfPageAdapter(
    file: File,
    quality: PdfPageQuality,
    dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
) : PdfPagesAdapter<DefaultPdfPageViewHolder>(file, quality, dispatcher) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultPdfPageViewHolder {
            val view = InvoicePdfPageBinding.inflate(LayoutInflater.from(parent.context), parent, false).root
        return DefaultPdfPageViewHolder(view, scope, ::renderPage)
    }

    override fun onBindViewHolder(holder: DefaultPdfPageViewHolder, position: Int) {
        holder.bind(position)
    }
}