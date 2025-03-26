package com.nithra.invoice_generator_tool.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceRecentItemlistBinding

class InvoiceRecentAdapter(var activity: Context) : RecyclerView.Adapter<InvoiceRecentAdapter.ViewHolder>() {

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

    }

    override fun getItemCount(): Int {
     return 3
    }

    class ViewHolder(var binding: ActivityInvoiceRecentItemlistBinding) :
        RecyclerView.ViewHolder(binding.root)
}