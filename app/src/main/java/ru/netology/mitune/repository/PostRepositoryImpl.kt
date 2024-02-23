package ru.netology.mitune.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.mitune.apiservice.ApiService
import ru.netology.mitune.dao.PostDao
import ru.netology.mitune.dto.*
import ru.netology.mitune.entity.PostEntity
import ru.netology.mitune.entity.toEntity
import ru.netology.mitune.error.ApiError
import ru.netology.mitune.error.AppError
import ru.netology.mitune.error.NetworkError
import ru.netology.mitune.error.UnknownAppError
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    mediator: PostRemoteMediator

) : PostRepository {

    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getAllPosts() },
        remoteMediator = mediator
    ).flow.map {
        it.map(PostEntity::toDto)
    }

    override val postUserData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())


    override suspend fun getPosts() {
        try {
            val response = apiService.getPosts()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            if (postDao.isEmpty()) {
                postDao.insert(body.toEntity())
            }

            if (body.size > postDao.countPosts()) {
                val notInRoomPosts = body.takeLast(body.size - postDao.countPosts())
                postDao.insert(notInRoomPosts.toEntity())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: java.lang.Exception) {
            throw UnknownAppError
        }
    }

    override suspend fun savePost(post: Post) {
        try {
            val response = apiService.savePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e : CancellationException) {
            throw CancellationException()
        }
        catch (e: java.lang.Exception) {
            throw UnknownAppError
        }
    }

    override suspend fun removePostById(postId: Int) {
        try {
            val response = apiService.removePostById(postId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            postDao.removeById(postId)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: java.lang.Exception) {
            throw UnknownAppError
        }
    }


    override suspend fun savePostWithAttachment(
        post: Post,
        media: MediaUpload,
        type: AttachmentType
    ) {

        try {
            val upload = uploadMedia(media)
            val postWithAttach = post.copy(attachment = Attachment(upload.url, type))

            savePost(postWithAttach)

        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: Exception) {
            throw UnknownAppError
        }
    }


    override suspend fun getLikedMentionedUsersList(post: Post) {
        try {
            val response = apiService.getPostById(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val usersList = response.body()?.users?.values?.toMutableList()
            postUserData.postValue(requireNotNull(usersList))
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun likeById(id: Int): Post {
        try {
            val response = apiService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))

            return body
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun unlikeById(id: Int): Post {
        try {
            val response = apiService.unlikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getUsers(): List<User> {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getPostById(id: Int): Post {
        try {
            val response = apiService.getPostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getUserById(id: Int): User {
        try {
            val response = apiService.getUserById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun uploadMedia(
        upload: MediaUpload,
    ): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )
            val response = apiService.uploadMedia(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }
}