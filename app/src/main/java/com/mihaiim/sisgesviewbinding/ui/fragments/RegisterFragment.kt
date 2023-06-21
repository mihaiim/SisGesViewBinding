package com.mihaiim.sisgesviewbinding.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mihaiim.sisgesviewbinding.databinding.FragmentRegisterBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.ui.activities.HomeActivity
import com.mihaiim.sisgesviewbinding.ui.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : BaseFragment<LoginViewModel, FragmentRegisterBinding>(
    FragmentRegisterBinding::inflate,
) {

    override val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservables()
        setListeners()
    }

    private fun setObservables() {
        viewModel.successEvent.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        })
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        viewModel.errorResIdEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }

    private fun setListeners() {
        binding.registerBtn.setOnClickListener {
            viewModel.registerUser(
                binding.firstNameEt.text.toString(),
                binding.lastNameEt.text.toString(),
                binding.emailEt.text.toString(),
                binding.passwordEt.text.toString(),
            )
        }
        binding.alreadyHasAnAccount.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}