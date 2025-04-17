package com.nithra.invoice_generator_tool.adapter

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceDynamicItemNewBinding
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData

class InvoiceAddedItemDataAdapter(
    var listOfGetInvoicelist: MutableList<InvoiceOfflineDynamicData>,
    var onItemClick: (InvoiceOfflineDynamicData, Int) -> Unit,
    var onShowItem: (ActivityInvoiceDynamicItemNewBinding, InvoiceOfflineDynamicData) -> Unit,
    var OnEditClick: (InvoiceOfflineDynamicData, Int, MutableList<InvoiceOfflineDynamicData>) -> Unit,
) : RecyclerView.Adapter<InvoiceAddedItemDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ActivityInvoiceDynamicItemNewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            println("itemname == ${listOfGetInvoicelist[position].item_name}")
           // tvItemName.text = "${position + 1} . " + listOfGetInvoicelist[position].item_name
            invoiceItemNo.text = "${ position + 1 }."
            tvItemName.text = "" + listOfGetInvoicelist[position].item_name
            val coloredText = SpannableString("Qty : ${listOfGetInvoicelist[position].qty}")

            coloredText.setSpan(
                ForegroundColorSpan(Color.RED), // You can use any color
                0,
                6,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
          /*  tvItemQty.text =
                "Qty : "+listOfGetInvoicelist[position].qty + " x " + listOfGetInvoicelist[position].amount*/
            tvItemQty.text = coloredText
            val AmtcoloredText = SpannableString("Rate : ${listOfGetInvoicelist[position].amount}")

            AmtcoloredText.setSpan(
                ForegroundColorSpan(Color.RED), // You can use any color
                0,
                6,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvItemRate.text = AmtcoloredText

            tvItemAmount.text = ""+listOfGetInvoicelist[position].total_amt

            onShowItem(holder.binding, listOfGetInvoicelist[position])

            removeItem.setOnClickListener {
                onItemClick(listOfGetInvoicelist[position], position)
            }
            itemEdit.setOnClickListener {
                OnEditClick(listOfGetInvoicelist[position]!!, position, listOfGetInvoicelist)
            }
        }
    }



    override fun getItemCount(): Int {
        return listOfGetInvoicelist.size
    }

    class ViewHolder(var binding: ActivityInvoiceDynamicItemNewBinding) :
        RecyclerView.ViewHolder(binding.root)

}
