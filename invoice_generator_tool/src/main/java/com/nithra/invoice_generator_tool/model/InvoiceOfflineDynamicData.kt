package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceOfflineDynamicData {
    var status: String? = ""
    var user_id: Int? = null
    var mobile: String? = null
    var item_id: Int? = null
    var item_name: String? = ""
    var amount: String? = null
    var qty_type: Int? = 0
    var qty: String? = null
    var tax: String? = null
    var description: String? = null
    var discount_type: Int? = null
    var discount: Int? = null
    var total_amt: String? = ""
}