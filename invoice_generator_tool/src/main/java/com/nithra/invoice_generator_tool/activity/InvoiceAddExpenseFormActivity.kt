package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessDetailFormActivity.Companion._TAG
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen.Companion
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAddExpenseFormBinding
import com.nithra.invoice_generator_tool.fragment.InvoiceDatePickerDialog
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
@AndroidEntryPoint
class InvoiceAddExpenseFormActivity : AppCompatActivity() {
    lateinit var binding : ActivityInvoiceAddExpenseFormBinding
    var selectedDate = ""
    private val viewModel: InvoiceViewModel by viewModels()

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
        binding.InvoiceExpenseLay.setOnClickListener {
            val datePicker = InvoiceDatePickerDialog({ selectedDatePickerDate ->
                showDatePickerFormat(selectedDatePickerDate)
            }, binding.InvoiceExpenseDate.text.toString().trim())
            datePicker.show(supportFragmentManager, "datePicker")
        }

        viewModel.getAddedExpenseList.observe(this@InvoiceAddExpenseFormActivity){addedExpense->
            if (addedExpense.status.equals("success")){
                Toast.makeText(this@InvoiceAddExpenseFormActivity, ""+addedExpense.msg, Toast.LENGTH_SHORT).show()
            }
        }

        binding.InvoiceItemSaveCard.setOnClickListener {
            when {
                binding.InvoiceExpenseDate.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceAddExpenseFormActivity,
                        "Enter your business Name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceExpenseItemName.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceAddExpenseFormActivity,
                        "Enter your mobile number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceItemRate.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceAddExpenseFormActivity,
                        "Enter your business mobile number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceExpenseRemark.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceAddExpenseFormActivity,
                        "Enter your billing address1",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                else -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceAddExpenseFormActivity)) {
                        val map = HashMap<String, Any>()
                        map["action"] = "addExpenses"
                        map["user_id"] = "1227994"
                        map["date"] = "" + selectedDate
                        map["item_name"] = "" + binding.InvoiceExpenseItemName.text.toString().trim()
                        map["amount"] = "" + binding.InvoiceItemRate.text.toString().trim()
                        map["inv_number"] =
                            "" + binding.InvoiceNumber.text.toString().trim()
                        map["seller_name"] = ""+binding.InvoiceExpenseSellerName.text.toString().trim()
                        map["remark"] = "" + binding.InvoiceExpenseRemark.text.toString().trim()

                        println("InvoiceRequest - ${_TAG} == $map")
                        viewModel.addExpenseData(map)

                    } else {
                        Toast.makeText(
                            this@InvoiceAddExpenseFormActivity,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    private fun showDatePickerFormat(selectedDatePickerDate: String) {

        val tamilLocale = Locale("ta", "IN")

        val inputFormat = SimpleDateFormat("dd MMMM yyyy", tamilLocale)
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val outputFormatServer = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        val date = inputFormat.parse(selectedDatePickerDate)
        val displayDate = date?.let { outputFormat.format(it) } ?: ""

        val date1 = inputFormat.parse(selectedDatePickerDate)
        selectedDate = date1?.let { outputFormatServer.format(it) } ?: ""

        binding.InvoiceExpenseDate.text = displayDate
    }
    companion object{
        var _TAG = "InvoiceAddExpenseFormActivity"
    }
}