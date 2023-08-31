package com.example.marvel.presentation

import com.example.marvel.data.models.CharacterEntity

data class MarvelState(
    val characters: Set<CharacterEntity> = emptySet(),
    val isLoading: Boolean = false,
    val isMoreLoading: Boolean = false,
    val isHome: Boolean = true,
    val error: String? = null
)
