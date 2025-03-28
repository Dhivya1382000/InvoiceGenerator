package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceAddedList {

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("msg")
    @Expose
    var msg: String? = null

    @SerializedName("data")
    @Expose
    var data: listData? = null

    class listData {
        @SerializedName("invoice_details")
        @Expose
        var invoiceDetails: List<GetAddedInvoiceDetail>? = null

        @SerializedName("items")
        @Expose
        var items: List<InvoiceGetDataMasterArray.GetItemList>? = null

        class GetAddedInvoiceDetail {
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
            var bussinessName: String? = null

            @SerializedName("bussiness_id")
            @Expose
            var bussinessId: Int? = null

            @SerializedName("client_name")
            @Expose
            var clientName: String? = null

            @SerializedName("client_id")
            @Expose
            var clientId: Int? = null

            @SerializedName("invoice_number")
            @Expose
            var invoiceNumber: String? = null

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
            var paidAmt: Int? = null

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
        }

    }
}

