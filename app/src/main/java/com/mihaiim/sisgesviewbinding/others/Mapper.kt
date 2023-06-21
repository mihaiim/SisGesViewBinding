package com.mihaiim.sisgesviewbinding.others

import com.mihaiim.sisgesviewbinding.data.dto.AdministrationDto
import com.mihaiim.sisgesviewbinding.data.dto.ProductDto
import com.mihaiim.sisgesviewbinding.domain.model.Administration
import com.mihaiim.sisgesviewbinding.domain.model.Product

fun ProductDto.toProduct() = Product(
    code = code,
    name = name,
)

fun AdministrationDto.toAdministration() = Administration(
    type = if (type == 0) AdministrationEnum.ADD else AdministrationEnum.RETRIEVE,
    productCode = productCode,
    productName = productName,
    quantity = quantity,
    timestamp = timestamp,
)
