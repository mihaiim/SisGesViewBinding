package com.mihaiim.sisgesviewbinding.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mihaiim.sisgesviewbinding.data.dto.UserDto
import com.mihaiim.sisgesviewbinding.domain.model.UpdateProfile
import com.mihaiim.sisgesviewbinding.domain.model.User
import com.mihaiim.sisgesviewbinding.domain.repository.UserRepository
import com.mihaiim.sisgesviewbinding.others.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val collectionRef: CollectionReference,
    private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    override suspend fun getUserData(): Result<User> = withContext(ioDispatcher) {
        try {
            val curUser = auth.currentUser!!
            val snapshot = collectionRef
                .whereEqualTo("id", curUser.uid)
                .get()
                .await()
            snapshot.documents[0].toObject<UserDto>()?.let {
                return@withContext Result.Success(User(
                    curUser.uid,
                    it.email,
                    it.firstName,
                    it.lastName,
                    if (it.photoUri == null) null else Uri.parse(it.photoUri),
                ))
            }
            Result.Error("An error occurred")
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val task = auth.signInWithEmailAndPassword(email, password).await()
            task.user?.let {
                Result.Success(Unit)
            } ?: Result.Error("Wrong email or password")
        } catch (e: Exception) {
            Result.Error("Wrong email or password")
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val userExistsResponse = userExists(email)
            if (userExistsResponse is Result.Success) {
                if (userExistsResponse.data) {
                    return@withContext Result.Error("Email address already used")
                }
            } else if (userExistsResponse is Result.Error) {
                return@withContext Result.Error(userExistsResponse.message)
            }

            val task = auth.createUserWithEmailAndPassword(email, password).await()
            task.user?.let {
                collectionRef
                    .add(UserDto(it.uid, email, firstName, lastName))
                    .await()
                Result.Success(Unit)
            } ?: Result.Error("Account creation failed")
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }

    override suspend fun update(
        firstName: String?,
        lastName: String?,
        email: String?,
        password: String?,
        photoUri: Uri?,
    ): Result<UpdateProfile> = withContext(ioDispatcher) {
        val updateProfile = UpdateProfile()
        val curUser = auth.currentUser!!

        if (email != null && email.isNotEmpty()) {
            try {
                curUser.updateEmail(email).await()
                updateProfile.email = email
            } catch (e: Exception) {
                return@withContext Result.Error("An error occurred when changing the email")
            }
        }
        if (password != null && password.isNotEmpty()) {
            try {
                curUser.updatePassword(password).await()
            } catch (e: Exception) {
                return@withContext Result.Error("An error occurred when changing the password")
            }
        }

        if (photoUri != null) {
            val imageRef = Firebase.storage.reference.child(
                "images/profile_picture/${curUser.uid}.jpg")
            var error = false
            imageRef.putFile(photoUri).addOnFailureListener {
                error = true
            }.await()
            if (error) {
                return@withContext Result.Error("An error occurred when changing the profile picture")
            }
            imageRef.downloadUrl.addOnSuccessListener {
                updateProfile.photoUri = it
            }.await()
        }

        if ((firstName != null && firstName.isNotEmpty()) ||
            (lastName != null && lastName.isNotEmpty()) ||
            updateProfile.email != null ||
            updateProfile.photoUri != null
        ) {
            val snapshot = collectionRef
                .whereEqualTo("id", curUser.uid)
                .get()
                .await()
            try {
                val document = snapshot.documents[0]

                if (firstName != null && firstName.isNotEmpty()) {
                    collectionRef
                        .document(document.id)
                        .update("firstName", firstName)
                        .await()
                    updateProfile.firstName = firstName
                }

                if (lastName != null && lastName.isNotEmpty()) {
                    collectionRef
                        .document(document.id)
                        .update("lastName", lastName)
                        .await()
                    updateProfile.lastName = lastName
                }

                if (updateProfile.email != null) {
                    collectionRef
                        .document(document.id)
                        .update("email", updateProfile.email)
                        .await()
                }

                if (updateProfile.photoUri != null) {
                    collectionRef
                        .document(document.id)
                        .update("photoUri", updateProfile.photoUri)
                        .await()
                }
            } catch (e: Exception) {
                return@withContext Result.Error("An error occurred when changing the name")
            }
        }
        Result.Success(updateProfile)
    }

    override suspend fun removeProfilePicture(): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val curUserId = auth.currentUser!!.uid
            Firebase.storage.reference
                .child("images/profile_picture/${curUserId}.jpg")
                .delete().await()
            val snapshot = collectionRef
                .whereEqualTo("id", curUserId)
                .get()
                .await()
            collectionRef.document(
                snapshot.documents[0].id
            ).update(mapOf(
                "photoUri" to FieldValue.delete(),
            ))
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error("An error occurred when deleting the profile picture")
        }
    }

    override fun logout() {
        auth.signOut()
    }

    private suspend fun userExists(email: String): Result<Boolean> {
        return try {
            val snapshot = collectionRef
                .whereEqualTo("email", email)
                .get()
                .await()
            Result.Success(snapshot.documents.size > 0)
        } catch (e: Exception) {
            Result.Error("An error occurred")
        }
    }
}