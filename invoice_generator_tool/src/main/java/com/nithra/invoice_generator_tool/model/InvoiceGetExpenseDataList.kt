package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceGetExpenseDataList {

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("data")
    @Expose
    var data: List<DataList>? = null

    class DataList {
        @SerializedName("user_id")
        @Expose
        var userId: Int? = null

        @SerializedName("mobile")
        @Expose
        var mobile: String? = null

        @SerializedName("invoice_id")
        @Expose
        var invoiceId: Int? = null

        @SerializedName("date")
        @Expose
        var date: String? = null

        @SerializedName("item_name")
        @Expose
        var itemName: String? = null

        @SerializedName("amount")
        @Expose
        var amount: String? = null

        @SerializedName("inv_number")
        @Expose
        var invNumber: String? = null

        @SerializedName("seller_name")
        @Expose
        var sellerName: String? = null

        @SerializedName("remark")
        @Expose
        var remark: String? = null

        @SerializedName("bussiness_name")
        @Expose
        var bussinessName: String? = null
    }

}