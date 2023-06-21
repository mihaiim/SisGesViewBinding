package com.mihaiim.sisgesviewbinding.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.mihaiim.sisgesviewbinding.BuildConfig
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.databinding.FragmentProfileBinding
import com.mihaiim.sisgesviewbinding.domain.model.EventObserver
import com.mihaiim.sisgesviewbinding.ui.ProfilePictureDialog
import com.mihaiim.sisgesviewbinding.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment<ProfileViewModel, FragmentProfileBinding>(
    FragmentProfileBinding::inflate,
) {

    @Inject
    lateinit var glide: RequestManager

    private var tempPhotoUri: Uri? = null
    private var photoUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) {
                return@registerForActivityResult
            }
            tempPhotoUri?.let {
                photoUri = it
                glide.load(photoUri)
                    .circleCrop()
                    .into(binding.profilePictureIv)
            }
        }

    private val choosePictureLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                photoUri = it
                glide.load(photoUri)
                    .circleCrop()
                    .into(binding.profilePictureIv)
            }
        }

    override val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservables()
        setData()
        setListeners()
    }

    override fun onPermissionsGranted() {
        takePicture()
    }

    private fun setObservables() {
        viewModel.successEvent.observe(viewLifecycleOwner, EventObserver {
            requireActivity().onBackPressed()
        })
        viewModel.pictureRemovedEvent.observe(viewLifecycleOwner, EventObserver {
            binding.profilePictureIv.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_default_profile_picture,
                )
            )
            Toast.makeText(
                requireContext(),
                R.string.profile_picture_removed_successfully,
                Toast.LENGTH_LONG,
            ).show()
        })
        viewModel.failEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
        viewModel.errorResIdEvent.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }

    private fun setData() {
        val user = viewModel.user
        binding.lastNameEt.setText(user.lastName)
        binding.firstNameEt.setText(user.firstName)
        binding.emailEt.setText(user.email)
        glide.load(user.photoUri)
            .circleCrop()
            .into(binding.profilePictureIv)
    }

    private fun setListeners() {
        binding.profilePictureContainer.setOnClickListener {
            if (checkAndRequestCameraPermission(
                    getString(R.string.camera_permission_denied_message_profile_picture))) {
                val dialog = ProfilePictureDialog()
                    .takePictureAction {
                        it.dismiss()
                        takePicture()
                    }
                    .chooseFromGalleryAction {
                        it.dismiss()
                        choosePicture()
                    }
                viewModel.user.photoUri?.let {
                    dialog.removePictureAction {
                        it.dismiss()
                        viewModel.removeProfilePicture()
                    }
                }
                dialog.show(parentFragmentManager, ProfilePictureDialog.TAG)

            }
        }
        binding.saveBtn.setOnClickListener {
            viewModel.updateUser(
                binding.firstNameEt.text.toString(),
                binding.lastNameEt.text.toString(),
                binding.emailEt.text.toString(),
                binding.newPasswordEt.text.toString(),
                photoUri,
            )
        }
    }

    private fun choosePicture() = choosePictureLauncher.launch("image/*")

    private fun takePicture() {
        lifecycleScope.launchWhenStarted {
            getTempFileUri().let { uri ->
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    private fun getTempFileUri(): Uri {
        val tmpFile = File.createTempFile("temp_image_file", ".png").apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }
}