package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAddItemFormBinding

class InvoiceAddItemFormActivity : AppCompatActivity() {
    lateinit var itemFormBinding : ActivityInvoiceAddItemFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemFormBinding = ActivityInvoiceAddItemFormBinding.inflate(layoutInflater)
        setContentView(itemFormBinding.root)

        setSupportActionBar(itemFormBinding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

            itemFormBinding.toolBarTitle.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

    }
}