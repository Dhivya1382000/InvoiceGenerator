package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class InvoiceGetClientDetails {

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("msg")
    @Expose
    var msg: String? = null

    @SerializedName("data")
    @Expose
    var data: List<GetClientData>? = null

    class GetClientData {
        @SerializedName("user_id")
        @Expose
        val userId: Int? = null

        @SerializedName("mobile")
        @Expose
        val mobile: String? = null

        @SerializedName("id")
        @Expose
        val id: Int? = null

        @SerializedName("state")
        @Expose
        val state: String? = null

        @SerializedName("invoice_id")
        @Expose
        val invoiceId: Int? = null

        @SerializedName("type")
        @Expose
        val type: Int? = null

        @SerializedName("name")
        @Expose
        val name: String? = null

        @SerializedName("company_name")
        @Expose
        val companyName: String? = null

        @SerializedName("mobile1")
        @Expose
        val mobile1: String? = null

        @SerializedName("mobile2")
        @Expose
        val mobile2: String? = null

        @SerializedName("display_name")
        @Expose
        val displayName: String? = null

        @SerializedName("email")
        @Expose
        val email: String? = null

        @SerializedName("bussiness_mobile")
        @Expose
        val bussinessMobile: String? = null

        @SerializedName("billing_address")
        @Expose
        val billingAddress: String? = null

        @SerializedName("shipping_address")
        @Expose
        val shippingAddress: String? = null

        @SerializedName("remark")
        @Expose
        val remark: String? = null
    }

}