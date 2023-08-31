package com.example.marvel.presentation

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marvel.data.models.CharacterEntity
import com.example.marvel.data.models.CharacterModel
import com.example.marvel.domain.repositories.MarvelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.marvel.data.repositories.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

@HiltViewModel
class MainViewModel @Inject constructor(
    private val marvelRepository: MarvelRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MarvelState(emptySet(), true))
    val state: StateFlow<MarvelState>
        get() = _state

    val favoriteCharacters = mutableSetOf<CharacterEntity>()

    init {
        viewModelScope.launch {
            marvelRepository.getFavouriteCharacters().collectLatest {
                favoriteCharacters.addAll(it)
            }
        }
        loadCharacters()
    }

    fun processIntent(intent: MarvelIntent) {
        when (intent) {
            is MarvelIntent.LoadCharacters -> loadCharacters()
            is MarvelIntent.ToggleFavorite -> toggleFavorite(intent.character)
            is MarvelIntent.LoadMoreCharacters -> loadMoreCharacters(intent.offset)
            is MarvelIntent.Refresh -> refresh()
            is MarvelIntent.Home -> _state.value = _state.value.copy(isHome = true)
            is MarvelIntent.Favorite -> _state.value = _state.value.copy(isHome = false)
            is MarvelIntent.Download -> download(
                character = intent.character,
                context = intent.context
            )
        }
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = MarvelState(isLoading = true)
            val result = marvelRepository.getCharacters(0, 20)
            _state.value = when (result) {
                is Result.Error -> MarvelState(error = "에러가 발생하였습니다.\nRefresh 버튼을 눌러주세요.")
                is Result.Success -> MarvelState(characters = currentState.characters + result.data.map { data ->
                    data.toEntity(
                        favoriteCharacters.any { it.id == data.id })
                }, isLoading = false, error = null)
            }
        }
    }

    private fun loadMoreCharacters(offset: Int) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = currentState.copy(isMoreLoading = true)
            val result = marvelRepository.getCharacters(offset, 20)
            _state.value = when (result) {
                is Result.Success -> currentState.copy(
                    characters = currentState.characters + result.data.map { data ->
                        data.toEntity(
                            favoriteCharacters.any { it.id == data.id })
                    },
                    isMoreLoading = false
                )

                is Result.Error -> currentState.copy(
                    error = result.exception.message,
                    isMoreLoading = false
                )
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = currentState.copy(isMoreLoading = true, characters = emptySet())
            loadMoreCharacters(0)
        }
    }

    private fun toggleFavorite(character: CharacterEntity) {
        val toggle = !character.isFavorite
        viewModelScope.launch {
            if (favoriteCharacters.any { it.id == character.id }) {
                favoriteCharacters.removeIf { it.id == character.id }
                marvelRepository.removeCharacterFromFavourite(character)
            } else {
                favoriteCharacters.add(character)
                marvelRepository.addCharacterToFavourite(character)
            }

            val currentState = _state.value
            _state.value = currentState.copy(characters = currentState.characters.map {
                if (it.id == character.id) it.copy(isFavorite = toggle) else it
            }.toSet())
        }
    }

    private fun download(character: CharacterEntity, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                Toast
                    .makeText(
                        context,
                        "${character.name} 썸네일 저장 시작",
                        Toast.LENGTH_LONG
                    )
                    .show()
            }

            val url = URL(character.thumbnailUrl)
            val connection = url.openConnection()
            val inputStream: InputStream = connection.getInputStream()
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "${character.name}_${System.currentTimeMillis()}_thumbnail.jpg"
            )
            val fos = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var len1: Int
            while (inputStream
                    .read(buffer)
                    .also { len1 = it } > 0
            ) {
                fos.write(buffer, 0, len1)
            }
            fos.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                Toast
                    .makeText(
                        context,
                        "${character.name} 썸네일이 저장되었습니다",
                        Toast.LENGTH_LONG
                    )
                    .show()
            }
        }
    }
}
