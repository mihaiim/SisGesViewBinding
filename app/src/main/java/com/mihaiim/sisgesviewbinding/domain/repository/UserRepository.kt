package com.mihaiim.sisgesviewbinding.domain.repository

import android.net.Uri
import com.mihaiim.sisgesviewbinding.domain.model.UpdateProfile
import com.mihaiim.sisgesviewbinding.domain.model.User
import com.mihaiim.sisgesviewbinding.others.Result

interface UserRepository {

    suspend fun getUserData(): Result<User>

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun update(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        password: String? = null,
        photoUri: Uri? = null,
    ): Result<UpdateProfile>

    suspend fun removeProfilePicture(): Result<Boolean>

    fun logout()
}