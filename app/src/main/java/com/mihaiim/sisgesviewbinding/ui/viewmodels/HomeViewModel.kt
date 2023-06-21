package com.mihaiim.sisgesviewbinding.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mihaiim.sisgesviewbinding.domain.model.Event
import com.mihaiim.sisgesviewbinding.domain.model.User
import com.mihaiim.sisgesviewbinding.domain.repository.UserRepository
import com.mihaiim.sisgesviewbinding.others.Constants.KEY_USER
import com.mihaiim.sisgesviewbinding.others.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPref: SharedPreferences,
    private val gson: Gson,
) : ViewModel()  {

    private val _successEvent = MutableLiveData<Event<User>>()
    val successEvent: LiveData<Event<User>> get() = _successEvent
    private val _failEvent = MutableLiveData<Event<String>>()
    val failEvent: LiveData<Event<String>> get() = _failEvent

    fun getUserData() = viewModelScope.launch {
        when(val result = userRepository.getUserData()) {
            is Result.Success -> {
                saveUserToSharedPref(result.data)
                _successEvent.value = Event(result.data)
            }
            is Result.Error -> _failEvent.value = Event(result.message)
        }
    }

    private fun saveUserToSharedPref(user: User) {
        sharedPref.edit()
            .putString(KEY_USER, gson.toJson(user))
            .apply()
    }
}