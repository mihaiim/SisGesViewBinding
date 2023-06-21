package com.mihaiim.sisgesviewbinding.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.databinding.FragmentHomeBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.others.ScanScreenTypeEnum
import com.mihaiim.sisgesviewbinding.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(
    FragmentHomeBinding::inflate,
) {

    @Inject
    lateinit var glide: RequestManager

    override val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.getUserData()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservables()
        setListeners()
    }

    private fun setObservables() {
        viewModel.successEvent.observe(viewLifecycleOwner, EventObserver {
            binding.welcomeTv.text = getString(R.string.hello_user, it.firstName)
            glide.load(it.photoUri)
                .circleCrop()
                .into(binding.profilePictureIv)
        })
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }

    private fun setListeners() {
        binding.productsIn.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections
                .actionHomeFragmentToScanFragment(ScanScreenTypeEnum.IN.value))
        }
        binding.productsOut.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections
                .actionHomeFragmentToScanFragment(ScanScreenTypeEnum.OUT.value))
        }
        binding.moveProducts.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections
                .actionHomeFragmentToScanFragment(ScanScreenTypeEnum.MOVE.value))
        }
        binding.seeStocks.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections
                .actionHomeFragmentToScanFragment(ScanScreenTypeEnum.SEE_STOCKS.value))
        }
        binding.seeInOut.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_administrationFragment)
        }
    }
}