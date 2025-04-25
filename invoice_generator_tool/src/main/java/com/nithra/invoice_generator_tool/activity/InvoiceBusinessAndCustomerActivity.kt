package com.nithra.invoice_generator_tool.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceRecyclerviewBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseDataList
import com.nithra.invoice_generator_tool.retrofit_interface.InvoicemasterClick
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceBusinessAndCustomerActivity : AppCompatActivity(), InvoicemasterClick {

    lateinit var binding: ActivityInvoiceRecyclerviewBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfCompany: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> = mutableListOf()
    var listOfClients: MutableList<InvoiceGetDataMasterArray.GetClientDetails> = mutableListOf()
    var listOfItems: MutableList<InvoiceGetDataMasterArray.GetItemList> = mutableListOf()
    var listOfExpenses: MutableList<InvoiceGetExpenseDataList.DataList> = mutableListOf()
    var preference = InvioceSharedPreference()
    var fromInvoice = 0
    var businessClickId = 0
    var fromPage: String = ""
    lateinit var adapter: InvoiceMasterAdapter<*>
    var ListPosition = 0
    var actionName = ""
    private var isAnimationRunning = true // Track animation state
    private val handler = Handler(Looper.getMainLooper())
    var hintTexts: ArrayList<String> = arrayListOf()
    private var currentIndex = 0
    var clicktabPos = 1

    val addItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra("INVOICE_FORM_DATA")
                println("Data == $data")
                data?.let {
                    when (fromPage) {
                        "Business" -> {
                            if (listOfCompany[0].status.equals("failure")) {
                                listOfCompany.clear()
                            }
                            val type = object :
                                TypeToken<List<InvoiceGetDataMasterArray.GetCompanyDetailList>>() {}.type
                            val itemList: List<InvoiceGetDataMasterArray.GetCompanyDetailList> =
                                Gson().fromJson(it, type)
                            println("typeclic--- ${itemList[0].type}")
                            listOfCompany.addAll(0, itemList.filter { it.type == itemList[0].type })
                            if (itemList[0].type == 0) {
                                binding.BusinessTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_blue
                                    )
                                )
                                binding.tabSelectTextBusiness.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )

                                binding.IndividualTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )
                                binding.tabSelectTextIndividual.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_black
                                    )
                                )
                            } else {
                                binding.BusinessTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )
                                binding.tabSelectTextBusiness.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_black
                                    )
                                )

                                binding.IndividualTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_blue
                                    )
                                )
                                binding.tabSelectTextIndividual.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )
                            }

                            println("Status== ${listOfCompany[0].status}")
                            if (itemList[0].status.equals("success")) {
                                binding.NoDataLay.visibility = View.GONE
                                binding.searchLay.visibility = View.GONE
                                binding.recyclerCustomers.visibility = View.VISIBLE
                            } else {
                                binding.NoDataLay.visibility = View.VISIBLE
                                binding.searchLay.visibility = View.VISIBLE
                                binding.recyclerCustomers.visibility = View.GONE
                            }
                            if (::adapter.isInitialized) {
                                //    adapter.notifyItemInserted(0)
                                val listOfCompanyFilter: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
                                    mutableListOf()
                                listOfCompanyFilter.addAll(listOfCompany.filter { it.type == itemList[0].type })
                                setAdapter<InvoiceGetDataMasterArray.GetCompanyDetailList>(
                                    0,
                                    listOfCompanyFilter
                                )
                            }
                        }

                        "Customers" -> {
                            if (listOfClients[0].status.equals("failure")) {
                                listOfClients.clear()
                            }
                            val type = object :
                                TypeToken<List<InvoiceGetDataMasterArray.GetClientDetails>>() {}.type
                            val itemList: List<InvoiceGetDataMasterArray.GetClientDetails> =
                                Gson().fromJson(it, type)
                           // listOfClients.add(0, itemList[0])  // Add at the first position
                            listOfClients.addAll(0, itemList.filter { it.type == itemList[0].type })
                            if (itemList[0].type == 2) {
                                binding.BusinessTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_blue
                                    )
                                )
                                binding.tabSelectTextBusiness.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )

                                binding.IndividualTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )
                                binding.tabSelectTextIndividual.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_black
                                    )
                                )
                            } else {
                                binding.BusinessTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )
                                binding.tabSelectTextBusiness.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_black
                                    )
                                )

                                binding.IndividualTypeClick.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_blue
                                    )
                                )
                                binding.tabSelectTextIndividual.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.invoice_white
                                    )
                                )
                            }

                            if (itemList[0].status.equals("success")) {
                                binding.NoDataLay.visibility = View.GONE
                                binding.searchLay.visibility = View.GONE
                                binding.recyclerCustomers.visibility = View.VISIBLE
                            } else {
                                binding.NoDataLay.visibility = View.VISIBLE
                                binding.searchLay.visibility = View.GONE
                                binding.recyclerCustomers.visibility = View.GONE
                            }
                            if (::adapter.isInitialized) {
                               /* adapter.notifyItemInserted(0)
                                adapter.notifyDataSetChanged()*/
                                val listOfClientFilter: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
                                    mutableListOf()
                                listOfClientFilter.addAll(listOfClients.filter { it.type == itemList[0].type })
                                setAdapter<InvoiceGetDataMasterArray.GetClientDetails>(
                                    1,
                                    listOfClientFilter
                                )
                            }
                        }

                        "Items" -> {
                            if (listOfItems[0].status.equals("failure")) {
                                listOfItems.clear()
                            }
                            // val newItem = Gson().fromJson(it, InvoiceGetDataMasterArray.GetItemList::class.java)
                            val type = object :
                                TypeToken<List<InvoiceGetDataMasterArray.GetItemList>>() {}.type
                            val itemList: List<InvoiceGetDataMasterArray.GetItemList> =
                                Gson().fromJson(it, type)
                            listOfItems.add(0, itemList[0])
                            if (itemList[0].status.equals("success")) {
                                binding.NoDataLay.visibility = View.GONE
                                binding.searchLay.visibility = View.VISIBLE
                                binding.recyclerCustomers.visibility = View.VISIBLE
                            } else {
                                binding.NoDataLay.visibility = View.VISIBLE
                                binding.searchLay.visibility = View.GONE
                                binding.recyclerCustomers.visibility = View.GONE
                            }
                            if (::adapter.isInitialized) {
                                adapter.notifyItemInserted(0)
                                adapter.notifyDataSetChanged()
                            }
                        }

                        "Expense" -> {
                            /*  val newItem = Gson().fromJson(it, InvoiceGetExpenseDataList.DataList::class.java)
                              listOfExpenses.add(0, newItem)*/
                            val type = object :
                                TypeToken<List<InvoiceGetExpenseDataList.DataList>>() {}.type
                            val itemList: List<InvoiceGetExpenseDataList.DataList> =
                                Gson().fromJson(it, type)
                            listOfExpenses.add(0, itemList[0])
                            println("listOfEx === ${listOfExpenses.size}")
                            if (listOfExpenses.size != 0) {
                                binding.NoDataLay.visibility = View.GONE
                                binding.searchLay.visibility = View.VISIBLE
                                binding.recyclerCustomers.visibility = View.VISIBLE
                            } else {
                                binding.NoDataLay.visibility = View.VISIBLE
                                binding.searchLay.visibility = View.GONE
                                binding.recyclerCustomers.visibility = View.GONE
                            }
                            if (::adapter.isInitialized) {
                                adapter.notifyItemInserted(0)
                                adapter.notifyDataSetChanged()
                            }

                        }
                    }
                }
            }
        }
    val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra("INVOICE_FORM_DATA_UPDATE")
                val dataClicpos = result.data?.getIntExtra("INVOICE_FORM_CLICK_POS", 0)
                println("Data == $data")
                println("dataClicpos == $dataClicpos")
                data?.let {
                    when (fromPage) {
                        "Business" -> {
                            val type = object :
                                TypeToken<List<InvoiceGetDataMasterArray.GetCompanyDetailList>>() {}.type
                            val itemList: List<InvoiceGetDataMasterArray.GetCompanyDetailList> =
                                Gson().fromJson(it, type)
                            if (dataClicpos != -1) {
                              //  listOfCompany[dataClicpos!!] = itemList[0]  // Update the correct index
                                if (::adapter.isInitialized) {
                                    EditUpdateAdapter<InvoiceGetDataMasterArray.GetCompanyDetailList>(
                                        itemList[0] ,dataClicpos!!
                                    )
                                } else {
                                    binding.NoDataLay.visibility = View.GONE
                                    binding.searchLay.visibility = View.VISIBLE
                                    binding.recyclerCustomers.visibility = View.VISIBLE
                                }
                            }
                        }

                        "Customers" -> {
                            val type = object :
                                TypeToken<List<InvoiceGetDataMasterArray.GetClientDetails>>() {}.type
                            val itemList: List<InvoiceGetDataMasterArray.GetClientDetails> =
                                Gson().fromJson(it, type)
                            if (dataClicpos != -1) {
                            //listOfClients[dataClicpos!!] = itemList[0]  // Update the correct index
                                if (::adapter.isInitialized) {
                                    //adapter.notifyItemChanged(dataClicpos)  // Refresh only updated item
                                    EditUpdateAdapter<InvoiceGetDataMasterArray.GetClientDetails>(
                                        itemList[0] ,dataClicpos!!
                                    )
                                }else{
                                    binding.NoDataLay.visibility = View.GONE
                                    binding.searchLay.visibility = View.GONE
                                    binding.recyclerCustomers.visibility = View.VISIBLE
                                }
                            }
                        }

                        "Items" -> {
                            val type = object :
                                TypeToken<List<InvoiceGetDataMasterArray.GetItemList>>() {}.type
                            val itemList: List<InvoiceGetDataMasterArray.GetItemList> =
                                Gson().fromJson(it, type)
                            if (dataClicpos != -1) {
                                listOfItems[dataClicpos!!] = itemList[0]  // Update the correct index
                                if (::adapter.isInitialized) {
                                    adapter.notifyItemChanged(dataClicpos)  // Refresh only updated item
                                }
                            }
                        }

                        "Expense" -> {
                            val newItem =
                                Gson().fromJson(it, InvoiceGetExpenseDataList.DataList::class.java)
                            listOfExpenses.add(0, newItem)
                            if (::adapter.isInitialized) {
                                adapter.notifyItemInserted(0)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }

    private fun <T> EditUpdateAdapter(
        EditlistOfCompany: T,
        dataClicpos: Int
    ) {
        val adapt = adapter as InvoiceMasterAdapter<T>
        adapt.Updatelist(dataClicpos,EditlistOfCompany)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceRecyclerviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (intent != null) {
            fromInvoice = intent.getIntExtra("fromInvoice", 0)
            fromPage = "" + intent.getStringExtra("InvoicefromPage")
        }

        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.title = fromPage
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        when {
            fromPage == "Business" -> {
                hintTexts.add("Search Name...")
                hintTexts.add("Find Business Name...")
                binding.tabcardlay.visibility = View.VISIBLE
                binding.searchLay.visibility = View.GONE
            }

            fromPage == "Customers" -> {
                hintTexts.add("Search Name...")
                hintTexts.add("Find Customer Name...")
                binding.tabcardlay.visibility = View.VISIBLE
                binding.searchLay.visibility = View.GONE
            }

            fromPage == "Items" -> {
                hintTexts.add("Search Name...")
                hintTexts.add("Find Item Name...")
                binding.tabcardlay.visibility = View.GONE
                binding.searchLay.visibility = View.VISIBLE
            }
        }

        println("fromPage == $fromPage")
        if (fromPage == "Expense") {
            if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                val InputMap = HashMap<String, Any>()
                InputMap["action"] = "getExpenses"
                InputMap["user_id"] = "" + preference.getString(
                    this@InvoiceBusinessAndCustomerActivity,
                    "INVOICE_USER_ID"
                )

                println("InvoiceRequest - $_TAG == $InputMap")
                InvoiceUtils.loadingProgress(
                    this@InvoiceBusinessAndCustomerActivity,
                    InvoiceUtils.messageLoading,
                    false
                ).show()
                viewModel.getExpenseList(InputMap)
            } else {
                Toast.makeText(
                    this@InvoiceBusinessAndCustomerActivity,
                    "Check Your Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            loadMasterData()
        }

        viewModel.getExpenseList.observe(this@InvoiceBusinessAndCustomerActivity) { getList ->
            InvoiceUtils.loadingDialog.dismiss()
            binding.swipeRefresh.isRefreshing = false
            if (getList.status.equals("success")) {
                binding.swipeRefresh.isRefreshing = false
                binding.NoDataLay.visibility = View.GONE
                binding.searchLay.visibility = View.VISIBLE
                binding.recyclerCustomers.visibility = View.VISIBLE
                getList.data?.let { listOfExpenses.addAll(it) }
                println("listEx == ${listOfExpenses[0].itemName}")
                setAdapter<InvoiceGetExpenseDataList.DataList>(3, listOfExpenses)
            } else {
                binding.NoDataLay.visibility = View.VISIBLE
                binding.searchLay.visibility = View.GONE
                binding.recyclerCustomers.visibility = View.GONE
                //  InvoiceUtils.loadingProgress(this@InvoiceBusinessAndCustomerActivity,InvoiceUtils.errorMessage,false).show()
            }
        }

        viewModel.errorMessage.observe(this@InvoiceBusinessAndCustomerActivity) {
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this@InvoiceBusinessAndCustomerActivity, "" + it, Toast.LENGTH_SHORT)
                .show()
        }


        binding.swipeRefresh.setOnRefreshListener {
            listOfCompany.clear()
            listOfItems.clear()
            listOfClients.clear()
            listOfExpenses.clear()

            println("fromPage swipe == $fromPage")
            if (fromPage == "Expense") {
                if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                    val InputMap = HashMap<String, Any>()
                    InputMap["action"] = "getExpenses"
                    InputMap["user_id"] = "" + preference.getString(
                        this@InvoiceBusinessAndCustomerActivity,
                        "INVOICE_USER_ID"
                    )

                    println("InvoiceRequest - $_TAG == $InputMap")
                    viewModel.getExpenseList(InputMap)
                } else {
                    Toast.makeText(
                        this@InvoiceBusinessAndCustomerActivity,
                        "Check Your Internet Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                loadMasterData()
            }
        }
        binding.BusinessTypeClick.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.invoice_blue
            )
        )
        binding.tabSelectTextBusiness.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.invoice_white
            )
        )
      binding.BusinessTypeClick.performClick()
        binding.BusinessTypeClick.setOnClickListener {
            clicktabPos = 1
            binding.BusinessTypeClick.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_blue
                )
            )
            binding.tabSelectTextBusiness.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_white
                )
            )

            binding.IndividualTypeClick.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_white
                )
            )
            binding.tabSelectTextIndividual.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_black
                )
            )
            println("fromPageOfRRadioClick === $fromPage")
          /*  if (fromPage == "Business") {
               *//* val listOfCompanyFilter: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
                    mutableListOf()
                listOfCompanyFilter.addAll(listOfCompany.filter { it.type == 0 })
                setAdapter<InvoiceGetDataMasterArray.GetCompanyDetailList>(0, listOfCompanyFilter)*//*
                loadMasterData()
            } else {
                *//*val listOfClientsFilter: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
                    mutableListOf()
                listOfClientsFilter.clear()
                listOfClientsFilter.addAll(listOfClients.filter { it.type == 2 })
                setAdapter<InvoiceGetDataMasterArray.GetClientDetails>(1, listOfClientsFilter)*//*
                loadMasterData()
            }*/
            loadMasterData()


        }

        binding.IndividualTypeClick.setOnClickListener {
            clicktabPos = 2
            binding.BusinessTypeClick.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_white
                )
            )
            binding.tabSelectTextBusiness.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_black
                )
            )

            binding.IndividualTypeClick.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_blue
                )
            )
            binding.tabSelectTextIndividual.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.invoice_white
                )
            )
            println("fromPageOfRRadioClick 11 === $fromPage")
            loadMasterData()
           /* if (fromPage == "Business") {
             *//*   val listOfCompanyFilter: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
                    mutableListOf()
                listOfCompanyFilter.addAll(listOfCompany.filter { it.type == 1 })
                setAdapter<InvoiceGetDataMasterArray.GetCompanyDetailList>(0, listOfCompanyFilter)*//*
                loadMasterData()
            } else {
               *//* val listOfClientsFilter: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
                    mutableListOf()
                listOfClientsFilter.clear()
                listOfClientsFilter.addAll(listOfClients.filter { it.type == 1 })
                setAdapter<InvoiceGetDataMasterArray.GetClientDetails>(1, listOfClientsFilter)*//*
                loadMasterData()
            }*/

        }

        viewModel.getMasterDetail.observe(this@InvoiceBusinessAndCustomerActivity) { getMasterArray ->
            println("getMastr == ${getMasterArray.companyDetails!!.size}")
            InvoiceUtils.loadingDialog.dismiss()
            if (getMasterArray.status.equals("success")) {
                binding.NoDataLay.visibility = View.GONE
                binding.searchLay.visibility = View.VISIBLE
                binding.recyclerCustomers.visibility = View.VISIBLE
                binding.swipeRefresh.isRefreshing = false
                listOfCompany.clear()
                listOfClients.clear()
                listOfCompany.addAll(getMasterArray.companyDetails!!)
                listOfClients.addAll(getMasterArray.clientDetails!!)
                listOfItems.addAll(getMasterArray.itemList!!)
                println("fromPage == $fromPage")
                if (fromPage == "Business") {
                    println("companyDet111 == ${getMasterArray.companyDetails!![0].status}")
                    if (getMasterArray.companyDetails!![0].status.equals("failure")) {
                        binding.NoDataLay.visibility = View.VISIBLE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.GONE
                    } else {
                        println("companyDet == ${getMasterArray.companyDetails!![0].status}")
                        binding.NoDataLay.visibility = View.GONE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.VISIBLE
                    }
                    val listOfCompanyFilter: MutableList<InvoiceGetDataMasterArray.GetCompanyDetailList> =
                        mutableListOf()
                    if (clicktabPos == 1) {
                        listOfCompanyFilter.clear()
                        listOfCompanyFilter.addAll(listOfCompany.filter { it.type == 0 })
                    } else {
                        listOfCompanyFilter.clear()
                        listOfCompanyFilter.addAll(listOfCompany.filter { it.type == 1 })
                        println("listOfCompany -== ${listOfCompanyFilter.size}")
                    }
                    if (listOfCompanyFilter.size != 0){
                        binding.NoDataLay.visibility = View.GONE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.VISIBLE
                    }else{
                        binding.NoDataLay.visibility = View.VISIBLE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.GONE
                    }
                    setAdapter<InvoiceGetDataMasterArray.GetCompanyDetailList>(
                        0,
                        listOfCompanyFilter
                    )

                } else if (fromPage == "Customers") {
                    if (getMasterArray.clientDetails!![0].status.equals("failure")) {
                        binding.NoDataLay.visibility = View.VISIBLE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.GONE
                    } else {
                        binding.NoDataLay.visibility = View.GONE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.VISIBLE
                    }
                    val listOfClientsFilter: MutableList<InvoiceGetDataMasterArray.GetClientDetails> =
                        mutableListOf()
                    if (clicktabPos == 1) {
                        listOfClientsFilter.clear()
                        listOfClientsFilter.addAll(listOfClients.filter { it.type == 2 })
                    } else {
                        listOfClientsFilter.clear()
                        listOfClientsFilter.addAll(listOfClients.filter { it.type == 1 })
                    }

                    if (listOfClientsFilter.size != 0){
                        binding.NoDataLay.visibility = View.GONE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.VISIBLE
                    }else{
                        binding.NoDataLay.visibility = View.VISIBLE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.GONE
                    }

                    setAdapter<InvoiceGetDataMasterArray.GetClientDetails>(1, listOfClientsFilter)

                } else if (fromPage == "Items") {
                    if (getMasterArray.itemList!![0].status.equals("failure")) {
                        binding.NoDataLay.visibility = View.VISIBLE
                        binding.searchLay.visibility = View.GONE
                        binding.recyclerCustomers.visibility = View.GONE
                    } else {
                        binding.NoDataLay.visibility = View.GONE
                        binding.searchLay.visibility = View.VISIBLE
                        binding.recyclerCustomers.visibility = View.VISIBLE
                    }
                    setAdapter<InvoiceGetDataMasterArray.GetItemList>(2, listOfItems)
                }
            } else {
                binding.NoDataLay.visibility = View.VISIBLE
                binding.searchLay.visibility = View.GONE
                binding.recyclerCustomers.visibility = View.GONE
            }
        }

        viewModel.getDelete.observe(this@InvoiceBusinessAndCustomerActivity) {
            if (it.isNotEmpty()) {
                if (it["status"].toString().equals("success")) {
                    Toast.makeText(
                        this@InvoiceBusinessAndCustomerActivity,
                        "" + it["msg"],
                        Toast.LENGTH_SHORT
                    ).show()
                    actionName = ""
                    println("listOfPo=== $ListPosition")
                    adapter.DeleteNotify(ListPosition)
                    println("list Data delete size == ${adapter.itemCount}")
                    if (adapter.itemCount == 0) {
                        loadMasterData()
                    }
                } else {
                    Toast.makeText(
                        this@InvoiceBusinessAndCustomerActivity,
                        "" + it["msg"],
                        Toast.LENGTH_SHORT
                    ).show()
                    actionName = ""
                    ShowDialogUsedBusiness("" + it["msg"], 0, "")
                }
            }
        }
        changeHintWithAnimation()

        /* binding.editTextSearch.setOnFocusChangeListener { _, hasFocus ->
             if (hasFocus) {
                 stopHintAnimation() // Stop animation when user types
                 binding.animatedHint.text = "" // Hide hint
             } else {
                 resumeHintAnimation() // Resume animation when focus is lost
             }
         }*/
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (!text.isNullOrEmpty()) {
                    stopHintAnimation() // User is typing → stop animation
                    binding.animatedHint.text = "" // Hide hint while typing
                } else {
                    resumeHintAnimation() // No input → resume animation
                }
            }

            override fun afterTextChanged(s: Editable?) {
                (binding.recyclerCustomers.adapter as InvoiceMasterAdapter<*>).filter.filter(s.toString())
            }
        })


    }

    fun loadMasterData(){
        if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = "" + preference.getString(
                this@InvoiceBusinessAndCustomerActivity,
                "INVOICE_USER_ID"
            )

            println("InvoiceRequest - $_TAG == $InputMap")
            InvoiceUtils.loadingProgress(
                this@InvoiceBusinessAndCustomerActivity,
                InvoiceUtils.messageLoading,
                false
            ).show()
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceBusinessAndCustomerActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopHintAnimation() {
        isAnimationRunning = false
        handler.removeCallbacksAndMessages(null) // Stop scheduled animations
    }

    private fun resumeHintAnimation() {
        if (!isAnimationRunning) {
            isAnimationRunning = true
            changeHintWithAnimation() // Restart animation loop
        }
    }

    private fun animateHintChange(newHint: String) {
        // Move the current hint up and fade out
        ObjectAnimator.ofFloat(binding.animatedHint, "translationY", 0f, -50f).apply {
            duration = 300
            start()
        }.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                if (isAnimationRunning) {
                    binding.animatedHint.text = newHint
                    // Reset position from below and fade in
                    ObjectAnimator.ofFloat(binding.animatedHint, "translationY", 50f, 0f).apply {
                        duration = 300
                        start()
                    }
                }
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun changeHintWithAnimation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isAnimationRunning) { // Only animate if allowed
                    animateHintChange(hintTexts[currentIndex])
                    currentIndex = (currentIndex + 1) % hintTexts.size // Loop through hints
                }
                handler.postDelayed(this, 3000) // Change every 3 seconds
            }
        }, 3000)
    }

    private fun <T> setAdapter(fromCLick: Int, GetList: MutableList<T>) {
        adapter = InvoiceMasterAdapter(
            this@InvoiceBusinessAndCustomerActivity,
            GetList,
            "",
            this,
            fromInvoice,
            fromCLick, onAddItemClick = { selectedItem ->
                println("selected == ${selectedItem}")
                val resultIntent = Intent()
                val selectedItemJson = Gson().toJson(selectedItem)
                println("selected Json == ${selectedItemJson}")
                preference.putString(
                    this@InvoiceBusinessAndCustomerActivity,
                    "INVOICE_SELECTED_ITEMS",
                    selectedItemJson
                )
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            },
            onDeleteItem = { deleteId, pos, actionNameServer ->
                println("swipe == $fromPage")
                ListPosition = pos
                actionName = actionNameServer
                ShowDialogUsedBusiness("Do you want to delete this item?", deleteId, actionName)
            },
            onSearchResult = {
                if (it) {
                    binding.recyclerCustomers.visibility = View.GONE
                    binding.NoDataLay.visibility = View.VISIBLE
                    binding.searchLay.visibility = View.VISIBLE
                    binding.NoDataLayText.text = "No search data"
                } else {
                    binding.recyclerCustomers.visibility = View.VISIBLE
                    binding.NoDataLay.visibility = View.GONE
                    binding.searchLay.visibility = View.VISIBLE
                }
            }
        )
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(this)
        binding.recyclerCustomers.adapter = adapter

    }

    private fun ShowDialogUsedBusiness(confirmTxt: String, deleteId: Int, actionName: String) {
        val builder = AlertDialog.Builder(this@InvoiceBusinessAndCustomerActivity)

        val titleView = TextView(this@InvoiceBusinessAndCustomerActivity)
        titleView.text = "Alert!"
        titleView.gravity = Gravity.START
        titleView.textSize = 18f
        val typeface1 =
            ResourcesCompat.getFont(this@InvoiceBusinessAndCustomerActivity, R.font.lexend_medium)
        titleView.typeface = typeface1
        titleView.setPadding(20, 20, 20, 5)
        titleView.setTextColor(
            ContextCompat.getColor(
                this@InvoiceBusinessAndCustomerActivity,
                R.color.invoice_red
            )
        )
        // Custom message with font
        val messageView = TextView(this@InvoiceBusinessAndCustomerActivity)
        messageView.text = "" + confirmTxt
        messageView.textSize = 15f
        val typeface =
            ResourcesCompat.getFont(this@InvoiceBusinessAndCustomerActivity, R.font.lexend_medium)
        messageView.typeface = typeface
        messageView.setPadding(20, 10, 20, 5)
        messageView.setTextColor(
            ContextCompat.getColor(
                this@InvoiceBusinessAndCustomerActivity,
                R.color.invoice_black
            )
        )
        if (actionName.isNotEmpty()) {
            builder.setCustomTitle(titleView)
                .setView(messageView)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
        } else {
            builder.setCustomTitle(titleView)
                .setView(messageView)
                .setPositiveButton("Ok", null)
        }

        val dialog = builder.create() // Create the dialog
        dialog.show() // Show the dialog first

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            dialog.dismiss()
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                Toast.makeText(
                    this@InvoiceBusinessAndCustomerActivity, "" + InvoiceUtils.messageNetCheck,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (actionName.isNotEmpty()) {
                deleteFun(deleteId, actionName)
            }
            dialog.dismiss()
        }
    }

    private fun deleteFun(deleteId: Int, actionName: String) {
        if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = actionName
            InputMap["id"] = "" + deleteId

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getDeleteData(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceBusinessAndCustomerActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onItemClick(item: String, clickId: Int, fromClick: Int, position: Int) {
        println("Form == Clcikc == $fromClick")
        println("Form == Clcikc clickId == $clickId")
        businessClickId = clickId
        if (fromClick == 0) {
            val intent = Intent(
                this@InvoiceBusinessAndCustomerActivity,
                InvoiceBusinessDetailFormActivity::class.java
            )
            intent.putExtra("clickDataId", clickId)
            intent.putExtra("clickPosition", position)
            editLauncher.launch(intent)
        } else if (fromClick == 1) {
            val intent = Intent(
                this@InvoiceBusinessAndCustomerActivity,
                InvoiceNewCustomerFormActivity::class.java
            )
            intent.putExtra("clickDataId", clickId)
            intent.putExtra("clickPosition", position)
            editLauncher.launch(intent)
        } else {
            val intent = Intent(
                this@InvoiceBusinessAndCustomerActivity,
                InvoiceAddItemFormActivity::class.java
            )
            intent.putExtra("clickDataId", clickId)
            intent.putExtra("clickPosition", position)
            intent.putExtra("fromInvoicePage", "InvoiceBusinessAndCustomerActivity_Item")
            editLauncher.launch(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.invoice_add_item, menu)
        return true
    }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.add_icon -> {
                    if (fromPage == "Business") {
                        val intent = Intent(
                            this@InvoiceBusinessAndCustomerActivity,
                            InvoiceBusinessDetailFormActivity::class.java
                        )
                        intent.putExtra(
                            "fromInvoicePage",
                            "InvoiceBusinessAndCustomerActivity_Business"
                        )
                        addItemLauncher.launch(intent)
                    } else if (fromPage == "Customers") {
                        val intent = Intent(
                            this@InvoiceBusinessAndCustomerActivity,
                            InvoiceNewCustomerFormActivity::class.java
                        )
                        intent.putExtra(
                            "fromInvoicePage",
                            "InvoiceBusinessAndCustomerActivity_Customers"
                        )
                        addItemLauncher.launch(intent)
                    } else if (fromPage == "Expense") {
                        if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                            val intent = Intent(
                                this@InvoiceBusinessAndCustomerActivity,
                                InvoiceAddExpenseFormActivity::class.java
                            )
                            intent.putExtra(
                                "fromInvoicePage",
                                "InvoiceBusinessAndCustomerActivity_Expenses"
                            )
                            addItemLauncher.launch(intent)
                        } else {
                            Toast.makeText(
                                this@InvoiceBusinessAndCustomerActivity,
                                "" + InvoiceUtils.messageNetCheck,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val intent = Intent(
                            this@InvoiceBusinessAndCustomerActivity,
                            InvoiceAddItemFormActivity::class.java
                        )
                        intent.putExtra("fromInvoicePage", "InvoiceBusinessAndCustomerActivity_Item")
                        addItemLauncher.launch(intent)
                    }
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }

    companion object {
        var _TAG = "InvoiceBusinessAndCustomerActivity"
    }
}