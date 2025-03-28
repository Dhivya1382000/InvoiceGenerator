package com.nithra.invoice_generator_tool.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.activity.InvoiceAddItemFormActivity.Companion._TAG
import com.nithra.invoice_generator_tool.adapter.InvoiceRecentAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceHomeScreenBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceHomeScreen : AppCompatActivity() {

    lateinit var binding: ActivityInvoiceHomeScreenBinding
    private lateinit var invoicerecentadapter: InvoiceRecentAdapter
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfGetInvoicelist: MutableList<InvoiceGetInvoiceList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding = DataBindingUtil.setContentView<ActivityInvoiceHomeScreenBinding>(this, R.layout.activity_invoice_home_screen)

        setSupportActionBar(binding.toolBarTitle)

        // Enable Navigation Icon (Hamburger Menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.invoice_menu_icon) // Custom Image

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolBarTitle,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.createInvoiceLay.setOnClickListener {
            val intent = Intent(this@InvoiceHomeScreen, InvoiceCreateFormActivity::class.java)
            startActivity(intent)
        }
        binding.InvoiceCustomerLay.setOnClickListener {
            val intent = Intent(this@InvoiceHomeScreen, InvoiceNewCustomerFormActivity::class.java)
            startActivity(intent)
        }
        binding.InvoiceExpensesLay.setOnClickListener {
            val intent = Intent(this@InvoiceHomeScreen, InvoiceAddExpenseFormActivity::class.java)
            startActivity(intent)
        }
        // Initialize RecyclerView
        invoicerecentadapter = InvoiceRecentAdapter(
            this@InvoiceHomeScreen,listOfGetInvoicelist
        )
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(this@InvoiceHomeScreen)
            adapter = invoicerecentadapter
        }

        if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getInvoiceList"
            InputMap["user_id"] = "3"

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getInvoiceList(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceHomeScreen,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.getInvoiceList.observe(this) { getInvoice ->
            if (getInvoice.size != 0) {
                binding.RecentInvoice.visibility = View.VISIBLE
                listOfGetInvoicelist.addAll(getInvoice)
            } else {
                binding.RecentInvoice.visibility = View.GONE
            }

        }

        // Handle Menu Clicks
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_dash -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_business_detail -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    /*   Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                           .show()*/
                    val intent = Intent(
                        this@InvoiceHomeScreen,
                        InvoiceBusinessAndCustomerActivity::class.java
                    )
                    intent.putExtra("InvoicefromPage", "Business")
                    startActivity(intent)
                }

                R.id.nav_business_report -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()
                    val intent =
                        Intent(this@InvoiceHomeScreen, InvoioceBusinessReportActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_invoice -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    val intent =
                        Intent(this@InvoiceHomeScreen, InvoiceCreateFormActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_customer -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(
                        this@InvoiceHomeScreen,
                        InvoiceBusinessAndCustomerActivity::class.java
                    )
                    intent.putExtra("InvoicefromPage", "Customers")
                    startActivity(intent)
                }

                R.id.nav_expence -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.nav_item -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    /*   Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                           .show()*/
                    /*   val intent = Intent(this@InvoiceHomeScreen, InvoiceAddItemFormActivity::class.java)
                       startActivity(intent)*/
                    val intent = Intent(
                        this@InvoiceHomeScreen,
                        InvoiceBusinessAndCustomerActivity::class.java
                    )
                    intent.putExtra("InvoicefromPage", "Items")
                    startActivity(intent)
                }

                R.id.nav_privacy -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.nav_feedback -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.nav_share -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START) // Close drawer after selecting
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT).show()
                true
            }

            else -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }
}