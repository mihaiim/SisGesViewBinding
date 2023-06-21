package com.mihaiim.sisgesviewbinding.domain.repository

import com.mihaiim.sisgesviewbinding.others.Result

interface StoragePositionRepository {

    suspend fun positionExists(code: String): Result<Boolean>
}