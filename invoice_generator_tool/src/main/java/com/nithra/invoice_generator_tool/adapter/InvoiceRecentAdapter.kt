package com.nithra.invoice_generator_tool.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.activity.InvoicePdfViewActivity
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceRecentItemlistBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceRecentAdapter(
    var activity: Context,
    var listOfGetInvoicelist: MutableList<InvoiceGetInvoiceList>
) : RecyclerView.Adapter<InvoiceRecentAdapter.ViewHolder>() {
var preference = InvioceSharedPreference()
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
            val reversedPosition =  listOfGetInvoicelist.size - 1 - position // Reverse the order
            val item = listOfGetInvoicelist[position]
            //InvoiceCusName.text =item.clientName
            InvoiceNo.text = "" +item.invoiceNumber
            if (item.amtType!! == 1) {
            //    InvoiceAmount.text = "Paid Amount - "+listOfGetInvoicelist[position].paidAmt
                InvoiceAmount.text = "Paid"
                InvoiceAmount.setTextColor(ContextCompat.getColor(activity, R.color.invoice_green))
            }else{
                InvoiceAmount.text = "Un paid"
                InvoiceAmount.setTextColor(ContextCompat.getColor(activity, R.color.invoice_red))
            }

            if (listOfGetInvoicelist[position].invoiceDate != null){
                val createDate = formatDate("" +item.invoiceDate)
                createdDate.text = "created on " + createDate
            }

            if (item.clientName != null){
                InvoiceCusName.text =item.clientName
            }

            val pdfFileUrl =  item.pdf

            var bussinessName = ""
            if (item.bussinessName != null){
                bussinessName =  ""+item.bussinessName
            }

            holder.itemView.setOnClickListener {
                if (pdfFileUrl!!.isNotEmpty()) {
                    val intent = Intent(activity, InvoicePdfViewActivity::class.java)
                    intent.putExtra("InvoicePdfLink", pdfFileUrl)
                    intent.putExtra("InvoicePdfName", bussinessName)
                    intent.putExtra("INVOICE_EDIT_ID", item.invoiceId)

                    val invoiceObject = listOfGetInvoicelist[position] // Data class
                    val jsonString = Gson().toJson(invoiceObject) // Convert to JSON
                    preference.putString(activity,"INVOICE_PDF_LIST_DATA",jsonString)

                    activity.startActivity(intent)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return listOfGetInvoicelist.size

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
