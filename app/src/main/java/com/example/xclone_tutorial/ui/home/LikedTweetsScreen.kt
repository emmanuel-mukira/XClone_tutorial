package com.example.xclone_tutorial.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xclone_tutorial.R
import com.example.xclone_tutorial.model.PostWithLikeState
import com.example.xclone_tutorial.ui.components.TweetCard
import com.example.xclone_tutorial.viewmodel.HomeViewModel
import com.example.xclone_tutorial.viewmodel.PostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedTweetsScreen(
    onBack: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    // Fetch liked tweets when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.fetchLikedTweets()
    }

    val postState by viewModel.postState.collectAsState()
    val postsWithLikeState by viewModel.postsWithLikeState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Liked Tweets") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (postState) {
                is PostState.Loading -> {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading liked tweets...",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is PostState.Empty -> {
                    Text(
                        text = "No liked tweets yet",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Start liking tweets to see them here",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Button(
                        onClick = onBack,
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Browse Tweets")
                    }
                }
                is PostState.Error -> {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = (postState as PostState.Error).message,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = { viewModel.fetchLikedTweets() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Try Again")
                    }
                }
                is PostState.Success -> {
                    LazyColumn {
                        items(postsWithLikeState) { postWithLikeState ->
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
