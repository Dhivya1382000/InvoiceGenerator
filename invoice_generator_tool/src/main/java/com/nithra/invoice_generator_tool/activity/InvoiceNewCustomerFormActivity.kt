package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceNewCustomerBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceNewCustomerFormActivity : AppCompatActivity(), InvoicemasterClick {
    lateinit var binding: ActivityInvoiceNewCustomerBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfState: MutableList<InvoiceGetDataMasterArray.GetStateList> = mutableListOf()
    var listOfIndustrial: MutableList<InvoiceGetDataMasterArray.GetIndustrialList> = mutableListOf()
    var listOfCompanyDetails: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
        mutableListOf()
    var listOfClientDetails: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
        mutableListOf()
    private lateinit var stateDialog: Dialog
    var invoiceClickId = 0

    var selectedStateId = 0
    var selectedIndiInvoiceStateId = 0
    var selectedBusinesTypeId = 1   //indi = 1,business = 2
    var fromInvoice = 0
    var fromInvoicePage = ""
    var clickPosition = 0
    var clickTabPosType = 0
    var preference = InvioceSharedPreference()
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
        if (intent != null) {
            invoiceClickId = intent.getIntExtra("clickDataId", 0)
            fromInvoice = intent.getIntExtra("fromInvoice", 0)
            fromInvoicePage = "" + intent.getStringExtra("fromInvoicePage")
            clickPosition = intent.getIntExtra("clickPosition", 0)
            clickTabPosType = intent.getIntExtra("clickTabPosType", 0)
        }
        println("invoiceClick == $invoiceClickId")

        if (clickTabPosType == 1){
            binding.InvoiceBusinessChoice.isChecked = true
            binding.InvoiceIndividualChoice.isChecked = false
        }else{
            binding.InvoiceBusinessChoice.isChecked = false
            binding.InvoiceIndividualChoice.isChecked = true
        }

        if (binding.InvoiceIndividualChoice.isChecked) {
            selectedBusinesTypeId = 1
            binding.BusinessLay.visibility = View.GONE
            binding.IndividualLay.visibility = View.VISIBLE
            selectedIndiInvoiceStateId = 0
        } else {
            selectedBusinesTypeId = 2
            binding.BusinessLay.visibility = View.VISIBLE
            binding.IndividualLay.visibility = View.GONE
            selectedStateId = 0
        }

        binding.InvoiceCustomerType.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected RadioButton by its ID
            when (checkedId) {
                R.id.InvoiceIndividualChoice -> {
                    selectedBusinesTypeId = 1
                    binding.InvoiceCusCompanyNameLay.visibility = View.GONE
                    binding.InvoiceCusTaxIdLay.visibility = View.GONE
                    binding.InvoiceCustomerName.requestFocus()
                    binding.BusinessLay.visibility = View.GONE
                    binding.IndividualLay.visibility = View.VISIBLE
                }

                R.id.InvoiceBusinessChoice -> {
                    selectedBusinesTypeId = 2
                    binding.InvoiceCusCompanyNameLay.visibility = View.VISIBLE
                    binding.InvoiceCusTaxIdLay.visibility = View.VISIBLE
                    binding.InvoiceCustomerName.requestFocus()
                    binding.BusinessLay.visibility = View.VISIBLE
                    binding.IndividualLay.visibility = View.GONE
                }

                else -> {

                }
            }
        }
        binding.InvoiceCustomerStateSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceNewCustomerFormActivity)) {
                Toast.makeText(
                    this@InvoiceNewCustomerFormActivity,
                    "Check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showSearchableDialog<InvoiceGetDataMasterArray.GetStateList>(0, listOfState)
        }

        binding.InvoiceIndividualCustomerStateSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceNewCustomerFormActivity)) {
                Toast.makeText(
                    this@InvoiceNewCustomerFormActivity,
                    "Check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showSearchableDialog<InvoiceGetDataMasterArray.GetStateList>(0, listOfState)
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
            InvoiceUtils.loadingProgress(
                this@InvoiceNewCustomerFormActivity,
                "" + InvoiceUtils.messageLoading,
                false
            ).show()
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "" + preference.getString(this@InvoiceNewCustomerFormActivity,"INVOICE_USER_ID")
            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceNewCustomerFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.errorMessage.observe(this@InvoiceNewCustomerFormActivity) {
            binding.mainCusFormLay.visibility = View.VISIBLE
            InvoiceUtils.loadingDialog.dismiss()
            Toast.makeText(this@InvoiceNewCustomerFormActivity, "" + it, Toast.LENGTH_SHORT).show()
        }
        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            binding.mainCusFormLay.visibility = View.VISIBLE
            InvoiceUtils.loadingDialog.dismiss()
            println("MaseterStatus == ${getMasterArray.status}")
            if (getMasterArray.status.equals("success")) {
                listOfState.addAll(getMasterArray.state!!)
                listOfIndustrial.addAll(getMasterArray.industrial!!)
                listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
                listOfClientDetails.addAll(getMasterArray.clientDetails!!)

                if (invoiceClickId != 0) {
                    println("invoiceClickId == $invoiceClickId")
                    binding.InvoiceCustomerType.visibility = View.GONE
                    binding.InvoiceTypeTxt.visibility = View.GONE
                    for (i in listOfClientDetails.indices) {
                        println("invoiceClickId Company == ${listOfClientDetails[i].clientId}")
                        if (invoiceClickId == listOfClientDetails[i].clientId) {
                            selectedBusinesTypeId = listOfClientDetails[i].type!!
                            println("selectedBusinesTypeId == $selectedBusinesTypeId")
                            println("selectedBusinesTax == ${listOfClientDetails[i].taxId}")
                            if (selectedBusinesTypeId == 2){ //business
                                binding.InvoiceBusinessChoice.isChecked = true
                                binding.InvoiceIndividualChoice.isChecked = false
                                binding.BusinessLay.visibility = View.VISIBLE
                                binding.IndividualLay.visibility = View.GONE
                                binding.InvoiceCustomerName.setText(listOfClientDetails[i].name)
                                binding.InvoiceCusCompanyName.setText(listOfClientDetails[i].companyName)
                                binding.InvoiceCusDisplayName.setText(listOfClientDetails[i].displayName)
                                binding.InvoiceCusEmail.setText(listOfClientDetails[i].email)
                                binding.InvoiceCusMobile1.setText(listOfClientDetails[i].mobile1)
                                binding.InvoiceCusMobile2.setText(listOfClientDetails[i].mobile2)
                                binding.InvoiceCusBillingAddress.setText(listOfClientDetails[i].billingAddress)
                                binding.InvoiceCusShippingAddress.setText(listOfClientDetails[i].shippingAddress)
                                if (listOfClientDetails[i].billingAddress.equals(listOfClientDetails[i].shippingAddress)) {
                                    binding.checkBox1.isChecked = true
                                }
                                binding.InvoiceCustomerStateText.setText(listOfClientDetails[i].state)
                                selectedStateId = listOfClientDetails[i].stateId!!
                                binding.InvoiceCustomerStateText.setText(listOfClientDetails[i].state)
                                binding.InvoiceRemark.setText(listOfClientDetails[i].remark)
                                binding.InvoiceCusTaxId.setText(listOfClientDetails[i].taxId)
                            }else{  //individual
                                binding.InvoiceIndividualChoice.isChecked = true
                                binding.InvoiceBusinessChoice.isChecked = false
                                binding.IndividualLay.visibility = View.VISIBLE
                                binding.BusinessLay.visibility = View.GONE
                                selectedIndiInvoiceStateId = listOfClientDetails[i].stateId!!
                                binding.InvoiceIndividualCustomerName.setText(""+listOfClientDetails[i].name)
                                binding.InvoiceIndilCusMobile1.setText(""+listOfClientDetails[i].mobile1)
                                binding.InvoiceIndividualCusBillingAddress.setText(""+listOfClientDetails[i].billingAddress)
                                binding.InvoiceIndividualCustomerStateText.setText(""+listOfClientDetails[i].state)
                                binding.InvoiceIndividualCusEmail.setText(""+listOfClientDetails[i].email)
                            }

                        }
                    }
                    binding.InvoiceSaveText.text = "Update"
                }
            }
            if (!listOfClientDetails[0].status.equals("failure")) {
                // Sample list of suggestions
                val SuggestionsBusinessName = listOfClientDetails.map {
                    "${it.name}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsBusinessName}")

                // Create an ArrayAdapter
                val adapterBusinessName = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBusinessName.toList()
                )
                binding.InvoiceCustomerName.setAdapter(adapterBusinessName)
                binding.InvoiceCusCompanyName.threshold =
                    1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBusinessMobile = listOfClientDetails.map {
                    "${it.companyName}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsBusinessMobile}")
                val adapterBusinessMobile = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBusinessMobile.toList()
                )
                binding.InvoiceCusCompanyName.setAdapter(adapterBusinessMobile)
                binding.InvoiceCustomerName.threshold =
                    1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBillingAddress1 = listOfClientDetails.map {
                    "${it.email}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsBillingAddress1}")
                val adapterBillingAddress1 = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBillingAddress1.toList()
                )
                binding.InvoiceCusEmail.setAdapter(adapterBillingAddress1)
                binding.InvoiceCusEmail.threshold = 1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBusinessEmail = listOfClientDetails.map {
                    "${it.email}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsBusinessEmail}")
                val adapterBusinessEmail = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBusinessEmail.toList()
                )
                binding.InvoiceCusBillingAddress.setAdapter(adapterBusinessEmail)
                binding.InvoiceCusBillingAddress.threshold =
                    1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsTaxId = listOfClientDetails.map {
                    "${it.shippingAddress}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsTaxId}")
                val adapterTaxId = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsTaxId.toList()
                )
                binding.InvoiceCusShippingAddress.setAdapter(adapterTaxId)
                binding.InvoiceCusShippingAddress.threshold =
                    1 // Start showing suggestions after 1 character
            }
        }

        binding.InvoiceCusSaveCard.setOnClickListener {

            if (binding.InvoiceBusinessChoice.isChecked) {
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
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer company name",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    /*  binding.InvoiceCusDisplayName.text.toString().trim().isEmpty() -> {
                          Toast.makeText(
                              this@InvoiceNewCustomerFormActivity,
                              "Enter customer display name",
                              Toast.LENGTH_SHORT
                          ).show()
                          return@setOnClickListener
                      }*/

                    /*    binding.InvoiceCusEmail.text.toString().trim().isEmpty() -> {
                            Toast.makeText(
                                this@InvoiceNewCustomerFormActivity,
                                "Enter customer email id",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }*/
                    binding.InvoiceCusEmail.text.toString().trim().isNotEmpty() && !isValidEmail(
                        binding.InvoiceCusEmail.text.toString().trim()
                    ) -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer email id correctly",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.InvoiceCusMobile1.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer mobile number1",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    binding.InvoiceCusBillingAddress.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer billing address",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.InvoiceCusShippingAddress.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer shipping address",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    selectedStateId == 0 -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer state",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.InvoiceCusTaxId.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter GST Id",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    !isValidGST(binding.InvoiceCusTaxId.text.toString().trim()) -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter valid GST number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    else -> {
                        if (InvoiceUtils.isNetworkAvailable(this@InvoiceNewCustomerFormActivity)) {
                            val map = HashMap<String, Any>()
                            map["action"] = "addClientDetails"
                            map["user_id"] = "" + preference.getString(this@InvoiceNewCustomerFormActivity,"INVOICE_USER_ID")
                            if (invoiceClickId != 0) {
                                map["id"] = invoiceClickId
                            }
                            map["name"] = "" + binding.InvoiceCustomerName.text.toString().trim()
                            map["company_name"] =
                                "" + binding.InvoiceCusCompanyName.text.toString().trim()
                            map["type"] = "" + selectedBusinesTypeId
                            map["display_name"] =
                                "" + binding.InvoiceCusDisplayName.text.toString().trim()
                            map["mobile1"] = "" + binding.InvoiceCusMobile1.text.toString().trim()
                            map["mobile2"] = "" + binding.InvoiceCusMobile2.text.toString().trim()
                            map["email"] = "" + binding.InvoiceCusEmail.text.toString().trim()
                            map["billing_address"] =
                                "" + binding.InvoiceCusBillingAddress.text.toString().trim()
                            map["shipping_address"] =
                                "" + binding.InvoiceCusShippingAddress.text.toString().trim()
                            map["remark"] = "" + binding.InvoiceRemark.text.toString().trim()
                            map["state"] = "" + selectedStateId
                            map["tax_id"] = "" + binding.InvoiceCusTaxId.text.toString().trim()

                            println("InvoiceRequest - Business $_TAG == $map")
                            InvoiceUtils.loadingProgress(
                                this@InvoiceNewCustomerFormActivity,
                                "" + InvoiceUtils.messageLoading,
                                false
                            ).show()
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

            } else {
                when {
                    binding.InvoiceIndividualCustomerName.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer Name",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    /* binding.InvoiceCusDisplayName.text.toString().trim().isEmpty() -> {
                         Toast.makeText(
                             this@InvoiceNewCustomerFormActivity,
                             "Enter customer display name",
                             Toast.LENGTH_SHORT
                         ).show()
                         return@setOnClickListener
                     }*/

                    binding.InvoiceIndividualCusEmail.text.toString().trim().isNotEmpty() && !isValidEmail(
                        binding.InvoiceIndividualCusEmail.text.toString().trim()
                    ) -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer email id correctly",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.InvoiceIndilCusMobile1.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer mobile number1",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.InvoiceIndividualCusBillingAddress.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer billing address",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    selectedIndiInvoiceStateId == 0 -> {
                        Toast.makeText(
                            this@InvoiceNewCustomerFormActivity,
                            "Enter customer state",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    else -> {
                        if (InvoiceUtils.isNetworkAvailable(this@InvoiceNewCustomerFormActivity)) {
                            val map = HashMap<String, Any>()
                            map["action"] = "addClientDetails"
                            map["user_id"] = "" +preference.getString(this@InvoiceNewCustomerFormActivity,"INVOICE_USER_ID")
                            if (invoiceClickId != 0) {
                                map["id"] = invoiceClickId
                            }
                            map["name"] = "" + binding.InvoiceIndividualCustomerName.text.toString().trim()
                            map["type"] = "" + selectedBusinesTypeId
                            map["mobile1"] = "" + binding.InvoiceIndilCusMobile1.text.toString().trim()
                            map["email"] = "" + binding.InvoiceIndividualCusEmail.text.toString().trim()
                            map["billing_address"] = "" + binding.InvoiceIndividualCusBillingAddress.text.toString().trim()
                            map["state"] = "" + selectedIndiInvoiceStateId

                            println("InvoiceRequest - Individual $_TAG == $map")
                            InvoiceUtils.loadingProgress(
                                this@InvoiceNewCustomerFormActivity,
                                "" + InvoiceUtils.messageLoading,
                                false
                            ).show()
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

            viewModel.getCustomerDetail.observe(this) { getCustomerDetail ->
                InvoiceUtils.loadingDialog.dismiss()
                if (getCustomerDetail.status.equals("success")) {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "" + getCustomerDetail.msg,
                        Toast.LENGTH_SHORT
                    ).show()
                    println("fromInvoicePage == $fromInvoicePage")
                    println("gstNume == ${getCustomerDetail.data!![0].taxId}")
                    if (invoiceClickId != 0) {
                        val resultIntent = Intent()
                        val addedData = Gson().toJson(getCustomerDetail.data)
                        resultIntent.putExtra("INVOICE_FORM_DATA_UPDATE", addedData)
                        resultIntent.putExtra("INVOICE_FORM_CLICK_POS", clickPosition)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        if (fromInvoicePage == "InvoiceBusinessAndCustomerActivity_Customers") {
                            val resultIntent = Intent()
                            getCustomerDetail.data!![0].status = getCustomerDetail.status!!
                            val addedData = Gson().toJson(getCustomerDetail.data)
                            resultIntent.putExtra("INVOICE_FORM_DATA", addedData)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        } else {
                            finish()
                        }
                    }


                } else {

                }
            }

            /*    if (InvoiceUtils.isNetworkAvailable(this@InvoiceNewCustomerFormActivity)) {
                    val map = HashMap<String, Any>()
                    map["action"] = "addClientDetails"
                    map["user_id"] = "1227994"
                    map["name"] = "" + binding.InvoiceCustomerName.text.toString().trim()
                    map["company_name"] =
                        "" + binding.InvoiceCusCompanyName.text.toString().trim()
                    map["type"] = "" + selectedBusinesTypeId
                    map["mobile1"] = "" + binding.InvoiceCusMobile1.text.toString().trim()
                    map["mobile2"] = "" + binding.InvoiceCusMobile1.text.toString().trim()
                    map["email"] = "" + binding.InvoiceCusEmail.text.toString().trim()
                    map["bussiness_mobile"] = ""
                    map["billing_address"] =
                        "" + binding.InvoiceCusBillingAddress.text.toString().trim()
                    map["shipping_address"] =
                        "" + binding.InvoiceCusShippingAddress.text.toString().trim()
                    map["remark"] = "" + binding.InvoiceRemark.text.toString().trim()
                    map["state"] = "" + selectedStateId

                    println("InvoiceRequest - $_TAG == $map")
                    //    viewModel.getCustomerDetail(map)

                } else {
                    Toast.makeText(
                        this@InvoiceNewCustomerFormActivity,
                        "Check Your Internet Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }*/
        }
    }

    fun isValidGST(gstNumber: String): Boolean {
        val gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"
        return gstNumber.matches(Regex(gstRegex))
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailPattern.toRegex())
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
        val AddItemCard = stateDialog.findViewById<CardView>(R.id.AddItemCard)
        if (fromSpinner == 0) {
            AddItemCard.visibility = View.GONE
        }
        val filteredList: MutableList<T> = mutableListOf()

        filteredList.clear()
        // Initialize adapter
        filteredList.addAll(listOfState) // Initially show all items

        val adapter = InvoiceMasterAdapter(
            this@InvoiceNewCustomerFormActivity,
            filteredList,
            "",
            this,
            fromInvoice,
            fromSpinner, onAddItemClick = {

            }, onDeleteItem = { deleteId, pos, actionName ->

            },
            onSearchResult = {

            }
        )
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
                filterData<T>(
                    searchQuery,
                    recyclerView,
                    fromSpinner,
                    listOfState,
                    filteredList,
                    NoDataLay,
                    stateDialog,
                    AddItemCard
                )
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
        NoDataLay: LinearLayout,
        stateDialog: Dialog,
        AddItemCard: CardView
    ) {
        filteredList.clear()
        println("query == $searchQuery")
        if (searchQuery!!.trim().isEmpty() || searchQuery.trim().equals("")) {
            filteredList.addAll(listOfState) // Show all items if the query is empty
            println("filterSize == ${filteredList.size}")
        } else {
            // Check if filteredList is empty and handle "no data found"
            filteredList.addAll(listOfState.filter { item ->
                // Example: If the item is a State, filter by stateName
                when (item) {
                    is InvoiceGetDataMasterArray.GetStateList -> item.english!!.contains(
                        searchQuery,
                        ignoreCase = true
                    )

                    is InvoiceGetDataMasterArray.GetIndustrialList -> item.industrial!!.contains(
                        searchQuery,
                        ignoreCase = true
                    )

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
            /* AddItemCard.setOnClickListener {
                 stateDialog.dismiss()
                 showMasterDataAddDia()
             }*/
        }
        val adapter = InvoiceMasterAdapter(
            this@InvoiceNewCustomerFormActivity, filteredList,
            searchQuery.toString(),
            this, fromInvoice, fromSpinner, onAddItemClick = {

            }, onDeleteItem = { deleteId, pos, actionName ->

            },
            onSearchResult = {

            }
        ) // Pass the query
        recyclerView.adapter = adapter
    }


    companion object {
        var _TAG = "InvoiceNewCustomerFormActivity"
    }

    override fun onItemClick(clikName: String, clikId: Int, fromClick: Int, position: Int) {
        stateDialog.dismiss()
        if (fromClick == 0) {
            if (selectedBusinesTypeId == 1){
                binding.InvoiceIndividualCustomerStateText.setText(clikName)
                selectedIndiInvoiceStateId = clikId
            }else{
                selectedStateId = clikId
                binding.InvoiceCustomerStateText.text = clikName
            }

        }
    }
}