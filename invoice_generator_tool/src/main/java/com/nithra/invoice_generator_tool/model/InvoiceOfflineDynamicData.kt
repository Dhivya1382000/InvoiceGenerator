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
    var hsn: String? = ""
    var sgst: Double? = 0.0
    var cgst: Double? = 0.0
    var Igst: String? = ""

    //Business Detail list
    var state_id: Int? = 0
    var s_code: String? = ""
    var state: String? = null
    var company_id: Int? = 0
    var bussiness_name: String? = null
    var email: String? = null
    var bussiness_type: Int? = 0
    var industrial_name: String? = ""
    var bussiness_mobile: String? = null
    var billing_address_1: String? = null
    var billing_address_2: String? = null
    var website: String? = null
    var tax_name: String? = null
    var tax_id: String? = null
    var bussiness_id: String? = null
    var bank_name: String? = ""
    var bank_acoount_number: String? = ""
    var micr_code: String? = ""
    var ifsc_code: String? = ""
    var bank_address: String? = ""
    var contact_person: String? = ""

    //Customer Detail list
    var stateId: Int? = null
    var invoice_id: Int? = 0
    var type: Int? = 0
    var taxId: String? = ""
    var name: String? = null
    var company_name: String? = null
    var mobile1: String? = null
    var mobile2: String? = null
    var display_name: String? = null
    var billing_address: String? = null
    var shipping_address: String? = null
    var remark: String? = null

}