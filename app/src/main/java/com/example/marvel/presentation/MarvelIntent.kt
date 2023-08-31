package com.example.marvel.presentation

import android.content.Context
import com.example.marvel.data.models.CharacterEntity

sealed class MarvelIntent {
    object LoadCharacters : MarvelIntent()
    data class ToggleFavorite(val character: CharacterEntity) : MarvelIntent()
    data class LoadMoreCharacters(val offset: Int) : MarvelIntent()
    object Home : MarvelIntent()
    object Favorite : MarvelIntent()
    object Refresh : MarvelIntent()
    data class Download(val character: CharacterEntity, val context: Context) : MarvelIntent()
}
