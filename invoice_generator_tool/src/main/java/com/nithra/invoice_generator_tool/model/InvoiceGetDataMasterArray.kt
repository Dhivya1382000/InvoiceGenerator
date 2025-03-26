package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceGetDataMasterArray {

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("state")
    @Expose
    var state: List<GetStateList>? = null

    class GetStateList {
        @SerializedName("id")
        @Expose
        var id: Int? = 0

        @SerializedName("english")
        @Expose
        var english: String? = null
    }

    @SerializedName("gst")
    @Expose
    var gst: List<GstList>? = null

    class GstList {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("gst")
        @Expose
        var gst: String? = null
    }

    @SerializedName("industrial")
    @Expose
    var industrial: List<GetIndustrialList>? = null

    class GetIndustrialList {

        @SerializedName("id")
        @Expose
        var id: Int? = 0

        @SerializedName("industrial")
        @Expose
        var industrial: String? = null
    }

    @SerializedName("company_details")
    @Expose
    var companyDetails: List<GetCompanyDetailList>? = null

    class GetCompanyDetailList {
        @SerializedName("user_id")
        @Expose
        var userId: String? = null

        @SerializedName("mobile")
        @Expose
        var mobile: String? = null

        @SerializedName("state_id")
        @Expose
        var stateId: Int? = 0

        @SerializedName("state")
        @Expose
        var state: String? = null

        @SerializedName("company_id")
        @Expose
        var companyId: Int? = 0

        @SerializedName("bussiness_name")
        @Expose
        var bussinessName: String? = null

        @SerializedName("email")
        @Expose
        var email: String? = null

        @SerializedName("bussiness_type")
        @Expose
        var bussinessType: Int? = 0

        @SerializedName("industrial_name")
        @Expose
        var industrialName: String? = ""

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

        @SerializedName("status")
        @Expose
        var status: String? = ""
    }

    @SerializedName("client_details")
    @Expose
    var clientDetails: List<GetClientDetails>? = null

    class GetClientDetails {

        @SerializedName("user_id")
        @Expose
        var userId: String? = null

        @SerializedName("mobile")
        @Expose
        var mobile: String? = null

        @SerializedName("id")
        @Expose
        var stateId: Int? = null

        @SerializedName("state")
        @Expose
        var state: String? = null

        @SerializedName("invoice_id")
        @Expose
        var clientId: Int? = 0

        @SerializedName("type")
        @Expose
        var type: Int? = 0

        @SerializedName("tax_id")
        @Expose
        var taxId: String? = ""

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("company_name")
        @Expose
        var companyName: String? = null

        @SerializedName("mobile1")
        @Expose
        var mobile1: String? = null

        @SerializedName("mobile2")
        @Expose
        var mobile2: String? = null

        @SerializedName("display_name")
        @Expose
        var displayName: String? = null

        @SerializedName("email")
        @Expose
        var email: String? = null

        @SerializedName("bussiness_mobile")
        @Expose
        var bussiness_mobile: String? = null

        @SerializedName("billing_address")
        @Expose
        var billingAddress: String? = null

        @SerializedName("shipping_address")
        @Expose
        var shippingAddress: String? = null

        @SerializedName("remark")
        @Expose
        var remark: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null
    }

    @SerializedName("item_list")
    @Expose
    var itemList: List<GetItemList>? = null

    class GetItemList {
        @SerializedName("status")
        @Expose
        var status: String? = ""

        @SerializedName("user_id")
        @Expose
        var userId: Int? = null

        @SerializedName("mobile")
        @Expose
        var mobile: String? = null

        @SerializedName("item_id")
        @Expose
        var itemId: Int? = null

        @SerializedName("item_name")
        @Expose
        var itemName: String? = null

        @SerializedName("amount")
        @Expose
        var amount: String? = null

        @SerializedName("qty_type")
        @Expose
        var qtyType: Int? = 0

        @SerializedName("qty")
        @Expose
        var qty: String? = null

        @SerializedName("tax")
        @Expose
        var tax: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("discount_type")
        @Expose
        var discountType: Int? = null

        @SerializedName("discount")
        @Expose
        var discount: Int? = null

        @SerializedName("total_amt")
        @Expose
        var totalAmt: Int? = 0
    }

    @SerializedName("unit_measurement")
    @Expose
    var unitMeasurement: List<GetUnitMeasure>? = null

    class GetUnitMeasure {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("label")
        @Expose
        var label: String? = null
    }

    @SerializedName("payment_status")
    @Expose
    var paymentStatus: List<GetpaymentStatus>? = null

    class GetpaymentStatus {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("label")
        @Expose
        var label: String? = null
    }

}


