package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceAddedItemDataAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceCreateFormBinding
import com.nithra.invoice_generator_tool.databinding.InvoiceBottomSheetBinding
import com.nithra.invoice_generator_tool.fragment.InvoiceDatePickerDialog
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData
import com.nithra.invoice_generator_tool.pdf_generator.InvoiceHtmlToPdfConvertor
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

@AndroidEntryPoint
class InvoiceCreateFormActivity : AppCompatActivity() {
    lateinit var binding: ActivityInvoiceCreateFormBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfState: MutableList<InvoiceGetDataMasterArray.GetStateList> = mutableListOf()
    var listOfIndustrial: MutableList<InvoiceGetDataMasterArray.GetIndustrialList> = mutableListOf()
    var listOfCompanyDetails: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
        mutableListOf()
    var listOfClientDetails: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
        mutableListOf()
    var listOfItemDetails: MutableList<InvoiceGetDataMasterArray.GetItemList> = mutableListOf()
    var listOfGstList: MutableList<InvoiceGetDataMasterArray.GstList> = mutableListOf()

    var preference = InvioceSharedPreference()
    var newInvoiceNumber = 0
    private lateinit var adapter: InvoiceAddedItemDataAdapter
    private var DynamicitemList = mutableListOf<InvoiceOfflineDynamicData>()

    var positionOfEDit = 0
    var clickEditId = 0
    var selectedPaymentType = 1
    var submitData = ""
    var selectedBusinessId = 0
    var selectedCustomerId = 0
    var selectedBusinessState = ""
    var selectedCustomerState = ""
    var selectedDate = ""
    var TotalAmount = 0.0
    var finalTotalAmount = 0.0
    var DisountAmount = 0.0
    var invoiceBillingAddress = ""
    var invoiceCustomerBillingAddress = ""
    var millis: Long = 0
    lateinit var htmlToPdfConvertor: InvoiceHtmlToPdfConvertor


    private val selectItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val json = preference.getString(
                    this@InvoiceCreateFormActivity,
                    "INVOICE_SELECTED_ITEMS"
                ) // Default empty list
                val type = object : TypeToken<InvoiceOfflineDynamicData>() {}.type
                val itemList: InvoiceOfflineDynamicData = Gson().fromJson(json, type)

