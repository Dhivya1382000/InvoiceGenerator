package com.nithra.invoice_generator_tool.support

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import com.airbnb.lottie.LottieAnimationView
import com.nithra.invoice_generator_tool.R

object InvoiceUtils {
    val messageNetCheck = "தங்களின் இணையதள சேவையை சரிபார்க்கவும்"

    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }
    fun getOpenActivity(context: Context, MetaDataFrom: String): Class<*>? {
        val activityToStart = "" + getClassName(context,MetaDataFrom)
        println("== book activity : $activityToStart")
        return try {
            Class.forName(activityToStart)
        } catch (ignored: ClassNotFoundException) {
            null
        }
    }
    fun getAndroidId(context: Context?): String {
        return Settings.Secure.getString(
            context?.contentResolver, Settings.Secure.ANDROID_ID
        )
    }

    fun versioncodeGet(context: Context?): Int {
        var pInfo: PackageInfo? = null
        try {
            pInfo = context!!.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {

            e.printStackTrace()
        }
        return PackageInfoCompat.getLongVersionCode(pInfo!!).toInt()
    }

    fun getClassName(context: Context, MetaDataFrom: String): String? {
        try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val bundle = ai.metaData
            return bundle.getString(MetaDataFrom)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    lateinit var loadingDialog: Dialog

    fun loadingProgress(context: Context,  boolean: Boolean): Dialog {

        loadingDialog = Dialog(
            context,
            android.R.style.Theme_DeviceDefault_Dialog_MinWidth
        )
        loadingDialog.setContentView(R.layout.invoice_common_loading)
        if (loadingDialog.window != null) {
            loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
       // val progressBar: ProgressBar = loadingDialog.findViewById(R.id.progressBar)
        val progressBar: LottieAnimationView = loadingDialog.findViewById(R.id.lottieAnimationView)

        loadingDialog.setCancelable(boolean)
        loadingDialog.setCanceledOnTouchOutside(boolean)
        return loadingDialog
    }

}