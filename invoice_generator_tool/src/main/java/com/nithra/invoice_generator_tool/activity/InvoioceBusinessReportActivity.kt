package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.LinearLayout
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
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseDataList
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData
import com.nithra.invoice_generator_tool.model.InvoicePieChart
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
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
                binding.InvoiceBusinessTypeText.text = itemList.bussiness_name
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
        if (InvoiceUtils.isNetworkAvailable(this@InvoioceBusinessReportActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "picChartReport"
            InputMap["user_id"] = "1227994"
            InputMap["company_id"] = ""+clickDataId

            println("InvoiceRequest - $_TAG == $InputMap")
            InvoiceUtils.loadingProgress(
                this@InvoioceBusinessReportActivity,
                InvoiceUtils.messageLoading, false
            ).show()
            viewmodel.getPieChart(InputMap)
        } else {
            Toast.makeText(
                this@InvoioceBusinessReportActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (InvoiceUtils.isNetworkAvailable(this@InvoioceBusinessReportActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "1227994"

            println("InvoiceRequest - ${InvoiceBusinessDetailFormActivity._TAG} == $InputMap")
            viewmodel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoioceBusinessReportActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewmodel.errorMessage.observe(this@InvoioceBusinessReportActivity) {
            Toast.makeText(this@InvoioceBusinessReportActivity, "" + it, Toast.LENGTH_SHORT).show()
        }

        viewmodel.getMasterDetail.observe(this) { getMasterArray ->
            listOfCompanyDetails.addAll(getMasterArray.companyDetails!!)
            binding.InvoiceBusinessTypeText.text = listOfCompanyDetails[0].bussinessName
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
            if (listOfCompanyDetails[0].status.equals("failure")){
                val intent = Intent(
                    this@InvoioceBusinessReportActivity,
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


        viewmodel.getPiechartList.observe(this@InvoioceBusinessReportActivity) { pieChartList ->
            InvoiceUtils.loadingDialog.dismiss()
            if (pieChartList.status.equals("success")) {
                listOfGetInvoicePieChart.addAll(pieChartList.data!!)
                listOfGetInvoiceExpense = pieChartList.expenses!!
                println("chart amount == " + pieChartList.expenses!!.data!!.totalAmount)
                binding.ExpensesAmount.text = " ₹ "  + pieChartList.expenses!!.data!!.totalAmount
                binding.TotalPaidAmount.text = " ₹ "  + pieChartList.total_amount
                val totalPaid = listOfGetInvoicePieChart.filter { it.amtType == 1 }
                    .sumOf { it.totalPaid!! } //paid
                val totalUnpaid = listOfGetInvoicePieChart.filter { it.amtType == 2 }
                    .sumOf { it.totalPaid!! } //un paid
                binding.PaidAmount.text = " ₹ " + totalPaid
                binding.UnPaidAmount.text = " ₹ " + totalUnpaid
                val composeView = findViewById<ComposeView>(R.id.pieChartComposeView)
                composeView.setContent {
                    //  PieChartScreen() // Load the Compose Pie Chart inside XML view
                    PieChart(listOfGetInvoicePieChart, pieChartList.total_amount!!)
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
            },  onDeleteItem ={deleteId ,pos,actionName->

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
                    is InvoiceGetDataMasterArray.GetCompanyDetailList -> item.bussinessName!!.contains(
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

        }
        val adapter = InvoiceMasterAdapter(
            this@InvoioceBusinessReportActivity,
            filteredList,
            searchQuery.toString(),
            this,
            2,
            fromSpinner, onAddItemClick = {

            },
            onDeleteItem ={deleteId ,pos,actionName->

            }
        ) // Pass the query
        recyclerView.adapter = adapter
    }

    @Composable
    fun PieChart(data: MutableList<InvoicePieChart.InvoiceDataList>, expensesTotal: Int) {
        val filteredData = data.filter { it.amtType != null }

        if (filteredData.isEmpty() && expensesTotal <= 0) {
            Log.e("PieChart", "No data to show!")
            return
        }

        val totalCount = filteredData.sumOf { it.totalPaid ?: 0 } + expensesTotal
        if (totalCount == 0) {
            Log.e("PieChart", "Total count is zero!")
            return
        }

        // Prepare Data for Pie Chart
        val dataWithPercentage = mutableListOf<Double>()

        filteredData.forEach {
            dataWithPercentage.add((it.totalPaid!!.toDouble() / totalCount) * 100)
        }

        if (expensesTotal > 0) {
            dataWithPercentage.add((expensesTotal.toDouble() / totalCount) * 100)
        }

        val sweepAngles = dataWithPercentage.map { percentage -> (360 * (percentage / 100)).toFloat() }
        val colors = listOf(colorResource(R.color.invoice_green_mild), colorResource(R.color.invoice_red_mild), colorResource(R.color.invoice_blue),colorResource(R.color.invoice_pink))

        Canvas(modifier = Modifier.size(250.dp)) {
            var startAngle = 0f
            val radius = size.minDimension / 2

            dataWithPercentage.forEachIndexed { index, percentage ->
                val colornew = colors[index % colors.size]
                val sweepAngle = sweepAngles[index]

                // Draw Pie Slice
                drawArc(
                    color = colornew,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                val typefacenew = Typeface.create("sans-serif", Typeface.BOLD)

                // Calculate Text Position
                val middleAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val textX = radius + (radius / 1.7f) * cos(middleAngle).toFloat()
                val textY = radius + (radius / 1.7f) * sin(middleAngle).toFloat()

                // Draw Only Percentage Text
                drawContext.canvas.nativeCanvas.drawText(
                    "${percentage.toInt()}%",
                    textX,
                    textY,
                    Paint().apply {
                        color = Color.White.toArgb()
                        textSize = 25f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                        typeface = typefacenew  // Set the custom font
                    }
                )

                startAngle += sweepAngle
            }
        }
    }




    companion object {
        var _TAG = "InvoioceBusinessReportActivity"
    }

    override fun onItemClick(item: String, clikId: Int, fromClick: Int, position: Int) {
        binding.InvoiceBusinessTypeText.text = "" + item
        clickDataId = clikId
        stateDialog.dismiss()
    }
}
