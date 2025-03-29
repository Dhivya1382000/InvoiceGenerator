package com.nithra.invoice_generator_tool.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.invoice_generator_tool.activity.InvoiceBusinessDetailFormActivity.Companion._TAG
import com.nithra.invoice_generator_tool.model.InvoiceAddedList
import com.nithra.invoice_generator_tool.model.InvoiceGetBusinessDetail
import com.nithra.invoice_generator_tool.model.InvoiceGetClientDetails
import com.nithra.invoice_generator_tool.model.InvoiceGetDataMasterArray
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseDataList
import com.nithra.invoice_generator_tool.model.InvoiceGetExpenseList
import com.nithra.invoice_generator_tool.model.InvoiceGetInvoiceList
import com.nithra.invoice_generator_tool.model.InvoiceGetItemData
import com.nithra.invoice_generator_tool.model.InvoiceIndustrialAdd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
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

    private val _getInvoiceList = MutableLiveData<MutableList<InvoiceGetInvoiceList>>()
    val getInvoiceList: LiveData<MutableList<InvoiceGetInvoiceList>> get() = _getInvoiceList

    private val _getAddedInvoiceList = MutableLiveData<InvoiceAddedList>()
    val getAddedInvoiceList: LiveData<InvoiceAddedList> get() = _getAddedInvoiceList

    private val _getAddedExpenseList = MutableLiveData<InvoiceGetExpenseList>()
    val getAddedExpenseList: LiveData<InvoiceGetExpenseList> get() = _getAddedExpenseList

    private val _getExpenseList = MutableLiveData<InvoiceGetExpenseDataList>()
    val getExpenseList: LiveData<InvoiceGetExpenseDataList> get() = _getExpenseList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun getBusinessDetail(map: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getBusinessDetail(map)
                _getBusinessDetailForm.value = response
                println("InvoiceResponse - $_TAG == $response")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }

    fun getOverAllMasterDetail(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getMasterArrayDetail(InputMap)
                _getMasterDetail.value = response
                println("InvoiceResponse - $_TAG == ${_getMasterDetail.value}")
            } catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }

    fun getCustomerDetail(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getCustomerDetail(InputMap)
                _getCustomerDetailForm.value = response
                println("InvoiceResponse - $_TAG == ${_getCustomerDetailForm.value}")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }

    fun addIndustrial(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getIndustries(InputMap)
                _addIndustries.value = response
                println("InvoiceResponse - $_TAG == ${_addIndustries.value}")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }

    fun addItemData(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getItemdata(InputMap)
                _getItemDetails.value = response
                println("InvoiceResponse - $_TAG == ${_getItemDetails.value}")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }
    fun getInvoiceList(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getInvoiceList(InputMap)
                _getInvoiceList.value = response
                println("InvoiceResponse - $_TAG == ${_getInvoiceList.value}")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }

    fun addInvoiceList(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getAddedList(InputMap)
                _getAddedInvoiceList.value = response
                println("InvoiceResponse - $_TAG == ${_getAddedInvoiceList.value}")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }

    fun addExpenseData(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getExpenseData(InputMap)
                _getAddedExpenseList.value = response
                println("InvoiceResponse - $_TAG == ${response}")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }

    fun getExpenseList(InputMap: HashMap<String, Any>) {
        viewModelScope.launch {
            try {
                val response = repository.getExpenseList(InputMap)
                _getExpenseList.value = response
                println("InvoiceResponse - $_TAG == ${response}")
            }  catch (e: SocketTimeoutException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: IOException) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }catch (e: Exception) {
                // Handle errors
                println("exception == ${e.toString()}")
                _errorMessage.value = e.message
            }
        }
    }
}