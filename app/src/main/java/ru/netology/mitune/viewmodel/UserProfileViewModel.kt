package ru.netology.mitune.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.mitune.auth.AppAuth
import ru.netology.mitune.dto.Job
import ru.netology.mitune.dto.User
import ru.netology.mitune.model.FeedModelState
import ru.netology.mitune.repository.JobRepository
import ru.netology.mitune.repository.UserRepository
import ru.netology.mitune.util.SingleLiveEvent
import javax.inject.Inject

val emptyJob = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = null,
    link = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val jobRepository: JobRepository,
    private val appAuth: AppAuth,
) : ViewModel() {

    val myId: Int = appAuth.authStateFlow.value.id

    val editedJob: MutableLiveData<Job> = MutableLiveData(emptyJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    val data: MutableLiveData<List<User>> = userRepository.data
    val userData: MutableLiveData<User> = userRepository.userData

    val jobData: LiveData<List<Job>> = jobRepository.data.asLiveData(Dispatchers.Default)


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState



    fun getUserById(id: Int) {
        viewModelScope.launch {
            try {
                userRepository.getUserById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }


    fun saveJob() {
        viewModelScope.launch {
            try {
                val job = requireNotNull(editedJob.value)
                jobRepository.saveJob(job)
                _dataState.value = FeedModelState(loading = false)
                deleteEditJob()
                _jobCreated.value = Unit

                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun removeJobById(id: Int) {
        viewModelScope.launch {
            try {
                jobRepository.removeJobById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getMyJobs() {
        viewModelScope.launch {
            try {
                jobRepository.getMyJobs()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun deleteEditJob() {
        editedJob.postValue(emptyJob)
    }

    fun editJob(job: Job) {
        editedJob.value = job
    }

    fun changeJobCompany(company: String) {
        var text = company.trim()

        if (editedJob.value?.name == text) return
        editedJob.value = editedJob.value?.copy(name = text)
    }


    fun changeJobPosition(position: String) {
        var positionText = position.trim()
        if (editedJob.value?.position == positionText) return
        editedJob.value = editedJob.value?.copy(position = positionText)
    }

    fun changeJobLink(link: String?) {
        var linkText = link?.trim()

        if (editedJob.value?.link == linkText) return
        editedJob.value = editedJob.value?.copy(link = linkText)
    }

    fun updateStartDate(date: String) {
        editedJob.value = editedJob.value?.copy(start = date)
    }

    fun updateEndDate(date: String?) {
        editedJob.value = editedJob.value?.copy(finish = date)
    }

    fun logOut() = viewModelScope.launch {

        try {
            _dataState.value = FeedModelState(loading = true)

            appAuth.removeAuth()
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }

    }

}
