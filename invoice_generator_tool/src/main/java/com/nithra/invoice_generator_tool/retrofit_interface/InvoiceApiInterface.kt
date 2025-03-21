package com.nithra.invoice_generator_tool.retrofit_interface

import com.nithra.invoice_generator_tool.model.InvoiceGetBusinessDetail
import com.nithra.invoice_generator_tool.model.InvoiceGetClientDetails
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface InvoiceApiInterface {
    @POST("invoiceData")  // Example endpoint
    suspend fun getBusinessDetail(@Body requestMap: HashMap<String, Any>):  InvoiceGetBusinessDetail

    @POST("invoiceData")  // Example endpoint
    suspend fun getMasterArray(@Body requestMap: HashMap<String, Any>): InvoiceGetDataMasterArray

    @POST("invoiceData")  // Example endpoint
    suspend fun getClientDetails(@Body requestMap: HashMap<String, Any>): InvoiceGetClientDetails
}