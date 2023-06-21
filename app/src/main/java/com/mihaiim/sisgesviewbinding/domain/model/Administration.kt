package com.mihaiim.sisgesviewbinding.domain.model

import com.mihaiim.sisgesviewbinding.others.AdministrationEnum
import com.mihaiim.sisgesviewbinding.others.Constants.DATE_TIME_PATTERN
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Administration(
    val type: AdministrationEnum = AdministrationEnum.NONE,
    val productCode: String,
    val productName: String,
    val quantity: Int,
    val timestamp: Long,
) {

    fun getFormattedDateTime(): String {
        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        return date.format(formatter)
    }
}
