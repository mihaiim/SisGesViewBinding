package com.mihaiim.sisgesviewbinding.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihaiim.sisgesviewbinding.domain.model.Event
import com.mihaiim.sisgesviewbinding.domain.model.Product
import com.mihaiim.sisgesviewbinding.domain.model.Storage
import com.mihaiim.sisgesviewbinding.domain.repository.ProductRepository
import com.mihaiim.sisgesviewbinding.domain.repository.StorageRepository
import com.mihaiim.sisgesviewbinding.others.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storageRepository: StorageRepository,
) : ViewModel() {

    private val _productsListEvent = MutableLiveData<Event<List<Product>>>()
    val productsListEvent: LiveData<Event<List<Product>>> get() = _productsListEvent
    private val _productDetailsEvent = MutableLiveData<Event<Product>>()
    val productDetailsEvent: LiveData<Event<Product>> get() = _productDetailsEvent
    private val _positionsEvent = MutableLiveData<Event<List<Storage>>>()
    val positionsEvent: LiveData<Event<List<Storage>>> get() = _positionsEvent
    private val _failEvent = MutableLiveData<Event<String>>()
    val failEvent: LiveData<Event<String>> get() = _failEvent

    fun getProductsList(searchTerm: String) = viewModelScope.launch {
        when (val result = productRepository.getMinifiedProductsByName(searchTerm.trim())) {
            is Result.Success -> _productsListEvent.value = Event(result.data)
            is Result.Error -> _failEvent.value = Event(result.message)
        }
    }

    fun getProductDetails(productCode: String) = viewModelScope.launch {
        when (val result = productRepository.getProductDetails(productCode)) {
            is Result.Success -> _productDetailsEvent.value = Event(result.data)
            is Result.Error -> _failEvent.value = Event(result.message)
        }
    }

    fun getProductPositions(productCode: String) = viewModelScope.launch {
        when (val result = storageRepository.getProductPositionsAndQuantities(productCode)) {
            is Result.Success -> _positionsEvent.value = Event(result.data)
            is Result.Error -> _failEvent.value = Event(result.message)
        }
    }
}