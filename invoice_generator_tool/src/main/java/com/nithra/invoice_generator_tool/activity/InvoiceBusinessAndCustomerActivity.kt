package com.nithra.invoice_generator_tool.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceMasterAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceRecyclerviewBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
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
    var preference = InvioceSharedPreference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceRecyclerviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fromPage = intent.getStringExtra("InvoicefromPage")

        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.title = fromPage
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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
        binding.swipeRefresh.setOnRefreshListener {
            listOfCompany.clear()
            listOfItems.clear()
            listOfClients.clear()
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

        binding.InvoiceFab.setOnClickListener {
            if (fromPage == "Business") {
                val intent = Intent(
                    this@InvoiceBusinessAndCustomerActivity,
                    InvoiceBusinessDetailFormActivity::class.java
                )
               /* val gson = Gson()
                val savedListOfCompany = gson.toJson(listOfCompany) // Convert User object to JSON

                preference.putString(
                    this@InvoiceBusinessAndCustomerActivity,
                    "listOfCompany",
                    savedListOfCompany
                )*/
                startActivity(intent)
            } else if (fromPage == "Customers") {
                val intent = Intent(
                    this@InvoiceBusinessAndCustomerActivity,
                    InvoiceNewCustomerFormActivity::class.java
                )
                startActivity(intent)
            } else {
                val intent = Intent(
                    this@InvoiceBusinessAndCustomerActivity,
                    InvoiceAddItemFormActivity::class.java
                )
                startActivity(intent)
            }
        }

        println("listOfCompany == ${listOfCompany.size}")

        viewModel.getMasterDetail.observe(this) { getMasterArray ->
            println("getMastr == ${getMasterArray.companyDetails!!.size}")
            if (getMasterArray.status.equals("success")) {
                binding.swipeRefresh.isRefreshing = false
                listOfCompany.addAll(getMasterArray.companyDetails!!)
                listOfClients.addAll(getMasterArray.clientDetails!!)
                listOfItems.addAll(getMasterArray.itemList!!)
                if (fromPage == "Business") {
                    setAdapter<InvoiceGetDataMasterArray.GetCompanyDetailList>(0, listOfCompany)
                } else if (fromPage == "Customers") {
                    setAdapter<InvoiceGetDataMasterArray.GetClientDetails>(1, listOfClients)
                } else {
                    setAdapter<InvoiceGetDataMasterArray.GetItemList>(2, listOfItems)
                }
            }
        }


    }

    private fun <T> setAdapter(fromCLick: Int, GetList: MutableList<T>) {
        val adapter = InvoiceMasterAdapter(
            this@InvoiceBusinessAndCustomerActivity,
            GetList,
            "",
            this,
            fromCLick
        )
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(this)
        binding.recyclerCustomers.adapter = adapter
    }

    override fun onItemClick(item: String, clickId: Int, fromClick: Int) {
        if (fromClick == 0) {
            val intent = Intent(
                this@InvoiceBusinessAndCustomerActivity,
                InvoiceBusinessDetailFormActivity::class.java
            )
            intent.putExtra("fromInvoicePage","InvoiceBusinessAndCustomerActivity_Business")
            intent.putExtra("clickDataId",clickId)
            startActivity(intent)
        } else if (fromClick == 1) {
            val intent = Intent(
                this@InvoiceBusinessAndCustomerActivity,
                InvoiceNewCustomerFormActivity::class.java
            )
            intent.putExtra("fromInvoicePage","InvoiceBusinessAndCustomerActivity_Customer")
            intent.putExtra("clickDataId",clickId)
            startActivity(intent)
        } else {
            val intent = Intent(
                this@InvoiceBusinessAndCustomerActivity,
                InvoiceAddItemFormActivity::class.java
            )
            intent.putExtra("fromInvoicePage","InvoiceBusinessAndCustomerActivity_ItemForm")
            intent.putExtra("clickDataId",clickId)
            startActivity(intent)
        }
    }
    companion object{
        var _TAG ="InvoiceBusinessAndCustomerActivity"
    }
}