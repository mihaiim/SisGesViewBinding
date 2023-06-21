package com.mihaiim.sisgesviewbinding.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.databinding.FragmentScanBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.others.ScanScreenTypeEnum
import com.mihaiim.sisgesviewbinding.ui.viewmodels.ScanViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanFragment : BaseFragment<ScanViewModel, FragmentScanBinding>(
    FragmentScanBinding::inflate,
) {

    private val args: ScanFragmentArgs by navArgs()

    override val viewModel: ScanViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.screenType = ScanScreenTypeEnum.values()[args.screenType]
        setObservables()
        setListeners()
        binding.barcodeView.setStatusText("")
        binding.input1.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.scanInProgress) {
            binding.barcodeView.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pauseAndWait()
    }

    override fun onPermissionsGranted() {
        startScan()
    }

    private fun setObservables() {
        viewModel.quantityLeft.observe(viewLifecycleOwner) { quantity ->
            clearAllInputs()
            binding.input1.requestFocus()
            closeKeyboard()
            if (viewModel.screenType == ScanScreenTypeEnum.IN) {
                binding.warningTv.text = resources.getQuantityString(
                    R.plurals.products_in_left,
                    quantity,
                    quantity,
                )
            } else {
                binding.warningTv.text = resources.getQuantityString(
                    R.plurals.products_out_left,
                    quantity,
                    quantity,
                )
            }
        }
        viewModel.nextStepEvent.observe(viewLifecycleOwner) { step ->
            nextStep(step)
        }
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        viewModel.errorResIdEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        viewModel.navigateBackEvent.observe(viewLifecycleOwner, EventObserver {
            stopScan()
            requireActivity().onBackPressed()
        })
        viewModel.navigateToProductsList.observe(viewLifecycleOwner, EventObserver {
            stopScan()
            findNavController().navigate(
                ScanFragmentDirections.actionScanFragmentToProductsListFragment(it))
        })
        viewModel.navigateToProductDetails.observe(viewLifecycleOwner, EventObserver {
            stopScan()
            findNavController().navigate(
                ScanFragmentDirections.actionScanFragmentToProductDetailsFragment(it))
        })
    }

    private fun setListeners() {
        binding.informationTv.setOnClickListener {
            viewModel.product?.let {
                findNavController().navigate(ScanFragmentDirections
                    .actionScanFragmentToProductPositionsFragment(it.code, it.name))
            }
        }
        binding.barcodeView.decodeContinuous {
            handleScanResult(it.text)
            stopScan()
        }
        binding.tvOverBarcodeView.setOnClickListener {
            if (checkAndRequestCameraPermission(
                    getString(R.string.camera_permission_denied_message_scan))) {
                startScan()
            }
        }
        binding.button.setOnClickListener {
            viewModel.goForward(
                binding.input1.text.toString(),
                binding.input2.text.toString(),
                binding.input3.text.toString(),
                binding.input4.text.toString(),
            )
        }
    }

    private fun nextStep(step: Int) {
        when (viewModel.screenType) {
            ScanScreenTypeEnum.IN -> nextStepIn(step)
            ScanScreenTypeEnum.OUT -> nextStepOut(step)
            ScanScreenTypeEnum.MOVE -> nextStepMove(step)
            ScanScreenTypeEnum.SEE_STOCKS -> nextStepSeeStocks()
        }
    }

    private fun nextStepIn(step: Int) {
        setInputsVisibility(true)
        binding.informationTv.visibility = GONE
        binding.input2.inputType = InputType.TYPE_CLASS_NUMBER
        binding.input2.setHint(R.string.quantity)
        when (step) {
            0 -> {
                binding.descriptionTv.setText(R.string.products_in_description)
                binding.input1.inputType = InputType.TYPE_CLASS_NUMBER
                binding.input1.setHint(R.string.product_code)
                binding.warningTv.visibility = GONE
                binding.button.setText(R.string.next)
            }
            else -> {
                binding.descriptionTv.setText(R.string.products_in_shelf_description)
                binding.input1.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                binding.input1.setHint(R.string.shelf_code)
                binding.warningTv.visibility = VISIBLE
                binding.button.setText(R.string.deposit)
            }
        }
    }

    private fun nextStepOut(step: Int) {
        setInputsVisibility(true)
        binding.input2.inputType = InputType.TYPE_CLASS_NUMBER
        binding.input2.setHint(R.string.quantity)
        when (step) {
            0 -> {
                binding.informationTv.visibility = GONE
                binding.descriptionTv.setText(R.string.products_out_description)
                binding.input1.inputType = InputType.TYPE_CLASS_NUMBER
                binding.input1.setHint(R.string.product_code)
                binding.warningTv.visibility = GONE
                binding.button.setText(R.string.next)
            }
            else -> {
                binding.informationTv.visibility = VISIBLE
                binding.descriptionTv.setText(R.string.products_out_shelf_description)
                binding.input1.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                binding.input1.setHint(R.string.shelf_code)
                binding.warningTv.visibility = VISIBLE
                binding.button.setText(R.string.collect)
            }
        }
    }

    private fun nextStepMove(step: Int) {
        binding.informationTv.visibility = GONE
        binding.descriptionTv.setText(R.string.move_products_description)
        binding.input1.inputType = InputType.TYPE_CLASS_NUMBER
        binding.input1.setHint(R.string.product_code)
        binding.input2.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        binding.input2.setHint(R.string.current_shelf_code)
        binding.input3.inputType = InputType.TYPE_CLASS_NUMBER
        binding.input3.setHint(R.string.quantity)
        binding.input4.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        binding.input4.setHint(R.string.new_shelf_code)
        binding.warningTv.visibility = GONE

        when (step) {
            0 -> {
                setInputsVisibility(true)
                binding.button.setText(R.string.next)
            }
            1 -> {
                setInputsVisibility(true, true)
                binding.button.setText(R.string.next)
            }
            else -> {
                setInputsVisibility(true, true, true)
                binding.button.setText(R.string.finish)
            }
        }
    }

    private fun nextStepSeeStocks() {
        binding.informationTv.visibility = GONE
        binding.descriptionTv.setText(R.string.see_stocks_description)
        binding.warningTv.visibility = GONE
        binding.input1.setHint(R.string.product_code_or_name)
        setInputsVisibility()
        binding.button.setText(R.string.see_stock)
    }

    private fun startScan() {
        viewModel.scanInProgress = true
        binding.tvOverBarcodeView.visibility = GONE
        binding.barcodeView.resume()
    }

    private fun stopScan() {
        viewModel.scanInProgress = false
        binding.tvOverBarcodeView.visibility = VISIBLE
        binding.barcodeView.pauseAndWait()
    }

    private fun handleScanResult(text: String) {
        requireActivity().currentFocus?.let { view ->
            if (view is AppCompatEditText) {
                view.setText(text)
            }
        }
    }

    private fun setInputsVisibility(
        input2: Boolean = false,
        input3: Boolean = false,
        input4: Boolean = false,
    ) {
        binding.input2.visibility = if (input2) VISIBLE else GONE
        binding.input3.visibility = if (input3) VISIBLE else GONE
        binding.input4.visibility = if (input4) VISIBLE else GONE
    }

    private fun clearAllInputs() {
        binding.input1.setText("")
        binding.input2.setText("")
        binding.input3.setText("")
        binding.input4.setText("")
    }

    private fun closeKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val imm = requireActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE,
            ) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}