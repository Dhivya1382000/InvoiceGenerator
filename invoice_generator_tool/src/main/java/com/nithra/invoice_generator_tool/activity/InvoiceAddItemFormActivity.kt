package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAddItemFormBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceAddItemFormActivity : AppCompatActivity() {
    lateinit var itemFormBinding: ActivityInvoiceAddItemFormBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var dataList: ArrayList<String> = arrayListOf()
    var dataListOfGst: ArrayList<String> = arrayListOf()
    var listOfGST: MutableList<InvoiceGetDataMasterArray.GstList> = mutableListOf()
    var listOfitemList: MutableList<InvoiceGetDataMasterArray.GetItemList> = mutableListOf()
    var listOfMeasures: MutableList<InvoiceGetDataMasterArray.GetUnitMeasure> = mutableListOf()
    private lateinit var stateDialog: Dialog
    var selectedGstId = 0
    var selectedGstIdData = ""
    var selectedMeasuresId = 0
    var selectedDiscount = 0
    var finalAmount = ""
    var invoiceClickId = 0
    var fromInvoicePage = ""
    var preference = InvioceSharedPreference()
    var clickPosition = 0

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
        if (intent != null) {
            invoiceClickId = intent.getIntExtra("clickDataId", 0)
            fromInvoicePage = "" + intent.getStringExtra("fromInvoicePage")
            clickPosition =intent.getIntExtra("clickPosition",0)
        }

        if (InvoiceUtils.isNetworkAvailable(this@InvoiceAddItemFormActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "1227994"

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceAddItemFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }


        viewModel.errorMessage.observe(this@InvoiceAddItemFormActivity) {
            Toast.makeText(this@InvoiceAddItemFormActivity, "" + it, Toast.LENGTH_SHORT).show()
        }

        itemFormBinding.InvoiceItemQuantity.setText("1")


        viewModel.getItemDetails.observe(this) { getItemList ->
            if (getItemList.status.equals("success")) {
                Toast.makeText(
                    this@InvoiceAddItemFormActivity,
                    "" + getItemList.msg,
                    Toast.LENGTH_SHORT
                ).show()
                println("invoice == $invoiceClickId")
                println("invoice fromInvoicePage == $fromInvoicePage")
                if (invoiceClickId != 0) {
                    if (fromInvoicePage == "InvoiceBusinessAndCustomerActivity_Item") {
                        val resultIntent = Intent()
                        val addedData = Gson().toJson(getItemList.data)
                        resultIntent.putExtra("INVOICE_FORM_DATA_UPDATE", addedData)
                        resultIntent.putExtra("INVOICE_FORM_CLICK_POS", clickPosition)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        val resultIntent = Intent()
                        val selectedItemJson = Gson().toJson(getItemList.data)
                        preference.putString(
                            this@InvoiceAddItemFormActivity,
                            "INVOICE_EDIT_ITEMS",
                            selectedItemJson
                        )
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                } else {
                    val resultIntent = Intent()
                    val addedData = Gson().toJson(getItemList.data)
                    resultIntent.putExtra("INVOICE_FORM_DATA", addedData)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }


            }
        }

        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            if (getMasterArray.status.equals("success")) {
                listOfGST.addAll(getMasterArray.gst!!)
                listOfitemList.addAll(getMasterArray.itemList!!)
                listOfMeasures.addAll(getMasterArray.unitMeasurement!!)

                if (listOfMeasures.isNotEmpty()) {
                    for (i in listOfMeasures) {
                        dataList.add(i.label!!)
                    }
                }
                if (listOfGST.isNotEmpty()) {
                    for (i in listOfGST) {
                        dataListOfGst.add(i.gst!! + "  " + "%")
                    }
                }

                val discountOptions = listOf("Percentage", "Flat Amount")
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    discountOptions
                )
                itemFormBinding.itemDiscountSpinner.adapter = adapter


                val adapter1 = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    dataListOfGst
                )
                itemFormBinding.InvoiceItemTaxSpinner.adapter = adapter1

                val adapter2 =
                    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dataList)
                itemFormBinding.itemQtySpinner.adapter = adapter2


                if (invoiceClickId != 0) {
                    println("invoiceClickId == $invoiceClickId")
                    for (i in listOfitemList.indices) {
                        println("invoiceClickId Company == ${listOfitemList[i].itemId}")
                        if (invoiceClickId == listOfitemList[i].itemId) {

                            itemFormBinding.InvoiceItemName.setText(listOfitemList[i].itemName)
                            itemFormBinding.InvoiceItemDesc.setText(listOfitemList[i].description)
                            itemFormBinding.InvoiceItemQuantity.setText(listOfitemList[i].qty)

                            selectedMeasuresId = listOfitemList[i].qtyType!!
                            selectedGstId = listOfitemList[i].tax!!.toInt()
                            selectedDiscount = listOfitemList[i].discountType!!.toInt()
                            println("qtyType == ${listOfitemList[i].qtyType!!}")

                            val MeasuresId =
                                listOfMeasures.indexOfFirst { it.id == selectedMeasuresId }
                            itemFormBinding.itemQtySpinner.setSelection(MeasuresId)

                            itemFormBinding.InvoiceItemRate.setText(listOfitemList[i].amount!!)

                            val GstId = listOfGST.indexOfFirst { it.id == selectedGstId }
                            itemFormBinding.InvoiceItemTaxSpinner.setSelection(GstId)

                            itemFormBinding.InvoiceItemDiscount.setText("" + listOfitemList[i].discount)

                            if (selectedDiscount == 1) {
                                itemFormBinding.itemDiscountSpinner.setSelection(0)
                            } else {
                                itemFormBinding.itemDiscountSpinner.setSelection(1)
                            }
                            finalAmount = listOfitemList[i].totalAmt.toString()
                            itemFormBinding.InvoiceTotalItemAmount.setText("Total Amount : " + listOfitemList[i].totalAmt)
                            itemFormBinding.InvoiceItemSaveCardText.text = "Update"
                        }
                    }
                } else {

                }

            }
            itemFormBinding.itemDiscountSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedOption = parent.getItemAtPosition(position)
                        if (selectedOption == "Percentage") {
                            selectedDiscount = 1
                        } else {
                            selectedDiscount = 2
                        }
                        calculateFinal()
                        /*  Toast.makeText(
                              applicationContext,
                              "Selected: $selectedOption",
                              Toast.LENGTH_SHORT
                          ).show()*/
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            itemFormBinding.InvoiceItemTaxSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedGst = listOfGST[position]  // Get corresponding object
                        selectedGstId = selectedGst.id!!
                        selectedGstIdData = selectedGst.gst!!
                        calculateFinal()
                        //Toast.makeText(applicationContext, "Selected ID: ${selectedGst.id}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }


            // Handle item selection
            itemFormBinding.itemQtySpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedMeasure =
                            listOfMeasures[position]  // Get corresponding object
                        selectedMeasuresId = selectedMeasure.id!!
                        calculateFinal()
                        //Toast.makeText(applicationContext, "Selected ID: ${selectedGst.id}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            itemFormBinding.InvoiceItemRate.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // Called after text is changed
                    Log.d("TextWatcher", "After: ${s.toString()}")
                    calculateFinal()
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

            itemFormBinding.InvoiceItemQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    Log.d("TextWatcher", "After: ${p0.toString()}")
                    calculateFinal()
                }

            })

            itemFormBinding.InvoiceItemDiscount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    Log.d("TextWatcher", "After: ${p0.toString()}")
                    calculateFinal()
                }

            })

            if (!listOfitemList[0].status.equals("failure")) {
                // Sample list of suggestions
                val SuggestionsItemName = listOfitemList.map {
                    "${it.itemName}"
                }
                println("itBusiness=Name == ${SuggestionsItemName}")

                // Create an ArrayAdapter
                val adapterBusinessName = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    SuggestionsItemName
                )
                // Set the adapter to the AutoCompleteTextView
                itemFormBinding.InvoiceItemName.setAdapter(adapterBusinessName)
                itemFormBinding.InvoiceItemName.threshold =
                    1 // Start showing suggestions after 1 character
            }
        }

        itemFormBinding.InvoiceItemSaveCard.setOnClickListener {

            when {
                itemFormBinding.InvoiceItemName.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceAddItemFormActivity,
                        "Enter your item Name",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                itemFormBinding.InvoiceItemQuantity.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceAddItemFormActivity,
                        "Enter your item quantity",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                itemFormBinding.InvoiceItemRate.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@InvoiceAddItemFormActivity,
                        "Enter your item rate",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                /*   itemFormBinding.InvoiceItemTax.text.toString().trim().isEmpty() -> {
                       Toast.makeText(
                           this@InvoiceAddItemFormActivity,
                           "Enter your billing address1",
                           Toast.LENGTH_SHORT
                       ).show()
                       return@setOnClickListener
                   }*/

                else -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceAddItemFormActivity)) {
                        val map = HashMap<String, Any>()
                        map["action"] = "addItem"
                        map["user_id"] = "1227994"
                        if (invoiceClickId != 0) {
                            map["id"] = invoiceClickId
                        }
                        map["item_name"] =
                            "" + itemFormBinding.InvoiceItemName.text.toString().trim()
                        map["amount"] = "" + itemFormBinding.InvoiceItemRate.text.toString().trim()
                        map["qty_type"] = "" + selectedMeasuresId
                        map["qty"] = "" + itemFormBinding.InvoiceItemQuantity.text.toString().trim()
                        map["tax"] = "" + selectedGstId
                        map["description"] =
                            "" + itemFormBinding.InvoiceItemDesc.text.toString().trim()
                        map["discount_type"] = "" + selectedDiscount
                        map["discount"] =
                            "" + itemFormBinding.InvoiceItemDiscount.text.toString().trim()
                        map["total_amt"] = "" + finalAmount

                        println("InvoiceRequest - $_TAG == $map")

                        viewModel.addItemData(map)

                    } else {
                        Toast.makeText(
                            this@InvoiceAddItemFormActivity,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun calculateFinal() {
        val quantity = itemFormBinding.InvoiceItemQuantity.text.toString().trim()
        val pricePerKg = itemFormBinding.InvoiceItemRate.text.toString().trim()  // 25 Rupees per kg
        val gstPercent = selectedGstIdData  // 3% GST
        val discountPercent =
            itemFormBinding.InvoiceItemDiscount.text.toString().trim()  // 45% Discount
        println("gstPercent == $gstPercent")
        finalAmount = calculateFinalPrice(quantity, pricePerKg, gstPercent, discountPercent)

        itemFormBinding.InvoiceTotalItemAmount.setText("Total Amount : ₹ " + finalAmount)
    }


    fun calculateFinalPrice(
        quantityStr: String,
        priceStr: String,
        gstStr: String?,
        discountStr: String?
    ): String {
        // Convert strings to numbers safely (default to 0.0 if conversion fails or is null)
        val quantity = quantityStr.toDoubleOrNull() ?: 0.0
        val pricePerKg = priceStr.toDoubleOrNull() ?: 0.0
        val gstPercent = gstStr?.toDoubleOrNull() ?: 0.0  // Handle missing GST
        val discountPercent = discountStr?.toDoubleOrNull() ?: 0.0  // Handle missing Discount

        /*  // Perform calculations
          val basePrice = quantity * pricePerKg  // Total price before tax and discount
          val gstAmount = (basePrice * gstPercent) / 100  // GST calculation (0 if missing)
          val discountAmount = (basePrice * discountPercent) / 100  // Discount calculation (0 if missing)
          val finalPrice = (basePrice + gstAmount) - discountAmount  // Final price after GST and discount

          // Return result as a formatted string
          return "₹${"%.2f".format(finalPrice)}"*/
        // Perform calculations
        val basePrice = quantity * pricePerKg  // Total price before tax and discount
        val gstAmount = (basePrice * gstPercent) / 100  // GST calculation (0 if missing)

        // Calculate discount based on type
        val discountAmount = if (selectedDiscount == 1) {
            (basePrice * discountPercent) / 100  // Percentage discount
        } else {
            discountPercent  // Flat discount
        }

        val finalPrice =
            (basePrice + gstAmount) - discountAmount  // Final price after GST and discount

        // Ensure final price is never negative
        val safeFinalPrice = maxOf(finalPrice, 0.0)

        // Return result as a formatted string
        return "${"%.2f".format(safeFinalPrice)}"
    }

    companion object {
        var _TAG = "InvoiceAddItemFormActivity"
    }


}