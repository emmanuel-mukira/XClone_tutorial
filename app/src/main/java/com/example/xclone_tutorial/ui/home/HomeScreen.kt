package com.example.xclone_tutorial.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.res.stringResource
import com.example.xclone_tutorial.R
import com.example.xclone_tutorial.firebase.FirebaseRepository
import com.example.xclone_tutorial.model.Post
import com.example.xclone_tutorial.ui.components.TweetCard
import com.example.xclone_tutorial.ui.theme.XClone_tutorialTheme
import com.example.xclone_tutorial.BuildConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Firebase state
    var loading by remember { mutableStateOf(true) }
    var post by remember { mutableStateOf<Post?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Seeder state (debug only)
    var seeding by remember { mutableStateOf(false) }
    var seedSuccessCount by remember { mutableStateOf<Int?>(null) }
    var seedError by remember { mutableStateOf<Boolean>(false) }

    LaunchedEffect(Unit) {
        val repo = FirebaseRepository()
        // Replace "sample1" with an existing key in your Realtime Database under /posts/sample1
        repo.fetchPostById(
            id = "sample1",
            onResult = {
                post = it
                loading = false
            },
            onError = {
                error = it.localizedMessage
                loading = false
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(8.dp)) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_home)) },
                        selected = true,
                        onClick = { }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_profile)) },
                        selected = false,
                        onClick = { }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_settings)) },
                        selected = false,
                        onClick = { }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_logout)) },
                        selected = false,
                        onClick = { }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.home_title)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = stringResource(R.string.cd_menu))
                        }
                    }
                )
            }
        ) { innerPadding: PaddingValues ->
            Column(modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)) {
                if (BuildConfig.DEBUG) {
                    Button(
                        onClick = {
                            val repo = FirebaseRepository()
                            seeding = true
                            seedSuccessCount = null
                            seedError = false
                            repo.seedPosts(
                                count = 10,
                                onDone = {
                                    seeding = false
                                    seedSuccessCount = 10
                                },
                                onError = {
                                    seeding = false
                                    seedError = true
                                }
                            )
                        }
                    ) {
                        Text(text = if (seeding) stringResource(R.string.seed_in_progress) else stringResource(R.string.seed_button))
                    }
                    if (seedSuccessCount != null) {
                        Text(text = stringResource(R.string.seed_success, seedSuccessCount!!))
                    }
                    if (seedError) {
                        Text(text = stringResource(R.string.seed_error))
                    }
                }
                when {
                    loading -> Text(text = stringResource(R.string.loading))
                    error != null -> Text(text = stringResource(R.string.error_loading_post))
                    post != null -> TweetCard(post = post!!)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    XClone_tutorialTheme { HomeScreen() }
}
