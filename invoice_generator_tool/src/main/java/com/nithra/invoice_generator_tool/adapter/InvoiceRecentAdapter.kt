package com.nithra.invoice_generator_tool.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceRecentItemlistBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceRecentAdapter(
    var activity: Context,
    var listOfGetInvoicelist: MutableList<InvoiceGetInvoiceList>
) : RecyclerView.Adapter<InvoiceRecentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ActivityInvoiceRecentItemlistBinding.inflate(
            LayoutInflater.from(activity), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            InvoiceCusName.text = listOfGetInvoicelist[position].clientName
            InvoiceNo.text = "." + listOfGetInvoicelist[position].invoiceNumber
            InvoiceAmount.text = ""+listOfGetInvoicelist[position].paidAmt
            if (listOfGetInvoicelist[position].invoiceDate != null){
                val createDate = formatDate("" + listOfGetInvoicelist[position].invoiceDate)
                createdDate.text = "created on " + createDate

            }
        }
    }

    override fun getItemCount(): Int {
        if (listOfGetInvoicelist.size >= 3) {
            return 3
        } else {
            return listOfGetInvoicelist.size
        }

    }

    class ViewHolder(var binding: ActivityInvoiceRecentItemlistBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }
}
