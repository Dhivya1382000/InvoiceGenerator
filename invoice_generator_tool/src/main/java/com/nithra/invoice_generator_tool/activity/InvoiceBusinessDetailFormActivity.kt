package com.nithra.invoice_generator_tool.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceBusinessDetailFormBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceBusinessDetailFormActivity : AppCompatActivity(), InvoicemasterClick {
    lateinit var binding: ActivityInvoiceBusinessDetailFormBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfState: MutableList<InvoiceGetDataMasterArray. GetStateList>  = mutableListOf()
    var listOfIndustrial: MutableList<InvoiceGetDataMasterArray. GetIndustrialList>  = mutableListOf()
    var listOfCompanyDetails: MutableList<InvoiceGetDataMasterArray. GetCompanyDetailList>  = mutableListOf()
    var listOfClientDetails: MutableList<InvoiceGetDataMasterArray. GetClientDetails>  = mutableListOf()
    private lateinit var stateDialog: Dialog
/*private lateinit var adapter: InvoiceMasterAdapter*/
    var selectedStateId = 0
    var selectedBusinesTypeId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_invoice_business_detail_form)
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Step 2: Set an OnCheckedChangeListener to handle checkbox state changes
        binding.mobileNumberCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.InvoiceBusinessMobile1.text = binding.InvoiceBusinessMobile.text
            } else {
                binding.InvoiceBusinessMobile1.setText("")
            }
        }

        if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "1227994"

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceBusinessDetailFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            if (getMasterArray.status.equals("success")){
                listOfState.addAll(getMasterArray.state!!)
                listOfIndustrial.addAll(getMasterArray.industrial!!)
                listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
                listOfClientDetails.addAll(getMasterArray.clientDetails!!)
            }
            if (!listOfCompanyDetails[0].status.equals("failure")){
                // Sample list of suggestions
                val SuggestionsBusinessName = listOfCompanyDetails.map {
                    "${it.bussinessName}"
                }
                println("itBusiness=Name == ${SuggestionsBusinessName}")

                // Create an ArrayAdapter
                val adapterBusinessName = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBusinessName)
                // Set the adapter to the AutoCompleteTextView
                binding.InvoiceBusinessName.setAdapter(adapterBusinessName)
                binding.InvoiceBusinessName.threshold = 1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBusinessMobile = listOfCompanyDetails.map {
                    "${it.bussinessMobile}"
                }
                println("itBusiness=Name == ${SuggestionsBusinessMobile}")
                val adapterBusinessMobile = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBusinessMobile)
                binding.InvoiceBusinessMobile1.setAdapter(adapterBusinessMobile)
                binding.InvoiceBusinessMobile1.threshold = 1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBillingAddress1 = listOfCompanyDetails.map {
                    "${it.billingAddress1}"
                }
                println("itBusiness=Name == ${SuggestionsBillingAddress1}")
                val adapterBillingAddress1 = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBillingAddress1)
                binding.InvoiceBillingAddress1.setAdapter(adapterBillingAddress1)
                binding.InvoiceBillingAddress1.threshold = 1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBusinessEmail = listOfCompanyDetails.map {
                    "${it.email}"
                }
                println("itBusiness=Name == ${SuggestionsBusinessEmail}")
                val adapterBusinessEmail = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsBusinessEmail)
                binding.InvoiceBusinessEmail.setAdapter(adapterBusinessEmail)
                binding.InvoiceBusinessEmail.threshold = 1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsTaxId = listOfCompanyDetails.map {
                    "${it.taxId}"
                }
                println("itBusiness=Name == ${SuggestionsTaxId}")
                val adapterTaxId = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, SuggestionsTaxId)
                binding.InvoiceTaxId.setAdapter(adapterTaxId)
                binding.InvoiceTaxId.threshold = 1 // Start showing suggestions after 1 character
            }

        }

        binding.InvoiceBusinessStateSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "Check your internet connetion",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showSearchableDialog<InvoiceGetDataMasterArray.GetStateList>(0,listOfState)
        }

        binding.InvoiceBusinessTypeSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "Check your internet connetion",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showSearchableDialog<InvoiceGetDataMasterArray.GetIndustrialList>(1,listOfIndustrial)
        }


        binding.InvoiceBusinessSaveCard.setOnClickListener {
            when {
                binding.InvoiceBusinessName.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Enter your business Name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceBusinessMobile.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Enter your mobile number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceBusinessMobile1.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Enter your business mobile number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceBillingAddress1.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Enter your billing address1",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceBusinessStateText.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Select your business state",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                binding.InvoiceTaxId.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Enter your Gst number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                else -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                        val map = HashMap<String, Any>()
                        map["action"] = "addCompanyDetails"
                        map["user_id"] = "1227994"
                        map["bussiness_name"] = "" + binding.InvoiceBusinessName.text.toString().trim()
                        map["email"] = "" + binding.InvoiceBusinessEmail.text.toString().trim()
                        map["bussiness_mobile"] = "" + binding.InvoiceBusinessMobile1.text.toString().trim()
                        map["billing_address_1"] = "" + binding.InvoiceBillingAddress1.text.toString().trim()
                        map["billing_address_2"] = ""
                        map["website"] = "" + binding.InvoiceWebsite.text.toString().trim()
                        map["tax_name"] = ""
                        map["tax_id"] = "" + binding.InvoiceTaxId.text.toString().trim()
                        map["bussiness_id"] = "" + binding.InvoiceBusinessId.text.toString().trim()
                        map["state"] = ""+selectedStateId

                        println("InvoiceRequest - $_TAG == $map")
                   viewModel.getBusinessDetail(map)

                    } else {
                        Toast.makeText(
                            this@InvoiceBusinessDetailFormActivity,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.getBusinessDetailForm.observe(this) { getBusinessDetail ->
            println("receiveData == ${getBusinessDetail.data!![0].bussinessName}")
            if (getBusinessDetail.status.equals("success")) {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "" + getBusinessDetail.msg,
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "" + getBusinessDetail.msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun <T> showSearchableDialog(
        fromSpinner: Int,
        listOfState: MutableList<T>,
    ) {
        stateDialog =
            Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        stateDialog.setContentView(R.layout.invoice_master_search_dia)
        stateDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        // Initialize views
        val searchBar = stateDialog.findViewById<AppCompatEditText>(R.id.search_bar)
        val recyclerView = stateDialog.findViewById<RecyclerView>(R.id.recycler_view)
        val NoDataLay = stateDialog.findViewById<LinearLayout>(R.id.NoDataLay)
        val filteredList: MutableList<T> = mutableListOf()

        filteredList.clear()
        // Initialize adapter
        filteredList.addAll(listOfState) // Initially show all items

        val adapter = InvoiceMasterAdapter(filteredList, "", this,fromSpinner)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Add search functionality
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(
                searchQuery: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterData<T>(searchQuery, recyclerView,fromSpinner,listOfState,filteredList,NoDataLay,stateDialog)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        stateDialog.show()
    }
    private fun <T> filterData(
        searchQuery: CharSequence?,
        recyclerView: RecyclerView,
        fromSpinner: Int,
        listOfState: MutableList<T>,
        filteredList: MutableList<T>,
        NoDataLay:LinearLayout,
        stateDialog:Dialog
    ) {
        filteredList.clear()
        println("query == $searchQuery")
        if (searchQuery!!.trim().isEmpty() || searchQuery.trim().equals("")) {
            filteredList.addAll(listOfState) // Show all items if the query is empty
            println("filterSize == ${filteredList.size}")
        } else {
            /*filteredList.addAll(filteredList.filter {
                it.english!!.contains(searchQuery, ignoreCase = true)  // Check if english contains search query
            })*/
            // Check if filteredList is empty and handle "no data found"
            filteredList.addAll(listOfState.filter { item ->
                // Example: If the item is a State, filter by stateName
                when (item) {
                    is InvoiceGetDataMasterArray.GetStateList -> item.english!!.contains(searchQuery, ignoreCase = true)
                    is InvoiceGetDataMasterArray.GetIndustrialList -> item.industrial!!.contains(searchQuery, ignoreCase = true)
                    else -> {
                        false
                    }
                }
            })
            if (filteredList.isEmpty()) {
                // Display a message or take action if no results were found
                println("filterSize 11  == ${filteredList.size}")
                NoDataLay.visibility = View.VISIBLE
            } else {
                println("filterSize 12  == ${filteredList.size}")
                NoDataLay.visibility = View.GONE
            }
            NoDataLay.setOnClickListener {
                stateDialog.dismiss()
                showMasterDataAddDia()
            }
        }
        val adapter = InvoiceMasterAdapter(
            filteredList,
            searchQuery.toString(),
            this,fromSpinner
        ) // Pass the query
        recyclerView.adapter = adapter
    }


    private fun showMasterDataAddDia() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.invoice_create_dia_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etItemName = dialog.findViewById<AppCompatEditText>(R.id.etItemName)
        val btnCreate = dialog.findViewById<AppCompatButton>(R.id.btnCreate)


        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            dialog.dismiss()
        }
        dialog.show()
    }
    companion object {
        var _TAG = "InvoiceBusinessDetailFormActivity"
    }

    override fun onItemClick(clikName: String, clikId: Int, fromClick: Int) {
        stateDialog.dismiss()
        if (fromClick == 0){
            selectedStateId = clikId
            binding.InvoiceBusinessStateText.setText(clikName)
        }else if (fromClick == 1){
            selectedBusinesTypeId = clikId
            binding.InvoiceBusinessTypeText.setText(clikName)
        }
    }

}