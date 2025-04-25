package com.nithra.invoicegenerator

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class InvioceSharedPreference {
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun putString(context: Context?, text: String?, text1: String?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.putString(text, text1)
        editor.commit()
    }

    fun getIntads(context: Context, prefKey: String?): Int {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getInt(prefKey, 1)
    }

    fun getString(context: Context?, prefKey: String?): String {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val text: String? = sharedPreference.getString(prefKey, "")
        return text!!
    }

    fun removeString(context: Context, prefKey: String?) {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.remove(prefKey)
        editor.commit()
    }

    fun putInt(context: Context, text: String?, text1: Int) {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.putInt(text, text1)
        editor.commit()
    }

    fun getInt(context: Context, prefKey: String?): Int {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getInt(prefKey, 0)
    }

    fun removeInt(context: Context, prefKey: String?) {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.remove(prefKey)
        editor.commit()
    }

    fun putBoolean(context: Context?, text: String?, text1: Boolean?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.putBoolean(text, text1!!)
        editor.commit()
    }

    fun getBoolean(context: Context?, prefKey: String?): Boolean {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(prefKey, true)
    }

    fun getBoolean1(context: Context?, prefKey: String?): Boolean {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(prefKey, false)
    }

    fun removeBoolean(context: Context?, prefKey: String?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.remove(prefKey)
        editor.commit()
    }

    fun clearSharedPreference(context: Context?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.clear()
        editor.commit()
    }

    companion object {
        const val PREFS_NAME = "pref"
    }
}