package ru.netology.mitune.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.mitune.dao.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Provides
    @Singleton
   fun provideDb(
        @ApplicationContext context : Context
   ) : AppDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun providesPostDao(
        appDb : AppDb
    ) : PostDao = appDb.postDao()

    @Provides
    @Singleton
    fun providesPostRemoteKeyDao(
        appDb: AppDb
    ) : PostRemoteKeyDao = appDb.postRemoteKeyDao()

    @Provides
    @Singleton
    fun providesUserDao(
        appDb: AppDb
    ) : UserDao = appDb.userDao()

    @Provides
    @Singleton
    fun providesJobDao(
        appDb: AppDb
    ) : JobDao = appDb.jobDao()

    @Provides
    @Singleton
    fun providesEventDao(
        appDb: AppDb
    ) : EventDao = appDb.eventDao()

    @Provides
    @Singleton
    fun providesEventRemoteKeyDao(
        appDb: AppDb
    ) : EventRemoteKeyDao = appDb.eventRemoteKeyDao()

}