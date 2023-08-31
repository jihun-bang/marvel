package com.example.marvel.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class MarvelResponse(
    val code: Int,
    val status: String,
    val data: MarvelData
)

data class MarvelData(
    val offset: Int,
    val limit: Int,
    val total: Int,
    val count: Int,
    val results: List<CharacterModel>
)

data class CharacterModel(
    val id: Int,
    val name: String,
    val description: String,
    val modified: String,
    val resourceURI: String,
    val urls: List<Url>,
    val thumbnail: Thumbnail,
    val comics: Comics,
    val stories: Stories,
    val events: Events,
    val series: Series
) {
    fun toEntity(isFavorite: Boolean): CharacterEntity {
        return CharacterEntity(
            id = id,
            name = name,
            thumbnailUrl = "${thumbnail.path}.${thumbnail.extension}",
            urls = urls.size,
            comics = comics.items.size,
            stories = stories.items.size,
            events = events.items.size,
            series = stories.items.size,
            isFavorite = isFavorite,
        )
    }
}

data class Url(val type: String, val url: String)
data class Thumbnail(val path: String, val extension: String)
data class Comics(
    val available: Int,
    val returned: Int,
    val collectionURI: String,
    val items: List<Item>
)

data class Stories(
    val available: Int,
    val returned: Int,
    val collectionURI: String,
    val items: List<StoryItem>
)

data class Events(
    val available: Int,
    val returned: Int,
    val collectionURI: String,
    val items: List<Item>
)

data class Series(
    val available: Int,
    val returned: Int,
    val collectionURI: String,
    val items: List<Item>
)

data class Item(val resourceURI: String, val name: String)
data class StoryItem(val resourceURI: String, val name: String, val type: String)
