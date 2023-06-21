package com.mihaiim.sisgesviewbinding.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.mihaiim.sisgesviewbinding.others.Constants
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

abstract class BaseFragment<VM : ViewModel, VB : ViewBinding>(
    private val bindingInflater: (
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean,
    ) -> VB
) : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: VB? = null
    val binding: VB get() = _binding!!

    abstract val viewModel: VM

    open fun VB.initialize() {}
    open fun onPermissionsGranted() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = bindingInflater.invoke(inflater, container, false)
        if (_binding == null) {
            throw IllegalArgumentException("Binding cannot be null")
        }
        binding.initialize()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        onPermissionsGranted()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    protected fun checkAndRequestCameraPermission(rationale: String): Boolean {
        val hasPermission = EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.CAMERA
        )
        if (!hasPermission) {
            EasyPermissions.requestPermissions(
                this,
                rationale,
                Constants.REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
        }
        return hasPermission
    }
}