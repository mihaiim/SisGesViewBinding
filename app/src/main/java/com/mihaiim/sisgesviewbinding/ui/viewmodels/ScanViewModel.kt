package com.mihaiim.sisgesviewbinding.ui.viewmodels

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.domain.model.Event
import com.mihaiim.sisgesviewbinding.domain.model.Product
import com.mihaiim.sisgesviewbinding.domain.repository.AdministrationRepository
import com.mihaiim.sisgesviewbinding.domain.repository.ProductRepository
import com.mihaiim.sisgesviewbinding.domain.repository.StoragePositionRepository
import com.mihaiim.sisgesviewbinding.domain.repository.StorageRepository
import com.mihaiim.sisgesviewbinding.others.AdministrationEnum
import com.mihaiim.sisgesviewbinding.others.Constants.PRODUCT_CODE_LENGTH
import com.mihaiim.sisgesviewbinding.others.Constants.SHELF_CODE_LENGTH
import com.mihaiim.sisgesviewbinding.others.Result
import com.mihaiim.sisgesviewbinding.others.ScanScreenTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val administrationRepository: AdministrationRepository,
    private val productRepository: ProductRepository,
    private val storagePositionRepository: StoragePositionRepository,
    private val storageRepository: StorageRepository,
) : ViewModel() {

    private val _quantityLeft = MutableLiveData<Int>()
    val quantityLeft: LiveData<Int> get() = _quantityLeft
    private val _nextStepEvent = MutableLiveData<Int>()
    val nextStepEvent: LiveData<Int> get() = _nextStepEvent
    private val _failEvent = MutableLiveData<Event<String>>()
    val failEvent: LiveData<Event<String>> get() = _failEvent
    private val _errorResIdEvent = MutableLiveData<Event<Int>>()
    val errorResIdEvent: LiveData<Event<Int>> get() = _errorResIdEvent
    private val _navigateBackEvent = MutableLiveData<Event<Unit>>()
    val navigateBackEvent: LiveData<Event<Unit>> get() = _navigateBackEvent
    private val _navigateToProductsList = MutableLiveData<Event<String>>()
    val navigateToProductsList: LiveData<Event<String>> get() = _navigateToProductsList
    private val _navigateToProductDetails = MutableLiveData<Event<String>>()
    val navigateToProductDetails: LiveData<Event<String>> get() = _navigateToProductDetails

    private var step = 0
    private var oldShelfCode: String? = null

    var screenType = ScanScreenTypeEnum.IN
    var scanInProgress = false
    var product: Product? = null
        private set

    init {
        _nextStepEvent.value = step
    }

    fun goForward(input1: String, input2: String, input3: String, input4: String) {
        when (screenType) {
            ScanScreenTypeEnum.IN -> goForwardIn(input1, input2)
            ScanScreenTypeEnum.OUT -> goForwardOut(input1, input2)
            ScanScreenTypeEnum.MOVE -> goForwardMove(input1, input2, input3, input4)
            ScanScreenTypeEnum.SEE_STOCKS -> nextStepSeeStocks(input1)
        }
    }

    private fun goForwardIn(input1: String, input2: String) {
        if (!checkQuantity(input2)) {
            return
        }
        when (step) {
            0 -> {
                nextStepIn(
                    productCode = input1,
                    quantity = input2.toInt(),
                )
            }
            1 -> {
                nextStepIn(
                    shelfCode = input1,
                    quantity = input2.toInt(),
                )
            }
        }
    }

    private fun goForwardOut(input1: String, input2: String) {
        if (!checkQuantity(input2)) {
            return
        }
        when (step) {
            0 -> {
                nextStepOut(
                    productCode = input1,
                    quantity = input2.toInt(),
                )
            }
            1 -> {
                nextStepOut(
                    productCode = product?.code,
                    shelfCode = input1,
                    quantity = input2.toInt(),
                )
            }
        }
    }

    private fun goForwardMove(input1: String, input2: String, input3: String, input4: String) {
        when (step) {
            0 -> {
                nextStepMove(
                    productCode = input1,
                    shelfCode = input2,
                )
            }
            1 -> {
                if (!checkQuantity(input3)) {
                    return
                }
                nextStepMove(
                    productCode = input1,
                    shelfCode = input2,
                    quantity = input3.toInt(),
                )
            }
            else -> {
                nextStepMove(
                    productCode = input1,
                    shelfCode = input4,
                    quantity = input3.toInt(),
                )
            }
        }
    }

    private fun checkQuantity(quantityStr: String): Boolean {
        if (quantityStr.isEmpty()) {
            _errorResIdEvent.value = Event(R.string.error_quantity_needed)
            return false
        }
        try {
            val quantity = quantityStr.toInt()
            if (quantity < 0) {
                _errorResIdEvent.value = Event(R.string.error_quantity_must_be_positive_number)
                return false
            }
            return true
        } catch (e: Exception) {
            _errorResIdEvent.value = Event(R.string.error_quantity_must_be_a_number)
            return false
        }
    }

    private fun nextStepIn(
        productCode: String? = null,
        shelfCode: String? = null,
        quantity: Int? = null,
    ) = viewModelScope.launch {
        if (step == 0) {
            if (checkIfProductExists(productCode!!)) {
                product = getProduct(productCode)
                if (product == null) {
                    return@launch
                }
                _quantityLeft.value = quantity!!
                step++
                _nextStepEvent.value = step
            }
        } else if (step == 1) {
            if (quantity!! > _quantityLeft.value!!) {
                _errorResIdEvent.value = Event(R.string.error_much_quantity_than_left_quantity)
                return@launch
            }
            if (checkIfShelfExists(shelfCode!!) &&
                productsInOut(shelfCode, quantity, AdministrationEnum.ADD)) {
                val newQuantity = _quantityLeft.value!! - quantity
                if (newQuantity == 0) {
                    _navigateBackEvent.value = Event(Unit)
                } else {
                    _quantityLeft.value = newQuantity
                }
            }
        }
    }

    private fun nextStepOut(
        productCode: String? = null,
        shelfCode: String? = null,
        quantity: Int? = null,
    ) = viewModelScope.launch {
        if (step == 0) {
            if (checkIfProductExists(productCode!!)) {
                product = getProduct(productCode)
                if (product == null) {
                    return@launch
                }
                _quantityLeft.value = quantity!!
                step++
                _nextStepEvent.value = step
            }
        } else if (step == 1) {
            if (quantity!! > _quantityLeft.value!!) {
                _errorResIdEvent.value = Event(R.string.error_much_quantity_than_left_quantity)
                return@launch
            }
            if (!checkIfShelfExists(shelfCode!!)) {
                return@launch
            }
            if (!checkIfShelfHasEnoughQuantity(productCode!!, shelfCode, quantity)) {
                return@launch
            }
            if (productsInOut(shelfCode, quantity, AdministrationEnum.RETRIEVE)) {
                val newQuantity = _quantityLeft.value!! - quantity
                if (newQuantity == 0) {
                    _navigateBackEvent.value = Event(Unit)
                } else {
                    _quantityLeft.value = newQuantity
                }
            }
        }
    }

    private fun nextStepMove(
        productCode: String? = null,
        shelfCode: String? = null,
        quantity: Int? = null,
    ) = viewModelScope.launch {
        if (step == 0) {
            if (checkIfProductExists(productCode!!) &&
                checkIfShelfExists(shelfCode!!, true)) {
                oldShelfCode = shelfCode
                step++
                _nextStepEvent.value = step
            }
        } else if (step == 1) {
            if (!checkIfShelfHasEnoughQuantity(productCode!!, shelfCode!!, quantity!!)) {
                return@launch
            }
            step++
            _nextStepEvent.value = step
        } else if (step == 2) {
            if (!checkIfShelfExists(shelfCode!!, new = true)) {
                return@launch
            }
            if (moveProducts(productCode!!, shelfCode, quantity!!)) {
                _navigateBackEvent.value = Event(Unit)
            }
        }
    }

    private fun nextStepSeeStocks(productCodeOrName: String) = viewModelScope.launch {
        if (productCodeOrName.isEmpty()) {
            _errorResIdEvent.value = Event(R.string.error_product_code_or_name_needed)
            return@launch
        }
        if (productCodeOrName.length == PRODUCT_CODE_LENGTH && productCodeOrName.isDigitsOnly()) {
            if (checkIfProductExists(productCodeOrName)) {
                _navigateToProductDetails.value = Event(productCodeOrName)
            }
        } else {
            _navigateToProductsList.value = Event(productCodeOrName)
        }
    }

    private suspend fun checkIfProductExists(productCode: String): Boolean {
        val errorResId = verifyProductCode(productCode)
        if (errorResId == 0) {
            when(val result = productRepository.productExists(productCode)) {
                is Result.Success -> {
                    if (!result.data) {
                        _errorResIdEvent.value = Event(R.string.error_product_does_not_exist_with_the_code)
                    }
                    return result.data
                }
                is Result.Error -> _failEvent.value = Event(result.message)
            }
        } else {
            _errorResIdEvent.value = Event(errorResId)
        }
        return false
    }

    private suspend fun getProduct(productCode: String): Product? =
        when(val result = productRepository.getMinifiedProductByCode(productCode)) {
            is Result.Success -> result.data
            is Result.Error -> {
                _failEvent.value = Event(result.message)
                null
            }
        }

    private suspend fun checkIfShelfExists(
        shelfCode: String,
        current: Boolean = false,
        new: Boolean = false,
    ): Boolean {
        val errorResId = verifyShelfCode(shelfCode, current, new)
        if (errorResId == 0) {
            when(val result = storagePositionRepository.positionExists(shelfCode)) {
                is Result.Success -> {
                    if (!result.data) {
                        _errorResIdEvent.value = Event(R.string.error_shelf_does_not_exist_with_the_code)
                    }
                    return result.data
                }
                is Result.Error -> _failEvent.value = Event(result.message)
            }
        } else {
            _errorResIdEvent.value = Event(errorResId)
        }
        return false
    }

    private suspend fun checkIfShelfHasEnoughQuantity(
        productCode: String,
        shelfCode: String,
        quantity: Int,
    ) = when (val result = storageRepository.getProductQuantityAtPosition(
        productCode,
        shelfCode,
    )) {
        is Result.Success -> {
            if (result.data < quantity) {
                _errorResIdEvent.value = Event(
                    R.string.error_much_quantity_than_total_quantity_at_shelf)
                false
            } else {
                true
            }
        }
        is Result.Error -> {
            _failEvent.value = Event(result.message)
            false
        }
    }

    private suspend fun productsInOut(
        shelfCode: String,
        quantity: Int,
        type: AdministrationEnum,
    ) = when(val result = administrationRepository.insert(
        product!!.code,
        product!!.name,
        shelfCode,
        quantity,
        System.currentTimeMillis(),
        type,
    )) {
        is Result.Success -> result.data
        is Result.Error -> {
            _failEvent.value = Event(result.message)
            false
        }
    }

    private suspend fun moveProducts(
        productCode: String,
        newShelfCode: String,
        quantity: Int,
    ) = when(val result = storageRepository.moveProduct(
        productCode,
        oldShelfCode!!,
        newShelfCode,
        quantity,
    )) {
        is Result.Success -> result.data
        is Result.Error -> {
            _failEvent.value = Event(result.message)
            false
        }
    }

    private fun verifyProductCode(productCode: String): Int {
        if (productCode.isEmpty()) {
            return R.string.error_product_code_needed
        }
        if (productCode.length != PRODUCT_CODE_LENGTH || !productCode.isDigitsOnly()) {
            return R.string.error_wrong_product_code
        }
        return 0
    }

    private fun verifyShelfCode(shelfCode: String, current: Boolean = false, new: Boolean = false): Int {
        if (shelfCode.isEmpty()) {
            if (current) {
                return R.string.error_current_shelf_code_needed
            }
            if (new) {
                return R.string.error_new_shelf_code_needed
            }
            return R.string.error_shelf_code_needed
        }
        if (shelfCode.length != SHELF_CODE_LENGTH) {
            return R.string.error_wrong_shelf_code
        }
        if (!(shelfCode[0].isLetter() && shelfCode[2].isLetter() && shelfCode[4].isLetter())) {
            return R.string.error_wrong_shelf_code
        }
        if (!(shelfCode[1].isDigit() && shelfCode[3].isDigit() && shelfCode[5].isDigit())) {
            return R.string.error_wrong_shelf_code
        }
        return 0
    }
}