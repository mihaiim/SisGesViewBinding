package com.mihaiim.sisgesviewbinding.domain.repository

import com.mihaiim.sisgesviewbinding.domain.model.Storage
import com.mihaiim.sisgesviewbinding.others.Result
import com.mihaiim.sisgesviewbinding.others.UpdateQuantityEnum

interface StorageRepository {

    suspend fun getProductQuantityAtPosition(productCode: String, positionCode: String): Result<Int>

    suspend fun getProductPositionsAndQuantities(productCode: String): Result<List<Storage>>

    suspend fun updateProductQuantityAtPosition(
        productCode: String,
        positionCode: String,
        quantity: Int,
        type: UpdateQuantityEnum,
    ): Result<Boolean>

    suspend fun removeProductPosition(productCode: String, positionCode: String): Result<Boolean>

    suspend fun moveProduct(
        productCode: String,
        oldPositionCode: String,
        newPositionCode: String,
        quantity: Int,
    ): Result<Boolean>
}