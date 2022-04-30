package com.tutorial.demo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [RunEntity::class],
    version = 1)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun getRunDao():RunEntityDao
}