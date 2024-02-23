package ru.netology.mitune.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.mitune.auth.AppAuth
import ru.netology.mitune.dto.AttachmentType
import ru.netology.mitune.dto.MediaModel
import ru.netology.mitune.dto.MediaUpload
import ru.netology.mitune.error.UnknownAppError
import ru.netology.mitune.model.FeedModelState
import ru.netology.mitune.repository.AuthRepository
import java.io.File
import javax.inject.Inject


private val noPhoto = MediaModel()

@HiltViewModel
class RegistrationLoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: AppAuth,
) : ViewModel() {


    private val _isSignedIn = MutableLiveData(false)
    val isSignedIn: LiveData<Boolean>
        get() = _isSignedIn

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<MediaModel>
        get() = _photo

    fun invalidateSignedInState() {
        _isSignedIn.value = false
    }

    fun invalidateDataState() {
        _dataState.value = FeedModelState()
    }

    fun register(login: String, pass: String, name: String) = viewModelScope.launch {

        try {
            _dataState.value = FeedModelState(loading = true)
            val response = repository.registerNewUser(login, pass, name)
            val id = response.id
            val token = response.token ?: "null"

            auth.setAuth(id, token)
            invalidateDataState()
            _isSignedIn.value = true
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)

            UnknownAppError
        }

    }


    fun registerWithPhoto(
        login: String,
        pass: String,
        name: String,
        media: MediaUpload,
    ) = viewModelScope.launch {

        try {
            _dataState.value = FeedModelState(loading = true)
            val response = repository.registerWithPhoto(login, pass, name, media)
            response.token?.let {
                auth.setAuth(response.id, it)
            }
            _isSignedIn.value = true
            _dataState.value = FeedModelState()
            invalidateDataState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = MediaModel(uri, file, AttachmentType.IMAGE)
    }

    fun signIn(login: String, password: String) = viewModelScope.launch {

        try {
            _dataState.value = FeedModelState(loading = true)

            val response = repository.signIn(login, password)
            response.token?.let {
                auth.setAuth(response.id, it)
            }
            _dataState.value = FeedModelState()
            _isSignedIn.value = true
            invalidateSignedInState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }

    }



}