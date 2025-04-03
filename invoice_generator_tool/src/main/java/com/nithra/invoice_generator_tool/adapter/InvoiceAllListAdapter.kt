package com.nithra.invoice_generator_tool.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.activity.InvoicePdfViewActivity
import com.nithra.invoice_generator_tool.databinding.FragmentInvoiceTabContentBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceAllListAdapter(
    var context : Context,
    private val itemList: MutableList<InvoiceGetInvoiceList>
) :
    RecyclerView.Adapter<InvoiceAllListAdapter.InvoiceViewHolder>() {

    class InvoiceViewHolder(var binding: FragmentInvoiceTabContentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = FragmentInvoiceTabContentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val item = itemList[position]
        holder.binding.apply {
            InvoiceAmount.text = "" + item.totalInvoiceAmt
            InvoiceNo.text = "" + item.invoiceNumber
            if (item.invoiceDate != null){
                val createDate = formatDate("" + item.invoiceDate)
                tvCreatedOn.text = "" + createDate
            }

            if (item.amtType != null){
                if (item.amtType!! == 1) {
                    tvStatus.text = "Paid"
                    InvoiceDateCard.visibility = View.GONE
                    InvoiceDateCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.invoice_lite_green))
                    InvoiceDueStatus.setTextColor(ContextCompat.getColor(context, R.color.invoice_green))
                } else if (item.amtType!! == 2) {
                    tvStatus.text = "Un paid"
                    InvoiceDateCard.visibility = View.GONE
                    InvoiceDateCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.invoice_lite_red))
                    InvoiceDueStatus.setTextColor(ContextCompat.getColor(context, R.color.invoice_red))
                } else if (item.amtType!! == 3) {
                    tvStatus.text = "Partially Paid"
                    InvoiceDateCard.visibility = View.GONE
                    InvoiceDateCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.invoice_lite_blue))
                    InvoiceDueStatus.setTextColor(ContextCompat.getColor(context, R.color.invoice_peack_green))
                }
            }
            if (item.clientName != null){
                tvUserName.text = item.clientName
            }
            val pdfFileUrl =  item.pdf
            val bussinessName =  item.bussinessName
            holder.itemView.setOnClickListener {
                if (pdfFileUrl!!.isNotEmpty()) {
                    val intent = Intent(context, InvoicePdfViewActivity::class.java)
                    intent.putExtra("InvoicePdfLink", pdfFileUrl)
                    intent.putExtra("InvoicePdfName", bussinessName)
                    context.startActivity(intent)
                }
            }
        }
    }

    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }

    override fun getItemCount(): Int = itemList.size
}