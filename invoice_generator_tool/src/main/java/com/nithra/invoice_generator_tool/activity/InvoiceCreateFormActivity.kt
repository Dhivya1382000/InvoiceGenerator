package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
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
                if (s.toString().isNotEmpty()){
                    DisountAmount = binding.ItemsDiscountAmount.text.toString().replace("₹", "").trim().toDouble() ?: 0.0
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
        for ((pos, i) in DynamicitemList.withIndex()) {
            map["item[$pos][item_id]"] = "" + i.item_id
            map["item[$pos][amount]"] = "" + i.amount
            map["item[$pos][qty_type]"] = "" + i.qty_type
            map["item[$pos][qty]"] = "" + i.qty
            map["item[$pos][tax]"] = "" + i.tax
            map["item[$pos][description]"] = "" + i.description
            map["item[$pos][discount_type]"] = "" + i.discount_type
            map["item[$pos][discount]"] = "" + i.discount
        }

        println("InvoiceRequest - ${_TAG} == $map")
        viewModel.addInvoiceList(map)

    }

    companion object {
        var _TAG = "InvoiceCreateFormActivity"
        const val REQUEST_CODE_SELECT_ITEM = 100

    }
}