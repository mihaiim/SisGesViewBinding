package com.mihaiim.sisgesviewbinding.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.toObject
import com.mihaiim.sisgesviewbinding.data.dto.ProductDto
import com.mihaiim.sisgesviewbinding.domain.model.Product
import com.mihaiim.sisgesviewbinding.domain.repository.ProductRepository
import com.mihaiim.sisgesviewbinding.domain.repository.StorageRepository
import com.mihaiim.sisgesviewbinding.others.Result
import com.mihaiim.sisgesviewbinding.others.toProduct
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val collectionRef: CollectionReference,
    private val storageRepository: StorageRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ProductRepository {

    override suspend fun getMinifiedProductByCode(
        productCode: String,
    ): Result<Product> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("code", productCode)
                .get()
                .await()
            if (snapshot.documents.size == 0) {
                return@withContext Result.Error("There is no product with code \"$productCode\"")
            }
            snapshot.documents[0].toObject<ProductDto>()?.let {
                Result.Success(it.toProduct())
            } ?: Result.Error("An error occurred")
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun getMinifiedProductsByName(
        searchTerm: String,
    ): Result<List<Product>> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .orderBy("name")
                .get()
                .await()
            if (snapshot.documents.size == 0) {
                return@withContext Result.Error("There is no product matching the name entered")
            }

            val list = snapshot.documents.mapNotNull { document ->
                document.toObject<ProductDto>()?.toProduct()
            }.toMutableList()
            list.removeIf { !it.name.contains(searchTerm, true) }

            if (list.size == 0) {
                return@withContext Result.Error("There is no product matching the name entered")
            }
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun getProductDetails(
        productCode: String,
    ): Result<Product> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("code", productCode)
                .get()
                .await()
            if (snapshot.documents.size == 0) {
                return@withContext Result.Error("There is no product with code \"$productCode\"")
            }

            snapshot.documents[0].toObject<ProductDto>()?.let {
                when (val result = storageRepository.getProductPositionsAndQuantities(productCode)) {
                    is Result.Success -> {
                        val product = it.toProduct()
                        product.positions = result.data
                        Result.Success(product)
                    }
                    is Result.Error -> result
                }
            } ?: Result.Error("An error occurred")
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun productExists(
        productCode: String,
    ): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("code", productCode)
                .get()
                .await()
            Result.Success(snapshot.documents.size > 0)
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }
}