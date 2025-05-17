package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceAddedItemDataAdapter
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceCreateFormBinding
import com.nithra.invoice_generator_tool.databinding.InvoiceBottomSheetBinding
import com.nithra.invoice_generator_tool.fragment.InvoiceDatePickerDialog
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData
import com.nithra.invoice_generator_tool.pdf_generator.InvoiceHtmlToPdfConvertor
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class InvoiceCreateFormActivity : AppCompatActivity(), InvoicemasterClick {
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
    var listOfInvoiceList: MutableList<InvoiceGetInvoiceList> = mutableListOf()

    var preference = InvioceSharedPreference()
    var newInvoiceNumber = 1
    private lateinit var adapter: InvoiceAddedItemDataAdapter
    private var DynamicitemList = mutableListOf<InvoiceOfflineDynamicData>()
    private lateinit var stateDialog: Dialog
    var positionOfEDit = 0
    var clickEditId = 0
    var selectedPaymentType = 1
    var submitData = ""
    var selectedBusinessId = 0
    var selectedCustomerId = 0
    var selectedBusinessState = ""
    var invoiceBusinesname = ""
    var invoiceBusinesEmail = ""
    var invoiceBusinesGst = ""
    var invoiceBusinesMobile = ""
    var selectedCustomerState = ""
    var selectedDate = ""
    var TotalAmount = 0.0
    var finalTotalAmount = 0.0
    var DisountAmount = 0.0
    var invoiceBillingAddress = ""
    var invoiceCustomerBillingAddress = ""
    var invoiceCustomerShippingAddress = ""
    var invoiceBusinessGSTId = ""
    var invoiceBusinessMobileNumber = ""
    var invoiceBusinessEmail = ""
    var invoiceBusinessState = ""
    var invoiceBankName = ""
    var invoiceBankAcc = ""
    var invoiceBankIFSC = ""
    var invoiceBankMICR = ""
    var invoiceBankAddress = ""
    var millis: Long = 0
    var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    lateinit var htmlToPdfConvertor: InvoiceHtmlToPdfConvertor
    var filePart: File? = null
    var selectedInvoiceId = 0
    var InvoiceEditId = 0

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
                    for (i in DynamicitemList.indices) {
                        println("itemId Dy==== ${DynamicitemList[i].item_id}")
                        if (DynamicitemList[i].item_id == itemList.item_id) {
                            Toast.makeText(
                                this@InvoiceCreateFormActivity,
                                "This item is already in the list",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@registerForActivityResult
                        }
                    }
                    if (selectedBusinessState == selectedCustomerState) {
                        println("itemListTax == ${itemList.tax}")
                        for (i in listOfGstList.indices) {
                            if (itemList.tax.toString() == listOfGstList[i].id.toString()) {
                                val totalGST = listOfGstList[i].gst
                                val result = splitGST(totalGST!!)
                                if (result != null) {
                                    val (sgstValu, cgstValu) = result
                                    val dynamicList = InvoiceOfflineDynamicData()
                                    dynamicList.user_id = itemList.user_id
                                    dynamicList.mobile = itemList.mobile
                                    dynamicList.item_id = itemList.item_id
                                    dynamicList.item_name = itemList.item_name
                                    dynamicList.amount = itemList.amount
                                    dynamicList.qty_type = itemList.qty_type
                                    dynamicList.qty = itemList.qty
                                    dynamicList.tax = itemList.tax
                                    dynamicList.hsn = itemList.hsn
                                    dynamicList.description = itemList.description
                                    dynamicList.discount_type = itemList.discount_type
                                    dynamicList.discount = itemList.discount
                                    dynamicList.total_amt = itemList.total_amt
                                    dynamicList.sgst = sgstValu
                                    dynamicList.cgst = cgstValu
                                    dynamicList.Igst = ""
                                    DynamicitemList.add(dynamicList)
                                }
                            }

                        }
                    } else {
                        for (i in listOfGstList.indices) {
                            if (itemList.tax.toString() == listOfGstList[i].id.toString()) {
                                val totalGST = listOfGstList[i].gst
                                val dynamicList = InvoiceOfflineDynamicData()
                                dynamicList.user_id = itemList.user_id
                                dynamicList.mobile = itemList.mobile
                                dynamicList.item_id = itemList.item_id
                                dynamicList.item_name = itemList.item_name
                                dynamicList.amount = itemList.amount
                                dynamicList.qty_type = itemList.qty_type
                                dynamicList.qty = itemList.qty
                                dynamicList.tax = itemList.tax
                                dynamicList.description = itemList.description
                                dynamicList.discount_type = itemList.discount_type
                                dynamicList.discount = itemList.discount
                                dynamicList.hsn = itemList.hsn
                                dynamicList.total_amt = itemList.total_amt
                                dynamicList.Igst = totalGST
                                dynamicList.sgst = 0.0
                                dynamicList.cgst = 0.0
                                DynamicitemList.add(dynamicList)
                            }

                        }
                    }

                    //DynamicitemList.add(itemList)

                    TotalAmount += itemList.total_amt!!.toDouble()
                    finalTotalAmount = TotalAmount - DisountAmount
                    val total = String.format("%.2f", finalTotalAmount)
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
                    invoiceBusinesname = itemList.bussiness_name!!
                    invoiceBusinesGst = itemList.tax_id!!
                    invoiceBusinesMobile = itemList.mobile1!!
                    invoiceBusinesEmail = itemList.email!!
                    invoiceBusinessMobileNumber = itemList.bussiness_mobile!!
                    invoiceBusinessState = itemList.state + "(" + itemList.s_code + ")"
                    invoiceBankName = itemList.bank_name!!
                    invoiceBankAcc = itemList.bank_acoount_number!!
                    invoiceBankIFSC = itemList.ifsc_code!!
                    invoiceBankMICR = itemList.micr_code!!
                    invoiceBankAddress = itemList.bank_address!!
                    binding.InvoiceBusinessTypeText.text = itemList.bussiness_name
                } else {
                    println("iTemLit === ${itemList.invoice_id}")
                    println("iTemLit state=== ${itemList.state}")
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


                if (DynamicitemList[positionOfEDit].item_id == itemList[0].item_id) {
                    DynamicitemList[positionOfEDit] = itemList[0] // Replace existing
                }

                /* if (foundIndex == -1) {
                     DynamicitemList.add(itemList[0])
                 }*/

                /*  if (selectedBusinessState == selectedCustomerState) {
                      println("itemListTax == ${itemList[0].tax}")
                      for (i in listOfGstList.indices) {
                          if (itemList[0].tax.toString() == listOfGstList[i].id.toString()) {
                              val totalGST = listOfGstList[i].gst
                              val result = splitGST(totalGST!!)
                              if (result != null) {
                                  val (sgstValu, cgstValu) = result
                                  val dynamicList = InvoiceOfflineDynamicData()
                                  dynamicList.user_id = itemList[0].user_id
                                  dynamicList.mobile = itemList[0].mobile
                                  dynamicList.item_id = itemList[0].item_id
                                  dynamicList.item_name = itemList[0].item_name
                                  dynamicList.amount = itemList[0].amount
                                  dynamicList.qty_type = itemList[0].qty_type
                                  dynamicList.qty = itemList[0].qty
                                  dynamicList.tax = itemList[0].tax
                                  dynamicList.description = itemList[0].description
                                  dynamicList.discount_type = itemList[0].discount_type
                                  dynamicList.discount = itemList[0].discount
                                  dynamicList.hsn = itemList[0].hsn
                                  dynamicList.total_amt = itemList[0].total_amt
                                  dynamicList.sgst = sgstValu
                                  dynamicList.cgst = cgstValu
                                  dynamicList.Igst = ""
                                  DynamicitemList[foundIndex] = dynamicList
                              }
                          }

                      }
                  }
                  else {
                      for (i in listOfGstList.indices) {
                          if (itemList[0].tax.toString() == listOfGstList[i].id.toString()) {
                              val totalGST = listOfGstList[i].gst
                              val dynamicList = InvoiceOfflineDynamicData()
                              dynamicList.user_id = itemList[0].user_id
                              dynamicList.mobile = itemList[0].mobile
                              dynamicList.item_id = itemList[0].item_id
                              dynamicList.item_name = itemList[0].item_name
                              dynamicList.amount = itemList[0].amount
                              dynamicList.qty_type = itemList[0].qty_type
                              dynamicList.qty = itemList[0].qty
                              dynamicList.tax = itemList[0].tax
                              dynamicList.description = itemList[0].description
                              dynamicList.discount_type = itemList[0].discount_type
                              dynamicList.discount = itemList[0].discount
                              dynamicList.total_amt = itemList[0].total_amt
                              dynamicList.hsn = itemList[0].hsn
                              dynamicList.sgst = 0.0
                              dynamicList.cgst = 0.0
                              dynamicList.Igst = totalGST
                              DynamicitemList.add(dynamicList)
                          }

                      }
                  }*/


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

        if (intent != null) {
            InvoiceEditId = intent.getIntExtra("INVOICE_EDIT_ID", 0)
            println("InvoiceEdit == $InvoiceEditId")
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
            InputMap["user_id"] =
                "" + preference.getString(this@InvoiceCreateFormActivity, "INVOICE_USER_ID")

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
            Toast.makeText(this@InvoiceCreateFormActivity, "Error" + it, Toast.LENGTH_SHORT).show()
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

                if (InvoiceEditId != 0) {
                    val InvoicejsonString = preference.getString(
                        this@InvoiceCreateFormActivity,
                        "INVOICE_PDF_LIST_DATA"
                    )
                    val listType = object : TypeToken<InvoiceGetInvoiceList>() {}.type
                    val invoiceList: InvoiceGetInvoiceList =
                        Gson().fromJson(InvoicejsonString, listType)

                    selectedCustomerId = invoiceList.clientId!!
                    selectedBusinessId = invoiceList.bussinessId!!
                    selectedPaymentType = invoiceList.amtType!!
                    binding.InvoicePaidAmount.setText("" + invoiceList.paidAmt)
                    binding.InvoiceRemarkEdit.setText("" + invoiceList.remark)

                    if (selectedPaymentType == 1) {
                        binding.InvoicePaid.isChecked = true
                        binding.InvoiceUnPaid.isChecked = false
                        binding.InvoicePaidAmountLay.visibility = View.VISIBLE
                        binding.InvoiceDueDateLay.visibility = View.GONE
                        binding.InvoiceRemarkLay.visibility = View.VISIBLE
                        binding.InvoiceDueDateEdit.text = ""
                    } else {
                        binding.InvoicePaid.isChecked = false
                        binding.InvoiceUnPaid.isChecked = true
                        binding.InvoicePaidAmountLay.visibility = View.GONE
                        binding.InvoiceDueDateLay.visibility = View.GONE
                        binding.InvoiceRemarkLay.visibility = View.VISIBLE
                        binding.InvoiceDueDateEdit.text = ""
                    }

                    val client = listOfClientDetails.find { it.clientId == selectedCustomerId }
                    selectedCustomerState = client!!.state!!

                    binding.InvoiceBusinessTypeText.text = invoiceList.bussinessName
                    binding.InvoiceCustomerName.text = invoiceList.clientName
                    binding.InvoiceIncreNumberLay.isClickable = false
                    binding.InvoiceIncreNumber.isClickable = false
                    binding.InvoiceIncreNumber.setText(invoiceList.invoiceNumber)
                    val userFormatText = formatForUser(invoiceList.invoiceDate!!)
                    binding.InvoiceDate.setText(userFormatText)


                    binding.DynamicItemCard.visibility = View.VISIBLE

                    binding.ItemsFinalAmount.text = "₹ " + invoiceList.totalInvoiceAmt
                    binding.ItemsTotalAmount.text = " ₹ " + invoiceList.totalInvoiceAmt
                    binding.ItemsDiscountAmount.setText(" ₹ " + invoiceList.discountAmt)

                    invoiceList.item?.forEach { itemList ->
                        val dynamicList = InvoiceOfflineDynamicData()
                        dynamicList.user_id = itemList.userId
                        dynamicList.mobile = itemList.mobile
                        dynamicList.item_id = itemList.itemId
                        dynamicList.item_name = itemList.itemName
                        dynamicList.amount = itemList.amount
                        dynamicList.qty_type = itemList.qtyType
                        dynamicList.qty = itemList.qty
                        dynamicList.tax = itemList.tax
                        dynamicList.description = itemList.description
                        dynamicList.discount_type = itemList.discountType
                        dynamicList.discount = itemList.discount
                        dynamicList.hsn = itemList.hsn
                        dynamicList.total_amt = itemList.totalAmt
                        dynamicList.Igst = itemList.tax
                        dynamicList.sgst = 0.0
                        dynamicList.cgst = 0.0
                        DynamicitemList.add(dynamicList)
                    }


                    val billingBusiness =
                        listOfCompanyDetails.find { it.bussinessId == selectedBusinessId.toString() }
                    invoiceBillingAddress = billingBusiness!!.billingAddress1!!
                    selectedBusinessState = billingBusiness.state!!
                    invoiceBusinessGSTId = billingBusiness.taxId!!
                    invoiceBusinessMobileNumber = billingBusiness.bussinessMobile!!
                    invoiceBusinessEmail = billingBusiness.email!!
                    invoiceBusinessState =
                        billingBusiness.state + "(" + billingBusiness.stateId + ")"
                    invoiceBankName = billingBusiness.bankName!!
                    invoiceBankAcc = billingBusiness.bankAcoountNumber!!
                    invoiceBankIFSC = billingBusiness.ifscCode!!
                    invoiceBankMICR = billingBusiness.micrCode!!
                    invoiceBankAddress = billingBusiness.bankAddress!!

                    adapter.notifyDataSetChanged()

                } else {
                    binding.InvoiceIncreNumberLay.isClickable = true
                    binding.InvoiceIncreNumber.isClickable = true
                    selectedCustomerId = listOfClientDetails[0].clientId!!
                    selectedBusinessId = listOfCompanyDetails[0].companyId!!
                    selectedCustomerState = listOfClientDetails[0].state!!
                    binding.InvoiceBusinessTypeText.text = listOfCompanyDetails[0].bussinessName
                    invoiceBillingAddress = listOfCompanyDetails[0].billingAddress1!!
                    selectedBusinessState = listOfCompanyDetails[0].state!!
                    invoiceBusinessGSTId = listOfCompanyDetails[0].taxId!!
                    invoiceBusinessMobileNumber = listOfCompanyDetails[0].bussinessMobile!!
                    invoiceBusinessEmail = listOfCompanyDetails[0].email!!
                    invoiceBusinessState =
                        listOfCompanyDetails[0].state + "(" + listOfCompanyDetails[0].stateId + ")"
                    invoiceBankName = listOfCompanyDetails[0].bankName!!
                    invoiceBankAcc = listOfCompanyDetails[0].bankAcoountNumber!!
                    invoiceBankIFSC = listOfCompanyDetails[0].ifscCode!!
                    invoiceBankMICR = listOfCompanyDetails[0].micrCode!!
                    invoiceBankAddress = listOfCompanyDetails[0].bankAddress!!


                }
            }
            val hasStatusKey = listOfItemDetails.any { !it.status.isNullOrEmpty() } ?: false
            println("map === " + hasStatusKey) // Output: true or false

            if (DynamicitemList.isEmpty()) {
                binding.DynamicItemCard.visibility = View.GONE
            } else {
                binding.DynamicItemCard.visibility = View.VISIBLE
            }
        }

        if (InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getInvoiceList"
            InputMap["user_id"] =
                "" + preference.getString(this@InvoiceCreateFormActivity, "INVOICE_USER_ID")
            InputMap["type"] = "0"
            println("InvoiceRequest - ${InvoiceHomeScreen._TAG} == $InputMap")
            viewModel.getInvoiceList(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceCreateFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.getInvoiceList.observe(this@InvoiceCreateFormActivity) { getInvoice ->
            if (InvoiceEditId == 0) {
                if (getInvoice[0].status != "failure") {
                    val lastUnselected = getInvoice.lastOrNull { it.autoEntry == 0 }
                    if (lastUnselected != null) {
                        val lastId = lastUnselected.invoiceNumber
                        val parts = lastId!!.split("-")
                        if (parts.size > 1) {
                            newInvoiceNumber = parts[1].toInt()
                            println("Number part: ${parts[1]}")  // Output: 1
                        }
                        listOfInvoiceList.addAll(getInvoice)
                        newInvoiceNumber += 1
                        // Check for duplicate invoice number
                        preference.putInt(
                            this@InvoiceCreateFormActivity,
                            "GEN_INVOICENUMBER",
                            newInvoiceNumber
                        )
                        binding.InvoiceIncreNumber.setText("INV$currentYear -" + newInvoiceNumber)
                    }
                } else {
                    newInvoiceNumber = 1
                    preference.putInt(
                        this@InvoiceCreateFormActivity,
                        "GEN_INVOICENUMBER",
                        newInvoiceNumber
                    )
                    binding.InvoiceIncreNumber.setText("INV$currentYear -" + newInvoiceNumber)
                }
            } else {

            }

        }



        binding.InvoiceBusinessTypeSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "Check Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                submitData = "business"

                if (binding.InvoiceBusinessTypeText.text.isEmpty()) {
                    val intent = Intent(
                        this@InvoiceCreateFormActivity,
                        InvoiceBusinessAndCustomerActivity::class.java
                    )
                    intent.putExtra("fromInvoice", 1)
                    intent.putExtra("InvoicefromPage", "Business")
                    selectItemLauncher.launch(intent)
                } else {
                    showSearchableDialog<InvoiceGetDataMasterArray.GetCompanyDetailList>(
                        0,
                        listOfCompanyDetails
                    )
                }

            }
        }

        binding.InvoiceCustomerDetails.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "Check Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                submitData = "customer"
                /*  if (binding.InvoiceCustomerName.text.isEmpty()) {
                  val intent = Intent(
                      this@InvoiceCreateFormActivity,
                      InvoiceBusinessAndCustomerActivity::class.java
                  )
                  intent.putExtra("fromInvoice", 1)
                  intent.putExtra("InvoicefromPage", "Customers")
                  selectItemLauncher.launch(intent)
              } else {

              }*/
                showSearchableDialog<InvoiceGetDataMasterArray.GetClientDetails>(
                    0,
                    listOfClientDetails
                )
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
                invoiceCustomerShippingAddress = invoiceCustomerShippingAddress,
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
                        val intent = Intent(
                            this@InvoiceCreateFormActivity,
                            InvoiceFilePdfViewActivity::class.java
                        ).apply {
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
            if (InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)) {

                val htmlContent = generateInvoiceHtml(
                    invoiceBillingAddress = invoiceBillingAddress,
                    invoiceCustomerBillingAddress = invoiceCustomerBillingAddress,
                    invoiceCustomerShippingAddress = invoiceCustomerShippingAddress,
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

                    DynamicitemList.isEmpty() -> {
                        Toast.makeText(
                            this@InvoiceCreateFormActivity,
                            "Add invoice item",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    selectedPaymentType == 1 -> {
                        if (binding.InvoicePaidAmount.text.toString().isEmpty()) {
                            Toast.makeText(
                                this@InvoiceCreateFormActivity,
                                "Enter paid amount",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }

                    selectedPaymentType == 2 -> {
                        if (binding.InvoicePaidAmount.text.toString().trim().isEmpty()) {
                            Toast.makeText(
                                this@InvoiceCreateFormActivity,
                                "Enter paid amount",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        } else if (binding.InvoiceDueDateEdit.text.toString().trim()
                                .isEmpty()
                        ) {
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

                            val requestBody =
                                pdfFile.asRequestBody("application/pdf".toMediaTypeOrNull())

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

                binding.InvoiceIncreNumber.setText("" + getAddeddat.data?.invoiceDetails!![0].invoiceNumber!!)
                if (pdfFileUrl!!.isNotEmpty()) {
                    val intent =
                        Intent(
                            this@InvoiceCreateFormActivity,
                            InvoicePdfViewActivity::class.java
                        )
                    intent.putExtra("InvoicePdfLink", pdfFileUrl)
                    intent.putExtra("InvoicePdfName", bussinessName)
                    intent.putExtra("INVOICE_EDIT_ID", 0)
                    startActivity(intent)
                    finish()
                }
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "addInvoice" + getAddeddat.msg,
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
            InvoiceAddedItemDataAdapter(
                DynamicitemList,
                onItemClick = { listOfDynamic, position ->
                    DynamicitemList.removeAt(position)
                    adapter.notifyDataSetChanged()
                    println("dyanmicList == ${DynamicitemList.size}")
                    if (DynamicitemList.size != 0) {
                        TotalAmount -= listOfDynamic.total_amt!!.toDouble()
                        //  finalTotalAmount = TotalAmount + DisountAmount
                        val finalTotalAmountNew = (TotalAmount + DisountAmount).toBigDecimal()
                            .setScale(2, RoundingMode.HALF_UP)
                        finalTotalAmount = finalTotalAmountNew.toDouble()
                        val total = String.format("%.2f", finalTotalAmount)
                        val totalAmt = String.format("%.2f", TotalAmount)
                        binding.ItemsFinalAmount.text = "₹ " + total
                        binding.ItemsTotalAmount.text = " ₹ " + totalAmt
                        binding.ItemsDiscountAmount.setText(" ₹ " + DisountAmount)
                        binding.DynamicItemCard.visibility = View.VISIBLE
                    } else {
                        TotalAmount = 0.0
                        DisountAmount = 0.0
                        binding.ItemsDiscountAmount.setText("")
                        val total = String.format("%.2f", finalTotalAmount)
                        binding.ItemsTotalAmount.text = " ₹ " + total
                        binding.DynamicItemCard.visibility = View.GONE
                    }
                },
                onShowItem = { binding, clickItemPOs ->

                    println("selectedBus == $selectedBusinessState")
                    println("selectedCus == $selectedCustomerState")

                    if (selectedBusinessState == selectedCustomerState) {
                        for (i in listOfGstList.indices) {
                            if (clickItemPOs.tax.toString() == listOfGstList[i].id.toString()) {
                                val totalGST = listOfGstList[i].gst
                                val result = splitGST(totalGST!!)
                                if (result != null) {
                                    val (sgstValu, cgstValu) = result

                                    val coloredText =
                                        SpannableString("SGST : $sgstValu % , CGST : $cgstValu %")

                                    coloredText.setSpan(
                                        ForegroundColorSpan(Color.RED), // You can use any color
                                        0,
                                        6, // "SGST :" length
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    val cgstStart = coloredText.indexOf("CGST")
                                    val cgstEnd = cgstStart + 6 // length of "CGST :"
                                    coloredText.setSpan(
                                        ForegroundColorSpan(Color.RED),
                                        cgstStart,
                                        cgstEnd,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    binding.itemCGstSplit.text = coloredText
                                }
                            }
                        }
                    } else {
                        for (i in listOfGstList.indices) {
                            if (clickItemPOs.tax.toString() == listOfGstList[i].id.toString()) {
                                val coloredText =
                                    SpannableString("IGST : ${listOfGstList[i].gst}%")
                                clickItemPOs.Igst = listOfGstList[i].gst
                                coloredText.setSpan(
                                    ForegroundColorSpan(Color.RED), // You can use any color
                                    0,
                                    6, // "SGST :" length
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                                binding.itemCGstSplit.text = coloredText
                            }
                        }

                    }

                },
                OnEditClick = { clickId, pos, listOfDynamic ->
                    val intent = Intent(
                        this@InvoiceCreateFormActivity,
                        InvoiceAddItemFormActivity::class.java
                    )
                    intent.putExtra("OffLineEdit", 1)
                    intent.putExtra("clickDataId", clickId.item_id)
                    positionOfEDit = pos
                    clickEditId = clickId.item_id!!
                    println("dynamicList --Sze ${DynamicitemList.size}")
                    val addedData = Gson().toJson(DynamicitemList)
                    println("DynamicList == $addedData")
                    preference.putString(
                        this@InvoiceCreateFormActivity,
                        "INVOICE_SET_LIST",
                        addedData
                    )
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
                    // binding.InvoiceTermsAndCondition.visibility = View.VISIBLE
                }

                R.id.InvoiceUnPaid -> {
                    selectedPaymentType = 3
                    binding.InvoicePaidAmountLay.visibility = View.GONE
                    binding.InvoiceDueDateLay.visibility = View.GONE
                    binding.InvoiceRemarkLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateEdit.text = ""
                    //   binding.InvoiceTermsAndCondition.visibility = View.VISIBLE
                }

                R.id.InvoicePartiallyPaid -> {
                    selectedPaymentType = 2
                    binding.InvoicePaidAmountLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateLay.visibility = View.VISIBLE
                    binding.InvoiceDueDateEdit.text = currentDate
                    binding.InvoiceRemarkLay.visibility = View.VISIBLE
                    // binding.InvoiceTermsAndCondition.visibility = View.VISIBLE
                }
            }

        }

        /*     binding.InvoiceBusinessTypeText.setOnClickListener {
             showBottomSheet()
         }
 */
        /*  binding.InvoiceCustomerName.setOnClickListener {
          if (!binding.InvoiceCustomerName.text.toString().trim().equals("")) {
              showBottomSheet1()
          }
      }
*/

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
        binding.InvoiceIncreNumberLay.setOnClickListener {
            showInvoiceDialog()
        }
        binding.InvoiceIncreNumber.setOnClickListener {
            showInvoiceDialog()
        }

        binding.dynamicContainer.isNestedScrollingEnabled = true

        binding.dynamicContainer.layoutManager = LinearLayoutManager(this)
        binding.dynamicContainer.adapter = adapter

    }


    private fun showInvoiceDialog() {
        val dialogView = Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
        dialogView.setContentView(R.layout.activity_show_invoice_incre_dialog)
        dialogView.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val etPrefix = dialogView.findViewById<EditText>(R.id.etPrefix)
        val etNextNumber = dialogView.findViewById<EditText>(R.id.etNextNumber)
        val btnCancel = dialogView.findViewById<AppCompatTextView>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<AppCompatTextView>(R.id.btnSave)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)
        val radioAuto = dialogView.findViewById<RadioButton>(R.id.radioAuto)

        selectedInvoiceId = 0

        etPrefix.setText("INV$currentYear -")
        etNextNumber.setText(
            "" + preference.getInt(
                this@InvoiceCreateFormActivity,
                "GEN_INVOICENUMBER"
            )
        )
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioAuto -> {
                    selectedInvoiceId = 0
                }

                R.id.radioManualEach -> {
                    selectedInvoiceId = 1
                }

            }
        }


        btnCancel.setOnClickListener {
            dialogView.dismiss()
        }

        btnSave.setOnClickListener {
            println("slesTedInvoice == $selectedInvoiceId")
            if (selectedInvoiceId == 0) {
                binding.InvoiceIncreNumber.isFocusable = false
                binding.InvoiceIncreNumber.isCursorVisible = false
                binding.InvoiceIncreNumber.isFocusableInTouchMode = false
                binding.InvoiceIncreNumber.isClickable = true
                if (etNextNumber.text.toString().trim().isEmpty()) {
                    Toast.makeText(
                        this@InvoiceCreateFormActivity,
                        "Enter the invoice number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                newInvoiceNumber = etNextNumber.text.toString().toInt()
                val duplicateInvoice =
                    listOfInvoiceList.any { it.invoiceNumber!!.contains("INV$currentYear -$newInvoiceNumber") }

                if (duplicateInvoice) {
                    Toast.makeText(
                        this@InvoiceCreateFormActivity,
                        "This invoice number is already created",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                preference.putInt(
                    this@InvoiceCreateFormActivity,
                    "GEN_INVOICENUMBER",
                    newInvoiceNumber
                )
                binding.InvoiceIncreNumber.setText("INV$currentYear -" + newInvoiceNumber)
            } else {
                binding.InvoiceIncreNumber.isFocusable = true
                binding.InvoiceIncreNumber.isFocusableInTouchMode = true
                binding.InvoiceIncreNumber.isCursorVisible = true
                binding.InvoiceIncreNumber.isClickable = true
                binding.InvoiceIncreNumber.setText("")
            }

            dialogView.dismiss()
        }

        dialogView.show()
    }


    /* private fun <T> showSearchableDialog(
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

         AddItemCard.visibility = View.GONE
         val filteredList: MutableList<T> = mutableListOf()

         filteredList.clear()
         // Initialize adapter
         filteredList.addAll(listOfState) // Initially show all items

         val adapter = InvoiceMasterAdapter(
             this@InvoiceCreateFormActivity,
             filteredList,
             "",
             this,
             2,
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

             override fun afterTextChanged(s: Editable?) {
                 if (s!!.isEmpty()) {
                     recyclerView.visibility = View.VISIBLE
                     NoDataLay.visibility = View.GONE
                 }
             }
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
                     is InvoiceGetDataMasterArray.GetCompanyDetailList -> item.bussinessName!!.contains(
                         searchQuery,
                         ignoreCase = true
                     )

                     else -> {
                         false
                     }
                 }
             })

             val adapter = InvoiceMasterAdapter(
                 this@InvoiceCreateFormActivity,
                 filteredList,
                 searchQuery.toString(),
                 this,
                 2,
                 fromSpinner, onAddItemClick = {

                 },
                 onDeleteItem = { deleteId, pos, actionName ->

                 },
                 onSearchResult = {

                 }
             ) // Pass the query
             recyclerView.adapter = adapter

             if (filteredList.isEmpty()) {
                 // Display a message or take action if no results were found
                 println("filterSize 11  == ${filteredList.size}")
                 NoDataLay.visibility = View.VISIBLE
                 recyclerView.visibility = View.GONE
             } else {
                 println("filterSize 12  == ${filteredList.size}")
                 NoDataLay.visibility = View.GONE
                 recyclerView.visibility = View.VISIBLE
             }
             adapter.notifyDataSetChanged()

         }

     }*/

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
            this@InvoiceCreateFormActivity,
            filteredList,
            "",
            this,
            2,
            fromSpinner, onAddItemClick = {
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

            override fun afterTextChanged(s: Editable?) {
                if (s!!.isEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    NoDataLay.visibility = View.GONE
                }
            }
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
                    is InvoiceGetDataMasterArray.GetCompanyDetailList -> item.bussinessName!!.contains(
                        searchQuery,
                        ignoreCase = true
                    )

                    is InvoiceGetDataMasterArray.GetClientDetails -> item.name!!.contains(
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
                recyclerView.visibility = View.GONE
            } else {
                println("filterSize 12  == ${filteredList.size}")
                NoDataLay.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
        val adapter = InvoiceMasterAdapter(
            this@InvoiceCreateFormActivity,
            filteredList,
            searchQuery.toString(),
            this,
            2,
            fromSpinner, onAddItemClick = {

            }, onDeleteItem = { deleteId, pos, actionName ->

            },
            onSearchResult = {

            }
        ) // Pass the query
        recyclerView.adapter = adapter
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

    fun formatForUser(input: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(input)
        return outputFormat.format(date!!)
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
        map["user_id"] = createPartFromString(
            "" + preference.getString(
                this@InvoiceCreateFormActivity,
                "INVOICE_USER_ID"
            )
        )
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
        val total = String.format("%.2f", finalTotalAmount)
        map["total_invoice_amt"] = createPartFromString("" + total)
        map["discount_amt"] = createPartFromString("" + DisountAmount)
        map["auto_entry"] = createPartFromString("" + selectedInvoiceId)
        if (InvoiceEditId != 0) {
            map["id"] = createPartFromString("" + InvoiceEditId)
        }
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
        invoiceCustomerShippingAddress: String,
        finalTotalAmount: String,
        invoiceItems: List<InvoiceOfflineDynamicData>, // List of items
        binding: ActivityInvoiceCreateFormBinding // Replace with actual binding class
    ): String {

        val htmlBuilder = StringBuilder()
        val cutomerName = binding.InvoiceCustomerName.text.toString()
        htmlBuilder.append(
            """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <style>
                body {
                    font-family: Arial, sans-serif;
                    font-size: 14px;
                    margin: 2px;
                }
                .invoice-box {
                    max-width: 100%;
                    padding: 20px;
                    border: 1px solid #eee;
                }
                .title {
               font-size: 35px;
                    font-weight: bold;
                }
                .section {
                    margin-bottom: 20px;
                     font-size: 18px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    font-size: 13px;
                }
                table td, table th {
                    border: 1px solid #ddd;
                    padding: 6px;
                }
                .no-border td {
                    border: none;
                }
                .right {
                    text-align: right;
                }
                .bold {
                    font-weight: bold;
                }
            </style>
        </head>
        <body>
        <div class="invoice-box">
            <div class="section">
                <table class="no-border">
                    <tr>
                        <td style="text-align: center;">
                            <div class="title">TAX INVOICE</div>
                        </td>
                    </tr>
                </table>
            </div>

            <div class="section">
                <b>${binding.InvoiceBusinessTypeText.text.toString()}</b><br/>
                $invoiceBillingAddress<br/>
                GSTIN: $invoiceBusinessGSTId<br/>
                Mobile: $invoiceBusinessMobileNumber<br/>
                Email: $invoiceBusinessEmail
            </div>

            <div class="section">
                <table>
                    <tr>
                            <td><b style="font-size:25px;">Invoice</b><br/><span style="font-size:20px;">${binding.InvoiceIncreNumber.text.toString()}</span></td>
                            <td><b style="font-size:25px;">Invoice Date</b><br/><span style="font-size:20px;">${binding.InvoiceDate.text.toString()}</span></td>
                            <td><b style="font-size:25px;">Place Of Supply</b><br/><span style="font-size:20px;">$invoiceBusinessState</span></td>

                    </tr>
                </table>
            </div>

         <div class="section">
    <table>
        <tr>
            <td>
                <b style="font-size:25px;">Bill To</b><br/>
                <span style="font-size:22px;">
                    ${cutomerName}<br/>
                    $invoiceCustomerBillingAddress<br/>
                </span>
            </td>
            <td>
                <b style="font-size:25px;">Ship To</b><br/>
                <span style="font-size:22px;">
                    ${cutomerName}<br/>
                    $invoiceCustomerShippingAddress<br/>
                </span>
            </td>
        </tr>
    </table>
</div>


            <div class="section">
                <table>
                    <tr>
                        <th style="font-size:25px;">S.No</th>
                        <th style="font-size:25px;">Item</th>
                        <th style="font-size:25px;">Qty</th>
                        <th style="font-size:25px;">Rate</th>
                        <th style="font-size:25px;">Discount</th>
                        <th style="font-size:25px;">CGST</th>
                        <th style="font-size:25px;">SGST</th>
                        <th style="font-size:25px;">IGST</th>
                        <th style="font-size:25px;">Total</th>
                    </tr>
        """.trimIndent()
        )
        invoiceItems.forEachIndexed { index, item ->
            htmlBuilder.append(
                """
                <tr>
                    <td   style="font-size:22px;">${index + 1}</td>
                    <td style="font-size:22px;">${item.item_name}<br/><span style="font-size:15px;">${if (item.hsn!!.isEmpty()) "" else "(HSN: ${item.hsn})"}</span></td>    
                    <td style="font-size:22px;">${item.qty}</td>
                    <td style="font-size:22px;">${item.amount}</td>
                     <td style="font-size:22px;">${item.discount}</td>
                    <td style="font-size:22px;">${if (selectedBusinessState == selectedCustomerState) item.sgst else "-"}</td>
                    <td style="font-size:22px;">${if (selectedBusinessState == selectedCustomerState) item.cgst else "-"}</td>
                    <td style="font-size:22px;">${if (selectedBusinessState == selectedCustomerState) "-" else item.Igst}</td>
                    <td style="font-size:22px;">${item.total_amt}</td>
                </tr>
            """.trimIndent()
            )
        }

        htmlBuilder.append(
            """
                
                <tr>
                    <td colspan="8" style="text-align: right; font-weight: bold; font-size: 22px; white-space: nowrap;">
                        Total Discount
                    </td>
                    <td style="font-size: 22px;">
                         ${
                if (binding.ItemsDiscountAmount.text.toString()
                        .isEmpty()
                ) "-" else " ₹ " + binding.ItemsDiscountAmount.text
            }
                    </td>
                </tr>

       <tr>
    <td colspan="8" style="text-align: right; font-weight: bold; font-size: 22px; white-space: nowrap;">
        Total Amount
    </td>
    <td style="font-size: 22px;">
         ₹ $finalTotalAmount
    </td>
</tr>

            </table>
        </div>
<div class="section">
    <b style="font-size:25px;">Notes:</b><br/>
    <span style="font-size:22px;">Thanks for your business.</span>
</div>

        <div class="section">
               <b style="font-size:25px;">Bank Details:</b><br/>
    <span style="font-size:22px;">
            Name: ${
                if (binding.InvoiceBusinessTypeText.text.toString()
                        .isEmpty()
                ) "-" else binding.InvoiceBusinessTypeText.text.toString()
            }<br/>
            Bank: ${if (invoiceBankName.isEmpty()) "-" else invoiceBankName}<br/>
            Account No: ${if (invoiceBankAcc.isEmpty()) "-" else invoiceBankAcc}<br/>
            IFSC Code: ${if (invoiceBankIFSC.isEmpty()) "-" else invoiceBankIFSC}<br/>
            Bank Address: ${if (invoiceBankAddress.isEmpty()) "-" else invoiceBankAddress}
            </span>
        </div>

    </div>
    </body>
    </html>
    """.trimIndent()
        )
        return htmlBuilder.toString()
    }


    fun createPartFromString(value: String): RequestBody {
        println("values invoice req --- $value")
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
                        Log.d(
                            "PDF",
                            "PDF successfully created: ${pdfFile.path} | Size: ${pdfFile.length()} bytes"
                        )
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

    override fun onItemClick(item: String, clikStateId: Int, fromClick: Int, position: Int) {
        println("submit == $submitData")
        println("submit item == $item")
        if (submitData == "customer") {
            binding.InvoiceCustomerName.text = "" + item
            invoiceCustomerBillingAddress = listOfClientDetails[position].billingAddress!!
            invoiceCustomerShippingAddress = listOfClientDetails[position].shippingAddress!!
            selectedCustomerState = listOfClientDetails[position].state!!
            selectedCustomerId = clikStateId
        } else {
            binding.InvoiceBusinessTypeText.text = "" + item
            invoiceBillingAddress = listOfCompanyDetails[position].billingAddress1!!
            selectedBusinessState = listOfCompanyDetails[position].state!!
            invoiceBusinessGSTId = listOfCompanyDetails[position].taxId!!
            invoiceBusinessMobileNumber = listOfCompanyDetails[position].bussinessMobile!!
            invoiceBusinessEmail = listOfCompanyDetails[position].email!!
            invoiceBusinessState =
                listOfCompanyDetails[position].state + "(" + listOfCompanyDetails[position].stateId + ")"
            invoiceBankName = listOfCompanyDetails[position].bankName!!
            invoiceBankAcc = listOfCompanyDetails[position].bankAcoountNumber!!
            invoiceBankIFSC = listOfCompanyDetails[position].ifscCode!!
            invoiceBankMICR = listOfCompanyDetails[position].micrCode!!
            invoiceBankAddress = listOfCompanyDetails[position].bankAddress!!
            selectedBusinessId = clikStateId
        }

        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
        stateDialog.dismiss()

    }
}