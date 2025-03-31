package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class InvoicePieChart {
    @SerializedName("status")
    @Expose
    var status: String? = ""

    @SerializedName("msg")
    @Expose
    var msg: String? = ""

    @SerializedName("total_amount")
    @Expose
    var total_amount: Int? = 0

    @SerializedName("data")
    @Expose
    var data: List<InvoiceDataList>? = null

    class InvoiceDataList {
        @SerializedName("count")
        @Expose
        var count: Int? = null

        @SerializedName("amt_type")
        @Expose
        var amtType: Int? = 0

        @SerializedName("total_paid")
        @Expose
        var totalPaid: Int? = 0
    }

    @SerializedName("expenses")
    @Expose
    var expenses: ListOfExpenses? = null

    class ListOfExpenses {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("data")
        @Expose
        var data: DataListOfExpenses? = null

        class DataListOfExpenses {
            @SerializedName("total_count")
            @Expose
           var totalCount: Int? = null

            @SerializedName("total_amount")
            @Expose
           var totalAmount: Int? = null
        }

    }

}