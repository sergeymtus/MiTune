package ru.netology.mitune.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.mitune.dto.Attachment
import ru.netology.mitune.dto.AttachmentType
import ru.netology.mitune.dto.EventRequest
import ru.netology.mitune.dto.EventType
import ru.netology.mitune.model.FeedModelState
import ru.netology.mitune.repository.NewEventRepository
import ru.netology.mitune.util.SingleLiveEvent
import javax.inject.Inject

val editedEvent = EventRequest(
    id = 0,
    content = "",
    datetime = null,
    coords = null,
    type = EventType.OFFLINE,
    attachment = null,
    link = null,
    speakerIds = listOf()
)

@ExperimentalCoroutinesApi
@HiltViewModel
class NewEventViewModel @Inject constructor(
    private val repository: NewEventRepository
) : ViewModel() {


    val newEvent: MutableLiveData<EventRequest> = MutableLiveData(editedEvent)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun getEvent(id: Int) {
        viewModelScope.launch {
            try {
                newEvent.value = repository.getEvent(id)
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }


    fun addEvent(content: String) {
        newEvent.value = newEvent.value?.copy(content = content)
        val event = requireNotNull(newEvent.value)
        viewModelScope.launch {
            try {
                repository.addEvent(event)
                _dataState.value = FeedModelState(error = false)
                _eventCreated.value = Unit
                deleteEditPost()
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun addLink(link: String) {
        if (link.isBlank()) {
            newEvent.value = newEvent.value?.copy(link = null)
        } else {
            newEvent.value = newEvent.value?.copy(link = link)
        }
    }

    fun addPictureToEvent(image: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val media = repository.addPictureToTheEvent(AttachmentType.IMAGE, image)
                newEvent.value = newEvent.value?.copy(attachment = Attachment(media.url, AttachmentType.IMAGE) )
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun addDateTime(dateTime: String) {
        newEvent.value = newEvent.value?.copy(datetime = dateTime)
    }

    fun addTypeEvent() {
        val type = when (newEvent.value?.type) {
            EventType.OFFLINE -> EventType.ONLINE
            else -> EventType.OFFLINE
        }
        newEvent.value = newEvent.value?.copy(type = type)
    }

    fun deletePicture() {
        newEvent.value = newEvent.value?.copy(attachment = null)
    }


    fun deleteEditPost() {
        newEvent.postValue(editedEvent)
    }
}