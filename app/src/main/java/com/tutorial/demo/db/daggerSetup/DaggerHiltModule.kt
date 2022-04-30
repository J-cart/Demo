package com.tutorial.demo.db.daggerSetup

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.tutorial.demo.db.AppDatabase
import com.tutorial.demo.others.Constants.KEY_FIRST_TIME_TOGGLE
import com.tutorial.demo.others.Constants.KEY_NAME
import com.tutorial.demo.others.Constants.KEY_WEIGHT
import com.tutorial.demo.others.Constants.RUNNING_DATABASE_NAME
import com.tutorial.demo.others.Constants.SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaggerHiltModule {

    @Singleton
    @Provides
    fun provideRunDatabase(
        @ApplicationContext appContext: Context
    ) = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: AppDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext app: Context): SharedPreferences = app.getSharedPreferences(
        SHARED_PREF_NAME, MODE_PRIVATE
    )

    @Singleton
    @Provides
    fun provideSharedPrefName(sharedPref: SharedPreferences) =
        sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideSharedPrefWeight(sharedPref: SharedPreferences) =
        sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

}