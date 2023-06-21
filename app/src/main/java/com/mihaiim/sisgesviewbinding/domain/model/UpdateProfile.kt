package com.mihaiim.sisgesviewbinding.domain.model

import android.net.Uri

data class UpdateProfile(
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var photoUri: Uri? = null,
)