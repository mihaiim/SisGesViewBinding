package com.mihaiim.sisgesviewbinding.domain.model

class Product(
    val code: String,
    val name: String,
    var positions: List<Storage>? = null,
) {
    fun getTotalQuantity() = positions?.let {
        it.sumOf { it.quantity }
    } ?: 0
}
