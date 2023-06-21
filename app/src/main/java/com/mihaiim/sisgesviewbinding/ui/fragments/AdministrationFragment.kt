package com.mihaiim.sisgesviewbinding.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.databinding.FragmentAdministrationBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.others.AdministrationEnum
import com.mihaiim.sisgesviewbinding.others.Constants.DATE_TIME_PATTERN
import com.mihaiim.sisgesviewbinding.ui.adapters.AdministrationAdapter
import com.mihaiim.sisgesviewbinding.ui.viewmodels.AdministrationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AdministrationFragment : BaseFragment<AdministrationViewModel, FragmentAdministrationBinding>(
    FragmentAdministrationBinding::inflate,
) {

    private lateinit var administrationAdapter: AdministrationAdapter

    private var isStartDate = true
    private var tempYear: Int = 0
    private var tempMonth: Int = 0
    private var tempDayOfMonth: Int = 0

    override val viewModel: AdministrationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.filtersContainer.visibility = View.GONE
        setupRecyclerView()
        setObservables()
        setListeners()
        getAdministration()
    }

    private fun setupRecyclerView() = binding.administrationRv.apply {
        administrationAdapter = AdministrationAdapter()
        adapter = administrationAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setObservables() {
        viewModel.startDate.observe(viewLifecycleOwner) {
            binding.startDateTv.text = it.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
        }
        viewModel.endDate.observe(viewLifecycleOwner) {
            binding.endDateTv.text = it.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
        }
        viewModel.successEvent.observe(viewLifecycleOwner, EventObserver {
            administrationAdapter.submitList(it)
        })
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        viewModel.errorResIdEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }

    private fun setListeners() {
        binding.filterTitleContainer.setOnClickListener {
            if (binding.filtersContainer.visibility == View.VISIBLE) {
                binding.filterArrowIv.setImageDrawable(AppCompatResources
                    .getDrawable(requireContext(), R.drawable.ic_arrow_down))
                binding.filtersContainer.visibility = View.GONE
            } else {
                binding.filterArrowIv.setImageDrawable(AppCompatResources
                    .getDrawable(requireContext(), R.drawable.ic_arrow_up))
                binding.filtersContainer.visibility = View.VISIBLE
            }
        }

        binding.administrationTypeInOutRb.setOnClickListener {
            viewModel.setAdministrationType(AdministrationEnum.ADD_AND_RETRIEVE)
        }
        binding.administrationTypeInRb.setOnClickListener {
            viewModel.setAdministrationType(AdministrationEnum.ADD)
        }
        binding.administrationTypeOutRb.setOnClickListener {
            viewModel.setAdministrationType(AdministrationEnum.RETRIEVE)
        }

        binding.startDateContainer.setOnClickListener {
            isStartDate = true
            showDatePicker()
        }
        binding.endDateContainer.setOnClickListener {
            isStartDate = false
            showDatePicker()
        }

        binding.applyFiltersBtn.setOnClickListener {
            getAdministration()
            binding.filterTitleContainer.callOnClick()
        }
        binding.resetFiltersBtn.setOnClickListener {
            viewModel.resetFilters()
            binding.administrationTypeInOutRb.isChecked = true
            binding.productCodeEt.setText("")
            binding.productNameEt.setText("")
        }
    }

    private fun showDatePicker() {
        val date = getDate()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                tempYear = year
                tempMonth = month + 1
                tempDayOfMonth = dayOfMonth
                showTimePicker()
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth,
        ).show()
    }

    private fun showTimePicker() {
        val date = getDate()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                if (isStartDate) {
                    viewModel.setStartDate(tempYear, tempMonth, tempDayOfMonth, hourOfDay, minute)
                } else {
                    viewModel.setEndDate(tempYear, tempMonth, tempDayOfMonth, hourOfDay, minute)
                }
            },
            date.hour,
            date.minute,
            true,
        ).show()
    }

    private fun getDate(): LocalDateTime = if (isStartDate) viewModel.startDate.value else
        { viewModel.endDate.value }
        ?: LocalDateTime.now()

    private fun getAdministration() {
        viewModel.getAdministration(
            binding.productCodeEt.text.toString(),
            binding.productNameEt.text.toString(),
        )
    }
}