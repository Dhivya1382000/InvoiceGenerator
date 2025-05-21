package com.nithra.invoice_generator_tool.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.nithra.invoice_generator_tool.activity.InvoiceCreateFormActivity
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen.Companion._TAG
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen.Companion.refreshRecentList
import com.nithra.invoice_generator_tool.adapter.InvoiceAllListAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAllDataBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InvoiceTabContentFragment : Fragment() {
    lateinit var binding: ActivityInvoiceAllDataBinding
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfGetInvoicelist: MutableList<InvoiceGetInvoiceList> = mutableListOf()
    lateinit var listAdapter: InvoiceAllListAdapter
    var tabId = ""
    var preference = InvioceSharedPreference()
    var listPos = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_invoice_tab_content, container, false)
        binding = ActivityInvoiceAllDataBinding.inflate(layoutInflater, container, false)
        tabId = requireArguments().getString("INVOICE_TAB_ID", "")

        binding.SwipeLay.setOnRefreshListener {
            listOfGetInvoicelist.clear()
            if (InvoiceUtils.isNetworkAvailable(requireContext())) {
                val InputMap = HashMap<String, Any>()
                InputMap["action"] = "getInvoiceList"
                InputMap["user_id"] = "" + preference.getString(requireContext(), "INVOICE_USER_ID")
                InputMap["type"] = "" + tabId
                println("InvoiceRequest - $_TAG == $InputMap")
                InvoiceUtils.loadingProgress(requireContext(), "Loading please wait....", false)
                viewModel.getInvoiceList(InputMap)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Check Your Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        if (InvoiceUtils.isNetworkAvailable(requireContext())) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getInvoiceList"
            InputMap["user_id"] = "" + preference.getString(requireContext(), "INVOICE_USER_ID")
            InputMap["type"] = "" + tabId
            println("InvoiceRequest - $_TAG == $InputMap")
            InvoiceUtils.loadingProgress(requireContext(), "Loading please wait....", false)
            viewModel.getInvoiceList(InputMap)
        } else {
            Toast.makeText(
                requireContext(),
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.tabRecycler.layoutManager = LinearLayoutManager(requireContext())
        listAdapter =
            InvoiceAllListAdapter(requireContext(), listOfGetInvoicelist, onDelete = { position ->
                listPos = position
                DeleteInvoice(listOfGetInvoicelist[position].invoiceId!!)
            },onEditClick={clickEditId,pos->
                val intent = Intent(requireContext(), InvoiceCreateFormActivity::class.java)
                intent.putExtra("INVOICE_EDIT_ID",clickEditId)
                val invoiceObject = listOfGetInvoicelist[pos] // Data class
                val jsonString = Gson().toJson(invoiceObject) // Convert to JSON
                preference.putString(activity,"INVOICE_PDF_LIST_DATA",jsonString)
                startActivity(intent)
            })
        binding.tabRecycler.adapter = listAdapter

        viewModel.getInvoiceList.observe(viewLifecycleOwner) { getInvoice ->
            InvoiceUtils.loadingDialog.dismiss()
            binding.SwipeLay.isRefreshing = false
            refreshRecentList = false
            println("invoiceList == ${getInvoice.size}")
            println("invoiceList listOfGetInvoicelist == ${listOfGetInvoicelist.size}")
            if (getInvoice.size != 0) {
                if (getInvoice[0].status.equals("failure")) {
                    binding.NoDataLay.visibility = View.VISIBLE
                    binding.tabRecycler.visibility = View.GONE
                }else{
                    binding.NoDataLay.visibility = View.GONE
                    binding.tabRecycler.visibility = View.VISIBLE
                    listOfGetInvoicelist.addAll(getInvoice)
                }
                listAdapter.notifyDataSetChanged()
            }
        }

        viewModel.deleteInvoiceData.observe(viewLifecycleOwner) { deleteRes ->
            if (deleteRes["status"] == "success") {
                Toast.makeText(requireContext(), "" + deleteRes["msg"], Toast.LENGTH_SHORT).show()
                listAdapter.notifyListData(listPos)
                println("invoiceListSize == "+listOfGetInvoicelist.size)
                refreshRecentList = true
                if (listOfGetInvoicelist.size == 0){
                    binding.NoDataLay.visibility = View.VISIBLE
                    binding.tabRecycler.visibility = View.GONE
                }else{
                    binding.NoDataLay.visibility = View.GONE
                    binding.tabRecycler.visibility = View.VISIBLE
                }
                listAdapter.notifyDataSetChanged()

            }
        }

        return binding.root

    }

    private fun DeleteInvoice(invoiceId: Int) {
        if (InvoiceUtils.isNetworkAvailable(requireContext())) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "deleteInvoiceDetails"
            InputMap["id"] = "" + invoiceId

            println("InvoiceRequest - $_TAG == $InputMap")
            InvoiceUtils.loadingProgress(requireContext(), "Loading please wait....", false)
            viewModel.deleteInvoiceData(InputMap)
        } else {
            Toast.makeText(
                requireContext(),
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(tabId: String) = InvoiceTabContentFragment().apply {
            arguments = Bundle().apply {
                putString("INVOICE_TAB_ID", tabId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (listOfGetInvoicelist.isNotEmpty()){
            listOfGetInvoicelist.clear()
        }
        if (refreshRecentList) {
            if (InvoiceUtils.isNetworkAvailable(requireContext())) {
                InvoiceUtils.loadingProgress(requireContext(), InvoiceUtils.messageLoading, false)
                    .show()
                val InputMap = HashMap<String, Any>()
                InputMap["action"] = "getInvoiceList"
                InputMap["user_id"] =
                    "" + preference.getString(requireContext(), "INVOICE_USER_ID")
                InputMap["type"] ="" + tabId

                println("InvoiceRequest - $_TAG == $InputMap")
                viewModel.getInvoiceList(InputMap)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Check Your Internet Connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}