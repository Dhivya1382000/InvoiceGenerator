package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
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
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceBusinessDetailFormBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetBusinessDetail
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceBusinessDetailFormActivity : AppCompatActivity(), InvoicemasterClick {
    lateinit var binding: ActivityInvoiceBusinessDetailFormBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfState: MutableList<InvoiceGetDataMasterArray.GetStateList> = mutableListOf()
    var listOfIndustrial: MutableList<InvoiceGetDataMasterArray.GetIndustrialList> = mutableListOf()
    var listOfCompanyDetails: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
        mutableListOf()
    var listOfClientDetails: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
        mutableListOf()
    private lateinit var stateDialog: Dialog

    /*private lateinit var adapter: InvoiceMasterAdapter*/
    var selectedStateId = 0
    var selectedInvoiceStateId = 0
    var selectedBusinesTypeId = 0
    var selectedBusinesChoiceTypeId = 0
    var fromInvoicePage = ""
    var invoiceClickId = 0
    var fromInvoice = 0
    var clickPosition = 0
    var clickTabPosType = 0
    var preference = InvioceSharedPreference()

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

        if (intent != null) {
            fromInvoicePage = "" + intent.getStringExtra("fromInvoicePage")
            invoiceClickId = intent.getIntExtra("clickDataId", 0)
            fromInvoice = intent.getIntExtra("fromInvoice", 0)
            clickPosition = intent.getIntExtra("clickPosition", 0)
            clickTabPosType = intent.getIntExtra("clickTabPosType", 0)
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
            InvoiceUtils.loadingProgress(this@InvoiceBusinessDetailFormActivity,""+InvoiceUtils.messageLoading,false).show()
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = ""+preference.getString(this@InvoiceBusinessDetailFormActivity,"INVOICE_USER_ID")

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceBusinessDetailFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
        println("clickTabPost == $clickTabPosType")
        if (clickTabPosType == 1){
            binding.InvoiceBusinessChoice.isChecked = true
            binding.InvoiceIndividualChoice.isChecked = false
        }else{
            binding.InvoiceBusinessChoice.isChecked = false
            binding.InvoiceIndividualChoice.isChecked = true
        }

        if (binding.InvoiceIndividualChoice.isChecked) {
            selectedBusinesChoiceTypeId = 1
            binding.IndividualChoiceLay.visibility = View.VISIBLE
            binding.BusinessChoiceLay.visibility = View.GONE
            selectedInvoiceStateId = 0
        } else {
            selectedBusinesChoiceTypeId = 0
            binding.IndividualChoiceLay.visibility = View.GONE
            binding.BusinessChoiceLay.visibility = View.VISIBLE
            selectedStateId = 0
        }

        binding.InvoiceBusinessType.setOnCheckedChangeListener { group, checkedId ->
            // Get the selected RadioButton by its ID
            when (checkedId) {
                R.id.InvoiceBusinessChoice -> {
                    selectedBusinesChoiceTypeId = 0
                    binding.IndividualChoiceLay.visibility = View.GONE
                    binding.BusinessChoiceLay.visibility = View.VISIBLE
                }
                R.id.InvoiceIndividualChoice -> {
                    selectedBusinesChoiceTypeId = 1
                    binding.IndividualChoiceLay.visibility = View.VISIBLE
                    binding.BusinessChoiceLay.visibility = View.GONE
                }

                else -> {

                }
            }
        }

        viewModel.errorMessage.observe(this@InvoiceBusinessDetailFormActivity) {
            InvoiceUtils.loadingDialog.dismiss()
            binding.mainFormLay.visibility = View.VISIBLE
            Toast.makeText(this@InvoiceBusinessDetailFormActivity, "" + it, Toast.LENGTH_SHORT)
                .show()
        }

        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            InvoiceUtils.loadingDialog.dismiss()
            binding.mainFormLay.visibility = View.VISIBLE
            if (getMasterArray.status.equals("success")) {
                listOfState.addAll(getMasterArray.state!!)
                listOfIndustrial.addAll(getMasterArray.industrial!!)
                listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
                listOfClientDetails.addAll(getMasterArray.clientDetails!!)
                println("invoiceClickId == $invoiceClickId")

                if (invoiceClickId != 0) {
                    binding.InvoiceBusinessType.visibility = View.GONE
                    for (i in listOfCompanyDetails.indices) {
                        println("invoiceClickId Company == ${listOfCompanyDetails[i].companyId}")
                        if (invoiceClickId == listOfCompanyDetails[i].companyId) {
                            selectedBusinesTypeId = listOfCompanyDetails[i].bussinessType!!
                            if (listOfCompanyDetails[i].type == 0) { //business
                               binding.BusinessChoiceLay.visibility = View.VISIBLE
                               binding.IndividualChoiceLay.visibility = View.GONE
                                selectedBusinesChoiceTypeId = 0
                                binding.InvoiceBusinessTypeText.text = listOfCompanyDetails[i].industrialName!!
                                binding.InvoiceBusinessId.setText("" + listOfCompanyDetails[i].bussinessId)
                                binding.InvoiceBusinessName.setText("" + listOfCompanyDetails[i].bussinessName)
                                binding.InvoiceBusinessEmail.setText("" + listOfCompanyDetails[i].email)
                                binding.InvoiceBusinessMobile.setText("" + listOfCompanyDetails[i].mobile)
                                binding.InvoiceBusinessMobile1.setText("" + listOfCompanyDetails[i].bussinessMobile)
                                binding.InvoiceBillingAddress1.setText("" + listOfCompanyDetails[i].billingAddress1)
                                selectedStateId = listOfCompanyDetails[i].stateId!!
                                binding.InvoiceBusinessStateText.setText("" + listOfCompanyDetails[i].state)
                                binding.InvoiceWebsite.setText("" + listOfCompanyDetails[i].website)
                                binding.InvoiceTaxId.setText("" + listOfCompanyDetails[i].taxId)
                                binding.InvoiceContactPersonDetailEdit.setText("" + listOfCompanyDetails[i].contactPerson)
                                binding.InvoiceBankNameEdit.setText("" + listOfCompanyDetails[i].bankName)
                                binding.InvoiceBankAccountEdit.setText("" + listOfCompanyDetails[i].bankAcoountNumber)
                                binding.InvoiceBankIFSCEdit.setText("" + listOfCompanyDetails[i].ifscCode)
                                binding.InvoiceBankMICREdit.setText("" + listOfCompanyDetails[i].micrCode)
                                binding.InvoiceBankAddressEdit.setText("" + listOfCompanyDetails[i].bankAddress)
                                if (listOfCompanyDetails[i].mobile.equals(listOfCompanyDetails[i].bussinessMobile)) {
                                    binding.mobileNumberCheckBox.isChecked = true
                                }
                            } else { //individual
                                binding.BusinessChoiceLay.visibility = View.GONE
                                binding.IndividualChoiceLay.visibility = View.VISIBLE
                                selectedBusinesChoiceTypeId = 1
                                selectedInvoiceStateId = listOfCompanyDetails[i].stateId!!
                                binding.IndividualName.setText(listOfCompanyDetails[i].bussinessName!!)
                                binding.IndividualMobile.setText("" + listOfCompanyDetails[i].bussinessMobile)
                                binding.IndividualBillingAddress1.setText("" + listOfCompanyDetails[i].billingAddress1)
                                binding.IndividualEmail.setText("" + listOfCompanyDetails[i].email)
                                binding.IndividualStateText.setText("" + listOfCompanyDetails[i].state)
                            }

                            binding.InvoiceBusCardText.text = "Update"
                        }
                    }
                }
            }
            if (!listOfCompanyDetails[0].status.equals("failure")) {
                // Sample list of suggestions
                val SuggestionsBusinessName = listOfCompanyDetails.map {
                    "${it.bussinessName}"
                }.toSet()

                println("itBusiness=Name == ${SuggestionsBusinessName}")

                // Create an ArrayAdapter
                val adapterBusinessName = ArrayAdapter(
                    this@InvoiceBusinessDetailFormActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBusinessName.toList()
                )
                // Set the adapter to the AutoCompleteTextView
                binding.InvoiceBusinessName.setAdapter(adapterBusinessName)
                binding.InvoiceBusinessName.threshold = 1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBusinessMobile = listOfCompanyDetails.map {
                    "${it.bussinessMobile}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsBusinessMobile}")
                val adapterBusinessMobile = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBusinessMobile.toList()
                )
                binding.InvoiceBusinessMobile1.setAdapter(adapterBusinessMobile)
                binding.InvoiceBusinessMobile1.threshold =
                    1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBillingAddress1 = listOfCompanyDetails.map {
                    "${it.billingAddress1}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsBillingAddress1}")
                val adapterBillingAddress1 = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBillingAddress1.toList()
                )
                binding.InvoiceBillingAddress1.setAdapter(adapterBillingAddress1)
                binding.InvoiceBillingAddress1.threshold =
                    1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsBusinessEmail = listOfCompanyDetails.map {
                    "${it.email}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsBusinessEmail}")
                val adapterBusinessEmail = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsBusinessEmail.toList()
                )
                binding.InvoiceBusinessEmail.setAdapter(adapterBusinessEmail)
                binding.InvoiceBusinessEmail.threshold = 1 // Start showing suggestions after 1 character

                // Sample list of suggestions
                val SuggestionsTaxId = listOfCompanyDetails.map {
                    "${it.taxId}"
                }.toSet()
                println("itBusiness=Name == ${SuggestionsTaxId}")
                val adapterTaxId = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsTaxId.toList()
                )
                binding.InvoiceTaxId.setAdapter(adapterTaxId)
                binding.InvoiceTaxId.threshold = 1 // Start showing suggestions after 1 character
            }

        }

        binding.InvoiceBusinessStateSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "Check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showSearchableDialog<InvoiceGetDataMasterArray.GetStateList>(0, listOfState)
        }

        binding.IndividualStateSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "Check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showSearchableDialog<InvoiceGetDataMasterArray.GetStateList>(0, listOfState)
        }

        binding.InvoiceBusinessTypeSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "Check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showSearchableDialog<InvoiceGetDataMasterArray.GetIndustrialList>(1, listOfIndustrial)
        }


        binding.InvoiceBusinessSaveCard.setOnClickListener {
            println("selectState === $selectedStateId")
            println("selectState individual === $selectedInvoiceStateId")
            println("selectState individual type=== $selectedBusinesChoiceTypeId")
            if (selectedBusinesChoiceTypeId == 0){
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
                            "Enter your GST number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    !isValidGST(binding.InvoiceTaxId.text.toString().trim()) -> {
                        Toast.makeText(
                            this@InvoiceBusinessDetailFormActivity,
                            "Enter valid GST number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    else -> {
                        if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                            val map = HashMap<String, Any>()
                            map["action"] = "addCompanyDetails"
                            map["user_id"] = ""+preference.getString(this@InvoiceBusinessDetailFormActivity,"INVOICE_USER_ID")
                            if (invoiceClickId != 0) {
                                map["id"] = invoiceClickId
                            }
                            map["bussiness_name"] =
                                "" + binding.InvoiceBusinessName.text.toString().trim()
                            map["email"] = "" + binding.InvoiceBusinessEmail.text.toString().trim()
                            map["bussiness_mobile"] =
                                "" + binding.InvoiceBusinessMobile.text.toString().trim()
                            map["billing_address_1"] =
                                "" + binding.InvoiceBillingAddress1.text.toString().trim()
                            map["billing_address_2"] = ""
                            map["website"] = "" + binding.InvoiceWebsite.text.toString().trim()
                            map["tax_name"] = ""
                            map["tax_id"] = "" + binding.InvoiceTaxId.text.toString().trim()
                            map["bussiness_id"] = "" + binding.InvoiceBusinessId.text.toString().trim()
                            map["state"] = "" + selectedStateId
                            map["bussiness_type"] = "" + selectedBusinesTypeId
                            map["type"] = "" + selectedBusinesChoiceTypeId
                            map["bank_name"] = "" + binding.InvoiceBankNameEdit.text.toString().trim()
                            map["bank_acoount_number"] = "" + binding.InvoiceBankAccountEdit.text.toString().trim()
                            map["micr_code"] = "" + binding.InvoiceBankMICREdit.text.toString().trim()
                            map["ifsc_code"] = "" + binding.InvoiceBankIFSCEdit.text.toString().trim()
                            map["bank_address"] = "" + binding.InvoiceBankAddressEdit.text.toString().trim()
                            map["contact_person"] = "" + binding.InvoiceContactPersonDetailEdit.text.toString().trim()

                            println("InvoiceRequest - $_TAG == $map")
                            InvoiceUtils.loadingProgress(this@InvoiceBusinessDetailFormActivity,""+InvoiceUtils.messageLoading,false).show()
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
            }else{
                when {
                    binding.IndividualName.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceBusinessDetailFormActivity,
                            "Enter Name",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.IndividualMobile.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceBusinessDetailFormActivity,
                            "Enter Mobile Number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.IndividualBillingAddress1.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceBusinessDetailFormActivity,
                            "Enter Address",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                /*    binding.IndividualEmail.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceBusinessDetailFormActivity,
                            "Enter your email",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }*/

                    binding.IndividualStateText.text.toString().trim().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceBusinessDetailFormActivity,
                            "Select  State",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    else -> {
                        if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                            val map = HashMap<String, Any>()
                            map["action"] = "addCompanyDetails"
                            map["user_id"] = ""+preference.getString(this@InvoiceBusinessDetailFormActivity,"INVOICE_USER_ID")
                            if (invoiceClickId != 0) {
                                map["id"] = invoiceClickId
                            }
                            map["bussiness_name"] =
                                "" + binding.IndividualName.text.toString().trim()
                            map["email"] = "" + binding.IndividualEmail.text.toString().trim()
                            map["bussiness_mobile"] =
                                "" + binding.IndividualMobile.text.toString().trim()
                            map["billing_address_1"] =
                                "" + binding.IndividualBillingAddress1.text.toString().trim()
                            map["state"] = "" + selectedInvoiceStateId
                            map["type"] = "" + selectedBusinesChoiceTypeId

                            println("InvoiceRequest - $_TAG == $map")
                            InvoiceUtils.loadingProgress(this@InvoiceBusinessDetailFormActivity,""+InvoiceUtils.messageLoading,false).show()
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

        }


        viewModel.getBusinessDetailForm.observe(this) { getBusinessDetail ->
            println("receiveData == ${getBusinessDetail.data!![0].bussinessName}")
            InvoiceUtils.loadingDialog.dismiss()
            if (getBusinessDetail.status.equals("success")) {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "" + getBusinessDetail.msg,
                    Toast.LENGTH_SHORT
                ).show()
                if (invoiceClickId != 0) {
                    val resultIntent = Intent()
                    val addedData = Gson().toJson(getBusinessDetail.data)
                    resultIntent.putExtra("INVOICE_FORM_DATA_UPDATE", addedData)
                    resultIntent.putExtra("INVOICE_FORM_CLICK_POS", clickPosition)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    if (fromInvoicePage == "InvoiceBusinessAndCustomerActivity_Business") {
                        val resultIntent = Intent()
                        getBusinessDetail.data!![0].status = getBusinessDetail.status
                        val addedData = Gson().toJson(getBusinessDetail.data)
                        resultIntent.putExtra("INVOICE_FORM_DATA", addedData)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }

            } else {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "" + getBusinessDetail.msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.addIndustries.observe(this) { getIndusData ->
            if (getIndusData.status.equals("success")) {
                if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                    val InputMap = HashMap<String, Any>()
                    InputMap["action"] = "getMaster"
                    InputMap["user_id"] = ""+preference.getString(this@InvoiceBusinessDetailFormActivity,"INVOICE_USER_ID")

                    println("InvoiceRequest - $_TAG == $InputMap")
                    viewModel.getOverAllMasterDetail(InputMap)
                } else {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Check Your Internet Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "Industrial Added Successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@InvoiceBusinessDetailFormActivity,
                    "" + getIndusData.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun isValidGST(gstNumber: String): Boolean {
        val gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"
        return gstNumber.matches(Regex(gstRegex))
    }

    fun isValidMICR(micr: String): Boolean {
        val micrRegex = Regex("^\\d{9}$")
        return micrRegex.matches(micr)
    }

    fun isValidIFSC(ifsc: String): Boolean {
        val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
        return ifscRegex.matches(ifsc)
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
            this@InvoiceBusinessDetailFormActivity,
            filteredList,
            "",
            this,
            fromInvoice,
            fromSpinner, onAddItemClick = {/*selectedItem ->
                val resultIntent = Intent()
                resultIntent.putExtra("selectedItem", selectedItem)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()*/
            },
            onDeleteItem = { deleteId, pos, actionName ->

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
            AddItemCard.setOnClickListener {
                stateDialog.dismiss()
                showMasterDataAddDia()
            }
        }
        val adapter = InvoiceMasterAdapter(
            this@InvoiceBusinessDetailFormActivity,
            filteredList,
            searchQuery.toString(),
            this,
            fromInvoice,
            fromSpinner, onAddItemClick = {

            }, onDeleteItem = { deleteId, pos,actionName ->

            },
            onSearchResult = {

            }
        ) // Pass the query
        recyclerView.adapter = adapter
    }


    private fun showMasterDataAddDia() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.invoice_create_dia_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etItemName = dialog.findViewById<AppCompatEditText>(R.id.etItemName)
        val btnCreate = dialog.findViewById<CardView>(R.id.btnCreate)


        btnCreate.setOnClickListener {
            val name = etItemName.text.toString().trim()
            if (name.isNotEmpty()) {
                dialog.dismiss()
                if (!InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessDetailFormActivity)) {
                    Toast.makeText(
                        this@InvoiceBusinessDetailFormActivity,
                        "Check Internet Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val InputMap = HashMap<String, Any>()
                InputMap["action"] = "addIndustrial"
                InputMap["industrial"] = name

                println("InvoiceRequest - $_TAG == $InputMap")
                viewModel.addIndustrial(InputMap)
            }
        }

        dialog.show()
    }

    companion object {
        var _TAG = "InvoiceBusinessDetailFormActivity"
    }

    override fun onItemClick(clikName: String, clikId: Int, fromClick: Int, position: Int) {
        stateDialog.dismiss()
        if (fromClick == 0) {

            if (selectedBusinesChoiceTypeId == 0){
                binding.InvoiceBusinessStateText.setText(clikName)
                selectedStateId = clikId
            }else{
                binding.IndividualStateText.setText(clikName)
                selectedInvoiceStateId = clikId
            }
        } else if (fromClick == 1) {
            selectedBusinesTypeId = clikId
            binding.InvoiceBusinessTypeText.text = clikName
        }
    }

}