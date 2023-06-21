package com.mihaiim.sisgesviewbinding.ui.viewmodels

import android.content.SharedPreferences
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.domain.model.Event
import com.mihaiim.sisgesviewbinding.domain.model.UpdateProfile
import com.mihaiim.sisgesviewbinding.domain.model.User
import com.mihaiim.sisgesviewbinding.domain.repository.UserRepository
import com.mihaiim.sisgesviewbinding.others.Constants.KEY_USER
import com.mihaiim.sisgesviewbinding.others.Constants.PASSWORD_MIN_LENGTH
import com.mihaiim.sisgesviewbinding.others.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPref: SharedPreferences,
    private val gson: Gson,
) : ViewModel() {

    private val _successEvent = MutableLiveData<Event<Unit>>()
    val successEvent: LiveData<Event<Unit>> get() = _successEvent
    private val _pictureRemovedEvent = MutableLiveData<Event<Unit>>()
    val pictureRemovedEvent: LiveData<Event<Unit>> get() = _pictureRemovedEvent
    private val _failEvent = MutableLiveData<Event<String>>()
    val failEvent: LiveData<Event<String>> get() = _failEvent
    private val _errorResIdEvent = MutableLiveData<Event<Int>>()
    val errorResIdEvent: LiveData<Event<Int>> get() = _errorResIdEvent

    lateinit var user: User

    init {
        getUserFromSharedPref()
    }

    fun updateUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String?,
        photoURI: Uri?,
    ) = viewModelScope.launch {
        val firstNameTrim = firstName.trim()
        val lastNameTrim = lastName.trim()
        val emailTrim = email.trim()

        val newFirstName = if (user.firstName == firstNameTrim) null else firstNameTrim
        val newLastName = if (user.lastName == lastNameTrim) null else lastNameTrim
        val newEmail = if (user.email == emailTrim) null else emailTrim

        val errorResId = verifyData(newFirstName, newLastName, newEmail, password)
        if (errorResId == 0) {
            viewModelScope.launch {
                when(val result = userRepository.update(
                    newFirstName,
                    newLastName,
                    newEmail,
                    password,
                    photoURI)) {
                    is Result.Success -> {
                        saveUserToSharedPref(result.data)
                        _successEvent.value = Event(Unit)
                    }
                    is Result.Error -> _failEvent.value = Event(result.message)
                }
            }
        } else {
            _errorResIdEvent.value = Event(errorResId)
        }
    }

    fun removeProfilePicture() = viewModelScope.launch {
        when(val result = userRepository.removeProfilePicture()) {
            is Result.Success -> {
                removeProfilePictureFromMemory()
                _pictureRemovedEvent.value = Event(Unit)
            }
            is Result.Error -> _failEvent.value = Event(result.message)
        }
    }

    private fun verifyData(
        firstName: String?,
        lastName: String?,
        email: String?,
        password: String?,
    ): Int {
        if (lastName != null && lastName.isEmpty()) {
            return R.string.error_last_name_empty
        }
        if (firstName != null && firstName.isEmpty()) {
            return R.string.error_first_name_empty
        }
        if (email != null && (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            return R.string.error_email_wrong_format
        }
        if (password != null && password.isNotEmpty() && password.length < PASSWORD_MIN_LENGTH) {
            return R.string.error_password_too_short
        }
        return 0
    }

    private fun getUserFromSharedPref() {
        sharedPref.getString(KEY_USER, null)?.let {
            user = gson.fromJson(it, User::class.java)
        }
    }

    private fun saveUserToSharedPref(updateProfile: UpdateProfile) {
        sharedPref.edit()
            .putString(KEY_USER, gson.toJson(
                User(
                    user.id,
                    updateProfile.email ?: user.email,
                    updateProfile.firstName ?: user.firstName,
                    updateProfile.lastName ?: user.lastName,
                    updateProfile.photoUri ?: user.photoUri,
                )
            ))
            .apply()
    }

    private fun removeProfilePictureFromMemory() {
        user.photoUri = null
        sharedPref.edit()
            .putString(KEY_USER, gson.toJson(
                User(
                    user.id,
                    user.email,
                    user.firstName,
                    user.lastName,
                    user.photoUri,
                )
            ))
            .apply()
    }
}