package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAddExpenseFormBinding
import com.nithra.invoice_generator_tool.fragment.InvoiceDatePickerDialog
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class InvoiceAddExpenseFormActivity : AppCompatActivity(), InvoicemasterClick {
    lateinit var binding: ActivityInvoiceAddExpenseFormBinding
    var selectedDate = ""
    private val viewModel: InvoiceViewModel by viewModels()
    var invoiceClickId = 0
    var preference = InvioceSharedPreference()
    var selectedBusinessId = 0
    var selectedBusinessState = ""
    var clickDataId =0
    var listOfCompanyDetails: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
        mutableListOf()
    private val viewmodel: InvoiceViewModel by viewModels()
    private lateinit var stateDialog: Dialog


    private val selectItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val json = preference.getString(
                    this@InvoiceAddExpenseFormActivity,
                    "INVOICE_SELECTED_ITEMS"
                ) // Default empty list
                val type = object : TypeToken<InvoiceOfflineDynamicData>() {}.type
                val itemList: InvoiceOfflineDynamicData = Gson().fromJson(json, type)
                println("iTemLit === ${itemList.company_id}")
                selectedBusinessId = itemList.company_id!!
                selectedBusinessState = itemList.state!!
                clickDataId = itemList.company_id!!
                binding.InvoiceBusinessTypeText.text = itemList.bussiness_name
                listOfCompanyDetails.clear()
                loadMasterData()
            }
        }

    private fun loadMasterData() {
        if (InvoiceUtils.isNetworkAvailable(this@InvoiceAddExpenseFormActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = ""+preference.getString(this@InvoiceAddExpenseFormActivity,"INVOICE_USER_ID")

            println("InvoiceRequest - ${InvoiceBusinessDetailFormActivity._TAG} == $InputMap")
            viewmodel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceAddExpenseFormActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
        if (intent != null) {
            invoiceClickId = intent.getIntExtra("clickDataId", 0)
        }

        loadMasterData()

        binding.InvoiceExpenseLay.setOnClickListener {
            val datePicker = InvoiceDatePickerDialog({ selectedDatePickerDate ->
                showDatePickerFormat(selectedDatePickerDate)
            }, binding.InvoiceExpenseDate.text.toString().trim())
            datePicker.show(supportFragmentManager, "datePicker")
        }

        viewModel.getAddedExpenseList.observe(this@InvoiceAddExpenseFormActivity) { addedExpense ->
            InvoiceUtils.loadingDialog.dismiss()
            if (addedExpense.status.equals("success")) {
                Toast.makeText(
                    this@InvoiceAddExpenseFormActivity,
                    "" + addedExpense.msg,
                    Toast.LENGTH_SHORT
                ).show()
                val resultIntent = Intent()
                addedExpense.data!![0].status = addedExpense.status
                val addedData = Gson().toJson(addedExpense.data)
                resultIntent.putExtra("INVOICE_FORM_DATA", addedData)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
        viewmodel.errorMessage.observe(this@InvoiceAddExpenseFormActivity) {
            println("eror ==== $it")
            Toast.makeText(this@InvoiceAddExpenseFormActivity, "" + it, Toast.LENGTH_SHORT).show()
        }

        viewmodel.getMasterDetail.observe(this) { getMasterArray ->
            listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
            if ( listOfCompanyDetails[0].bussinessName!!.isNotEmpty()){
                binding.InvoiceBusinessTypeText.text = listOfCompanyDetails[0].bussinessName
                clickDataId = listOfCompanyDetails[0].companyId!!
            }
        }

        binding.InvoiceBusinessTypeSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceAddExpenseFormActivity)) {
                Toast.makeText(
                    this@InvoiceAddExpenseFormActivity,
                    "Check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (binding.InvoiceBusinessTypeText.text.isEmpty()){
                val intent = Intent(
                    this@InvoiceAddExpenseFormActivity,
                    InvoiceBusinessAndCustomerActivity::class.java
                )
                intent.putExtra("fromInvoice", 1)
                intent.putExtra("InvoicefromPage", "Business")
                selectItemLauncher.launch(intent)
            }else{
                showSearchableDialog<InvoiceGetDataMasterArray.GetCompanyDetailList>(
                    0,
                    listOfCompanyDetails
                )
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
                        InvoiceUtils.loadingProgress(
                            this@InvoiceAddExpenseFormActivity,
                            "" + InvoiceUtils.messageLoading,
                            false
                        ).show()
                        val map = HashMap<String, Any>()
                        map["action"] = "addExpenses"
                        map["user_id"] = ""+preference.getString(this@InvoiceAddExpenseFormActivity,"INVOICE_USER_ID")
                        map["date"] = "" + selectedDate
                        map["item_name"] =
                            "" + binding.InvoiceExpenseItemName.text.toString().trim()
                        map["amount"] = "" + binding.InvoiceItemRate.text.toString().trim()
                        map["inv_number"] =
                            "" + binding.InvoiceNumber.text.toString().trim()
                        map["seller_name"] =
                            "" + binding.InvoiceExpenseSellerName.text.toString().trim()
                        map["remark"] = "" + binding.InvoiceExpenseRemark.text.toString().trim()
                        map["company_id"] = ""+clickDataId

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

        AddItemCard.visibility = View.GONE
        val filteredList: MutableList<T> = mutableListOf()

        filteredList.clear()
        // Initialize adapter
        filteredList.addAll(listOfState) // Initially show all items

        val adapter = InvoiceMasterAdapter(
            this@InvoiceAddExpenseFormActivity,
            filteredList,
            "",
            this,
            2,
            fromSpinner, onAddItemClick = {
            },  onDeleteItem ={deleteId ,pos,actionName->

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
                if (s!!.isEmpty()){
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
                this@InvoiceAddExpenseFormActivity,
                filteredList,
                searchQuery.toString(),
                this,
                2,
                fromSpinner, onAddItemClick = {

                },
                onDeleteItem ={deleteId ,pos,actionName->

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

    companion object {
        var _TAG = "InvoiceAddExpenseFormActivity"
    }

    override fun onItemClick(item: String, clikId: Int, fromClick: Int, position: Int) {
        binding.InvoiceBusinessTypeText.text = "" + item
        clickDataId = clikId
        stateDialog.dismiss()
    }
}