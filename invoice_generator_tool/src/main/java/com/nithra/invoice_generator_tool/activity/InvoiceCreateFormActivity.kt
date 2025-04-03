package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
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
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var filePart: File? = null

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
                    println("itemId==== ${itemList.item_id}")
                    for (i in DynamicitemList.indices){
                        println("itemId Dy==== ${DynamicitemList[i].item_id}")
                        if (DynamicitemList[i].item_id == itemList.item_id){
                            Toast.makeText(this@InvoiceCreateFormActivity, "This item is already in the list", Toast.LENGTH_SHORT).show()
                            return@registerForActivityResult
                        }
                    }
                    DynamicitemList.add(itemList)
                    TotalAmount += itemList.total_amt!!.toDouble()
                    finalTotalAmount = TotalAmount - DisountAmount
                    val total = String.format ("%.2f", finalTotalAmount)
                    binding.ItemsFinalAmount.text = "₹ " + total
                    binding.ItemsTotalAmount.text = " ₹ " + total
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
                    invoiceBillingAddress = itemList.billing_address_1!!
                    binding.InvoiceBusinessTypeText.text = itemList.bussiness_name
                } else {
                    println("iTemLit === ${itemList.invoice_id}")
                    selectedCustomerId = itemList.invoice_id!!
                    selectedCustomerState = itemList.state!!
                    invoiceCustomerBillingAddress = itemList.billing_address!!
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
                println("iTemLit from amt === ${itemList[0].total_amt}")

                println("iTemLit click === ${clickEditId}")

               // val index = itemList.indexOfFirst { clickEditId == itemList[0].item_id }
                var foundIndex = -1  // Default to -1 (not found)
                for (i in DynamicitemList.indices) {
                    println("itemId Dy==== ${DynamicitemList[i].item_id}")
                    if (DynamicitemList[i].item_id == itemList[0].item_id) {
                        foundIndex = i  // Store the index
                        break  // Exit loop early
                    }
                }

                println("iTemLit from amt name === ${itemList[0].item_name}")
                println("inddex === $foundIndex")
                println("inddex size === ${DynamicitemList.size}")
                println("inddex itemList size === ${itemList.size}")
                if (foundIndex != -1) {
                    DynamicitemList[foundIndex] = itemList[0]
                } else {
                    DynamicitemList.add(itemList[0])
                }

                TotalAmount = DynamicitemList.sumOf { it.total_amt?.toDouble() ?: 0.0 }
                finalTotalAmount = TotalAmount - DisountAmount
                val total = String.format("%.2f", finalTotalAmount)
// Update UI
                binding.ItemsFinalAmount.text = "₹ $total"
                binding.ItemsTotalAmount.text = "₹ $total"

                if (DynamicitemList.isEmpty()) {
                    binding.DynamicItemCard.visibility = View.GONE
                } else {
                    binding.DynamicItemCard.visibility = View.VISIBLE
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
            InvoiceUtils.loadingProgress(
                this@InvoiceCreateFormActivity,
                "" + InvoiceUtils.messageLoading,
                false
            ).show()
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = ""+InvoiceUtils.userId

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
            binding.mainCusFormLay.visibility = View.VISIBLE
            InvoiceUtils.loadingDialog.dismiss()
            Toast.makeText(this@InvoiceCreateFormActivity, "" + it, Toast.LENGTH_SHORT).show()
        }

        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            binding.mainCusFormLay.visibility = View.VISIBLE
            InvoiceUtils.loadingDialog.dismiss()
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
                invoiceBillingAddress = listOfCompanyDetails[0].billingAddress1!!
            }
            val hasStatusKey = listOfItemDetails.any { !it.status.isNullOrEmpty() } ?: false
            println("map === " + hasStatusKey) // Output: true or false
        }

        if (DynamicitemList.isEmpty()) {
            binding.DynamicItemCard.visibility = View.GONE
        } else {
            binding.DynamicItemCard.visibility = View.VISIBLE
        }
        val currentInvoiceNumber = preference.getInt(this@InvoiceCreateFormActivity, "InvoiceNumber") // Start from 1000

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
                        "Please choose your customer detail",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.InvoiceGeneratePreview.setOnClickListener {
            val htmlContent = generateInvoiceHtml(
                invoiceBillingAddress = invoiceBillingAddress,
                invoiceCustomerBillingAddress = invoiceCustomerBillingAddress,
                finalTotalAmount = finalTotalAmount.toString(),
                invoiceItems = DynamicitemList,
                binding = binding
            )
            runBlocking {

                createPdf(htmlContent) { pdfFile ->
                    if (pdfFile != null) {
                        // Step 3: Validate the PDF file before sending
                        Log.d(
                            "Upload",
                            "PDF File Ready: ${pdfFile.path}, Size: ${pdfFile.length()} bytes"
                        )
                        val intent = Intent(this@InvoiceCreateFormActivity, InvoiceFilePdfViewActivity::class.java).apply {
                            putExtra("pdf_path", pdfFile.absolutePath)
                        }
                        startActivity(intent)

                    } else {
                        Log.e("Upload Error", "PDF file is not valid, cannot upload!")
                    }
                }
            }
        }
        binding.InvoiceGenerateSaveCard.setOnClickListener {
            preference.putInt(this@InvoiceCreateFormActivity, "InvoiceNumber", newInvoiceNumber)
            if (InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {

                val htmlContent = generateInvoiceHtml(
                    invoiceBillingAddress = invoiceBillingAddress,
                    invoiceCustomerBillingAddress = invoiceCustomerBillingAddress,
                    finalTotalAmount = finalTotalAmount.toString(),
                    invoiceItems = DynamicitemList,
                    binding = binding
                )

                when {
                    binding.InvoiceBusinessTypeText.text.toString().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceCreateFormActivity,
                            "Select business details",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    binding.InvoiceCustomerName.text.toString().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceCreateFormActivity,
                            "Select customer details",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    binding.InvoiceIncreNumber.text.toString().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceCreateFormActivity,
                            "Enter invoice number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    binding.InvoiceDate.text.toString().isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceCreateFormActivity,
                            "Enter invoice number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    DynamicitemList.isEmpty() ->{
                        Toast.makeText(
                            this@InvoiceCreateFormActivity,
                            "Add invoice item",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    selectedPaymentType == 1 ->{
                        if (binding.InvoicePaidAmount.text.toString().isEmpty()){
                            Toast.makeText(
                                this@InvoiceCreateFormActivity,
                                "Enter paid amount",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }
                    selectedPaymentType == 2 ->{
                        if (binding.InvoicePaidAmount.text.toString().trim().isEmpty()){
                            Toast.makeText(
                                this@InvoiceCreateFormActivity,
                                "Enter paid amount",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }else if (binding.InvoiceDueDateEdit.text.toString().trim().isEmpty()){
                            Toast.makeText(
                                this@InvoiceCreateFormActivity,
                                "Enter due date",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }

                }

                runBlocking {

                    createPdf(htmlContent) { pdfFile ->
                        if (pdfFile != null) {
                            // Step 3: Validate the PDF file before sending
                            Log.d(
                                "Upload",
                                "PDF File Ready: ${pdfFile.path}, Size: ${pdfFile.length()} bytes"
                            )

                            val requestBody = pdfFile.asRequestBody("application/pdf".toMediaTypeOrNull())

                            val pdfPart = MultipartBody.Part.createFormData(
                                "pdf",
                                pdfFile.path,
                                requestBody
                            )
                            GenerateInvoice(pdfPart)

                        } else {
                            Log.e("Upload Error", "PDF file is not valid, cannot upload!")
                        }
                    }
                }
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
                val pdfFileUrl = getAddeddat.data?.invoiceDetails!![0].pdf
                val bussinessName = getAddeddat.data?.invoiceDetails!![0].bussinessName
                if (pdfFileUrl!!.isNotEmpty()) {
                    val intent = Intent(this@InvoiceCreateFormActivity, InvoicePdfViewActivity::class.java)
                    intent.putExtra("InvoicePdfLink", pdfFileUrl)
                    intent.putExtra("InvoicePdfName", bussinessName)
                    startActivity(intent)
                }

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
                    TotalAmount -= listOfDynamic.total_amt!!.toDouble()
                  //  finalTotalAmount = TotalAmount + DisountAmount
                    val finalTotalAmountNew = (TotalAmount + DisountAmount).toBigDecimal().setScale(2, RoundingMode.HALF_UP)
                  finalTotalAmount = finalTotalAmountNew.toDouble()
                    val total = String.format ("%.2f", finalTotalAmount)
                    val totalAmt = String.format ("%.2f", TotalAmount)
                    binding.ItemsFinalAmount.text = "₹ " + total
                    binding.ItemsTotalAmount.text = " ₹ " + totalAmt
                    binding.ItemsDiscountAmount.setText(" ₹ " + DisountAmount)
                    binding.DynamicItemCard.visibility = View.VISIBLE
                } else {
                    TotalAmount = 0.0
                    DisountAmount = 0.0
                    binding.ItemsDiscountAmount.setText("")
                    val total = String.format ("%.2f", finalTotalAmount)
                    binding.ItemsTotalAmount.text = " ₹ " + total
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
                intent.putExtra("OffLineEdit", 1)
                intent.putExtra("clickDataId", clickId.item_id)
                positionOfEDit = pos
                clickEditId = clickId.item_id!!
                val addedData = Gson().toJson(DynamicitemList)
                println("DynamicList == $addedData")
                preference.putString(this@InvoiceCreateFormActivity,"INVOICE_SET_LIST",addedData)
                intent.putExtra("InvoiceCickPosition",pos)
           //     intent.putExtra("INVOICE_SET_LIST", addedData)
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
            }, binding.InvoiceDueDateEdit.text.toString().trim())
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

    private fun GenerateInvoice(pdfPart: MultipartBody.Part) {
        //check
        val map = LinkedHashMap<String, RequestBody>()
        map["action"] = createPartFromString("addInvoice")
        map["user_id"] = createPartFromString(""+InvoiceUtils.userId)
        map["bussiness_id"] = createPartFromString("" + selectedBusinessId)
        map["client_id"] = createPartFromString("" + selectedCustomerId)
        map["invoice_number"] =
            createPartFromString("" + binding.InvoiceIncreNumber.text.toString().trim())
        map["order_no"] = createPartFromString("" + binding.InvoiceOrderId.text.toString().trim())
        map["invoice_date"] = createPartFromString("" + selectedDate)
        map["amt_type"] = createPartFromString("" + selectedPaymentType)
        map["paid_amt"] =
            createPartFromString("" + binding.InvoicePaidAmount.text.toString().trim())
        map["due_date"] =
            createPartFromString("" + binding.InvoiceDueDateEdit.text.toString().trim())
        map["remark"] = createPartFromString("" + binding.InvoiceRemarkEdit.text.toString().trim())
        map["terms_condition"] =
            createPartFromString("" + binding.InvoiceTermsAndConditionEdit.text.toString())
        val total = String.format ("%.2f", finalTotalAmount)
        map["total_invoice_amt"] = createPartFromString("" + total)
        map["discount_amt"] = createPartFromString("" + DisountAmount)

        for ((pos, i) in DynamicitemList.withIndex()) {
            map["item[$pos][item_id]"] = createPartFromString("" + i.item_id)
            map["item[$pos][amount]"] = createPartFromString("" + i.amount)
            map["item[$pos][qty_type]"] = createPartFromString("" + i.qty_type)
            map["item[$pos][qty]"] = createPartFromString("" + i.qty)
            map["item[$pos][tax]"] = createPartFromString("" + i.tax)
            map["item[$pos][description]"] = createPartFromString("" + i.description)
            map["item[$pos][discount_type]"] = createPartFromString("" + i.discount_type)
            map["item[$pos][discount]"] = createPartFromString("" + i.discount)
        }

        println("InvoiceRequest - : $map")
        println("InvoiceRequest pdfPart - : $pdfPart")

        viewModel.addInvoiceList(map, pdfPart)
        }



    fun generateInvoiceHtml(
        invoiceBillingAddress: String,
        invoiceCustomerBillingAddress: String,
        finalTotalAmount: String,
        invoiceItems: List<InvoiceOfflineDynamicData>, // List of items
        binding: ActivityInvoiceCreateFormBinding // Replace with actual binding class
    ): String {
        val htmlBuilder = StringBuilder()
        htmlBuilder.append(
            """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
                body { font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; }
                .invoice-container { width: 100%; margin: auto; border-radius: 8px; padding: 0; background: none; }
                .info-address { font-size: 18px; margin-left: 15px; }
                .header { text-align: center; font-size: 45px; font-weight: bold; color: green; padding: 20px; margin-bottom: 15px; }
                .info-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                .info-table th, .info-table td { padding: 8px; text-align: left; }
                .info-table th { background-color: green; color: white; }
                .table-container { width: 100%; border-collapse: collapse; }
                .table-container th { background: green; color: white; padding: 8px; text-align: left; }
                .table-container td { border: 1px solid #ccc; padding: 8px; text-align: left; }
                .total { text-align: right; font-size: 20px; font-weight: bold; margin-top: 10px; padding: 20px; }
                .note-title { font-size: 22px; margin-left: 15px; font-weight: bold; }
                .note-text { font-size: 18px; margin-left: 15px; }
            </style>
        </head>
        <body>
            <div class="invoice-container">
                <div class="header">INVOICE</div>
                <table class="info-table">
                    <tr>
                        <th>From</th>
                        <th>To</th>
                    </tr>
                    <tr>
                        <td class="info-address">$invoiceBillingAddress</td>
                        <td class="info-address">$invoiceCustomerBillingAddress</td>
                    </tr>
                </table>
                <table class="info-table">
                    <tr>
                        <th>Invoice No</th>
                        <th>Invoice Date</th>
                        <th>Due Date</th>
                    </tr>
                    <tr>
                        <td class="info-address">${binding.InvoiceIncreNumber.text.toString()}</td>
                        <td class="info-address">${binding.InvoiceDate.text.toString()}</td>
                        <td class="info-address">${binding.InvoiceDueDateEdit.text.toString()}</td>
                    </tr>
                </table>
                <table class="table-container">
                    <tr>
                        <th>S.no / Item List</th>
                        <th>Qty</th>
                        <th>Amount</th>
                    </tr>
        """.trimIndent()
        )

        // **Loop through the list to generate table rows dynamically**
        invoiceItems.forEachIndexed { index, item ->
            htmlBuilder.append(
                """
            <tr>
                <td class="info-address">${index + 1}. ${item.item_name}<br><small>(${item.tax} GST included)</small></td>
                <td class="info-address">${item.qty}</td>
                <td class="info-address">₹ ${item.total_amt}</td>
            </tr>
            """.trimIndent()
            )
        }

        htmlBuilder.append(
            """
                </table>
                <div class="total">Total: ₹ $finalTotalAmount</div>
                <p><span class="note-title">Note:</span> <span class="note-text">${binding.InvoiceRemarkEdit.text.toString()}</span></p>
                <p><span class="note-title">Terms & Conditions:</span></p>
                <p class="note-text">${binding.InvoiceTermsAndConditionEdit.text.toString()}</p>
            </div>
        </body>
        </html>
        """.trimIndent()
        )

        return htmlBuilder.toString()
    }

    fun createPartFromString(value: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), value)
    }
    private fun createPdf(htmlContent: String, callback: (File?) -> Unit) {
        InvoiceUtils.loadingProgress(this@InvoiceCreateFormActivity, "loading...", false).show()

        try {
            val pdfLocation = File(getPdfFilePath(), "Calendar_Invoice.pdf")

            if (pdfLocation.exists()) {
                pdfLocation.delete()
                Thread.sleep(50) // Ensure deletion is completed
            }
            pdfLocation.createNewFile()

            htmlToPdfConvertor.convert(
                pdfLocation = pdfLocation,
                htmlString = htmlContent,
                onPdfGenerationFailed = { exception ->
                    InvoiceUtils.loadingDialog.dismiss()
                    Log.e("PDF Error", "PDF generation failed: ${exception.message}")
                    callback(null) // Return null on failure
                },
                onPdfGenerated = { pdfFile ->
                    InvoiceUtils.loadingDialog.dismiss()
                    if (pdfFile.exists() && pdfFile.length() > 0) {
                        Log.d("PDF", "PDF successfully created: ${pdfFile.path} | Size: ${pdfFile.length()} bytes")
                        callback(pdfFile) // Return generated PDF
                    } else {
                        Log.e("PDF Error", "PDF file was not created properly")
                        callback(null)
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PDF Error", "Exception while creating PDF: ${e.message}")
            callback(null)
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

    companion object {
        var _TAG = "InvoiceCreateFormActivity"
        const val REQUEST_CODE_SELECT_ITEM = 100

    }
}