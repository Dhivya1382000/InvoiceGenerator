package com.nithra.invoice_generator_tool.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessDetailFormActivity.Companion._TAG
import com.nithra.invoice_generator_tool.model.InvoiceGetBusinessDetail
import com.nithra.invoice_generator_tool.model.InvoiceGetClientDetails
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetItemData
import com.nithra.invoice_generator_tool.model.InvoiceIndustrialAdd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(private val repository: InvoiceRepository) : ViewModel()  {
    private val _getBusinessDetailForm = MutableLiveData<InvoiceGetBusinessDetail>()
    val getBusinessDetailForm: LiveData<InvoiceGetBusinessDetail> get() = _getBusinessDetailForm

    private val _getCustomerDetailForm = MutableLiveData<InvoiceGetClientDetails>()
    val getCustomerDetail: LiveData<InvoiceGetClientDetails> get() = _getCustomerDetailForm

    private val _getMasterDetail = MutableLiveData<InvoiceGetDataMasterArray>()
    val getMasterDetail: LiveData<InvoiceGetDataMasterArray> get() = _getMasterDetail

    private val _addIndustries = MutableLiveData<InvoiceIndustrialAdd>()
    val addIndustries: LiveData<InvoiceIndustrialAdd> get() = _addIndustries

    private val _getItemDetails = MutableLiveData<InvoiceGetItemData>()
    val getItemDetails: LiveData<InvoiceGetItemData> get() = _getItemDetails

    fun getBusinessDetail(map: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getBusinessDetail(map)
                _getBusinessDetailForm.value = response
                println("InvoiceResponse - $_TAG == $response")
            } catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
            }
        }
    }

    fun getOverAllMasterDetail(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getMasterArrayDetail(InputMap)
                _getMasterDetail.value = response
                println("InvoiceResponse - $_TAG == ${_getMasterDetail.value}")
            } catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
            }
        }
    }

    fun getCustomerDetail(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getCustomerDetail(InputMap)
                _getCustomerDetailForm.value = response
                println("InvoiceResponse - $_TAG == ${_getCustomerDetailForm.value}")
            } catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
            }
        }
    }

    fun addIndustrial(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getIndustries(InputMap)
                _addIndustries.value = response
                println("InvoiceResponse - $_TAG == ${_addIndustries.value}")
            } catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
            }
        }
    }

    fun getItemDetails(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getItemdata(InputMap)
                _getItemDetails.value = response
                println("InvoiceResponse - $_TAG == ${_addIndustries.value}")
            } catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
            }
        }
    }
}