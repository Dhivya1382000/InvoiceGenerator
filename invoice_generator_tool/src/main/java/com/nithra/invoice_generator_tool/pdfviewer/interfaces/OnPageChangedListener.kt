package com.nithra.invoice_generator_tool.pdfviewer.interfaces

interface OnPageChangedListener {

    fun onPageChanged(page : Int, total : Int)
}