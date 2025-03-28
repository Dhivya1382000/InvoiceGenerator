package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAllDataBinding
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceRecyclerviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceAllDataActivity : AppCompatActivity() {
    lateinit var binding: ActivityInvoiceAllDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     //   setContentView(R.layout.activity_invoice_all_data)

    }
}