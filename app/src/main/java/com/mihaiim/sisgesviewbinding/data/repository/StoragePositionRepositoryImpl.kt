package com.mihaiim.sisgesviewbinding.data.repository

import com.google.firebase.firestore.CollectionReference
import com.mihaiim.sisgesviewbinding.domain.repository.StoragePositionRepository
import com.mihaiim.sisgesviewbinding.others.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StoragePositionRepositoryImpl @Inject constructor(
    private val collectionRef: CollectionReference,
    private val ioDispatcher: CoroutineDispatcher,
) : StoragePositionRepository {

    override suspend fun positionExists(code: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val snapshot = collectionRef
                .whereEqualTo("code", code)
                .get()
                .await()
            Result.Success(snapshot.documents.size > 0)
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }
}