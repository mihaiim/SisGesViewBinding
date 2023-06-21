package com.mihaiim.sisgesviewbinding.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.databinding.FragmentProductPositionsBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.ui.adapters.ProductPositionsAdapter
import com.mihaiim.sisgesviewbinding.ui.viewmodels.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductPositionsFragment : BaseFragment<ProductViewModel, FragmentProductPositionsBinding>(
    FragmentProductPositionsBinding::inflate,
) {

    private val args: ProductPositionsFragmentArgs by navArgs()

    override val viewModel: ProductViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservables()
        binding.titleTv.text = getString(R.string.product_positions_title, args.productName)
        viewModel.getProductPositions(args.productCode)
    }

    private fun setObservables() {
        viewModel.positionsEvent.observe(viewLifecycleOwner, EventObserver {
            binding.positionsRv.apply {
                adapter = ProductPositionsAdapter(Array(it.size) { i ->
                    it[i].getProductPositionString(requireContext())
                })
                layoutManager = LinearLayoutManager(requireContext())
            }
        })
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }
}