package com.example.xclone_tutorial.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xclone_tutorial.R
import com.example.xclone_tutorial.model.Post
import com.example.xclone_tutorial.ui.components.TweetCard
import com.example.xclone_tutorial.ui.theme.XClone_tutorialTheme
import com.example.xclone_tutorial.viewmodel.HomeViewModel
import com.example.xclone_tutorial.viewmodel.PostState
import com.example.xclone_tutorial.viewmodel.SeedState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onLogin: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    // Refresh like states when screen is displayed (handles user changes)
    LaunchedEffect(Unit) {
        viewModel.refreshLikeStates()
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Collect state from ViewModel
    val postState by viewModel.postState.collectAsState()
    val seedState by viewModel.seedState.collectAsState()
    val showLikedOnly by viewModel.showLikedOnly.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(8.dp)) {
                    NavigationDrawerItem(
                        label = { Text(if (showLikedOnly) stringResource(R.string.liked_tweets) else stringResource(R.string.drawer_home)) },
                        selected = true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (showLikedOnly) {
                                viewModel.showAllPosts()
                            }
                        }
                    )
                    if (!showLikedOnly) {
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.liked_tweets)) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.showLikedPosts()
                            }
                        )
                    }
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.login)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogin()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(if (isDarkTheme) stringResource(R.string.light_mode) else stringResource(R.string.dark_mode)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onToggleTheme()
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.drawer_logout)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(if (showLikedOnly) stringResource(R.string.liked_tweets) else stringResource(R.string.home_title)) },
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
                // Show posts
                when (postState) {
                    is PostState.Loading -> Text(text = stringResource(R.string.loading))
                    is PostState.Empty -> Text(text = stringResource(R.string.no_posts))
                    is PostState.Error -> Text(
                        text = (postState as PostState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    is PostState.Success -> {
                        val postsWithLikeState by viewModel.postsWithLikeState.collectAsState()
                        // When showing liked tweets, only display posts that this user has liked
                        val displayedPosts = if (showLikedOnly) {
                            postsWithLikeState.filter { it.isLikedByCurrentUser }
                        } else {
                            postsWithLikeState
                        }

                        LazyColumn {
                            items(displayedPosts) { postWithLikeState ->
                                TweetCard(
                                    postWithLikeState = postWithLikeState,
                                    onLike = { postId, currentLikeCount ->
                                        viewModel.toggleLike(postId, currentLikeCount)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
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
