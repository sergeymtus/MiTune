package ru.netology.mitune.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.mitune.R
import ru.netology.mitune.auth.AppAuth
import ru.netology.mitune.databinding.FragmentLoginBinding
import ru.netology.mitune.repository.AuthRepository
import ru.netology.mitune.util.Utils
import ru.netology.mitune.viewmodel.RegistrationLoginViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {
//    @Inject
//    lateinit var auth: AppAuth
//
//    @Inject
//    lateinit var repository: AuthRepository

    private val viewModel: RegistrationLoginViewModel by activityViewModels()
    private lateinit var navController: NavController

    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

   // private lateinit var savedStateHandle: SavedStateHandle
   private var savedStateHandle = SavedStateHandle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        navController = findNavController()
        savedStateHandle = requireNotNull(navController.previousBackStackEntry).savedStateHandle
        savedStateHandle.set(LOGIN_SUCCESSFUL, false)

        viewModel.dataState.observe(viewLifecycleOwner) {
            binding.progress.isVisible = it.loading
        }

        binding.signInBtn.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            if (binding.login.text.isNullOrBlank() || binding.password.text.isNullOrBlank()) {
                Toast.makeText(
                    activity,
                    getString(R.string.field_cant_be_empty),
                    Toast.LENGTH_LONG
                )
                    .show()
            } else {
                viewModel.signIn(login, pass)
                Utils.hideKeyboard(requireView())
            }
        }

        viewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedId ->
            if (isSignedId) {
                savedStateHandle.set(LOGIN_SUCCESSFUL, true)
                navController.popBackStack()
                viewModel.invalidateSignedInState()
            }
        }
        return binding.root
    }

}