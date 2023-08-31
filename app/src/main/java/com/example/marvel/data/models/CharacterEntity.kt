package com.example.marvel.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character")
data class CharacterEntity(
        @PrimaryKey val id: Int,
        val isFavorite: Boolean,
        val name: String,
        val thumbnailUrl: String,
        val urls: Int,
        val comics: Int,
        val stories: Int,
        val events: Int,
        val series: Int,
)