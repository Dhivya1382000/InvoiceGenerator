package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceTypeOfAllListBinding
import com.nithra.invoice_generator_tool.fragment.InvoiceTabContentFragment
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InvoiceTypeOfAllListActivity : AppCompatActivity() {
    lateinit var binding: ActivityInvoiceTypeOfAllListBinding
    private lateinit var adapter: InvoiceDynamicTabAdapter
    private val tabList: MutableList<InvoiceGetDataMasterArray.GetpaymentStatus> = mutableListOf()
    private val viewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceTypeOfAllListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolBarTitle)
        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_back_arrow) // Custom Image

        binding.toolBarTitle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

// Set up ViewPager2 with adapter
        adapter = InvoiceDynamicTabAdapter(this@InvoiceTypeOfAllListActivity, tabList)
        binding.InvoiceViewPager.adapter = adapter

        TabLayoutMediator(binding.InvoiceTabLayout, binding.InvoiceViewPager) { tab, position ->
            tab.customView = getTabView(position)
        }.attach()

        // Load data dynamically
        loadDynamicTabs()

        viewModel.getMasterDetail.observe(this@InvoiceTypeOfAllListActivity) { getMasterDetail ->
            InvoiceUtils.loadingDialog.dismiss()
            tabList.addAll(getMasterDetail.paymentStatus!!)
            adapter.notifyDataSetChanged()
        }

        binding.InvoiceTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val textView = tab?.customView?.findViewById<TextView>(R.id.tabText)
                val TabCard = tab?.customView?.findViewById<CardView>(R.id.TabCard)
                TabCard?.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this@InvoiceTypeOfAllListActivity,
                        R.color.invoice_blue
                    )
                )
                textView?.setTextColor(
                    ContextCompat.getColor(
                        this@InvoiceTypeOfAllListActivity,
                        R.color.invoice_white
                    )
                )

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val textView = tab?.customView?.findViewById<TextView>(R.id.tabText)
                val TabCard = tab?.customView?.findViewById<CardView>(R.id.TabCard)
                TabCard?.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this@InvoiceTypeOfAllListActivity,
                        android.R.color.white
                    )
                )
                textView?.setTextColor(
                    ContextCompat.getColor(
                        this@InvoiceTypeOfAllListActivity,
                        R.color.invoice_black
                    )
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }

    private fun getTabView(position: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.invoice_custom_tab, null)
        val tabText = view.findViewById<TextView>(R.id.tabText)

        tabText.text = "" + tabList[position].label

        return view
    }

    private fun loadDynamicTabs() {
        if (InvoiceUtils.isNetworkAvailable(this@InvoiceTypeOfAllListActivity)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getMaster"
            InputMap["user_id"] = ""+InvoiceUtils.userId

            println("InvoiceRequest - $_TAG == $InputMap")
            InvoiceUtils.loadingProgress(
                this@InvoiceTypeOfAllListActivity,
                InvoiceUtils.messageLoading, false
            ).show()
            viewModel.getOverAllMasterDetail(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceTypeOfAllListActivity,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        var _TAG = "InvoiceTypeOfAllListActivity"
    }
}

class InvoiceDynamicTabAdapter(
    fragment: InvoiceTypeOfAllListActivity,
    private val tabs: MutableList<InvoiceGetDataMasterArray.GetpaymentStatus>
) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return InvoiceTabContentFragment.newInstance("" + tabs[position].id);
    }

    override fun getItemCount(): Int {
        return tabs.size
    }
}