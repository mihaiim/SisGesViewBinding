package com.mihaiim.sisgesviewbinding.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.toObject
import com.mihaiim.sisgesviewbinding.data.dto.StorageDto
import com.mihaiim.sisgesviewbinding.domain.model.Storage
import com.mihaiim.sisgesviewbinding.domain.repository.StorageRepository
import com.mihaiim.sisgesviewbinding.others.Result
import com.mihaiim.sisgesviewbinding.others.UpdateQuantityEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val collectionRef: CollectionReference,
    private val ioDispatcher: CoroutineDispatcher,
) : StorageRepository {

    override suspend fun getProductQuantityAtPosition(
        productCode: String,
        positionCode: String,
    ): Result<Int> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("productCode", productCode)
                .whereEqualTo("positionCode", positionCode)
                .get()
                .await()
            if (snapshot.documents.size == 0) {
                return@withContext Result.Error("The product does not exist at location $positionCode")
            }
            snapshot.documents[0].toObject<StorageDto>()?.let {
                Result.Success(it.quantity)
            } ?: Result.Error("An error occurred")
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun getProductPositionsAndQuantities(
        productCode: String,
    ): Result<List<Storage>> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("productCode", productCode)
                .get()
                .await()
            Result.Success(snapshot.documents.mapNotNull { document ->
                document.toObject<StorageDto>()?.let {
                    Storage(it.positionCode, it.quantity)
                }
            })
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun updateProductQuantityAtPosition(
        productCode: String,
        positionCode: String,
        quantity: Int,
        type: UpdateQuantityEnum,
    ): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("productCode", productCode)
                .whereEqualTo("positionCode", positionCode)
                .get()
                .await()
            if (snapshot.documents.size == 0) {
                collectionRef
                    .add(StorageDto(productCode, positionCode, quantity))
                    .await()
                return@withContext Result.Success(true)
            }

            val document = snapshot.documents[0]
            document.toObject<StorageDto>()?.let {
                var newQuantity = it.quantity
                if (type == UpdateQuantityEnum.ADD) {
                    newQuantity += quantity
                } else {
                    newQuantity -= quantity
                    if (newQuantity == 0) {
                        collectionRef.document(document.id).delete().await()
                        return@withContext Result.Success(true)
                    }
                }
                collectionRef
                    .document(document.id)
                    .update("quantity", newQuantity)
                    .await()
                Result.Success(true)
            } ?: Result.Error("An error occurred")
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun removeProductPosition(
        productCode: String,
        positionCode: String,
    ): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("productCode", productCode)
                .whereEqualTo("positionCode", positionCode)
                .get()
                .await()
            if (snapshot.documents.size == 0) {
                return@withContext Result.Error("The product does not exist at location $positionCode")
            }
            val docId = snapshot.documents[0].id
            collectionRef.document(docId).delete().await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun moveProduct(
        productCode: String,
        oldPositionCode: String,
        newPositionCode: String,
        quantity: Int,
    ): Result<Boolean> = withContext(ioDispatcher) {
        when (val currentQuantityResult = getProductQuantityAtPosition(
            productCode,
            oldPositionCode,
        )) {
            is Result.Success -> {
                if (currentQuantityResult.data - quantity == 0) {
                    val result = removeProductPosition(productCode, oldPositionCode)
                    if (result is Result.Error) {
                        return@withContext result
                    }
                } else {
                    val result = updateProductQuantityAtPosition(productCode, oldPositionCode,
                        quantity, UpdateQuantityEnum.RETRIEVE)
                    if (result is Result.Error) {
                        return@withContext result
                    }
                }
                updateProductQuantityAtPosition(
                    productCode,
                    newPositionCode,
                    quantity,
                    UpdateQuantityEnum.ADD
                )
            }

            is Result.Error -> currentQuantityResult
        }
    }
}