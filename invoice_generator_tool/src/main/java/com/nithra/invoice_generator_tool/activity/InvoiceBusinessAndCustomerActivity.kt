package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceRecyclerviewBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseDataList
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData
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
    lateinit var adapter : InvoiceMasterAdapter<*>

    val addItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringExtra("INVOICE_FORM_DATA")
            println("Data == $data")
            data?.let {
                when (fromPage) {
                    "Business" -> {
                        val type = object : TypeToken<List<InvoiceGetDataMasterArray.GetCompanyDetailList>>() {}.type
                        val itemList: List<InvoiceGetDataMasterArray.GetCompanyDetailList> = Gson().fromJson(it, type)
                        listOfCompany.add(0, itemList[0])  // Add at the first position
                        adapter.notifyItemInserted(0)
                        adapter.notifyDataSetChanged()
                    }
                    "Customers" -> {
                        val type = object : TypeToken<List<InvoiceGetDataMasterArray.GetClientDetails>>() {}.type
                        val itemList: List<InvoiceGetDataMasterArray.GetClientDetails> = Gson().fromJson(it, type)
                        listOfClients.add(0, itemList[0])  // Add at the first position
                        adapter.notifyItemInserted(0)
                        adapter.notifyDataSetChanged()
                    }
                    "Items" -> {
                       // val newItem = Gson().fromJson(it, InvoiceGetDataMasterArray.GetItemList::class.java)
                        val type = object : TypeToken<List<InvoiceGetDataMasterArray.GetItemList>>() {}.type
                        val itemList: List<InvoiceGetDataMasterArray.GetItemList> = Gson().fromJson(it, type)
                        listOfItems.add(0,itemList[0])
                        adapter.notifyItemInserted(0)
                        adapter.notifyDataSetChanged()
                    }
                    "Expense" -> {
                        val newItem = Gson().fromJson(it, InvoiceGetExpenseDataList.DataList::class.java)
                        listOfExpenses.add(0,newItem)
                        adapter.notifyItemInserted(0)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
    val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringExtra("INVOICE_FORM_DATA_UPDATE")
            val dataClicpos = result.data?.getIntExtra("INVOICE_FORM_CLICK_POS",0)
            println("Data == $data")
            println("dataClicpos == $dataClicpos")
            data?.let {
                when (fromPage) {
                    "Business" -> {
                        val type = object : TypeToken<List<InvoiceGetDataMasterArray.GetCompanyDetailList>>() {}.type
                        val itemList: List<InvoiceGetDataMasterArray.GetCompanyDetailList> = Gson().fromJson(it, type)
                        if (dataClicpos != -1) {
                            listOfCompany[dataClicpos!!] = itemList[0]  // Update the correct index
                            adapter.notifyItemChanged(dataClicpos)  // Refresh only updated item
                        }
                    }
                    "Customers" -> {
                        val type = object : TypeToken<List<InvoiceGetDataMasterArray.GetClientDetails>>() {}.type
                        val itemList: List<InvoiceGetDataMasterArray.GetClientDetails> = Gson().fromJson(it, type)
                        if (dataClicpos != -1 ) {
                            listOfClients[dataClicpos!!] = itemList[0]  // Update the correct index
                            adapter.notifyItemChanged(dataClicpos)  // Refresh only updated item
                        }
                    }
                    "Items" -> {
                        val type = object : TypeToken<List<InvoiceGetDataMasterArray.GetItemList>>() {}.type
                        val itemList: List<InvoiceGetDataMasterArray.GetItemList> = Gson().fromJson(it, type)
                        if (dataClicpos != -1) {
                            listOfItems[dataClicpos!!] = itemList[0]  // Update the correct index
                            adapter.notifyItemChanged(dataClicpos)  // Refresh only updated item
                        }
                    }
                    "Expense" -> {
                        val newItem = Gson().fromJson(it, InvoiceGetExpenseDataList.DataList::class.java)
                        listOfExpenses.add(0,newItem)
                        adapter.notifyItemInserted(0)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceRecyclerviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent != null){
            fromInvoice = intent.getIntExtra("fromInvoice", 0)
            fromPage = ""+intent.getStringExtra("InvoicefromPage")

        }
        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.title = fromPage
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image


        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        println("fromPage == $fromPage")
        if (fromPage == "Expense"){
            if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                val InputMap = HashMap<String, Any>()
                InputMap["action"] = "getExpenses"
                InputMap["user_id"] = "1227994"

                println("InvoiceRequest - $_TAG == $InputMap")
                InvoiceUtils.loadingProgress(this@InvoiceBusinessAndCustomerActivity,InvoiceUtils.messageLoading,false).show()
                viewModel.getExpenseList(InputMap)
            } else {
                Toast.makeText(
                    this@InvoiceBusinessAndCustomerActivity,
                    "Check Your Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }else{
            if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                val InputMap = HashMap<String, Any>()
                InputMap["action"] = "getMaster"
                InputMap["user_id"] = "1227994"

                println("InvoiceRequest - $_TAG == $InputMap")
                InvoiceUtils.loadingProgress(this@InvoiceBusinessAndCustomerActivity,InvoiceUtils.messageLoading,false).show()
                viewModel.getOverAllMasterDetail(InputMap)
            } else {
                Toast.makeText(
                    this@InvoiceBusinessAndCustomerActivity,
                    "Check Your Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

   viewModel.getExpenseList.observe(this@InvoiceBusinessAndCustomerActivity){getList ->
       InvoiceUtils.loadingDialog.dismiss()
       if (getList.status.equals("success")){
           binding.swipeRefresh.isRefreshing = false
           getList.data?.let { listOfExpenses.addAll(it) }
           println("listEx == ${listOfExpenses[0].itemName}")
           setAdapter<InvoiceGetExpenseDataList.DataList>(3, listOfExpenses)
       }else{
         //  InvoiceUtils.loadingProgress(this@InvoiceBusinessAndCustomerActivity,InvoiceUtils.errorMessage,false).show()
       }
   }

        viewModel.errorMessage.observe(this@InvoiceBusinessAndCustomerActivity){
            Toast.makeText(this@InvoiceBusinessAndCustomerActivity, ""+it, Toast.LENGTH_SHORT).show()
        }

        binding.swipeRefresh.setOnRefreshListener {
            listOfCompany.clear()
            listOfItems.clear()
            listOfClients.clear()
            listOfExpenses.clear()

            if (fromPage == "Expense"){
                if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                    val InputMap = HashMap<String, Any>()
                    InputMap["action"] = "getExpenses"
                    InputMap["user_id"] = "1227994"

                    println("InvoiceRequest - $_TAG == $InputMap")
                    viewModel.getExpenseList(InputMap)
                } else {
                    Toast.makeText(
                        this@InvoiceBusinessAndCustomerActivity,
                        "Check Your Internet Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                if (InvoiceUtils.isNetworkAvailable(this@InvoiceBusinessAndCustomerActivity)) {
                    val InputMap = HashMap<String, Any>()
                    InputMap["action"] = "getMaster"
                    InputMap["user_id"] = "1227994"

                    println("InvoiceRequest - $_TAG == $InputMap")
                    viewModel.getOverAllMasterDetail(InputMap)
                } else {
                    Toast.makeText(
                        this@InvoiceBusinessAndCustomerActivity,
                        "Check Your Internet Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            println("getMastr == ${getMasterArray.companyDetails!!.size}")
            InvoiceUtils.loadingDialog.dismiss()
            if (getMasterArray.status.equals("success")) {
                binding.swipeRefresh.isRefreshing = false
                listOfCompany.addAll(getMasterArray.companyDetails!!)
                listOfClients.addAll(getMasterArray.clientDetails!!)
                listOfItems.addAll(getMasterArray.itemList!!)
                if (fromPage == "Business") {
                    setAdapter<InvoiceGetDataMasterArray.GetCompanyDetailList>(0, listOfCompany)
                } else if (fromPage == "Customers") {
                    setAdapter<InvoiceGetDataMasterArray.GetClientDetails>(1, listOfClients)
                } else if (fromPage == "Items"){
                    setAdapter<InvoiceGetDataMasterArray.GetItemList>(2, listOfItems)
                }
            }
        }

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
                preference.putString(this@InvoiceBusinessAndCustomerActivity,"INVOICE_SELECTED_ITEMS",selectedItemJson)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            },
             onDeleteItem ={

             }
        )
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(this)
        binding.recyclerCustomers.adapter = adapter
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
            intent.putExtra("fromInvoicePage","InvoiceBusinessAndCustomerActivity_Item")
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
                    intent.putExtra("fromInvoicePage","InvoiceBusinessAndCustomerActivity_Business")
                    addItemLauncher.launch(intent)
                } else if (fromPage == "Customers") {
                    val intent = Intent(
                        this@InvoiceBusinessAndCustomerActivity,
                        InvoiceNewCustomerFormActivity::class.java
                    )
                    intent.putExtra("fromInvoicePage","InvoiceBusinessAndCustomerActivity_Customers")
                    addItemLauncher.launch(intent)
                } else {
                    val intent = Intent(
                        this@InvoiceBusinessAndCustomerActivity,
                        InvoiceAddItemFormActivity::class.java
                    )
                    intent.putExtra("fromInvoicePage","InvoiceBusinessAndCustomerActivity_Item")
                    addItemLauncher.launch(intent)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object{
        var _TAG ="InvoiceBusinessAndCustomerActivity"
    }
}