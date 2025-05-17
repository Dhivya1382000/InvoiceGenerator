package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceGetDataMasterArray {

    @SerializedName("status")
    @Expose
    var status: String? = ""

    @SerializedName("state")
    @Expose
    var state: List<GetStateList>? = null

    class GetStateList {
        @SerializedName("id")
        @Expose
        var id: Int? = 0

        @SerializedName("english")
        @Expose
        var english: String? = ""

        @SerializedName("status")
        @Expose
        var status: String? = ""

        @SerializedName("s_code")
        @Expose
        var stateCode: String? = ""
    }

    @SerializedName("gst")
    @Expose
    var gst: List<GstList>? = null

    class GstList {
        @SerializedName("id")
        @Expose
        var id: Int? = 0

        @SerializedName("gst")
        @Expose
        var gst: String? = ""
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
        var industrial: String? = ""
    }

    @SerializedName("company_details")
    @Expose
    var companyDetails: List<GetCompanyDetailList>? = null

    class GetCompanyDetailList {
        @SerializedName("user_id")
        @Expose
        var userId: String? = ""

        @SerializedName("mobile")
        @Expose
        var mobile: String? = ""

        @SerializedName("state_id")
        @Expose
        var stateId: Int? = 0

        @SerializedName("state")
        @Expose
        var state: String? = ""

        @SerializedName("company_id")
        @Expose
        var companyId: Int? = 0

        @SerializedName("bussiness_name")
        @Expose
        var bussinessName: String? = ""

        @SerializedName("email")
        @Expose
        var email: String? = ""

        @SerializedName("bussiness_type")
        @Expose
        var bussinessType: Int? = 0

        @SerializedName("industrial_name")
        @Expose
        var industrialName: String? = ""

        @SerializedName("bussiness_mobile")
        @Expose
        var bussinessMobile: String? = ""

        @SerializedName("billing_address_1")
        @Expose
        var billingAddress1: String? = ""

        @SerializedName("billing_address_2")
        @Expose
        var billingAddress2: String? = ""

        @SerializedName("website")
        @Expose
        var website: String? = ""

        @SerializedName("tax_name")
        @Expose
        var taxName: String? = ""

        @SerializedName("tax_id")
        @Expose
        var taxId: String? = ""

        @SerializedName("bussiness_id")
        @Expose
        var bussinessId: String? = ""

        @SerializedName("bank_name")
        @Expose
        var bankName: String? = ""

        @SerializedName("bank_acoount_number")
        @Expose
        var bankAcoountNumber: String? = ""

        @SerializedName("micr_code")
        @Expose
        var micrCode: String? = ""

        @SerializedName("ifsc_code")
        @Expose
        var ifscCode: String? = ""

        @SerializedName("bank_address")
        @Expose
        var bankAddress: String? = ""

        @SerializedName("contact_person")
        @Expose
        var contactPerson: String? = ""

        @SerializedName("type")
        @Expose
        var type: Int? = 0

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
        var userId: String? = ""

        @SerializedName("mobile")
        @Expose
        var mobile: String? = ""

        @SerializedName("id")
        @Expose
        var stateId: Int? = 0

        @SerializedName("state")
        @Expose
        var state: String? = ""

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
        var name: String? = ""

        @SerializedName("company_name")
        @Expose
        var companyName: String? = ""

        @SerializedName("mobile1")
        @Expose
        var mobile1: String? = ""

        @SerializedName("mobile2")
        @Expose
        var mobile2: String? = ""

        @SerializedName("display_name")
        @Expose
        var displayName: String? = ""

        @SerializedName("email")
        @Expose
        var email: String? = ""

        @SerializedName("bussiness_mobile")
        @Expose
        var bussiness_mobile: String? = ""

        @SerializedName("billing_address")
        @Expose
        var billingAddress: String? = ""

        @SerializedName("shipping_address")
        @Expose
        var shippingAddress: String? = ""

        @SerializedName("remark")
        @Expose
        var remark: String? = ""

        @SerializedName("status")
        @Expose
        var status: String? = ""
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
        var userId: Int? = 0

        @SerializedName("mobile")
        @Expose
        var mobile: String? = ""

        @SerializedName("item_id")
        @Expose
        var itemId: Int? = 0

        @SerializedName("item_name")
        @Expose
        var itemName: String? = ""

        @SerializedName("amount")
        @Expose
        var amount: String? = ""

        @SerializedName("qty_type")
        @Expose
        var qtyType: Int? = 0

        @SerializedName("qty")
        @Expose
        var qty: String? = ""

        @SerializedName("tax")
        @Expose
        var tax: String? = ""

        @SerializedName("hsn")
        @Expose
        var hsn: String? = ""

        @SerializedName("description")
        @Expose
        var description: String? = ""

        @SerializedName("discount_type")
        @Expose
        var discountType: Int? = 0

        @SerializedName("discount")
        @Expose
        var discount: Int? = 0

        @SerializedName("total_amt")
        @Expose
        var totalAmt: String? = ""

        @SerializedName("qty_type_label")
        @Expose
        var qtyTypeLabel: String? = ""

    }

    @SerializedName("unit_measurement")
    @Expose
    var unitMeasurement: List<GetUnitMeasure>? = null

    class GetUnitMeasure {
        @SerializedName("id")
        @Expose
        var id: Int? = 0

        @SerializedName("label")
        @Expose
        var label: String? = ""
    }

    @SerializedName("payment_status")
    @Expose
    var paymentStatus: List<GetpaymentStatus>? = null

    class GetpaymentStatus {
        @SerializedName("id")
        @Expose
        var id: Int? = 0

        @SerializedName("label")
        @Expose
        var label: String? = ""
    }

}


