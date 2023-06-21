package com.mihaiim.sisgesviewbinding.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mihaiim.sisgesviewbinding.databinding.DialogProfilePictureBinding

class ProfilePictureDialog : DialogFragment() {

    companion object {
        val TAG: String = ProfilePictureDialog::class.java.simpleName
    }

    private var _binding: DialogProfilePictureBinding? = null
    private val binding get() = _binding!!

    private lateinit var takePictureAction: () -> Unit
    private lateinit var chooseFromGalleryAction: () -> Unit
    private var removePictureAction: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogProfilePictureBinding.inflate(layoutInflater)
        binding.takePictureTv.setOnClickListener {
            takePictureAction()
        }
        binding.chooseFromGalleryTv.setOnClickListener {
            chooseFromGalleryAction()
        }
        if (removePictureAction == null) {
            binding.removePictureSeparator.visibility = View.GONE
            binding.removePictureTv.visibility = View.GONE
        } else {
            binding.removePictureTv.setOnClickListener {
                removePictureAction?.invoke()
            }
        }
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun takePictureAction(action: (ProfilePictureDialog) -> Unit) = this.apply {
        takePictureAction = { action(this) }
    }

    fun chooseFromGalleryAction(action: (ProfilePictureDialog) -> Unit) = this.apply {
        chooseFromGalleryAction = { action(this) }
    }

    fun removePictureAction(action: (ProfilePictureDialog) -> Unit) = this.apply {
        removePictureAction = { action(this) }
    }
}