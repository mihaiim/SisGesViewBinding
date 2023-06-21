package com.mihaiim.sisgesviewbinding.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.mihaiim.sisgesviewbinding.data.dto.AdministrationDto
import com.mihaiim.sisgesviewbinding.domain.model.Administration
import com.mihaiim.sisgesviewbinding.domain.repository.AdministrationRepository
import com.mihaiim.sisgesviewbinding.domain.repository.StorageRepository
import com.mihaiim.sisgesviewbinding.others.AdministrationEnum
import com.mihaiim.sisgesviewbinding.others.Result
import com.mihaiim.sisgesviewbinding.others.UpdateQuantityEnum
import com.mihaiim.sisgesviewbinding.others.toAdministration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AdministrationRepositoryImpl @Inject constructor(
    private val collectionRef: CollectionReference,
    private val storageRepository: StorageRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : AdministrationRepository {

    override suspend fun insert(
        productCode: String,
        productName: String,
        positionCode: String,
        quantity: Int,
        timestamp: Long,
        type: AdministrationEnum,
    ): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val updateType = if (type == AdministrationEnum.ADD)
                UpdateQuantityEnum.ADD else UpdateQuantityEnum.RETRIEVE
            val result = storageRepository.updateProductQuantityAtPosition(
                productCode,
                positionCode,
                quantity,
                updateType,
            )
            if (result is Result.Error) {
                return@withContext result
            }

            collectionRef
                .add(AdministrationDto(
                    type.value,
                    productCode,
                    productName,
                    quantity,
                    timestamp,
                ))
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun get(
        type: AdministrationEnum,
        startDate: Long,
        endDate: Long,
        productCode: String?,
        productName: String?,
    ): Result<List<Administration>> = withContext(ioDispatcher) {
        try {
            var query = collectionRef
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .orderBy("timestamp", Query.Direction.DESCENDING)
            if (type != AdministrationEnum.ADD_AND_RETRIEVE) {
                query = query.whereEqualTo("type", type.value)
            }
            if (productCode != null && productCode.isNotEmpty()) {
                query = query.whereEqualTo("productCode", productCode)
            }
            val snapshot = query.get().await()
            val list = mutableListOf<Administration>()
            for (document in snapshot.documents) {
                document.toObject<AdministrationDto>()?.let {
                    if (productName == null || productName.isEmpty()) {
                        list.add(it.toAdministration())
                    } else if (it.productName.contains(productName, true)) {
                        list.add(it.toAdministration())
                    } else {}
                }
            }
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }
}