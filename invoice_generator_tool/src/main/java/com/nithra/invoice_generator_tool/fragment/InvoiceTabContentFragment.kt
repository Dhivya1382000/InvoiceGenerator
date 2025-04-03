package com.nithra.invoice_generator_tool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessAndCustomerActivity
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessAndCustomerActivity.Companion
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen.Companion._TAG
import com.nithra.invoice_generator_tool.adapter.InvoiceAllListAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceAllDataBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                InputMap["user_id"] = "1227994"
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
            InputMap["user_id"] = "1227994"
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
        listAdapter = InvoiceAllListAdapter(requireContext(), listOfGetInvoicelist)
        binding.tabRecycler.adapter = listAdapter

        viewModel.getInvoiceList.observe(viewLifecycleOwner) { getInvoice ->
            InvoiceUtils.loadingDialog.dismiss()
            binding.SwipeLay.isRefreshing = false
            println("invoiceList == ${getInvoice.size}")
            if (getInvoice.size != 0) {
                binding.NoDataLay.visibility = View.GONE
                binding.tabRecycler.visibility = View.VISIBLE
                listOfGetInvoicelist.addAll(getInvoice)
                if (getInvoice[0].status.equals("failure")) {
                    binding.NoDataLay.visibility = View.VISIBLE
                    binding.tabRecycler.visibility = View.GONE
                }
                listAdapter.notifyDataSetChanged()
            }
        }

        return binding.root

    }

    companion object {

        @JvmStatic
        fun newInstance(tabId: String) = InvoiceTabContentFragment().apply {
            arguments = Bundle().apply {
                putString("INVOICE_TAB_ID", tabId)
            }
        }
    }
}