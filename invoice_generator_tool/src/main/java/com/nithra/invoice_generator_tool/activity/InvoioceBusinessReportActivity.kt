package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoioceBusinessReportBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData
import com.nithra.invoice_generator_tool.model.InvoicePieChart
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class InvoioceBusinessReportActivity : AppCompatActivity(), InvoicemasterClick {
    lateinit var binding: ActivityInvoioceBusinessReportBinding
    private val viewmodel: InvoiceViewModel by viewModels()
    lateinit var listOfGetInvoicePieChart: MutableList<InvoicePieChart.InvoiceDataList>
    lateinit var listOfGetInvoiceExpense: InvoicePieChart.ListOfExpenses
    var listOfCompanyDetails: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
        mutableListOf()
    private lateinit var stateDialog: Dialog
    var clickDataId = 0
    var preference = InvioceSharedPreference()
    var selectedBusinessId = 0
    var selectedBusinessState = ""

    private val selectItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val json = preference.getString(
                    this@InvoioceBusinessReportActivity,
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoioceBusinessReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBarTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image


        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        listOfGetInvoicePieChart = arrayListOf()

        loadMasterData()

        // Automatically load "This Month" data on screen load
        handleDateFilter("This Month")

        viewmodel.errorMessage.observe(this@InvoioceBusinessReportActivity) {
            println("eror ==== $it")
            Toast.makeText(this@InvoioceBusinessReportActivity, "" + it, Toast.LENGTH_SHORT).show()
        }

        viewmodel.getMasterDetail.observe(this) { getMasterArray ->
            listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
            if (listOfCompanyDetails[0].bussinessName!!.isNotEmpty()) {
                binding.InvoiceBusinessTypeText.text = listOfCompanyDetails[0].bussinessName
                clickDataId = listOfCompanyDetails[0].companyId!!
                if (clickDataId != 0) {
                    listOfGetInvoicePieChart.clear()
                    loadPiechart()
                }
            }
        }

        binding.InvoiceBusinessTypeSpinner.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoioceBusinessReportActivity)) {
                Toast.makeText(
                    this@InvoioceBusinessReportActivity,
                    "Check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (binding.InvoiceBusinessTypeText.text.isEmpty()) {
                val intent = Intent(
                    this@InvoioceBusinessReportActivity,
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


        viewmodel.getPiechartList.observe(this@InvoioceBusinessReportActivity) { pieChartList ->
            InvoiceUtils.loadingDialog.dismiss()
            println("pieChartList st== ${pieChartList.status}")
            if (pieChartList.status.equals("success")) {
                listOfGetInvoicePieChart.clear()
                listOfGetInvoicePieChart.addAll(pieChartList.data!!)
                listOfGetInvoiceExpense = pieChartList.expenses!!
                if (!pieChartList.expenses!!.status.equals("failure")) {
                    binding.ExpensesAmount.text = " ₹ " + pieChartList.expenses!!.data!!.totalAmount
                } else {
                    binding.ExpensesAmount.text = "NIL"
                }
                binding.TotalPaidAmount.text = " ₹ " + pieChartList.total_amount
                val totalPaidnew = listOfGetInvoicePieChart.filter { it.amtType == 1 }
                    .sumOf { it.totalPaid!! } //paid
                val totalUnpaid = listOfGetInvoicePieChart.filter { it.amtType == 2 }
                    .sumOf { it.totalPaid!! } //un paid
                binding.PaidAmount.text = " ₹ " + totalPaidnew
                binding.UnPaidAmount.text = " ₹ " + totalUnpaid
                val composeView = findViewById<ComposeView>(R.id.pieChartComposeView)
                var expenseAmount = 0
                if (pieChartList.status.equals("success")) {
                    val currentChartData = pieChartList.data?.toMutableList() ?: mutableListOf()
                    composeView.visibility = View.VISIBLE
                    composeView.setContent {
                        if (!pieChartList.expenses!!.status.equals("failure")) {
                            expenseAmount = pieChartList.expenses!!.data!!.totalAmount!!
                           /* pieChartList.data!![0].amtType = 4
                            pieChartList.data!![0].totalPaid = listOfGetInvoiceExpense.data!!.totalAmoun*/
                            currentChartData.add(
                                InvoicePieChart.InvoiceDataList().apply {
                                    count =  pieChartList.expenses!!.data!!.totalCount!!
                                    amtType = 4  // New type for expenses
                                    totalPaid =  pieChartList.expenses!!.data!!.totalAmount!! // Expense amount
                                }
                            )

                        }
                        println(" Expens ==== $expenseAmount")
                        PieChart(
                            currentChartData,expenseAmount,
                            pieChartList.status!!,
                        )
                    }
                } else {
                    composeView.visibility = View.GONE
                }

            }else{
                println("pieChartList == ${pieChartList.status}")
                binding.PaidAmount.text = "NIL"
                binding.UnPaidAmount.text = "NIL"
                binding.TotalPaidAmount.text = "NIL"
                binding.ExpensesAmount.text = "NIL"
                findViewById<ComposeView>(R.id.pieChartComposeView).visibility = View.GONE
            }
        }

    }

    private fun showFilterDialog() {
        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter_options, null)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupOptions)

        // Handle radio button click directly
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = dialogView.findViewById<RadioButton>(checkedId)
            val selectedOption = selectedRadioButton.text.toString()
            handleDateFilter(selectedOption)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun handleDateFilter(option: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val startDate: String
        val endDate: String

        when (option) {
            "Yesterday" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                startDate = dateFormat.format(calendar.time)
                endDate = startDate
                println("yesterdaystartDate == $startDate")
                println("yesterdayendDate == $endDate")
            }
            "Today" -> {
                startDate = dateFormat.format(calendar.time)
                endDate = startDate
                println("todaystartDate == $startDate")
                println("todayendDate == $endDate")
            }
            "Last Week" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                startDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 7)
                endDate = dateFormat.format(calendar.time)

                println("lastWeekstartDate == $startDate")
                println("lastweekenddate====$endDate")

            }
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                startDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                endDate = dateFormat.format(calendar.time)
            }
            "Last 7 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                startDate = dateFormat.format(calendar.time)
                endDate = dateFormat.format(calendar.time)
            }
            "Last Month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                endDate = dateFormat.format(calendar.time)
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                endDate = dateFormat.format(calendar.time)
            }
            "Custom" -> {
                showCustomDatePicker()
                return
            }
            else -> {
                // Default to today's date
                startDate = dateFormat.format(calendar.time)
                endDate = startDate
            }
        }

        // Call your API with the selected date range
        loadPiechart(startDate, endDate)

    }

    private fun showCustomDatePicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Start Date Picker
        val startDatePicker = DatePickerDialog(this, { _, startYear, startMonth, startDay ->
            val startCalendar = Calendar.getInstance()
            startCalendar.set(startYear, startMonth, startDay)
            val startDate = dateFormat.format(startCalendar.time)

            // End Date Picker
            val endDatePicker = DatePickerDialog(this, { _, endYear, endMonth, endDay ->
                val endCalendar = Calendar.getInstance()
                endCalendar.set(endYear, endMonth, endDay)
                val endDate = dateFormat.format(endCalendar.time)

                // Call your API with the selected date range
                loadPiechart(startDate, endDate)

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            endDatePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        startDatePicker.show()
    }

    private fun loadMasterData() {
        if (InvoiceUtils.isNetworkAvailable(this@InvoioceBusinessReportActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "" + preference.getString(this@InvoioceBusinessReportActivity,"INVOICE_USER_ID")

            println("InvoiceRequest - ${InvoiceBusinessDetailFormActivity._TAG} == $InputMap")
            viewmodel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoioceBusinessReportActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadPiechart(startDate: String = "", endDate: String = "") {
        if (InvoiceUtils.isNetworkAvailable(this@InvoioceBusinessReportActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "picChartReport"
            InputMap["user_id"] = preference.getString(this, "INVOICE_USER_ID") ?: ""
            InputMap["company_id"] = "$clickDataId"
            InputMap["startDate"] = startDate
            InputMap["endDate"] = endDate

            println("InvoiceRequesttt - $_TAG == $InputMap")
            InvoiceUtils.loadingProgress(this, InvoiceUtils.messageLoading, false).show()
            viewmodel.getPieChart(InputMap)
        } else {
            Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
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
        if (fromSpinner == 0) {
            AddItemCard.visibility = View.GONE
        }

        val filteredList: MutableList<T> = mutableListOf()

        filteredList.clear()
        // Initialize adapter
        filteredList.addAll(listOfState) // Initially show all items

        val adapter = InvoiceMasterAdapter(
            this@InvoioceBusinessReportActivity,
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
            this@InvoioceBusinessReportActivity,
            filteredList,
            searchQuery.toString(),
            this,
            2,
            fromSpinner, onAddItemClick = {

            }, onDeleteItem = { deleteId, pos,actionName ->

            },
            onSearchResult = {

            }
        ) // Pass the query
        recyclerView.adapter = adapter
    }

    @Composable
    fun PieChart(
        data: List<InvoicePieChart.InvoiceDataList>,
        expensesTotal: Int,
        status: String
    ) {
        if (status != "success") {
            Log.e("PieChart", "Status is failure. Hiding chart.")
            return
        }

        // Define fixed colors
        val fixedColors = mapOf(
            1 to Color(0xFF00CB1C), // Green //paid
            2 to Color(0xFFFD3A3A), //  //unpaid
            3 to Color( 0xFF008999), //  // partially
            4 to Color(0xFF2196F3)  // Blue (for expenses)
        )

        // Prepare valid chart entries
        val chartEntries = mutableListOf<Pair<Int, Int>>() // (amt_type, totalPaid)
        data.filter { it.totalPaid != null && it.totalPaid!! > 0 }.forEach {
            chartEntries.add(it.amtType!! to it.totalPaid!!)
        }

        if (expensesTotal > 0) {
            chartEntries.add(4 to expensesTotal) // Treat expenses as type 4
        }

        if (chartEntries.isEmpty()) {
            Log.e("PieChart", "No valid data to draw pie.")
            return
        }

        // Calculate total count from valid entries
        val totalCount = chartEntries.sumOf { it.second }

        // Compute angles for valid data only
        val dataWithAngles = chartEntries.map { (type, amount) ->
            type to (360f * (amount.toFloat() / totalCount))
        }

        // Draw Pie Chart
        Canvas(modifier = Modifier.size(250.dp)) {
            var startAngle = 0f
            val radius = size.minDimension / 2

            dataWithAngles.forEach { (type, sweepAngle) ->
                val color1 = fixedColors[type] ?: Color.Gray // Fallback color

                drawArc(
                    color = color1,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                // Calculate Text Position
                val middleAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val textX = radius + (radius / 1.7f) * cos(middleAngle).toFloat()
                val textY = radius + (radius / 1.7f) * sin(middleAngle).toFloat()

                // Draw percentage text
                drawContext.canvas.nativeCanvas.drawText(
                    "${(sweepAngle / 3.6).toInt()}%",
                    textX,
                    textY,
                    Paint().apply {
                        color = Color.White.toArgb()
                        textSize = 25f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                        typeface = Typeface.create("sans-serif", Typeface.BOLD)
                    }
                )

                startAngle += sweepAngle
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.report_filter_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    companion object {
        var _TAG = "InvoioceBusinessReportActivity"
    }

    override fun onItemClick(item: String, clikId: Int, fromClick: Int, position: Int) {
        binding.InvoiceBusinessTypeText.text = "" + item
        clickDataId = clikId
        stateDialog.dismiss()
        if (clickDataId != 0) {
            listOfGetInvoicePieChart.clear()
            loadPiechart()
        }
    }
}
