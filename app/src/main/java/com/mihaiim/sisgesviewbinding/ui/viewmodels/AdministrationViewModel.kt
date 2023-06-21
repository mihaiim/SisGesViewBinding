package com.mihaiim.sisgesviewbinding.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.domain.model.Administration
import com.mihaiim.sisgesviewbinding.domain.model.Event
import com.mihaiim.sisgesviewbinding.domain.repository.AdministrationRepository
import com.mihaiim.sisgesviewbinding.others.AdministrationEnum
import com.mihaiim.sisgesviewbinding.others.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AdministrationViewModel @Inject constructor(
    private val administrationRepository: AdministrationRepository,
) : ViewModel() {

    private val _startDate = MutableLiveData<LocalDateTime>()
    val startDate: LiveData<LocalDateTime> get() = _startDate
    private val _endDate = MutableLiveData<LocalDateTime>()
    val endDate: LiveData<LocalDateTime> get() = _endDate
    private val _successEvent = MutableLiveData<Event<List<Administration>>>()
    val successEvent: LiveData<Event<List<Administration>>> get() = _successEvent
    private val _failEvent = MutableLiveData<Event<String>>()
    val failEvent: LiveData<Event<String>> get() = _failEvent
    private val _errorResIdEvent = MutableLiveData<Event<Int>>()
    val errorResIdEvent: LiveData<Event<Int>> get() = _errorResIdEvent

    private var administrationType = AdministrationEnum.ADD_AND_RETRIEVE

    init {
        resetFilters()
    }

    fun setAdministrationType(administrationType: AdministrationEnum) {
        this.administrationType = administrationType
    }

    fun setStartDate(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int) {
        val dateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, 0)
        if (dateTime.isAfter(_endDate.value)) {
            _errorResIdEvent.value = Event(R.string.error_start_date_after_end_date)
            return
        }
        _startDate.value = dateTime
    }

    fun setEndDate(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int) {
        val dateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, 59)
        if (dateTime.isBefore(_startDate.value)) {
            _errorResIdEvent.value = Event(R.string.error_end_date_before_start_date)
            return
        }
        _endDate.value = dateTime
    }

    fun resetFilters() {
        administrationType = AdministrationEnum.ADD_AND_RETRIEVE
        resetStartDate()
        resetEndDate()
    }

    fun getAdministration(productCode: String?, productName: String?) = viewModelScope.launch {
        val startDateMillis = startDate.value?.let {
            getTimeInMillisFromLocalDateTime(it)
        }
        val endDateMillis = endDate.value?.let {
            getTimeInMillisFromLocalDateTime(it)
        }
        if (startDateMillis == null || endDateMillis == null) {
            return@launch
        }

        when (val result = administrationRepository.get(
            administrationType,
            startDateMillis,
            endDateMillis,
            productCode,
            productName,
        )) {
            is Result.Success -> _successEvent.value = Event(result.data)
            is Result.Error -> _failEvent.value = Event(result.message)
        }
    }

    private fun resetStartDate() {
        val now: LocalDate = LocalDate.now()
        val firstDay = LocalDateTime.of(now.year, now.month, 1,
            0, 0, 0)
        _startDate.value = firstDay
    }

    private fun resetEndDate() {
        var now: LocalDate = LocalDate.now()
        now = now.withDayOfMonth(now.month.length(now.isLeapYear))
        val lastDay = LocalDateTime.of(now.year, now.month, now.dayOfMonth,
            23, 59, 59)
        _endDate.value = lastDay
    }

    private fun getTimeInMillisFromLocalDateTime(dateTime: LocalDateTime): Long {
        val calendar: Calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(dateTime.year,
            dateTime.monthValue - 1,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute,
            dateTime.second,
        )
        return calendar.time.time
    }
}