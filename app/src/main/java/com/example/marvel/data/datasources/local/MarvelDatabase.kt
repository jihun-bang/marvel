package com.example.marvel.data.datasources.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.marvel.data.models.CharacterEntity

@Database(entities = [CharacterEntity::class], version = 1)
abstract class MarvelDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}
