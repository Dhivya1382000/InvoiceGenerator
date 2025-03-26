package com.nithra.invoice_generator_tool.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InvoiceGetItemData {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("msg")
    @Expose
    var msg: String? = null

    @SerializedName("data")
    @Expose
    var data: List<GetItemDataList>? = null

    class GetItemDataList{
        @SerializedName("user_id")
        @Expose
        var userId: Int? = null

        @SerializedName("mobile")
        @Expose
        var mobile: String? = null

        @SerializedName("invoice_id")
        @Expose
        var invoiceId: Int? = null

        @SerializedName("item_name")
        @Expose
        var itemName: String? = null

        @SerializedName("amount")
        @Expose
        var amount: String? = null

        @SerializedName("qty_type")
        @Expose
        var qtyType: Int? = null

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
    }
}