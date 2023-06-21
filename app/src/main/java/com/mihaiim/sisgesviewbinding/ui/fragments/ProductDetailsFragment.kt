package com.mihaiim.sisgesviewbinding.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mihaiim.sisgesviewbinding.databinding.FragmentProductDetailsBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.ui.adapters.ProductPositionsAdapter
import com.mihaiim.sisgesviewbinding.ui.viewmodels.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailsFragment : BaseFragment<ProductViewModel, FragmentProductDetailsBinding>(
    FragmentProductDetailsBinding::inflate,
) {

    private val args: ProductDetailsFragmentArgs by navArgs()

    override val viewModel: ProductViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservables()
        binding.titleTv.text = args.productName
        binding.codeTv.text = args.productCode
        viewModel.getProductDetails(args.productCode)
    }

    private fun setObservables() {
        viewModel.productDetailsEvent.observe(viewLifecycleOwner, EventObserver { product ->
            binding.titleTv.text = product.name
            binding.quantityTv.text = product.getTotalQuantity().toString()
            product.positions?.let { positions ->
                binding.positionsRv.apply {
                    adapter = ProductPositionsAdapter(Array(positions.size) { i ->
                        positions[i].getProductPositionString(requireContext())
                    })
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }
        })
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }
}