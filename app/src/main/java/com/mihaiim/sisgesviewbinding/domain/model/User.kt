package com.mihaiim.sisgesviewbinding.domain.model

import android.net.Uri

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    var photoUri: Uri? = null,
)