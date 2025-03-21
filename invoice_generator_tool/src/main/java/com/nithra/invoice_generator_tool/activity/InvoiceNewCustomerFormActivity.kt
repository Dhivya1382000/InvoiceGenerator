package com.nithra.invoice_generator_tool.activity

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessDetailFormActivity.Companion._TAG
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceNewCustomerBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceNewCustomerFormActivity : AppCompatActivity() {
    lateinit var binding: ActivityInvoiceNewCustomerBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfState: MutableList<InvoiceGetDataMasterArray.GetStateList> = mutableListOf()
    var listOfIndustrial: MutableList<InvoiceGetDataMasterArray.GetIndustrialList> = mutableListOf()
    var listOfCompanyDetails: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
        mutableListOf()
    var listOfClientDetails: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
        mutableListOf()
    private lateinit var stateDialog: Dialog

    var selectedStateId = 0
    var selectedBusinesTypeId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceNewCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.InvoiceCustomerType.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected RadioButton by its ID
            when (checkedId) {
                R.id.InvoiceIndividualChoice -> {
                    binding.InvoiceCusCompanyNameLay.visibility = View.GONE
                    binding.InvoiceCusTaxIdLay.visibility = View.GONE
                }
                R.id.InvoiceBusinessChoice -> {
                    binding.InvoiceCusCompanyNameLay.visibility = View.VISIBLE
                    binding.InvoiceCusDisplayNameLay.visibility = View.VISIBLE
                    binding.InvoiceCusTaxIdLay.visibility = View.VISIBLE
                }
                else -> {

                }
            }

        }

        // Step 2: Set an OnCheckedChangeListener to handle checkbox state changes
        binding.checkBox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.InvoiceCusShippingAddress.text = binding.InvoiceCusBillingAddress.text
            } else {
                binding.InvoiceCusShippingAddress.setText("")
            }
        }

        if (InvoiceUtils.isNetworkAvailable(this@InvoiceNewCustomerFormActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "1227994"

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceNewCustomerFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }


        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            if (getMasterArray.status.equals("success")) {
                listOfState.addAll(getMasterArray.state!!)
                listOfIndustrial.addAll(getMasterArray.industrial!!)
                listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
                listOfClientDetails.addAll(getMasterArray.clientDetails!!)
            }

             if (!listOfClientDetails[0].status.equals("failure")){
                 // Sample list of suggestions
                 val SuggestionsBusinessName = listOfClientDetails.map {
                     "${it.name}"
                 }
                 println("itBusiness=Name == ${SuggestionsBusinessName}")

                 // Create an ArrayAdapter
                 val adapterBusinessName = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBusinessName)
                 // Set the adapter to the AutoCompleteTextView
                 binding.InvoiceCustomerName.setAdapter(adapterBusinessName)
                 binding.InvoiceCusCompanyName.threshold = 1 // Start showing suggestions after 1 character

                 // Sample list of suggestions
                 val SuggestionsBusinessMobile = listOfClientDetails.map {
                     "${it.companyName}"
                 }
                 println("itBusiness=Name == ${SuggestionsBusinessMobile}")
                 val adapterBusinessMobile = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBusinessMobile)
                 binding.InvoiceCusCompanyName.setAdapter(adapterBusinessMobile)
                 binding.InvoiceCustomerName.threshold = 1 // Start showing suggestions after 1 character

                 // Sample list of suggestions
                 val SuggestionsBillingAddress1 = listOfClientDetails.map {
                     "${it.email}"
                 }
                 println("itBusiness=Name == ${SuggestionsBillingAddress1}")
                 val adapterBillingAddress1 = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBillingAddress1)
                 binding.InvoiceCusEmail.setAdapter(adapterBillingAddress1)
                 binding.InvoiceCusEmail.threshold = 1 // Start showing suggestions after 1 character

                 // Sample list of suggestions
                 val SuggestionsBusinessEmail = listOfClientDetails.map {
                     "${it.email}"
                 }
                 println("itBusiness=Name == ${SuggestionsBusinessEmail}")
                 val adapterBusinessEmail = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBusinessEmail)
                 binding.InvoiceCusBillingAddress.setAdapter(adapterBusinessEmail)
                 binding.InvoiceCusBillingAddress.threshold = 1 // Start showing suggestions after 1 character

                 // Sample list of suggestions
                 val SuggestionsTaxId = listOfClientDetails.map {
                     "${it.shippingAddress}"
                 }
                 println("itBusiness=Name == ${SuggestionsTaxId}")
                 val adapterTaxId = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsTaxId)
                 binding.InvoiceCusShippingAddress.setAdapter(adapterTaxId)
                 binding.InvoiceCusShippingAddress.threshold = 1 // Start showing suggestions after 1 character
             }
        }
        binding.InvoiceCusSaveCard.setOnClickListener {
            when {
                binding.InvoiceCustomerName.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Enter customer Name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                binding.InvoiceCusCompanyName.text.toString().trim().isEmpty() -> {
                    if (binding.InvoiceBusinessChoice.isSelected){
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter company name",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }

                binding.InvoiceCusDisplayName.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Enter display name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceCusEmail.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Enter email id",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceCusMobile1.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Enter mobile number1",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceCusMobile2.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Enter mobile number2",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                binding.InvoiceCusBillingAddress.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Enter mobile number2",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                binding.InvoiceCusShippingAddress.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Enter mobile number2",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                binding.InvoiceCusTaxId.text.toString().trim().isEmpty() -> {
                    if (binding.InvoiceBusinessChoice.isSelected){
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter mobile number2",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                }

                else -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceNewCustomerFormActivity)) {
                        val map = HashMap<String, Any>()
                        map["action"] = "addClientDetails"
                        map["user_id"] = "1227994"
                        map["name"] = "" + binding.InvoiceCustomerName.text.toString().trim()
                        map["company_name"] = "" + binding.InvoiceCustomerName.text.toString().trim()
                        map["type"] = "" + binding.InvoiceIndividualChoice.text.toString().trim()
                        map["mobile1"] = "" + binding.InvoiceCusMobile1.text.toString().trim()
                        map["mobile2"] = ""+ binding.InvoiceCusMobile1.text.toString().trim()
                        map["email"] = "" + binding.InvoiceCusEmail.text.toString().trim()
                        map["bussiness_mobile"] = ""
                        map["billing_address"] = "" + binding.InvoiceCusBillingAddress.text.toString().trim()
                        map["shipping_address"] = "" + binding.InvoiceCusShippingAddress.text.toString().trim()
                        map["remark"] = "" + binding.InvoiceRemark.text.toString().trim()
                        map["state"] = ""+selectedStateId

                        println("InvoiceRequest - $_TAG == $map")
                        viewModel.getCustomerDetail(map)

                    } else {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }
}