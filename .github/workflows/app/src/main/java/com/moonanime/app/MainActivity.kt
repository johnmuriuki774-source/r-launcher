package com.moonanime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MoonAnimeApp()
        }
    }
}

@Composable
fun MoonAnimeApp() {

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            MoonAnimeMain()
        }
    }
}

enum class MainTab {
    HOME,
    SEARCH,
    LIBRARY
}

@Composable
fun MoonAnimeMain() {

    var selectedTab by remember {
        mutableStateOf(MainTab.HOME)
    }

    var selectedAnime by remember {
        mutableStateOf<Anime?>(null)
    }

    if (selectedAnime != null) {

        AnimeDetailsScreen(
            anime = selectedAnime!!,
            onBack = {
                selectedAnime = null
            }
        )

        return
    }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "MoonAnime",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected =
                        selectedTab == MainTab.HOME,
                    onClick = {
                        selectedTab = MainTab.HOME
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = {
                        Text("Home")
                    }
                )

                NavigationBarItem(
                    selected =
                        selectedTab == MainTab.SEARCH,
                    onClick = {
                        selectedTab = MainTab.SEARCH
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    label = {
                        Text("Search")
                    }
                )

                NavigationBarItem(
                    selected =
                        selectedTab == MainTab.LIBRARY,
                    onClick = {
                        selectedTab = MainTab.LIBRARY
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Library"
                        )
                    },
                    label = {
                        Text("Library")
                    }
                )
            }
        }

    ) { padding ->

        when (selectedTab) {

            MainTab.HOME -> {

                HomeScreen(
                    padding = padding,
                    onAnimeClick = {
                        selectedAnime = it
                    }
                )
            }

            MainTab.SEARCH -> {

                SearchScreen(
                    padding = padding,
                    onAnimeClick = {
                        selectedAnime = it
                    }
                )
            }

            MainTab.LIBRARY -> {

                LibraryScreen(
                    padding = padding,
                    onAnimeClick = {
                        selectedAnime = it
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onAnimeClick: (Anime) -> Unit
) {

    var trending by remember {
        mutableStateOf<List<Anime>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {

        try {

            trending = AniListApi().trending()

        } catch (e: Exception) {

            error =
                e.message ?: "Unable to load anime"

        } finally {

            loading = false
        }
    }

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(padding),

        contentPadding =
            PaddingValues(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {

        item {

            Text(
                text = "Welcome to MoonAnime",
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "Discover your next anime."
            )
        }

        item {

            SectionTitle(
                title = "🔥 Trending"
            )
        }

        item {

            when {

                loading -> {

                    Box(
                        modifier =
                            Modifier.fillMaxWidth(),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

                error != null -> {

                    Text(
                        text =
                            error ?: "Unknown error",
                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }

                trending.isEmpty() -> {

                    Text(
                        text =
                            "No anime found."
                    )
                }

                else -> {

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        items(trending) { anime ->

                            AnimePoster(
                                anime = anime,
                                onClick = {
                                    onAnimeClick(anime)
                                }
                            )
                        }
                    }
                }
            }
        }

        item {

            SectionTitle(
                title =
                    "🎯 Recommendations"
            )
        }

        item {

            Text(
                text =
                    "Personalized recommendations will use your library and watch history."
            )
        }
    }
}

@Composable
fun SearchScreen(
    padding: PaddingValues,
    onAnimeClick: (Anime) -> Unit
) {

    val scope =
        rememberCoroutineScope()

    var query by remember {
        mutableStateOf("")
    }

    var results by remember {
        mutableStateOf<List<Anime>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {

        OutlinedTextField(

            value = query,

            onValueChange = {
                query = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            placeholder = {
                Text("Search anime...")
            }
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Button(

            onClick = {

                if (query.isBlank()) {
                    return@Button
                }

                scope.launch {

                    loading = true
                    error = null

                    try {

                        results =
                            AniListApi()
                                .search(query)

                    } catch (e: Exception) {

                        results =
                            emptyList()

                        error =
                            e.message
                                ?: "Search failed"

                    } finally {

                        loading = false
                    }
                }
            },

            enabled = !loading,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                if (loading)
                    "Searching..."
                else
                    "Search"
            )
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        if (error != null) {

            Text(
                text =
                    error ?: "",
                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )

        } else if (loading) {

            CircularProgressIndicator()

        } else {

            LazyColumn(

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(results) { anime ->

                    AnimeListItem(
                        anime = anime,
                        onClick = {
                            onAnimeClick(anime)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    padding: PaddingValues,
    onAnimeClick: (Anime) -> Unit
) {

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {

        Text(
            text = "My Library",
            style =
                MaterialTheme.typography.headlineMedium,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text =
                "Your saved anime will appear here."
        )
    }
}

@Composable
fun AnimeDetailsScreen(
    anime: Anime,
    onBack: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = anime.title,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Text(
                            text = "←",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge
                        )
                    }
                }
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding =
                PaddingValues(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            item {

                if (anime.imageUrl.isNotBlank()) {

                    coil3.compose.AsyncImage(

                        model =
                            anime.imageUrl,

                        contentDescription =
                            anime.title,

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(
                                    RoundedCornerShape(
                                        16.dp
                                    )
                                ),

                        contentScale =
                            ContentScale.Crop
                    )

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )
                }
            }

            item {

                Text(
                    text = anime.title,
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            item {

                Row {

                    Text(
                        text =
                            "Format: ${
                                anime.format.ifBlank {
                                    "Unknown"
                                }
                            }"
                    )

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Text(
                        text =
                            "Episodes: ${
                                anime.episodes ?: "?"
                            }"
                    )
                }
            }

            item {

                if (anime.score > 0) {

                    Text(
                        text =
                            "⭐ ${
                                anime.score / 10.0
                            } / 10"
                    )
                }
            }

            item {

                if (anime.genres.isNotEmpty()) {

                    Text(
                        text =
                            anime.genres
                                .joinToString(
                                    " • "
                                )
                    )
                }
            }

            item {

                Text(
                    text =
                        "Status: ${
                            anime.status.ifBlank {
                                "Unknown"
                            }
                        }"
                )
            }

            item {

                if (
                    anime.description
                        .isNotBlank()
                ) {

                    Text(
                        text =
                            anime.description
                                .replace(
                                    Regex("<[^>]*>"),
                                    ""
                                )
                    )
                }
            }

            item {

                Text(
                    text =
                        "🔢 Watch Order",
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Related titles and the recommended viewing order will be loaded from AniList."
                )
            }

            item {

                Text(
                    text =
                        "🎯 Recommendations",
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Similar anime will appear here."
                )
            }
        }
    }
}

@Composable
fun AnimePoster(
    anime: Anime,
    onClick: () -> Unit
) {

    Card(

        modifier = Modifier
            .width(145.dp)
            .clickable(
                onClick = onClick
            )
    ) {

        Column {

            if (
                anime.imageUrl.isNotBlank()
            ) {

                coil3.compose.AsyncImage(

                    model =
                        anime.imageUrl,

                    contentDescription =
                        anime.title,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(205.dp),

                    contentScale =
                        ContentScale.Crop
                )

            } else {

                Box(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(205.dp)
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Tv,
                        contentDescription =
                            null
                    )
                }
            }

     