package com.nithra.invoice_generator_tool.pdfviewer.view.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nithra.invoice_generator_tool.pdfviewer.renderer.PdfPageRenderer
import com.nithra.invoice_generator_tool.pdfviewer.utils.PdfPageQuality
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

abstract class PdfPagesAdapter<T : PdfPageViewHolder>(
    pdfFile: File,
    quality: PdfPageQuality,
    dispatcher: CoroutineDispatcher,
) : ListAdapter<Bitmap, T>(DiffCallback()) {

    private val pdfPageRenderer = PdfPageRenderer(pdfFile, quality, dispatcher)

    suspend fun renderPage(position: Int): Result<Bitmap> {
        return pdfPageRenderer.render(position)
    }

    override fun getItemCount(): Int {
        return pdfPageRenderer.pageCount
    }

    class DiffCallback: DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return oldItem === newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
           return oldItem.hashCode() == newItem.hashCode()
        }
    }
}
