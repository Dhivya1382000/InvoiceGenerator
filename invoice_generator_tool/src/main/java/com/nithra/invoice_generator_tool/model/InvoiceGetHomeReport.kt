package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class  InvoiceGetHomeReport{
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("msg")
    @Expose
    var msg: String? = null

    @SerializedName("data")
    @Expose
    var data: List<GetHomeReport>? = null

    class GetHomeReport {
        @SerializedName("month_name")
        @Expose
        var monthName: String? = null

        @SerializedName("total_amount")
        @Expose
        var totalAmount: Int? = null

        @SerializedName("total_invoice")
        @Expose
        var totalInvoice: Int? = null
    }


}