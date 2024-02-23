package ru.netology.mitune.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.netology.mitune.auth.AppAuth
import ru.netology.mitune.auth.AuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
) : ViewModel() {
    val authState: LiveData<AuthState> = appAuth.authStateFlow.asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = appAuth.authStateFlow.value.id != 0
}