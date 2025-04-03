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
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceBusinessItemlistBinding
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceExpenseItemlistBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseDataList
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceMasterAdapter<T>(
    var activity: Context,
    var filteredList: MutableList<T>,
    var updatedSearchText: String,
    var invoicemasterclick: InvoicemasterClick,
    var fromInvoice: Int,
    var fromClick: Int,
    var onAddItemClick: (T) -> Unit,
    var onDeleteItem: (Int, Int, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_STATE = 0
        const val VIEW_TYPE_COMPANY = 1
        const val VIEW_TYPE_EXPENSE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (filteredList[position]) {
            is InvoiceGetDataMasterArray.GetStateList -> VIEW_TYPE_STATE
            is InvoiceGetDataMasterArray.GetIndustrialList -> VIEW_TYPE_STATE
            is InvoiceGetDataMasterArray.GstList -> VIEW_TYPE_STATE
            is InvoiceGetDataMasterArray.GetCompanyDetailList -> VIEW_TYPE_COMPANY
            is InvoiceGetDataMasterArray.GetClientDetails -> VIEW_TYPE_COMPANY
            is InvoiceGetDataMasterArray.GetItemList -> VIEW_TYPE_COMPANY
            is InvoiceGetExpenseDataList.DataList -> VIEW_TYPE_EXPENSE
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

            VIEW_TYPE_EXPENSE -> {
                val binding = ActivityInvoiceExpenseItemlistBinding.inflate(
                    LayoutInflater.from(activity), parent, false
                )
                ExpenseViewHolder(binding)
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
                invoicemasterclick.onItemClick(clikStateName, clikStateId, fromClick, position)
            }

        } else if (holder is BusinessViewHolder) {
            var clickDataId = 0
            var clickDeleteDataAction = ""
            when (item) {
                is InvoiceGetDataMasterArray.GetCompanyDetailList -> {
                    clickDataId = item.companyId!!
                    clikStateName = item.bussinessName!!
                    clickDeleteDataAction = "deleteCompanyDetails"
                    holder.binding.listOfNumbers.text = listCount
                    holder.binding.invoiceCustomerName.text = item.bussinessName
                    holder.binding.invoiceCustomerMobile.text = item.mobile
                    if (fromInvoice == 1) {
                        holder.binding.AddInvoice.visibility = View.VISIBLE
                        holder.binding.menuIcon.visibility = View.GONE
                    } else if (fromInvoice == 2) {
                        holder.binding.AddInvoice.visibility = View.GONE
                        holder.binding.menuIcon.visibility = View.GONE
                    } else {
                        holder.binding.AddInvoice.visibility = View.GONE
                        holder.binding.menuIcon.visibility = View.VISIBLE
                    }
                }

                is InvoiceGetDataMasterArray.GetClientDetails -> {
                    clickDataId = item.clientId!!
                    clickDeleteDataAction = "deleteClientDetails"
                    holder.binding.listOfNumbers.text = listCount
                    holder.binding.invoiceCustomerName.text = item.name
                    holder.binding.invoiceCustomerMobile.text = item.mobile
                    if (fromInvoice == 1) {
                        holder.binding.AddInvoice.visibility = View.VISIBLE
                        holder.binding.menuIcon.visibility = View.GONE
                    } else {
                        holder.binding.AddInvoice.visibility = View.GONE
                        holder.binding.menuIcon.visibility = View.VISIBLE
                    }
                }

                is InvoiceGetDataMasterArray.GetItemList -> {
                    clickDataId = item.itemId!!
                    clickDeleteDataAction = "deleteItemDetails"
                    holder.binding.listOfNumbers.text = listCount
                    holder.binding.invoiceCustomerName.text = item.itemName
                    holder.binding.invoiceCustomerMobile.text = " ₹ "+item.amount
                    if (fromInvoice == 1) {
                        holder.binding.AddInvoice.visibility = View.VISIBLE
                        holder.binding.menuIcon.visibility = View.GONE
                    } else {
                        holder.binding.AddInvoice.visibility = View.GONE
                        holder.binding.menuIcon.visibility = View.VISIBLE
                    }
                }
            }
            holder.binding.AddInvoice.setOnClickListener {
                onAddItemClick(item)
            }
            holder.itemView.setOnClickListener {
                invoicemasterclick.onItemClick(clikStateName, clickDataId, fromClick, position)
            }
            holder.binding.menuIcon.setOnClickListener {
                showPopupMenu(it, position, clikStateName, clickDataId, clickDeleteDataAction)
            }

        } else if (holder is ExpenseViewHolder) {
            var clickDataId = 0
            when (item) {
                is InvoiceGetExpenseDataList.DataList -> {
                    clickDataId = item.invoiceId!!
                    val createDate = formatDate("" + item.date)
                    holder.binding.ExpItemName.text = item.itemName
                    holder.binding.ExpInvoiceNo.text = item.invNumber
                    holder.binding.ExpCreateDate.text = "" + createDate
                    holder.binding.ExpAmount.text = " ₹ " + item.amount
                    holder.binding.ExpSellerName.text = "" + item.sellerName
                    holder.binding.businessName.text = "" + item.bussinessName
                }
            }
            holder.itemView.setOnClickListener {

            }
        }

    }

    private fun showPopupMenu(
        view: View?,
        position: Int,
        clikStateName: String,
        clickDataId: Int,
        clickDeleteDataAction: String
    ) {
        val popupMenu = PopupMenu(view!!.context, view)
        popupMenu.inflate(R.menu.invoice_list_pop)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    //Toast.makeText(view.context, "Edit item $position", Toast.LENGTH_SHORT).show()
                    invoicemasterclick.onItemClick(clikStateName, clickDataId, fromClick, position)
                    true
                }

                R.id.delete -> {
                    // Toast.makeText(view.context, "Delete item $position", Toast.LENGTH_SHORT).show()
                    println("dele === $clickDataId")
                    onDeleteItem(clickDataId, position, clickDeleteDataAction)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()

    }

    fun DeleteNotify(position: Int) {
        filteredList.removeAt(position) // Remove from list
        notifyItemRemoved(position) // Notify adapter
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
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

    class ExpenseViewHolder(var binding: ActivityInvoiceExpenseItemlistBinding) :
        RecyclerView.ViewHolder(binding.root)
}
