package com.example.marvel.data.datasources.local

import androidx.room.*
import com.example.marvel.data.models.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterEntity)

    @Delete
    suspend fun delete(character: CharacterEntity)

    @Query("SELECT * FROM character")
    fun getFavouriteCharacters(): Flow<List<CharacterEntity>>
}
