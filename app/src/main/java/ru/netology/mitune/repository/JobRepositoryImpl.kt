package ru.netology.mitune.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.mitune.apiservice.ApiService
import ru.netology.mitune.dao.JobDao
import ru.netology.mitune.dto.Job
import ru.netology.mitune.entity.JobEntity
import ru.netology.mitune.entity.toDto
import ru.netology.mitune.entity.toEntity
import ru.netology.mitune.error.ApiError
import ru.netology.mitune.error.NetworkError
import java.io.IOException
import javax.inject.Inject


class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao
): JobRepository {



    override val data: Flow<List<Job>> = jobDao.getAllJobs()
        .map(List<JobEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getUserJobs(id: Int) {
        try {
            val response = apiService.getJobsByUserId(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val response = apiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun removeJobById(id: Int) {
        try {
            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            jobDao.removeJobById(id)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getMyJobs() {
        try {
            val response = apiService.getMyJobs()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        }
    }
}