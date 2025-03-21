package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceCreateFormBinding

class InvoiceCreateFormActivity : AppCompatActivity() {
    lateinit var binding : ActivityInvoiceCreateFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceCreateFormBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}