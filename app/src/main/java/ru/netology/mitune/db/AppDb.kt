package ru.netology.mitune.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.mitune.dao.*
import ru.netology.mitune.entity.*

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class, UserEntity::class, JobEntity::class, EventEntity::class, EventRemoteKeyEntity::class], version = 5, exportSchema = false)
@TypeConverters(InstantConverter::class, ListConverter::class, UserMapConverter::class, CoordinatesConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao() : PostDao
    abstract fun postRemoteKeyDao() : PostRemoteKeyDao
    abstract fun userDao() : UserDao
    abstract fun jobDao() : JobDao

    abstract fun eventDao() : EventDao

    abstract fun eventRemoteKeyDao() : EventRemoteKeyDao
}