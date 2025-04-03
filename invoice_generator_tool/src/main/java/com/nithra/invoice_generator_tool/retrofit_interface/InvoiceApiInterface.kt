package com.nithra.invoice_generator_tool.retrofit_interface

import com.nithra.invoice_generator_tool.model.InvoiceAddedList
import com.nithra.invoice_generator_tool.model.InvoiceGetBusinessDetail
import com.nithra.invoice_generator_tool.model.InvoiceGetClientDetails
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseDataList
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseList
import com.nithra.invoice_generator_tool.model.InvoiceGetHomeReport
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.model.InvoiceGetItemData
import com.nithra.invoice_generator_tool.model.InvoiceIndustrialAdd
import com.nithra.invoice_generator_tool.model.InvoicePieChart
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface InvoiceApiInterface {
    @POST("invoiceData")
    suspend fun getBusinessDetail(@Body requestMap: HashMap<String, Any>):  InvoiceGetBusinessDetail

    @POST("invoiceData")
    suspend fun getMasterArray(@Body requestMap: HashMap<String, Any>): InvoiceGetDataMasterArray

    @POST("invoiceData")
    suspend fun getClientDetails(@Body requestMap: HashMap<String, Any>): InvoiceGetClientDetails

    @POST("invoiceData")
    suspend fun getIndustries(@Body requestMap: HashMap<String, Any>): InvoiceIndustrialAdd

    @POST("invoiceData")
    suspend fun getItemdata(@Body requestMap: HashMap<String, Any>): InvoiceGetItemData

    @POST("invoiceData")
    suspend fun getInvoiceList(@Body requestMap: HashMap<String, Any>): MutableList<InvoiceGetInvoiceList>

    @Multipart
    @POST("invoiceData")
    suspend fun addedList(
        @PartMap requestMap: LinkedHashMap<String, RequestBody>,
        @Part pdfPart: MultipartBody.Part?, // ✅ File
    ): InvoiceAddedList

    @POST("invoiceData")
    suspend fun addedexpenseList(@Body requestMap: HashMap<String, Any>): InvoiceGetExpenseList

    @POST("invoiceData")
    suspend fun expenseList(@Body requestMap: HashMap<String, Any>): InvoiceGetExpenseDataList

    @POST("invoiceData")
    suspend fun pieChart(@Body requestMap: HashMap<String, Any>): InvoicePieChart

    @POST("invoiceData")
    suspend fun deleteData(@Body requestMap: HashMap<String, Any>): Map<String,Any>

    @POST("invoiceData")
    suspend fun homeReport(@Body requestMap: HashMap<String, Any>): InvoiceGetHomeReport
}