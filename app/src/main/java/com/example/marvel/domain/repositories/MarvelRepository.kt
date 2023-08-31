package com.example.marvel.domain.repositories

import com.example.marvel.data.models.CharacterEntity
import com.example.marvel.data.models.CharacterModel
import com.example.marvel.data.repositories.Result
import kotlinx.coroutines.flow.Flow

interface MarvelRepository {
    suspend fun getCharacters(offset: Int, limit: Int): Result<List<CharacterModel>>
    suspend fun addCharacterToFavourite(character: CharacterEntity)
    suspend fun removeCharacterFromFavourite(character: CharacterEntity)
    fun getFavouriteCharacters(): Flow<List<CharacterEntity>>
}
