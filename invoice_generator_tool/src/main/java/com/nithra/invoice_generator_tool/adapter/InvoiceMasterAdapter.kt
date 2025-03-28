package com.nithra.invoice_generator_tool.adapter

import android.content.Context
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
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceBusinessItemlistBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick

class InvoiceMasterAdapter<T>(
    var activity: Context,
    var filteredList: MutableList<T>,
    var updatedSearchText: String,
    var invoicemasterclick: InvoicemasterClick,
    var fromInvoice:Int,
    var fromClick: Int,
   var onAddItemClick: (T) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_STATE = 0
        const val VIEW_TYPE_COMPANY = 1
        const val VIEW_TYPE_GST = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (filteredList[position]) {
            is InvoiceGetDataMasterArray.GetStateList -> VIEW_TYPE_STATE
            is InvoiceGetDataMasterArray.GetIndustrialList -> VIEW_TYPE_STATE
            is InvoiceGetDataMasterArray.GstList -> VIEW_TYPE_STATE
            is InvoiceGetDataMasterArray.GetCompanyDetailList -> VIEW_TYPE_COMPANY
            is InvoiceGetDataMasterArray.GetClientDetails -> VIEW_TYPE_COMPANY
            is InvoiceGetDataMasterArray.GetItemList -> VIEW_TYPE_COMPANY
            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_STATE -> {
                val view = LayoutInflater.from(activity)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                ViewHolder(view)
            }
            VIEW_TYPE_COMPANY -> {
                val binding = ActivityInvoiceBusinessItemlistBinding.inflate(
                    LayoutInflater.from(activity), parent, false
                )
                BusinessViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unknown View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = filteredList[position]
        var clikStateName = ""
        var clikStateId = 0
        val post = position + 1
        val listCount = if (post < 10) {
            "0" + post
        } else {
            "" + post
        }
        /*  when (item) {
              is InvoiceGetDataMasterArray.GetStateList  -> {
                   clikStateName = item.english ?: "" // Cast your POJO to the expected type
                   clikStateId = item.id!!
              }
              is InvoiceGetDataMasterArray.GetIndustrialList -> {
                  clikStateName = item.industrial ?: "" // Cast your POJO to the expected type
                  clikStateId = item.id!!
              }
              is InvoiceGetDataMasterArray.GstList -> {
                  clikStateName = item.gst ?: "" // Cast your POJO to the expected type
                  clikStateId = item.id!!
              }
              else -> {
                  println("Unknown list type")
              }
          }*/
        if (holder is ViewHolder) {
            when (item) {
                is InvoiceGetDataMasterArray.GetStateList -> {
                    clikStateName = item.english ?: "" // Cast your POJO to the expected type
                    clikStateId = item.id!!
                }
                is InvoiceGetDataMasterArray.GetIndustrialList -> {
                    clikStateName = item.industrial ?: "" // Cast your POJO to the expected type
                    clikStateId = item.id!!
                }

                is InvoiceGetDataMasterArray.GstList -> {
                    clikStateName = item.gst ?: "" // Cast your POJO to the expected type
                    clikStateId = item.id!!
                }

                else -> {
                    println("Unknown list type")
                }

            }
            holder.textView.text = getHighlightedText(clikStateName, updatedSearchText)

            holder.itemView.setOnClickListener {
                invoicemasterclick.onItemClick(clikStateName, clikStateId!!, fromClick)
            }

        } else if (holder is BusinessViewHolder) {
            var clickDataId = 0
            when (item) {
                is InvoiceGetDataMasterArray.GetCompanyDetailList -> {
                    clickDataId = item.companyId!!
                    holder.binding.listOfNumbers.text = listCount
                    holder.binding.invoiceCustomerName.text = item.bussinessName
                    holder.binding.invoiceCustomerMobile.text = item.mobile
                    if (fromInvoice == 1){
                        holder.binding.AddInvoice.visibility = View.VISIBLE
                    }else{
                        holder.binding.AddInvoice.visibility = View.GONE
                    }
                }
                is InvoiceGetDataMasterArray.GetClientDetails -> {
                    clickDataId = item.clientId!!
                    holder.binding.listOfNumbers.text = listCount
                    holder.binding.invoiceCustomerName.text = item.name
                    holder.binding.invoiceCustomerMobile.text = item.mobile
                    if (fromInvoice == 1){
                        holder.binding.AddInvoice.visibility = View.VISIBLE
                    }else{
                        holder.binding.AddInvoice.visibility = View.GONE
                    }
                }
                is InvoiceGetDataMasterArray.GetItemList -> {
                    clickDataId = item.itemId!!
                    holder.binding.listOfNumbers.text = listCount
                    holder.binding.invoiceCustomerName.text = item.itemName
                    holder.binding.invoiceCustomerMobile.text = item.amount + "/" + "kg"
                    if (fromInvoice == 1){
                        holder.binding.AddInvoice.visibility = View.VISIBLE
                    }else{
                        holder.binding.AddInvoice.visibility = View.GONE
                    }
                }
            }
            holder.binding.AddInvoice.setOnClickListener {
                onAddItemClick(item)
            }
            holder.itemView.setOnClickListener {
                invoicemasterclick.onItemClick(clikStateName, clickDataId, fromClick)
            }
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(android.R.id.text1)
    }


    class BusinessViewHolder(var binding: ActivityInvoiceBusinessItemlistBinding) :
        RecyclerView.ViewHolder(binding.root)


}
