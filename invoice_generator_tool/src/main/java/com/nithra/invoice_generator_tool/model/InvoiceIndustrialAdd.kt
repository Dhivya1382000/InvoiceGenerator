package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName



class InvoiceIndustrialAdd {

    @SerializedName("status")
    @Expose
     var status: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("industrial")
    @Expose
     var industrial: List<AddIndustrial>? = null

    class AddIndustrial {

        @SerializedName("id")
        @Expose
         var id: Int? = null

        @SerializedName("industrial")
        @Expose
         var industrial: String? = null
    }

}