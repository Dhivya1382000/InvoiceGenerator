package com.nithra.invoice_generator_tool.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceDynamicItemBinding
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData

class InvoiceAddedItemDataAdapter(
    var listOfGetInvoicelist: MutableList<InvoiceOfflineDynamicData>,
    var onItemClick: (InvoiceOfflineDynamicData,Int) -> Unit,
    var onShowItem: (ActivityInvoiceDynamicItemBinding,InvoiceOfflineDynamicData) -> Unit,
    var OnEditClick :(InvoiceOfflineDynamicData,Int) -> Unit,
) : RecyclerView.Adapter<InvoiceAddedItemDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ActivityInvoiceDynamicItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            println("itemname == ${listOfGetInvoicelist[position].item_name}")
            tvItemName.text = "${position + 1} . " + listOfGetInvoicelist[position].item_name
            tvItemQty.text = listOfGetInvoicelist[position].qty
            tvItemAmount.text = listOfGetInvoicelist[position].total_amt

            onShowItem(holder.binding,listOfGetInvoicelist[position])

            removeItem.setOnClickListener {
                onItemClick(listOfGetInvoicelist[position],position)
            }
            itemEdit.setOnClickListener {
                OnEditClick(listOfGetInvoicelist[position]!!,position)
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfGetInvoicelist.size
    }

    class ViewHolder(var binding: ActivityInvoiceDynamicItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}
