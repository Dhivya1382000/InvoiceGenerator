package com.nithra.invoice_generator_tool.viewmodel

import com.nithra.invoice_generator_tool.model.InvoiceGetBusinessDetail
import com.nithra.invoice_generator_tool.model.InvoiceGetClientDetails
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
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

}