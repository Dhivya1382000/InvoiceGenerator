package com.nithra.invoice_generator_tool.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessDetailFormActivity.Companion._TAG
import com.nithra.invoice_generator_tool.activity.InvoiceNewCustomerFormActivity.Companion
import com.nithra.invoice_generator_tool.adapter.InvoiceAddedItemDataAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceCreateFormBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceOfflineDynamicData
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

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
    var preference = InvioceSharedPreference()
    var newInvoiceNumber = 0
    private lateinit var adapter: InvoiceAddedItemDataAdapter
    private var DynamicitemList = mutableListOf<InvoiceOfflineDynamicData>()
 var positionOfEDit = 0
 var clickEditId = 0
    private val selectItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val json = preference.getString(this@InvoiceCreateFormActivity, "INVOICE_SELECTED_ITEMS") // Default empty list
                val type = object : TypeToken<InvoiceOfflineDynamicData>() {}.type
                val itemList: InvoiceOfflineDynamicData = Gson().fromJson(json, type)

                println("iTemLit === ${itemList.item_name}")
                // Add new item to list
                DynamicitemList.add(itemList)
                adapter.notifyDataSetChanged()
            }
        }
    private val selectItemEditLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val json = preference.getString(this@InvoiceCreateFormActivity, "INVOICE_EDIT_ITEMS") // Default empty list
                val type = object : TypeToken<List<InvoiceOfflineDynamicData>>() {}.type
                val itemList: List<InvoiceOfflineDynamicData> = Gson().fromJson(json, type)
                println("iTemLit from === ${itemList[0].item_id}")
                println("iTemLit click === ${clickEditId}")

                val index = itemList.indexOfFirst { clickEditId == itemList[0].item_id }
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
            }
            val hasStatusKey = listOfItemDetails.any { !it.status.isNullOrEmpty() } ?: false
            println("map === " + hasStatusKey) // Output: true or false
        }

        val currentInvoiceNumber =
            preference.getInt(this@InvoiceCreateFormActivity, "InvoiceNumber") // Start from 1000

        newInvoiceNumber = currentInvoiceNumber + 1

        binding.InvoiceIncreNumber.setText(".inv" + newInvoiceNumber)

        binding.InvoiceBusinessChange.setOnClickListener {
            val intent = Intent(
                this@InvoiceCreateFormActivity,
                InvoiceBusinessDetailFormActivity::class.java
            )
            intent.putExtra("fromInvoice",1)
            startActivity(intent)
        }

        binding.InvoiceCustomerAdd.setOnClickListener {
            val intent = Intent(
                this@InvoiceCreateFormActivity,
                InvoiceBusinessDetailFormActivity::class.java
            )
            intent.putExtra("fromInvoice",1)
            startActivity(intent)
        }

        binding.AddInvoiceText.setOnClickListener {
            /* if (listOfItemDetails[0].status.equals("failure")){
                 val intent = Intent(this@InvoiceCreateFormActivity,InvoiceAddItemFormActivity::class.java)
                 startActivity(intent)
                 return@setOnClickListener
             }*/

            val intent = Intent(
                this@InvoiceCreateFormActivity,
                InvoiceBusinessAndCustomerActivity::class.java
            )
            intent.putExtra("fromInvoice",1)
            selectItemLauncher.launch(intent)
        }
        binding.InvoiceBusinessSaveCard.setOnClickListener {
            preference.putInt(this@InvoiceCreateFormActivity, "InvoiceNumber", newInvoiceNumber)
            if (InvoiceUtils.isNetworkAvailable(this@InvoiceCreateFormActivity)){
                GenerateInvoice()
            }else{
                Toast.makeText(
                    this@InvoiceCreateFormActivity,
                    "Check Your Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        adapter = InvoiceAddedItemDataAdapter(DynamicitemList, onItemClick = { position ->
            DynamicitemList.removeAt(position)
            adapter.notifyDataSetChanged()
        },OnEditClick = { clickId,pos ->
            val intent = Intent(
                this@InvoiceCreateFormActivity,
                InvoiceAddItemFormActivity::class.java
            )
            intent.putExtra("fromInvoice",1)
            intent.putExtra("clickDataId",clickId.item_id)
            positionOfEDit = pos
            clickEditId = clickId.item_id!!
            selectItemEditLauncher.launch(intent)
        })

        binding.dynamicContainer.isNestedScrollingEnabled = true

        binding.dynamicContainer.layoutManager = LinearLayoutManager(this)
        binding.dynamicContainer.adapter = adapter

    }

        private fun GenerateInvoice() {
        val map = LinkedHashMap<String, Any>()
        map["action"] = "addInvoice"
        map["user_id"] = "1227994"
        map["bussiness_id"] = ""
        map["client_id"] = ""
        map["invoice_number"] = ""
        map["order_no"] = ""
        map["invoice_date"] = ""
        map["amt_type"] = ""
        map["paid_amt"] = ""
        map["due_date"] = ""
        map["remark"] = ""
        map["terms_condition"] = ""
        for ((pos,i) in DynamicitemList.withIndex()){
            map["item[$pos][item_id]"] = ""+i.item_id
            map["item[$pos][amount]"] = ""+i.amount
            map["item[$pos][qty_type]"] = ""+i.qty_type
            map["item[$pos][qty]"] = ""+i.qty
            map["item[$pos][tax]"] = ""+i.tax
            map["item[$pos][description]"] = ""+i.description
            map["item[$pos][discount_type]"] = ""+i.discount_type
            map["item[$pos][discount]"] = ""+i.discount
        }
        println("InvoiceRequest - ${_TAG} == $map")
      // viewModel.addInvoiceList(map)

    }

    companion object{
        var _TAG = "InvoiceCreateFormActivity"
        const val REQUEST_CODE_SELECT_ITEM = 100

    }
}