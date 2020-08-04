package com.example.securelocker.ui.sharedpref

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.securelocker.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_shared_pref.*

class SharedPrefFragment : Fragment() {

    private lateinit var viewModel: SharedPrefViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(SharedPrefViewModel::class.java)

        return inflater.inflate(R.layout.fragment_shared_pref, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //observe live data
        Observer()

        //get the user data
        viewModel.getUserName()
        viewModel.getEmail()

        //fab click
        fab_save_pref.setOnClickListener {
            viewModel.userNameField.value = txtUserId.text.toString()
            viewModel.userEmailField.value = txtEmail.text.toString()
            viewModel.saveUserData()
        }
    }

    private fun Observer() {
        viewModel.userNameField.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            txtUserId.setText(it)
        })

        viewModel.userEmailField.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            txtEmail.setText(it)
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
        })
    }
}