package com.nithra.invoice_generator_tool.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceRecentAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceHomeScreenBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

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

        val greetings = "" + getGreetingMessage()
        binding.InvoiceUserName.setText(" " + greetings + " " + "Dhivya ,")

        binding.createInvoiceLay.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                Toast.makeText(
                    this@InvoiceHomeScreen,
                    "" + InvoiceUtils.messageNetCheck,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(this@InvoiceHomeScreen, InvoiceCreateFormActivity::class.java)
            startActivity(intent)
        }
        binding.InvoiceCustomerLay.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                Toast.makeText(
                    this@InvoiceHomeScreen,
                    "" + InvoiceUtils.messageNetCheck,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(this@InvoiceHomeScreen, InvoiceNewCustomerFormActivity::class.java)
            startActivity(intent)
        }
        binding.InvoiceExpensesLay.setOnClickListener {
            if (!InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                Toast.makeText(
                    this@InvoiceHomeScreen,
                    "" + InvoiceUtils.messageNetCheck,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(this@InvoiceHomeScreen, InvoiceAddExpenseFormActivity::class.java)
            startActivity(intent)
        }
        // Initialize RecyclerView
        invoicerecentadapter = InvoiceRecentAdapter(
            this@InvoiceHomeScreen, listOfGetInvoicelist
        )
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(this@InvoiceHomeScreen)
            adapter = invoicerecentadapter
        }

        if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
//            InvoiceUtils.loadingProgress(this@InvoiceHomeScreen,InvoiceUtils.messageLoading,false).show()
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "homeReport"
            InputMap["user_id"] = "1227994"
            InputMap["type"] = "1"
            InputMap["year"] = "2025"

            println("InvoiceRequest - $_TAG == $InputMap")
            viewModel.getHomeReport(InputMap)
        } else {
            Toast.makeText(
                this@InvoiceHomeScreen,
                "Check Your Internet Connection",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.getHomeChart.observe(this@InvoiceHomeScreen) { getHomeReport ->
            val composeView = findViewById<ComposeView>(R.id.chartComposeView)
            composeView.setContent {
                //BarChartView(getHomeReport)
            }
        }

        if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
            InvoiceUtils.loadingProgress(this@InvoiceHomeScreen, InvoiceUtils.messageLoading, false)
                .show()
            val InputMap = HashMap<String, Any>()
            InputMap["action"] = "getInvoiceList"
            InputMap["user_id"] = "1227994"
            InputMap["type"] = "0"

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
            InvoiceUtils.loadingDialog.dismiss()
            println("getinvoice === ${getInvoice.size}")
            println("getinvoice === ${getInvoice[0].status}")
            if (getInvoice.size != 0) {
                if (getInvoice[0].status.equals("failure")) {
                    binding.RecentInvoice.visibility = View.GONE
                    listOfGetInvoicelist.addAll(getInvoice)
                } else {
                    binding.RecentInvoice.visibility = View.VISIBLE
                    listOfGetInvoicelist.addAll(getInvoice)
                }

            } else {
                binding.RecentInvoice.visibility = View.GONE
            }
            binding.seeAllText.setOnClickListener {
                val intent =
                    Intent(this@InvoiceHomeScreen, InvoiceTypeOfAllListActivity::class.java)
                startActivity(intent)
            }
        }

        // Handle Menu Clicks
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_dash -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_business_detail -> {
                    /*   Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                           .show()*/
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                        val intent = Intent(
                            this@InvoiceHomeScreen,
                            InvoiceBusinessAndCustomerActivity::class.java
                        )
                        intent.putExtra("InvoicefromPage", "Business")
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@InvoiceHomeScreen,
                            "" + InvoiceUtils.messageNetCheck,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                }

                R.id.nav_business_report -> {
                    /*Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()*/
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                        val intent = Intent(
                            this@InvoiceHomeScreen,
                            InvoioceBusinessReportActivity::class.java
                        )
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@InvoiceHomeScreen,
                            "" + InvoiceUtils.messageNetCheck,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                }

                R.id.nav_invoice -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                        val intent =
                            Intent(this@InvoiceHomeScreen, InvoiceTypeOfAllListActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@InvoiceHomeScreen,
                            "" + InvoiceUtils.messageNetCheck,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_customer -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                        val intent = Intent(
                            this@InvoiceHomeScreen,
                            InvoiceBusinessAndCustomerActivity::class.java
                        )
                        intent.putExtra("InvoicefromPage", "Customers")
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@InvoiceHomeScreen,
                            "" + InvoiceUtils.messageNetCheck,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_expence -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                        val intent = Intent(
                            this@InvoiceHomeScreen,
                            InvoiceBusinessAndCustomerActivity::class.java
                        )
                        intent.putExtra("InvoicefromPage", "Expense")
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@InvoiceHomeScreen,
                            "" + InvoiceUtils.messageNetCheck,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_item -> {
                    if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                        val intent = Intent(
                            this@InvoiceHomeScreen,
                            InvoiceBusinessAndCustomerActivity::class.java
                        )
                        intent.putExtra("InvoicefromPage", "Items")
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@InvoiceHomeScreen,
                            "" + InvoiceUtils.messageNetCheck,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.drawerLayout.closeDrawer(GravityCompat.START)

                }

                R.id.nav_privacy -> {
                    /*  Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                          .show()*/
                    val intent = Intent(
                        this@InvoiceHomeScreen,
                        InvoiceWebviewLoadActivity::class.java
                    )
                    startActivity(intent)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }

                /*R.id.nav_feedback -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()
                }

                R.id.nav_share -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    Toast.makeText(this@InvoiceHomeScreen, "clickDashBoard", Toast.LENGTH_SHORT)
                        .show()
                }*/
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START) // Close drawer after selecting
            true
        }
    }

    fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when {
            hour in 5..11 -> "Good Morning ☀️"
            hour in 12..16 -> "Good Afternoon 🌞"
            hour in 17..20 -> "Good Evening 🌇"
            else -> "Good Night 🌙"
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

    companion object {
        var _TAG = "InvoiceHomeScreen"
    }
}