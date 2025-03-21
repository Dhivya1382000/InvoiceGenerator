package com.nithra.invoice_generator_tool.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessDetailFormActivity
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick

class InvoiceMasterAdapter<T>(
    var filteredList: MutableList<T>,
    var updatedSearchText: String,
    var invoicemasterclick: InvoicemasterClick,
    var fromClick:Int
) : RecyclerView.Adapter<InvoiceMasterAdapter.ViewHolder<T>>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InvoiceMasterAdapter.ViewHolder<T> {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceMasterAdapter.ViewHolder<T>, position: Int) {
        val item = filteredList[position]
        var clikStateName = ""
        var clikStateId  = 0
        when (item) {
            is InvoiceGetDataMasterArray.GetStateList  -> {
                 clikStateName = item.english ?: "" // Cast your POJO to the expected type
                 clikStateId = item.id!!
            }
            is InvoiceGetDataMasterArray.GetIndustrialList -> {
                clikStateName = item.industrial ?: "" // Cast your POJO to the expected type
                clikStateId = item.id!!
            }
            else -> {
                println("Unknown list type")
            }
        }

        holder.textView.text = getHighlightedText(clikStateName.toString(), updatedSearchText)

        holder.itemView.setOnClickListener {
            invoicemasterclick.onItemClick(clikStateName.toString(), clikStateId!!,fromClick)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }
    private fun getHighlightedText(fullText: String, updatedSearchText: String): SpannableString? {
        val spannableString = SpannableString(fullText)
        val startIndex = fullText.indexOf(updatedSearchText, ignoreCase = true)
        if (startIndex >= 0) {
            val endIndex = startIndex + updatedSearchText.length
            spannableString.setSpan(
                ForegroundColorSpan(Color.RED), // Set highlight color
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD), // Set bold style
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannableString
    }
    class ViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(android.R.id.text1)
    }
}