                if (submitData == "item") {
                    // Add new item to list
                    DynamicitemList.add(itemList)
                    TotalAmount += itemList.total_amt!!.toDouble()
                    finalTotalAmount = TotalAmount - DisountAmount
                    binding.ItemsFinalAmount.text = "₹ " + finalTotalAmount
                    binding.ItemsTotalAmount.text = " ₹ " + TotalAmount
                    if (DynamicitemList.isEmpty()) {
                        binding.DynamicItemCard.visibility = View.GONE
                    } else {
                        binding.DynamicItemCard.visibility = View.VISIBLE
                    }
                    adapter.notifyDataSetChanged()
                } else if (submitData == "business") {
                    println("iTemLit === ${itemList.company_id}")
                    selectedBusinessId = itemList.company_id!!
                    selectedBusinessState = itemList.state!!
                    binding.InvoiceBusinessTypeText.text = itemList.bussiness_name
                } else {
                    println("iTemLit === ${itemList.invoice_id}")
                    selectedCustomerId = itemList.invoice_id!!
                    selectedCustomerState = itemList.state!!
                    binding.InvoiceCustomerName.text = itemList.name
                }

            }
        }

    private val selectItemEditLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val json = preference.getString(
                    this@InvoiceCreateFormActivity,
                    "INVOICE_EDIT_ITEMS"
                ) // Default empty list
                val type = object : TypeToken<List<InvoiceOfflineDynamicData>>() {}.type
                val itemList: List<InvoiceOfflineDynamicData> = Gson().fromJson(json, type)
                println("iTemLit from === ${itemList[0].item_id}")
                println("iTemLit click === ${clickEditId}")

                val index = itemList.indexOfFirst { clickEditId == itemList[0].item_id }
                println("inddex === $index")
                if (index != -1) {
                    DynamicitemList[index] = itemList[0]
                } else {
                    DynamicitemList.add(itemList[0])
                }
                adapter.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceCreateFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.title = "New Invoice Form"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image


        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        htmlToPdfConvertor = InvoiceHtmlToPdfConvertor(this@InvoiceCreateFormActivity)


        if (InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "1227994"

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceCreateFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.errorMessage.observe(this@InvoiceCreateFormActivity) {
            println("")
            Toast.makeText(this@InvoiceCreateFormActivity, "" + it, Toast.LENGTH_SHORT).show()
        }

        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            if (getMasterArray.status.equals("success")) {
                listOfState.addAll(getMasterArray.state!!)
                listOfIndustrial.addAll(getMasterArray.industrial!!)
                listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
                listOfClientDetails.addAll(getMasterArray.clientDetails!!)
                listOfItemDetails.addAll(getMasterArray.itemList!!)
                listOfGstList.addAll(getMasterArray.gst!!)

                selectedCustomerId = listOfClientDetails[0].clientId!!
                selectedBusinessId = listOfCompanyDetails[0].companyId!!
                selectedBusinessState = listOfCompanyDetails[0].state!!
                selectedCustomerState = listOfClientDetails[0].state!!
                binding.InvoiceBusinessTypeText.text = listOfCompanyDetails[0].bussinessName
            }
            val hasStatusKey = listOfItemDetails.any { !it.status.isNullOrEmpty() } ?: false
            println("map === " + hasStatusKey) // Output: true or false
        }

        if (DynamicitemList.isEmpty()) {
            binding.DynamicItemCard.visibility = View.GONE
        } else {
            binding.DynamicItemCard.visibility = View.VISIBLE
        }
        val currentInvoiceNumber =
            preference.getInt(this@InvoiceCreateFormActivity, "InvoiceNumber") // Start from 1000

        newInvoiceNumber = currentInvoiceNumber + 1

        binding.InvoiceIncreNumber.setText("#NITHRA_INV - " + newInvoiceNumber)

        binding.InvoiceBusinessChange.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "Check Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                submitData = "business"
                val intent = Intent(
                    this@InvoiceCreateFormActivity,
                    InvoiceBusinessAndCustomerActivity::class.java
                )
                intent.putExtra("fromInvoice", 1)
                intent.putExtra("InvoicefromPage", "Business")
                selectItemLauncher.launch(intent)
            }
        }

        binding.InvoiceCustomerAdd.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "Check Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                submitData = "customer"
                val intent = Intent(
                    this@InvoiceCreateFormActivity,
                    InvoiceBusinessAndCustomerActivity::class.java
                )
                intent.putExtra("fromInvoice", 1)
                intent.putExtra("InvoicefromPage", "Customers")
                selectItemLauncher.launch(intent)
            }

        }

        binding.AddInvoiceText.setOnClickListener {
            /* if (listOfItemDetails[0].status.equals("failure")){
                 val intent = Intent(this@InvoiceCreateFormActivity,InvoiceAddItemFormActivity::class.java)
                 startActivity(intent)
                 return@setOnClickListener
             }*/
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "Check Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (binding.InvoiceBusinessTypeText.text.toString().trim()
                        .isNotEmpty() && binding.InvoiceCustomerName.text.toString().trim()
                        .isNotEmpty()
                ) {
                    submitData = "item"
                    val intent = Intent(
                        this@InvoiceCreateFormActivity,
                        InvoiceBusinessAndCustomerActivity::class.java
                    )
                    intent.putExtra("fromInvoice", 1)
                    intent.putExtra("InvoicefromPage", "Items")
                    selectItemLauncher.launch(intent)
                } else {
                    Toast.makeText(
                        this@InvoiceCreateFormActivity,
                        "Please choose your business detail",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.InvoiceGenerateSaveCard.setOnClickListener {
            preference.putInt(this@InvoiceCreateFormActivity, "InvoiceNumber", newInvoiceNumber)
            if (InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
                GenerateInvoice()
            } else {
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "Check Your Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        viewModel.getAddedInvoiceList.observe(this@InvoiceCreateFormActivity) { getAddeddat ->
            if (getAddeddat.status.equals("success")) {
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "" + getAddeddat.msg,
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

        binding.ItemsDiscountAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Called after text is changed
                Log.d("TextWatcher", "After: ${s.toString()}")
                if (s.toString().isNotEmpty()) {
                    DisountAmount =
                        binding.ItemsDiscountAmount.text.toString().replace("₹", "").trim()
                            .toDouble() ?: 0.0
                    finalTotalAmount = TotalAmount - DisountAmount
                    binding.ItemsFinalAmount.text = "" + finalTotalAmount
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Called before text is changed
                Log.d("TextWatcher", "Before: ${s.toString()}")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Called while text is changing
                Log.d("TextWatcher", "OnChange: ${s.toString()}")
            }
        })

        adapter =
            InvoiceAddedItemDataAdapter(DynamicitemList, onItemClick = { listOfDynamic, position ->
                DynamicitemList.removeAt(position)
                adapter.notifyDataSetChanged()
                println("dyanmicList == ${DynamicitemList.size}")
                if (DynamicitemList.size != 0) {
                    TotalAmount -= listOfDynamic.total_amt!!.toInt()
                    finalTotalAmount = TotalAmount + DisountAmount
                    binding.ItemsFinalAmount.text = "₹ " + finalTotalAmount
                    binding.ItemsTotalAmount.text = " ₹ " + TotalAmount
                    binding.ItemsDiscountAmount.setText(" ₹ " + DisountAmount)
                    binding.DynamicItemCard.visibility = View.VISIBLE
                } else {
                    TotalAmount = 0.0
                    DisountAmount = 0.0
                    binding.ItemsDiscountAmount.setText("")
                    binding.ItemsTotalAmount.text = " ₹ " + TotalAmount
                    binding.ItemsTotalAmount.text = " ₹ " + TotalAmount
                    binding.DynamicItemCard.visibility = View.GONE
                }
            }, onShowItem = { binding, clickItemPOs ->

                println("selectedBus == $selectedBusinessState")
                println("selectedCus == $selectedCustomerState")

                if (selectedBusinessState == selectedCustomerState) {
                    for (i in listOfGstList.indices) {
                        if (clickItemPOs.tax.toString() == listOfGstList[i].id.toString()) {
                            val totalGST = listOfGstList[i].gst
                            val result = splitGST(totalGST!!)
                            if (result != null) {
                                val (sgst, cgst) = result
                                binding.itemCGstSplit.text =
                                    "" + sgst + " % " + "sgst" + " + " + cgst + " % " + "cgst"
                            }
                        }
                    }
                } else {
                    for (i in listOfGstList.indices) {
                        if (clickItemPOs.tax.toString() == listOfGstList[i].id.toString()) {
                            binding.itemCGstSplit.text = "" + listOfGstList[i].gst + " % " + "Igst"
                        }
                    }

                }

            }, OnEditClick = { clickId, pos ->
                val intent = Intent(
                    this@InvoiceCreateFormActivity,
                    InvoiceAddItemFormActivity::class.java
                )
                intent.putExtra("fromInvoice", 1)
                intent.putExtra("clickDataId", clickId.item_id)
                positionOfEDit = pos
                clickEditId = clickId.item_id!!
                selectItemEditLauncher.launch(intent)
            })
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        binding.InvoiceDate.text = currentDate

        val sdf1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate1 = sdf1.format(Date())
        selectedDate = currentDate1
        binding.InvoicePaymentType.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.InvoicePaid -> {
                    selectedPaymentType = 1
                    binding.InvoicePaidAmountLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateLay.visibility = View.GONE
                    binding.InvoiceRemarkLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateEdit.text = ""
                    binding.InvoiceTermsAndCondition.visibility = View.VISIBLE
                }

                R.id.InvoiceUnPaid -> {
                    selectedPaymentType = 3
                    binding.InvoicePaidAmountLay.visibility = View.GONE
                    binding.InvoiceDueDateLay.visibility = View.GONE
                    binding.InvoiceRemarkLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateEdit.text = ""
                    binding.InvoiceTermsAndCondition.visibility = View.VISIBLE
                }

                R.id.InvoicePartiallyPaid -> {
                    selectedPaymentType = 2
                    binding.InvoicePaidAmountLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateEdit.text = currentDate
                    binding.InvoiceRemarkLay.visibility = View.VISIBLE
                    binding.InvoiceTermsAndCondition.visibility = View.VISIBLE
                }
            }

        }

        binding.InvoiceBusinessTypeText.setOnClickListener {
            showBottomSheet()
        }

        binding.InvoiceCustomerName.setOnClickListener {
            if (!binding.InvoiceCustomerName.text.toString().trim().equals("")) {
                showBottomSheet1()
            }
        }


        binding.InvoiceDateLay.setOnClickListener {
            val datePicker = InvoiceDatePickerDialog({ selectedDatePickerDate ->
                showDatePickerFormat(selectedDatePickerDate)
            }, binding.InvoiceDate.text.toString().trim())
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.InvoiceDueDateEditLay.setOnClickListener {
            val datePicker = InvoiceDatePickerDialog({ selectedDatePickerDate ->
                showDatePickerFormat(selectedDatePickerDate)
            }, binding.InvoiceDate.text.toString().trim())
            datePicker.show(supportFragmentManager, "datePicker")
        }

        binding.dynamicContainer.isNestedScrollingEnabled = true

        binding.dynamicContainer.layoutManager = LinearLayoutManager(this)
        binding.dynamicContainer.adapter = adapter

    }

    fun splitGST(totalGST: String): Pair<Double, Double>? {
        return try {
            val gstValue = totalGST.toDouble() // Convert String to Double
            val sgst = gstValue / 2
            val cgst = gstValue / 2
            Pair(sgst, cgst)
        } catch (e: NumberFormatException) {
            println("Invalid GST value: $totalGST") // Handle invalid input
            null
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

        binding.InvoiceDate.text = displayDate
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val binding = InvoiceBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)
        binding.InvoiceBusinessLay.visibility = View.VISIBLE
        binding.InvoiceCustomerLay.visibility = View.GONE
        for (i in listOfCompanyDetails.indices) {
            if (selectedBusinessId == listOfCompanyDetails[i].companyId) {

                binding.InvoiceBusinessName.text = listOfCompanyDetails[i].bussinessName

                if (listOfCompanyDetails[i].bussinessId!!.isNotEmpty()) {
                    binding.InvoiceBusinessTypeText.text = listOfCompanyDetails[i].industrialName
                } else {
                    binding.InvoiceBusinessTypeTextLay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].bussinessId!!.isNotEmpty()) {
                    binding.InvoiceBusinessId.text = listOfCompanyDetails[i].bussinessId
                } else {
                    binding.InvoiceBusinessIdLay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].email!!.isNotEmpty()) {
                    binding.InvoiceBusinessEmail.text = listOfCompanyDetails[i].email
                } else {
                    binding.InvoiceBusinessEmailLay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].mobile!!.isNotEmpty()) {
                    binding.InvoiceMobileNamber1.text = listOfCompanyDetails[i].mobile
                } else {
                    binding.InvoiceMobileNamber1Lay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].bussinessMobile!!.isNotEmpty()) {
                    binding.InvoiceMobileNamber2.text = listOfCompanyDetails[i].bussinessMobile
                } else {
                    binding.InvoiceMobileNamber2Lay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].billingAddress1!!.isNotEmpty()) {
                    binding.InvoiceBillingAddress.text = listOfCompanyDetails[i].billingAddress1
                    invoiceBillingAddress = "" + listOfCompanyDetails[i].billingAddress1
                } else {
                    binding.InvoiceBillingAddressLay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].state!!.isNotEmpty()) {
                    binding.InvoiceBusinessState.text = listOfCompanyDetails[i].state
                } else {
                    binding.InvoiceBusinessStateLay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].website!!.isNotEmpty()) {
                    binding.InvoiceBusinessWebsite.text = listOfCompanyDetails[i].website
                } else {
                    binding.InvoiceBusinessWebsiteLay.visibility = View.GONE
                }
                if (listOfCompanyDetails[i].taxId!!.isNotEmpty()) {
                    binding.InvoiceBusinessGST.text = listOfCompanyDetails[i].taxId
                } else {
                    binding.InvoiceBusinessGSTLay.visibility = View.GONE
                }

            }
        }
        bottomSheetDialog.show()
    }


    private fun showBottomSheet1() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val binding = InvoiceBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)
        binding.InvoiceBusinessLay.visibility = View.GONE
        binding.InvoiceCustomerLay.visibility = View.VISIBLE

        for (i in listOfClientDetails.indices) {
            if (selectedCustomerId == listOfClientDetails[i].clientId) {
                binding.InvoiceCustomerName.text = listOfClientDetails[i].name
                if (listOfClientDetails[i].type == 1) {
                    binding.InvoiceCustomerTypeText.text = "Individual"
                } else {
                    binding.InvoiceCustomerTypeText.text = "Business"
                }
                if (listOfClientDetails[i].displayName!!.isNotEmpty()) {
                    binding.InvoiceCustomerDisplay.text = listOfClientDetails[i].displayName
                } else {
                    binding.InvoiceCustomerDisplayLay.visibility = View.GONE
                }
                if (listOfClientDetails[i].email!!.isNotEmpty()) {
                    binding.InvoiceCustomerEmail.text = listOfClientDetails[i].email
                } else {
                    binding.InvoiceCustomerEmailLay.visibility = View.GONE
                }
                if (listOfClientDetails[i].mobile1!!.isNotEmpty()) {
                    binding.InvoiceCusMobileNamber1.text = listOfClientDetails[i].mobile1
                } else {
                    binding.InvoiceCusMobileNamber1Lay.visibility = View.GONE
                }
                if (listOfClientDetails[i].mobile2!!.isNotEmpty()) {
                    binding.InvoiceCusMobileNamber2.text = listOfClientDetails[i].mobile2
                } else {
                    binding.InvoiceCusMobileNamber2Lay.visibility = View.GONE
                }
                if (listOfClientDetails[i].billingAddress!!.isNotEmpty()) {
                    binding.InvoiceCusBillingAddress.text = listOfClientDetails[i].billingAddress
                    invoiceCustomerBillingAddress = "" + listOfClientDetails[i].billingAddress
                } else {
                    binding.InvoiceCusBillingAddressLay.visibility = View.GONE
                }
                if (listOfClientDetails[i].shippingAddress!!.isNotEmpty()) {
                    binding.InvoiceCusShippingAddress.text = listOfClientDetails[i].shippingAddress
                } else {
                    binding.InvoiceCusShippingAddressLay.visibility = View.GONE
                }
                if (listOfClientDetails[i].state!!.isNotEmpty()) {
                    binding.InvoiceCustomerState.text = listOfClientDetails[i].state
                } else {
                    binding.InvoiceCustomerStateLay.visibility = View.GONE
                }
                if (listOfClientDetails[i].remark!!.isNotEmpty()) {
                    binding.InvoiceCustomerRemark.text = listOfClientDetails[i].remark
                } else {
                    binding.InvoiceCustomerRemarkLay.visibility = View.GONE
                }
                if (listOfClientDetails[i].taxId!!.isNotEmpty()) {
                    binding.InvoiceCustomerGST.text = listOfClientDetails[i].taxId
                } else {
                    binding.InvoiceCustomerGSTLay.visibility = View.GONE
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun GenerateInvoice() {

        val htmlContent = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f9f9f9;
            padding: 20px;
        }
        .invoice-container {
      width: 100%;
    margin: auto;
    border-radius: 8px;
    padding: 0; 
    border: none; 
    background: none;
    box-shadow: none;
        }
        .info-address{
        font-size: 18px;
             margin-left: 15px;
       }
        .header {
            text-align: center;
            font-size: 45px;
            font-weight: bold;
            color: green;
              padding: 20px;
            margin-bottom: 15px;
        }
        .info-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 15px;
        }
        .info-table th, .info-table td {
            padding: 8px;
            text-align: left;
        }
        .info-table th {
            background-color: green;
            color: white;
        }
        .details {
            font-size: 14px;
            margin-bottom: 15px;
             margin-left: 15px;
        }
        .table-container {
            width: 100%;
             align:center
            border-collapse: collapse;
        }
        .table-container th {
            background: green;
            color: white;
            padding: 8px;
            text-align: left;
        }
        .table-container td {
            border: 1px solid #ccc;
            padding: 8px;
            text-align: left;
        }
        .total {
            text-align: right;
            font-size: 20px;
            font-weight: bold;
            margin-top: 10px;
              padding: 20px;
        }
        .note {
            font-size: 14px;
            margin-top: 15px;
            margin-left: 15px;
        }
        .note-title {
        font-size: 22px;
        margin-left: 15px;
        font-weight: bold;
    }
    .note-text {
        font-size: 18px;
         margin-left: 15px;
    }
    </style>
</head>
<body>
    <div class="invoice-container">
        <div class="header">INVOICE</div>

        <!-- From & To Table -->
        <table class="info-table">
            <tr>
                <th>From</th>
                <th>To</th>
            </tr>
            <tr>
                <td class="info-address">
                  $invoiceBillingAddress
                </td>
                <td class="info-address">
                $invoiceCustomerBillingAddress
                </td>
            </tr>
        </table>

        <!-- Invoice Details Table -->
        <table class="info-table">
            <tr>
                <th>Invoice No</th>
                <th>Invoice Date</th>
                <th>Due Date</th>
                <th>Due Period</th>
            </tr>
            <tr >
                <td class="info-address" >NH8123RE</td>
                <td class="info-address" >23-May-2025</td>
                <td class="info-address" >23-Jun-2025</td>
                <td class="info-address">25 days</td>
            </tr>
        </table>

        <!-- Item List -->
        <table class="table-container">
            <tr>
                <th>S.no / Item List</th>
                <th>Qty</th>
                <th>Amount</th>
            </tr>
            <tr>
                <td class="info-address" >1. Sugar<br><small>(5% GST included)</small></td>
                <td class="info-address">2 kgs</td>
                <td class="info-address" >₹ 250</td>
            </tr>
            <tr>
                <td class="info-address" >2. Rice<br><small>(5% GST included)</small></td>
                <td class="info-address" >2 kgs</td>
                <td class="info-address">₹ 5000</td>
            </tr>
            <tr>
                <td class="info-address">3. Tomato<br><small>(5% GST included)</small></td>
                <td class="info-address">1/2 kgs</td>
                <td class="info-address">₹ 350</td>
            </tr>
            <tr>
                <td class="info-address">4. Wheat<br><small>(5% GST included)</small></td>
                <td class="info-address">2 kgs</td>
                <td class="info-address">₹ 50000</td>
            </tr>
            
            <tr>
                <td class="info-address">4. Wheat<br><small>(5% GST included)</small></td>
                <td class="info-address">2 kgs</td>
                <td class="info-address">₹ 50000</td>
            </tr>
            
            
        </table>

        <div class="total">Total: $finalTotalAmount</div>

     <p><span class="note-title">Note:</span> <span class="note-text">${binding.InvoiceRemarkEdit.text.toString()}</span></p>

     <p><span class="note-title">Terms & Conditions:</span></p>
     <p class="note-text">${binding.InvoiceTermsAndConditionEdit.text.toString()}</p>
    </div>
</body>
</html>
        """.trimIndent()

        createPdf(htmlContent)

        when{
            binding.InvoiceBusinessTypeText.text.toString().isEmpty()->{
                Toast.makeText(this@InvoiceCreateFormActivity, "Select business details", Toast.LENGTH_SHORT).show()
            }
        }
        //check
        val map = LinkedHashMap<String, Any>()
        map["action"] = "addInvoice"
        map["user_id"] = "1227994"
        map["bussiness_id"] = "" + selectedBusinessId
        map["client_id"] = "" + selectedCustomerId
        map["invoice_number"] = "" + binding.InvoiceIncreNumber.text.toString().trim()
        map["order_no"] = "" + binding.InvoiceOrderId.text.toString().trim()
        map["invoice_date"] = "" + selectedDate
        map["amt_type"] = "" + selectedPaymentType
        map["paid_amt"] = "" + binding.InvoicePaidAmount.text.toString().trim()
        map["due_date"] = "" + binding.InvoiceDueDateEdit.text.toString().trim()
        map["remark"] = "" + binding.InvoiceRemarkEdit.text.toString().trim()
        map["terms_condition"] = "" + binding.InvoiceTermsAndConditionEdit.text.toString()
        map["total_amount"] = "" + finalTotalAmount
        map["discount_amt"] = "" + DisountAmount
        // Create the desired map structure
        val itemList = DynamicitemList.map {
            mapOf(
                "item_id" to it.item_id.toString(),
                "amount" to it.amount.toString(),
                "qty_type" to it.qty_type.toString(),
                "qty" to it.qty.toString(),
                "tax" to it.tax.toString(),
                "description" to it.description.toString(),
                "discount_type" to it.discount_type.toString(),
                "discount" to it.discount.toString()
            )
        }
        map["item"] = itemList

        println("InvoiceRequest - : $map")
//   createInvoicePdf(this@InvoiceCreateFormActivity,DynamicitemList,map,)

        // viewModel.addInvoiceList(map)

    }

    private fun createPdf(htmlContent: String) {
        val handler = Handler(Objects.requireNonNull(Looper.myLooper())!!)
        InvoiceUtils.loadingProgress(this@InvoiceCreateFormActivity, "loading...", false).show()
        try {
            // define output file location and html content
            val pdfLocation = File(
                getPdfFilePath()!!.path,
                "InvoiceGenerate " + System.currentTimeMillis() + ".pdf"
            )
// Check if the file exists and delete it
            if (pdfLocation.exists()) {
                pdfLocation.delete()
                Thread.sleep(20)
                pdfLocation.createNewFile()
            } else {
                pdfLocation.createNewFile()
            }
            // start conversion
            htmlToPdfConvertor.convert(
                pdfLocation = pdfLocation,
                htmlString = htmlContent,
                onPdfGenerationFailed = { exception ->
                    // something went wrong, stop loading and log the exception
                    InvoiceUtils.loadingDialog.dismiss()
                    exception.printStackTrace()

                },
                onPdfGenerated = { pdfFile ->
                    // pdf was generated, stop the loading and open it
                    InvoiceUtils.loadingDialog.dismiss()
                   // openPdf(pdfFile)
                })
        } catch (e: Exception) {
            e.printStackTrace()
            println("pdfGstCalulation " + e.message.toString())

        }

    }

    private fun openPdf(pdfFile: File) {
        if (pdfFile.exists()) {
            val uri = FileProvider.getUriForFile(
                this@InvoiceCreateFormActivity,
                "$packageName.provider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(intent, "Open PDF with"))
        }
    }

    private fun getPdfFilePath(): File? {
        val dir = File(filesDir.toString() + "/Nithra/Invoice")
        return if (!dir.exists()) {
            val success = dir.mkdirs()
            if (success) {
                Log.d("pdf", "Pdf Directory created")
                dir
            } else {
                Log.i("ATG", "Can't create directory to save the image")
                null
            }
        } else {
            dir
        }
    }

    /*   fun createInvoicePdf(
           context: Context, invoice: MutableList<InvoiceOfflineDynamicData>,
           map: LinkedHashMap<String, Any>
       ) {
           val root = filesDir.toString()
           val handler = Handler(Looper.myLooper()!!)
           val millis = System.currentTimeMillis() / 1000
           val fileName = "Invoice_Pdf_$millis.pdf"
           val emptyPdfFile = createEmptyFile()
           var filePath = emptyPdfFile.path
           println("PDF Created Successfully!")

           try {
               InvoiceUtils.loadingProgress(this@InvoiceCreateFormActivity, "PDF Generating...", false).show()
               val paint = Paint().apply {
                   textSize = 20f
                   color = Color.BLACK
               }

               val document = Document(PageSize.A4)
               val writer = PdfWriter.getInstance(document, FileOutputStream(emptyPdfFile))

               // Set the header event
               writer.pageEvent =
                   HeaderFooterEvent(
                       this@InvoiceCreateFormActivity,
                       getString(R.string.tc_app_name_english)
                   )

               document.open()

               // Add spacing before the title
               val emptyLine2 = Paragraph("\n\n\n")
               document.add(emptyLine2)

               val tablenew = PdfPTable(1) // 2 columns
               tablenew.widthPercentage = 100f // Table width is 100% of page width

               tablenew.setWidths(floatArrayOf(1f))

               val cell = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       "GST Calculation",
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               cell.horizontalAlignment = Element.ALIGN_CENTER // Horizontally center
               cell.setVerticalAlignment(Element.ALIGN_MIDDLE)
               tablenew.addCell(cell)
               document.add(tablenew)

               val table = PdfPTable(2) // 2 columns
               table.widthPercentage = 100f // Table width is 100% of page width
               table.setWidths(floatArrayOf(1f, 1f))

               val textCell = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       listofData["purchase_amount"]!!.first,
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               textCell.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               textCell.setVerticalAlignment(Element.ALIGN_MIDDLE)
               textCell.backgroundColor = BaseColor.WHITE
               table.addCell(textCell)


               val quantityCell = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       "Rs. " + listofData["purchase_amount"]!!.second.toString(),
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               quantityCell.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               quantityCell.setVerticalAlignment(Element.ALIGN_MIDDLE)
               quantityCell.backgroundColor = BaseColor.WHITE
               table.addCell(quantityCell)

               val textCell1 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       listofData["SGST"]!!.first.toString(),
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               textCell1.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               textCell1.setVerticalAlignment(Element.ALIGN_MIDDLE)
               textCell1.backgroundColor = BaseColor.WHITE
               table.addCell(textCell1)

               val quantityCell1 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       "Rs. " + listofData["SGST"]!!.second.toString(),
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               quantityCell1.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               quantityCell1.setVerticalAlignment(Element.ALIGN_MIDDLE)
               quantityCell1.backgroundColor = BaseColor.WHITE
               table.addCell(quantityCell1)

               val textCell2 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       listofData["CGST"]!!.first,
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               textCell2.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               textCell2.setVerticalAlignment(Element.ALIGN_MIDDLE)
               textCell2.backgroundColor = BaseColor.WHITE
               table.addCell(textCell2)

               val quantityCell2 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       "Rs. " + listofData["CGST"]!!.second.toString(),
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               quantityCell2.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               quantityCell2.setVerticalAlignment(Element.ALIGN_MIDDLE)
               quantityCell2.backgroundColor = BaseColor.WHITE
               table.addCell(quantityCell2)


               val textCell3 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       listofData["total_gst"]!!.first,
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               textCell3.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               textCell3.setVerticalAlignment(Element.ALIGN_MIDDLE)
               textCell3.backgroundColor = BaseColor.WHITE
               table.addCell(textCell3)

               val quantityCell3 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       "Rs. " + listofData["total_gst"]!!.second.toString(),
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               quantityCell3.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               quantityCell3.setVerticalAlignment(Element.ALIGN_MIDDLE)
               quantityCell3.backgroundColor = BaseColor.WHITE
               table.addCell(quantityCell3)

               if (listofData["amount_without_gst"] != Pair("", "")) {
                   val textCell4 = PdfPCell().apply {
                       val fontSize = 15f // Set your desired font size here
                       val font = Font()
                       font.size = fontSize
                       phrase = Phrase(
                           listofData["amount_without_gst"]!!.first,
                           font
                       )
                       borderWidth = 0.5f
                       paddingTop = 10f
                       paddingBottom = 10f
                       paddingLeft = 10f
                       paddingRight = 10f
                   }
                   textCell4.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
                   textCell4.setVerticalAlignment(Element.ALIGN_MIDDLE)
                   textCell4.backgroundColor = BaseColor.WHITE
                   table.addCell(textCell4)

                   val quantityCell4 = PdfPCell().apply {
                       val fontSize = 15f // Set your desired font size here
                       val font = Font()
                       font.size = fontSize
                       phrase = Phrase(
                           "Rs. " + listofData["amount_without_gst"]!!.second.toString(),
                           font
                       )
                       borderWidth = 0.5f
                       paddingTop = 10f
                       paddingBottom = 10f
                       paddingLeft = 10f
                       paddingRight = 10f
                   }
                   quantityCell4.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
                   quantityCell4.setVerticalAlignment(Element.ALIGN_MIDDLE)
                   quantityCell4.backgroundColor = BaseColor.WHITE
                   table.addCell(quantityCell4)
               }

               val textCell5 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(listofData["total_with_gst"]!!.first, font)
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               textCell5.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               textCell5.setVerticalAlignment(Element.ALIGN_MIDDLE)
               textCell5.backgroundColor = BaseColor.WHITE
               table.addCell(textCell5)

               val quantityCell5 = PdfPCell().apply {
                   val fontSize = 15f // Set your desired font size here
                   val font = Font()
                   font.size = fontSize
                   phrase = Phrase(
                       "Rs. " + listofData["total_with_gst"]!!.second.toString(),
                       font
                   )
                   borderWidth = 0.5f
                   paddingTop = 10f
                   paddingBottom = 10f
                   paddingLeft = 10f
                   paddingRight = 10f
               }
               quantityCell5.horizontalAlignment = Element.ALIGN_LEFT // Horizontally center
               quantityCell5.setVerticalAlignment(Element.ALIGN_MIDDLE)
               quantityCell5.backgroundColor = BaseColor.WHITE
               table.addCell(quantityCell5)


               //document.add(PdfPCell(Paragraph("$vegtableName - இன்றைய விலை நிலவரம்"))) // Add table title directly as a cell
               document.add(table)
               document.close()
               InvoiceUtils.loadingDialog.dismiss()

               handler.postDelayed({
                   val uri =
                       FileProvider.getUriForFile(
                           this@GstCalulation_Resultpage,
                           packageName,
                           emptyPdfFile
                       )
                   println("pdfFile : $emptyPdfFile")

                   *//*        val i = Intent(this@GstCalulation_Resultpage, TcPdfViewer::class.java)
                        i.putExtra("title", "")
                        i.putExtra("type", "uri")
                        i.putExtra("path", emptyPdfFile)*//*

                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "application/pdf"
                sharingIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "நித்ரா காலண்டர்"
                )

                sharingIntent.putExtra(
                    Intent.EXTRA_TEXT, """
     ஜிஎஸ்டி, ஈ.எம்.ஐ மற்றும் வட்டி கணக்கீடு குறித்த கூடுதல் விவரங்களையும் மற்றும் நாட்காட்டி பற்றிய(Tamil Calendar) தகவல்களை இலவசமாக பெற கீழ்க்கண்ட லிங்கை கிளிக் செய்யுங்கள்
     
     http://bit.ly/2vfArRP
     """.trimIndent()
                )
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(sharingIntent, "Share Via"))
                Utils.mProgress.dismiss()
                load = 0
            }, 500)
        } catch (e: IOException) {
            e.printStackTrace()
            Utils.mProgress.dismiss()
            println("dir==$e")
        }
    }*/

    private fun createEmptyFile(): File {
        val fileName = "Invoice_Pdf_$millis.pdf"
        val directory = getExternalBreezyDirectory(this@InvoiceCreateFormActivity)
        println("dir==$directory")
        val file = File(directory, fileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }

    fun getExternalBreezyDirectory(context: Context): File {
        val directory = File(context.filesDir.toString() + "/Nithra/InvoiceGenerator")
        println("dir==1$directory")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    companion object {
        var _TAG = "InvoiceCreateFormActivity"
        const val REQUEST_CODE_SELECT_ITEM = 100

    }
}