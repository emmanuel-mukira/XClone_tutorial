package com.example.xclone_tutorial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.xclone_tutorial.R
import com.example.xclone_tutorial.model.Post
import com.example.xclone_tutorial.ui.theme.XClone_tutorialTheme

@Composable
fun TweetCard(post: Post) {
    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
            )
            Column(modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = post.handle,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = stringResource(R.string.cd_comment)) }
                    IconButton(onClick = { }) { Icon(Icons.Filled.Repeat, contentDescription = stringResource(R.string.cd_repost)) }
                    IconButton(onClick = { }) { Icon(Icons.Filled.FavoriteBorder, contentDescription = stringResource(R.string.cd_like)) }
                    IconButton(onClick = { }) { Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.cd_share)) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TweetCardPreview() {
    XClone_tutorialTheme {
        TweetCard(
            post = Post(
                id = "1",
                authorName = "John Doe",
                handle = "@johndoe · 2h",
                text = "This is a sample tweet text to simulate a social post in our Compose UI.",
                likeCount = 12,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
