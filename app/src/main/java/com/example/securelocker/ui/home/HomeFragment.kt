package com.example.securelocker.ui.home


import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.example.securelocker.R
import com.example.securelocker.data.FileEntity
import com.example.securelocker.ui.home.adaptor.FileAdaptor
import com.example.securelocker.ui.image.ImageViewModel
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.auth_dialog.*
import kotlinx.android.synthetic.main.detail_dialog.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_image.*
import kotlinx.android.synthetic.main.key_edit_dialog.*
import java.io.File

class HomeFragment : Fragment() {
    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var viewModel: HomeViewModel
    private var fileListEntity = ArrayList<FileEntity>()
    lateinit var adapter: FileAdaptor
    private var mPosition: Int = 0
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricPrompt: BiometricPrompt

    private val callBack = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Log.e(TAG, "Biometric auth sucessful")
            dialogupdateMasterKey()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.e(TAG, "Error: $errorCode ${errString.toString()}")
        }
    }

    private val onClickListener: (Int) -> Unit = {

        mPosition = it
        viewModel.fileName.value = fileListEntity[mPosition].fileName
        // get the master key
        viewModel.getMasterKey()
        if (viewModel.masterKey.value.isNullOrEmpty()) {
            showDetailDialog(fileListEntity[mPosition].file)
        } else {
            dialogAuth()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setup biometric
        setupBiometric()

        //option menu
        setHasOptionsMenu(true)
        //observe live data
        Observer()

        // get the list wof file
        viewModel.getFileList()
        viewModel.getMasterKey()
    }

    private fun Observer() {

        viewModel.isProcess.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
        })

        viewModel.fileListEntity.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            fileListEntity = it
            setupRecyclerView()
        })
    }

    private fun setupRecyclerView() {
        adapter = FileAdaptor(fileListEntity, onClickListener)
        fileRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        fileRecyclerView.adapter = adapter
    }

    private fun showDetailDialog(file: File) {
        val extension = file.extension
        val dialog = MaterialDialog(requireContext())
            .customView(R.layout.detail_dialog)
            .cornerRadius(8f)
        val customView = dialog.getCustomView()

        if (extension == "jpg") {
            customView.dialog_imageView.visibility = View.VISIBLE
            viewModel.bitmap.observe(viewLifecycleOwner, Observer {
                customView.dialog_imageView.setImageBitmap(it)
            })
            viewModel.getEncryptedBitmap()
        } else {
            customView.dialog_textView.visibility = View.VISIBLE
            viewModel.message.observe(viewLifecycleOwner, Observer {
                customView.dialog_textView.text = it
            })
            viewModel.getEncryptedFile()
        }
        dialog.show()
    }

    private fun dialogSetMasterKey() {
        MaterialDialog(requireContext()).show() {
            val view = customView(R.layout.key_setup_dialog)
            positiveButton(R.string.dialog_ok) {
                val key = view.txtApiKey.text.toString()
                viewModel.masterKey.value = key
                viewModel.setMasterKey()
                dismiss()
            }
            negativeButton(R.string.dialog_close) {
                it.dismiss()
            }
        }
    }

    private fun setupBiometric() {
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unloc")
            .setDescription("Scan Fingerprint")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            callBack
        )
    }

    private fun dialogupdateMasterKey() {
        MaterialDialog(requireContext()).show() {
            val view = customView(R.layout.key_edit_dialog)
            positiveButton(R.string.dialog_ok) {
                val oldKey = view.txtOldApiKey.text.toString()
                val newKey = view.txtNewApiKey.text.toString()
                viewModel.masterKey.value = oldKey
                viewModel.newMasterKey.value = newKey
                viewModel.updateMasterKey()
                dismiss()
            }
            negativeButton(R.string.dialog_close) {
                it.dismiss()
            }
        }
    }

    private fun dialogAuth() {
        MaterialDialog(requireContext()).show() {
            val view = customView(R.layout.auth_dialog)
            positiveButton(R.string.dialog_ok) {
                val key = view.txtApiKey.text.toString()
                if (key == viewModel.masterKey.value) {
                    showDetailDialog(fileListEntity[mPosition].file)
                } else {
                    viewModel.snackBarMessage.value = "Auth falied"
                    it.dismiss()
                }
            }
            negativeButton(R.string.dialog_close) {
                it.dismiss()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_master_key -> {
                viewModel.getMasterKey()
                if (viewModel.masterKey.value.isNullOrEmpty()) {
                    dialogSetMasterKey()
                } else {

                    when (BiometricManager.from(requireContext()).canAuthenticate()){
                        BiometricManager.BIOMETRIC_SUCCESS ->{
                            biometricPrompt.authenticate(promptInfo)
                        }
                        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{
                            dialogupdateMasterKey()
                        }
                    }


                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}