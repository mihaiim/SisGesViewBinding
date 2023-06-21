package com.mihaiim.sisgesviewbinding.data.dto

data class AdministrationDto(
    val type: Int = -1,
    val productCode: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val timestamp: Long = 0,
)
