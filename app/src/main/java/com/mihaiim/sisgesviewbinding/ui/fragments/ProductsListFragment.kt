package com.mihaiim.sisgesviewbinding.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mihaiim.sisgesviewbinding.databinding.FragmentProductsListBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.ui.adapters.ProductAdapter
import com.mihaiim.sisgesviewbinding.ui.viewmodels.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductsListFragment : BaseFragment<ProductViewModel, FragmentProductsListBinding>(
    FragmentProductsListBinding::inflate,
) {

    private val args: ProductsListFragmentArgs by navArgs()

    override val viewModel: ProductViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservables()
        viewModel.getProductsList(args.searchTerm)
    }

    private fun setObservables() {
        viewModel.productsListEvent.observe(viewLifecycleOwner, EventObserver {
            val dataSet = Array(it.size) { i -> it[i] }
            binding.positionsRv.apply {
                adapter = ProductAdapter(dataSet) {
                    findNavController().navigate(
                        ProductsListFragmentDirections
                            .actionProductsListFragmentToProductDetailsFragment(it.code, it.name))
                }
                layoutManager = LinearLayoutManager(requireContext())
            }
        })
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        })
    }
}