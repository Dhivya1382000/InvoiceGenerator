package com.nithra.invoice_generator_tool.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.activity.InvoicePdfViewActivity
import com.nithra.invoice_generator_tool.databinding.FragmentInvoiceTabContentBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceAllListAdapter(
    var context : Context,
    val itemList: MutableList<InvoiceGetInvoiceList>
) :
    RecyclerView.Adapter<InvoiceAllListAdapter.InvoiceViewHolder>() {

        var preference = InvioceSharedPreference()

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
            InvoiceAmount.text = " ₹ " + item.totalInvoiceAmt
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
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.invoice_green))
                } else if (item.amtType!! == 2) {
                    tvStatus.text = "Un paid"
                    InvoiceDateCard.visibility = View.GONE
                    InvoiceDateCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.invoice_lite_red))
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.invoice_red))
                } else if (item.amtType!! == 3) {
                    tvStatus.text = "Partially Paid"
                    InvoiceDateCard.visibility = View.GONE
                    InvoiceDateCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.invoice_lite_blue))
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.invoice_peack_green))
                }
            }

            if (item.clientName != ""){
                tvUserName.text = item.clientName
            }else{
                tvUserName.text = "test"
            }
            if (item.bussinessName != ""){
                tvBusinessName.text = item.bussinessName
            }
            val pdfFileUrl =  item.pdf
            var bussinessName = ""
            if (item.bussinessName != null){
                 bussinessName =  ""+item.bussinessName
            }

            holder.itemView.setOnClickListener {
                if (pdfFileUrl!!.isNotEmpty()) {
                    val intent = Intent(context, InvoicePdfViewActivity::class.java)
                    intent.putExtra("InvoicePdfLink", pdfFileUrl)
                    intent.putExtra("InvoicePdfName", bussinessName)
                    intent.putExtra("INVOICE_EDIT_ID", item.invoiceId)
                    val invoiceObject = itemList[position] // Data class
                    val jsonString = Gson().toJson(invoiceObject) // Convert to JSON

                    preference.putString(context,"INVOICE_PDF_LIST_DATA",jsonString)
                    context.startActivity(intent)
                }
            }
            holder.binding.menuIcon.setOnClickListener {
                showPopupMenu(it,position)
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

    private fun showPopupMenu(view: View,position: Int) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.invoice_list_pop)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    true
                }

                R.id.delete -> {
                    AlertDialog.Builder(view.context)
                        .setTitle("Delete Invoice")
                        .setMessage("Are you sure you want to delete this invoice?")
                        .setPositiveButton("Yes") { _, _ ->
                            itemList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, itemList.size)
                        }
                        .setNegativeButton("No", null)
                        .show()
                    true
                }


                else -> false
            }
        }
        popupMenu.show()

    }
}