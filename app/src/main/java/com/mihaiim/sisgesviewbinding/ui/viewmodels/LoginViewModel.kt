package com.mihaiim.sisgesviewbinding.ui.viewmodels

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.domain.model.Event
import com.mihaiim.sisgesviewbinding.domain.repository.UserRepository
import com.mihaiim.sisgesviewbinding.others.Constants.PASSWORD_MIN_LENGTH
import com.mihaiim.sisgesviewbinding.others.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel()  {

    private val _successEvent = MutableLiveData<Event<Unit>>()
    val successEvent: LiveData<Event<Unit>> get() = _successEvent
    private val _failEvent = MutableLiveData<Event<String>>()
    val failEvent: LiveData<Event<String>> get() = _failEvent
    private val _errorResIdEvent = MutableLiveData<Event<Int>>()
    val errorResIdEvent: LiveData<Event<Int>> get() = _errorResIdEvent

    fun loginUser(
        email: String,
        password: String,
    ) {
        val emailTrim = email.trim()
        val errorResId = verifyLogin(emailTrim, password)
        if (errorResId == 0) {
            viewModelScope.launch {
                when(val result = userRepository.login(emailTrim, password)) {
                    is Result.Success -> _successEvent.value = Event(Unit)
                    is Result.Error -> _failEvent.value = Event(result.message)
                }
            }
        } else {
            _errorResIdEvent.value = Event(errorResId)
        }
    }

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
    ) {
        val firstNameTrim = firstName.trim()
        val lastNameTrim = lastName.trim()
        val emailTrim = email.trim()

        val errorResId = verifyRegister(firstNameTrim, lastNameTrim, emailTrim, password)
        if (errorResId == 0) {
            viewModelScope.launch {
                when(val result = userRepository.register(
                    firstNameTrim,
                    lastNameTrim,
                    emailTrim,
                    password,
                )) {
                    is Result.Success -> _successEvent.value = Event(Unit)
                    is Result.Error -> _failEvent.value = Event(result.message)
                }
            }
        } else {
            _errorResIdEvent.value = Event(errorResId)
        }
    }

    private fun verifyLogin(
        email: String,
        password: String,
    ): Int {
        if (email.isEmpty() || password.isEmpty()) {
            return R.string.error_all_fields_must_be_filled
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return R.string.error_email_wrong_format
        }
        if (password.length < PASSWORD_MIN_LENGTH) {
            return R.string.error_password_too_short
        }
        return 0
    }

    private fun verifyRegister(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
    ): Int {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            return R.string.error_all_fields_must_be_filled
        }
        return verifyLogin(email, password)
    }
}