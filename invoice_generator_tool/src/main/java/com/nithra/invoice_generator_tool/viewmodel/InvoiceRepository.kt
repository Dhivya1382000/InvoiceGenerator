package com.nithra.invoice_generator_tool.viewmodel

import com.nithra.invoice_generator_tool.model.InvoiceAddedList
import com.nithra.invoice_generator_tool.model.InvoiceGetBusinessDetail
import com.nithra.invoice_generator_tool.model.InvoiceGetClientDetails
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseDataList
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseList
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.model.InvoiceGetItemData
import com.nithra.invoice_generator_tool.model.InvoiceIndustrialAdd
import com.nithra.invoice_generator_tool.retrofit_interface.InvoiceApiInterface
import javax.inject.Inject

class InvoiceRepository @Inject constructor(private val api: InvoiceApiInterface) {

    suspend fun getBusinessDetail(requestInputMap: HashMap<String, Any>):InvoiceGetBusinessDetail {
        return api.getBusinessDetail(requestInputMap)
    }
    suspend fun getMasterArrayDetail(requestInputMap: HashMap<String, Any>):InvoiceGetDataMasterArray {
        return api.getMasterArray(requestInputMap)
    }

    suspend fun getCustomerDetail(requestInputMap: HashMap<String, Any>):InvoiceGetClientDetails {
        return api.getClientDetails(requestInputMap)
    }

    suspend fun getIndustries(requestInputMap: HashMap<String, Any>):InvoiceIndustrialAdd {
        return api.getIndustries(requestInputMap)
    }
    suspend fun getItemdata(requestInputMap: HashMap<String, Any>): InvoiceGetItemData {
        return api.getItemdata(requestInputMap)
    }

    suspend fun getInvoiceList(requestInputMap: HashMap<String, Any>):  MutableList<InvoiceGetInvoiceList> {
        return api.getInvoiceList(requestInputMap)
    }

    suspend fun getAddedList(requestInputMap: HashMap<String, Any>):  InvoiceAddedList {
        return api.addedList(requestInputMap)
    }

    suspend fun getExpenseData(requestInputMap: HashMap<String, Any>): InvoiceGetExpenseList {
        return api.addedexpenseList(requestInputMap)
    }

        suspend fun getExpenseList(requestInputMap: HashMap<String, Any>): InvoiceGetExpenseDataList {
        return api.expenseList(requestInputMap)
    }
}