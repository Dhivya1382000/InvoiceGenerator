package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceGetBusinessDetail {

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("msg")
    @Expose
    var msg: String? = null

    @SerializedName("data")
    @Expose
    var data: List<DataBusiness>? = null
}

class DataBusiness {
    @SerializedName("user_id")
    @Expose
    var userId: String? = null

    @SerializedName("company_id")
    @Expose
    var companyId: String? = null

    @SerializedName("mobile")
    @Expose
    var mobile: String? = null

    @SerializedName("bussiness_name")
    @Expose
    var bussinessName: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("bussiness_mobile")
    @Expose
    var bussinessMobile: String? = null

    @SerializedName("billing_address_1")
    @Expose
    var billingAddress1: String? = null

    @SerializedName("billing_address_2")
    @Expose
    var billingAddress2: String? = null

    @SerializedName("website")
    @Expose
    var website: String? = null

    @SerializedName("tax_name")
    @Expose
    var taxName: String? = null

    @SerializedName("tax_id")
    @Expose
    var taxId: String? = null

    @SerializedName("bussiness_id")
    @Expose
    var bussinessId: String? = null
}
