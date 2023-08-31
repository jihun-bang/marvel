package com.example.marvel.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.marvel.data.models.CharacterEntity
import com.example.marvel.ui.theme.MarvelTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MarvelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppView(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppView(viewModel: MainViewModel) {
    val state = viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = if (state.value.isHome) "홈" else "즐겨찾기",
                )
            })
        },
        bottomBar = { BottomNavigation(viewModel) },
        floatingActionButton = { Fab(viewModel) }
    ) { padding ->
        Characters(
            viewModel = viewModel,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun Fab(viewModel: MainViewModel) {
    FloatingActionButton(onClick = {
        viewModel.processIntent(MarvelIntent.Refresh)
    }) {
        Icon(Icons.Default.Refresh, contentDescription = "")
    }
}

@Composable
fun Characters(viewModel: MainViewModel, modifier: Modifier) {
    val listState = rememberLazyListState()
    val state = viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward }
            .collect {
                if (!it && listState.layoutInfo.totalItemsCount > 0 && state.value.isHome) {
                    viewModel.processIntent(MarvelIntent.LoadMoreCharacters(listState.layoutInfo.visibleItemsInfo.size))
                }
            }
    }

    when {
        state.value.isLoading -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        state.value.error != null -> Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = state.value.error!!)
        }

        else -> {
            val characters =
                state.value.characters.filter { c ->
                    if (state.value.isHome) {
                        true
                    } else {
                        viewModel.favoriteCharacters.any { it.id == c.id }
                    }
                }
            Box(modifier = modifier) {
                LazyColumn(state = listState) {
                    items(characters.size) { index ->
                        val character = characters[index]
                        CharacterCell(
                            character = character,
                            download = {
                                viewModel.processIntent(
                                    MarvelIntent.Download(
                                        character = character,
                                        context = context
                                    )
                                )
                            },
                            onFavoriteClick = {
                                viewModel.processIntent(MarvelIntent.ToggleFavorite(character))
                            })
                    }
                }
                AnimatedVisibility(visible = state.value.isMoreLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun BottomNavigation(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state.collectAsState()

    NavigationBar(
        containerColor = MaterialTheme.colors.surface,
        modifier = modifier
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = {
                Text("홈")
            },
            selected = state.value.isHome,
            onClick = {
                viewModel.processIntent(MarvelIntent.Home)
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null
                )
            },
            label = {
                Text("즐겨찾기")
            },
            selected = !state.value.isHome,
            onClick = {
                viewModel.processIntent(MarvelIntent.Favorite)
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CharacterCell(
    character: CharacterEntity,
    download: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    val writePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val readPermissionState = rememberPermissionState(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                if (!writePermissionState.status.isGranted) {
                    writePermissionState.launchPermissionRequest()
                } else if (!readPermissionState.status.isGranted) {
                    readPermissionState.launchPermissionRequest()
                } else {
                    download.invoke()
                }
            }
            .background(color = Color.Gray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thumbnail
        AsyncImage(
            model = character.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(200.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Name
        Text(
            text = character.name,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Details
        Text(
            text = "URLs: ${character.urls}\nComics: ${character.comics}\nStories: ${character.stories}\nEvents: ${character.events}\nSeries: ${character.series}",
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onFavoriteClick) {
            Text("${if (character.isFavorite) "Remove" else "Add"} to Favorite")
        }
    }
}
