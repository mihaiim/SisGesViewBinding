package com.mihaiim.sisgesviewbinding.domain.repository

import com.mihaiim.sisgesviewbinding.domain.model.Administration
import com.mihaiim.sisgesviewbinding.others.AdministrationEnum
import com.mihaiim.sisgesviewbinding.others.Result

interface AdministrationRepository {

    suspend fun insert(
        productCode: String,
        productName: String,
        positionCode: String,
        quantity: Int,
        timestamp: Long,
        type: AdministrationEnum,
    ): Result<Boolean>

    suspend fun get(
        type: AdministrationEnum,
        startDate: Long,
        endDate: Long,
        productCode: String? = null,
        productName: String? = null,
    ): Result<List<Administration>>
}