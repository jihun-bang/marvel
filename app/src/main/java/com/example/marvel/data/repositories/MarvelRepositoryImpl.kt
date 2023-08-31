package com.example.marvel.data.repositories

import com.example.marvel.data.datasources.local.CharacterDao
import com.example.marvel.data.datasources.remote.MarvelApi
import com.example.marvel.data.models.CharacterEntity
import com.example.marvel.data.models.CharacterModel
import com.example.marvel.domain.repositories.MarvelRepository

class MarvelRepositoryImpl(
    private val marvelApi: MarvelApi,
    private val characterDao: CharacterDao
) : MarvelRepository {
    override suspend fun getCharacters(offset: Int, limit: Int): Result<List<CharacterModel>> {
        return try {
            val response = marvelApi.getCharacters(
                offset = offset,
                limit = limit,
            )
            if (response.isSuccessful) {
                val characters = response.body()?.data?.results ?: emptyList()
                Result.Success(characters)
            } else {
                Result.Error(Exception("API error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addCharacterToFavourite(character: CharacterEntity) =
        characterDao.insert(character)

    override suspend fun removeCharacterFromFavourite(character: CharacterEntity) =
        characterDao.delete(character)

    override fun getFavouriteCharacters() =
        characterDao.getFavouriteCharacters()
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
