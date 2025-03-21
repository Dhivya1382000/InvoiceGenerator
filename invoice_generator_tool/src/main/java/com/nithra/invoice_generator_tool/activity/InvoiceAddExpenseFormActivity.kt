package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAddExpenseFormBinding

class InvoiceAddExpenseFormActivity : AppCompatActivity() {
    lateinit var binding : ActivityInvoiceAddExpenseFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceAddExpenseFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}