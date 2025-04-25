package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceGetInvoiceList {
    @SerializedName("user_id")
    @Expose
    var userId: Int? = null

    @SerializedName("mobile")
    @Expose
    var mobile: String? = null

    @SerializedName("invoice_id")
    @Expose
    var invoiceId: Int? = null

    @SerializedName("bussiness_name")
    @Expose
    var bussinessName: String? = ""

    @SerializedName("bussiness_id")
    @Expose
    var bussinessId: Int? = null

    @SerializedName("client_name")
    @Expose
    var clientName: String? = ""

    @SerializedName("client_id")
    @Expose
    var clientId: Int? = null

    @SerializedName("invoice_number")
    @Expose
    var invoiceNumber: String? = ""

    @SerializedName("order_no")
    @Expose
    var orderNo: String? = null

    @SerializedName("invoice_date")
    @Expose
    var invoiceDate: String? = null

    @SerializedName("amt_type")
    @Expose
    var amtType: Int? = null

    @SerializedName("paid_amt")
    @Expose
    var paidAmt: Double? = null

    @SerializedName("due_date")
    @Expose
    var dueDate: String? = null

    @SerializedName("remark")
    @Expose
    var remark: String? = null

    @SerializedName("due_term")
    @Expose
    var dueTerm: Int? = null

    @SerializedName("terms_condition")
    @Expose
    var termsCondition: String? = null

    @SerializedName("pdf")
    @Expose
    var pdf: String? = null

    @SerializedName("total_invoice_amt")
    @Expose
    var totalInvoiceAmt: String? = ""

    @SerializedName("discount_amt")
    @Expose
    var discountAmt: String? = ""

    @SerializedName("status")
    @Expose
    var status: String? = ""

    @SerializedName("auto_entry")
    @Expose
    var autoEntry: Int? = 0

    @SerializedName("item")
    @Expose
    var item: List<GetItemList>? = null

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


    }
}