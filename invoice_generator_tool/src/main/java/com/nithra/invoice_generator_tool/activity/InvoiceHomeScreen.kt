package com.nithra.invoice_generator_tool.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.adapter.InvoiceRecentAdapter
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceHomeScreenBinding
import com.nithra.invoice_generator_tool.databinding.ActivityInvoiceNewHomeScreenBinding
import com.nithra.invoice_generator_tool.databinding.InvoiceNavHeaderBinding
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.support.InvioceSharedPreference
import com.nithra.invoice_generator_tool.support.InvoiceUtils
import com.nithra.invoice_generator_tool.support.InvoiceUtils.getOpenActivity
import com.nithra.invoice_generator_tool.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class InvoiceHomeScreen : AppCompatActivity() {

    lateinit var binding: ActivityInvoiceNewHomeScreenBinding
    private lateinit var invoicerecentadapter: InvoiceRecentAdapter
    private val viewModel: InvoiceViewModel by viewModels()
    var listOfGetInvoicelist: MutableList<InvoiceGetInvoiceList> = mutableListOf()
    var AppLogin = 0
    var AppLoginClick = 0
    var AppLoginFrom = ""
    var OpenFromHome = 0
    var preference = InvioceSharedPreference()


    private var activityCommonLoginResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                println("activityResultLauncher ${result.data!!.getStringExtra("country_code")}")
                println("activityResultLauncher ${result.data!!.getStringExtra("user_mobile")}")
                println("activityResultLauncher ${result.data.toString()}")
                println("activityResultLauncher $result")

                val InvoiceUserName = preference.getString(this@InvoiceHomeScreen,"TC_USER_NAME")
                val InvoiceUserID = preference.getString(this@InvoiceHomeScreen,"TC_USER_ID")
                val InvoiceUserMobile = preference.getString(this@InvoiceHomeScreen,"TC_USER_MOBILE")

                if (InvoiceUserID.isNotEmpty()) {
                    preference.putString(
                        this@InvoiceHomeScreen,
                        "INVOICE_USER_NAME",
                        InvoiceUserName
                    )
                    preference.putString(
                        this@InvoiceHomeScreen,
                        "INVOICE_USER_ID",
                        InvoiceUserName
                    )
                    preference.putString(
                        this@InvoiceHomeScreen,
                        "INVOICE_USER_MOBILE",
                        InvoiceUserMobile
                    )

                    setContentView(binding.root)

                }else{
                    setContentView(binding.root)
                    finish()
                }
            }else{
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceNewHomeScreenBinding.inflate(layoutInflater)

        val bundle = intent.extras
        OpenFromHome = bundle!!.getInt("OpenFromHome", 0)
        if (bundle.getString("AppLoginFrom") != null) {
            AppLoginFrom = bundle.getString("AppLoginFrom")!!
            AppLogin = bundle.getInt("AppLogin", 0)
            preference.putString(this@InvoiceHomeScreen, "AS_APP_LOGIN_FROM", AppLoginFrom)
        }

        if (OpenFromHome == 0) {
            if (AppLogin == 1) {
                if (AppLoginFrom == "tamil_calendar") {
                    val otpStatus = preference.getString(
                        this@InvoiceHomeScreen,
                        "TC_USER_OTP_STATUS"
                    )
                    if (otpStatus == "VERIFIED") {
                        val UserName = preference.getString(
                            this@InvoiceHomeScreen,
                            "TC_USER_NAME"
                        )
                        val UserID = preference.getString(
                            this@InvoiceHomeScreen,
                            "TC_USER_ID"
                        )

                        val UserMobile = preference.getString(this@InvoiceHomeScreen,"TC_USER_MOBILE")

                        preference.putString(
                            this@InvoiceHomeScreen,
                            "INVOICE_USER_NAME",
                            UserName
                        )
                        preference.putString(
                            this@InvoiceHomeScreen,
                            "INVOICE_USER_ID",
                            UserID
                        )
                        preference.putString(
                            this@InvoiceHomeScreen,
                            "INVOICE_USER_MOBILE",
                            UserMobile
                        )
                        setContentView(binding.root)
                    } else {
                        val i = Intent(
                            this@InvoiceHomeScreen, getOpenActivity(
                                this@InvoiceHomeScreen, "nithra.invoice.common.login"
                            )
                        )
                        activityCommonLoginResultLauncher.launch(i)

                    }
                }else {
                    preference.putString(
                        this@InvoiceHomeScreen,
                        "INVOICE_USER_NAME",
                        ""
                    )
                    preference.putString(
                        this@InvoiceHomeScreen,
                        "INVOICE_USER_ID",
                        InvoiceUtils.userId
                    )
                    preference.putString(
                        this@InvoiceHomeScreen,
                        "TC_USER_MOBILE",
                       ""
                    )
                    setContentView(binding.root)
                }
            } else {
                preference.putString(
                    this@InvoiceHomeScreen,
                    "INVOICE_USER_NAME",
                    ""
                )
                preference.putString(
                    this@InvoiceHomeScreen,
                    "INVOICE_USER_ID",
                    InvoiceUtils.userId
                )
                preference.putString(
                    this@InvoiceHomeScreen,
                    "TC_USER_MOBILE",
                    ""
                )
                setContentView(binding.root)
            }
        }

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
        binding.InvoiceUserName.setText(" " + greetings + " " + preference.getString(this@InvoiceHomeScreen,"INVOICE_USER_NAME"))
        val navView = binding.navView  // if you're using ViewBinding for your Activity layout

// Access the header view
        val headerView = navView.getHeaderView(0)

        val headerBinding = InvoiceNavHeaderBinding.bind(headerView)

        headerBinding.InvoiceUserNameTxt.text = ""+preference.getString(this@InvoiceHomeScreen,"INVOICE_USER_NAME")
        headerBinding.InvoiceMobileTxt.text =""+preference.getString(this@InvoiceHomeScreen,"INVOICE_USER_MOBILE")

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
           /* if (!InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                Toast.makeText(
                    this@InvoiceHomeScreen,
                    "" + InvoiceUtils.messageNetCheck,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(this@InvoiceHomeScreen, InvoiceNewCustomerFormActivity::class.java)
            startActivity(intent)*/
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
        }
        binding.CardCreateInvoiceLay.setOnClickListener {
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

        binding.InvoiceAddItemsLay.setOnClickListener {
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
            InputMap["user_id"] = ""+preference.getString(this@InvoiceHomeScreen,"INVOICE_USER_ID")
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
            InputMap["user_id"] = ""+preference.getString(this@InvoiceHomeScreen,"INVOICE_USER_ID")
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

        binding.AddBusinesslay.setOnClickListener {
          /*  if (InvoiceUtils.isNetworkAvailable(this@InvoiceHomeScreen)) {
                val intent = Intent(
                    this@InvoiceHomeScreen,
                    InvoiceBusinessDetailFormActivity::class.java
                )
                intent.putExtra("InvoicefromPage", "Business")
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@InvoiceHomeScreen,
                    "" + InvoiceUtils.messageNetCheck,
                    Toast.LENGTH_SHORT
                ).show()
            }*/
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
        }

        binding.AddBusinessReportlay.setOnClickListener {
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

              /*  R.id.nav_expence -> {
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
                }*/

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