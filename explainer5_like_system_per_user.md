# Like System & Liked Tweets — Exam Study Guide

This guide explains the **per-user like system** and **Liked Tweets view** you just implemented. It is written so you can reproduce the same code in an exam.

---

## Overview — What We Built

### Purpose

- **Per-user likes**: Each user can like/unlike posts independently.
- **Global like count**: `posts` table stores total `likeCount` shared by everyone.
- **Per-user like state**: `userLikes/{userId}/{postId} = true` indicates that *this* user liked *that* post.
- **UI behavior**:
  - Heart turns red only if **current user** liked the post.
  - Like counter increases/decreases for all users.
  - "Liked Tweets" menu shows **only posts liked by current user**.

### Main Files Involved

1. `Post.kt`
2. `FirebaseRepository.kt`
3. `HomeViewModel.kt`
4. `HomeScreen.kt`
5. Firebase Realtime Database rules (`posts` + `userLikes`)

---

## 1. Post.kt — Models for Posts and Like State

```kotlin
package com.example.xclone_tutorial.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val handle: String = "",
    val text: String = "",
    val likeCount: Int = 0,
    val timestamp: Long = 0L
)

// Used for UI to track if current user liked this post
data class PostWithLikeState(
    val post: Post,
    val isLikedByCurrentUser: Boolean = false
)
```

### Line-by-line breakdown

- `data class Post(`
  - **`data class`**: Kotlin class that automatically gives you `equals`, `hashCode`, `copy`, and `toString`.
  - Used for **pure data** models.

- `val id: String = ""`
  - `val` = **read-only** property (cannot be reassigned).
  - Type is `String`.
  - Default value: empty string.
  - **Exam tip:** Always give default values when using Firebase deserialization, so it can construct objects even if some fields are missing.

- Other fields (`authorId`, `authorName`, `handle`, `text`, `likeCount`, `timestamp`):
  - Same pattern: `val name: Type = defaultValue`.
  - `likeCount: Int = 0` is the **global like counter** for this post.
  - `timestamp: Long = 0L` uses `Long` for storing time.

- `data class PostWithLikeState(`
  - A **wrapper** around `Post` for the UI.
  - `post: Post` is the original post.
  - `isLikedByCurrentUser: Boolean` tells if **current logged-in user** liked this post.

**Exam tip:** Pattern for wrapping models in UI:

```kotlin
data class ModelWithUiState(
    val model: Model,
    val someUiFlag: Boolean
)
```

Use this when your UI needs extra state **per user**, but you do not want that state in the database.

---

## 2. FirebaseRepository.kt — Per-User Likes and Seeding Posts

### a) Seeding Posts With Different Authors

Relevant part:

```kotlin
val christianTweets = listOf(
    // 15 Christian faith-based tweets
)

val authorData = mapOf(
    "author_001" to ("David Thompson" to "@davidthompson"),
    "author_002" to ("Sarah Miller" to "@sarahmiller"),
    // ... up to author_015
)

val updates = mutableMapOf<String, Any>()

repeat(count) { index ->
    val id = "seed_${index + 1}"
    val authorEntry = authorData.entries.elementAt(index % authorData.size)
    val authorId = authorEntry.key
    val author = authorEntry.value
    val post = Post(
        id = id,
        authorId = authorId,
        authorName = author.first,
        handle = "${author.second} · ${index + 1}h",
        text = christianTweets[index % christianTweets.size],
        likeCount = (index + 1) * 3,
        timestamp = System.currentTimeMillis() - index * 60_000L
    )
    updates["posts/$id"] = post
}

database.reference.updateChildren(updates)
```

### Key ideas

- `repeat(count) { index -> ... }`:
  - Runs the block `count` times.
  - `index` goes from `0` to `count - 1`.

- `authorData.entries.elementAt(index % authorData.size)`:
  - Cycles through authors so different posts have different authors.
  - `%` is the **modulo** operator.

- `updates["posts/$id"] = post`:
  - Builds a map of multiple paths → values.
  - `updateChildren(updates)` writes everything in one network call.

**Exam tip:** For seeding multiple documents/rows, build a `MutableMap<String, Any>` and use one `updateChildren` or batch write.

---

### b) Toggling Likes Per User

```kotlin
fun toggleLike(
    postId: String,
    currentLikeCount: Int,
    onResult: (Boolean, Int) -> Unit,
    onError: (Throwable) -> Unit
) {
    val currentUser = auth.currentUser
    if (currentUser == null) {
        onError(Exception("Must be logged in to like posts"))
        return
    }

    val userLikeRef = database.getReference("userLikes/${currentUser.uid}/$postId")

    // Check if user already liked this post
    userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val isCurrentlyLiked = snapshot.exists()
            val newLiked = !isCurrentlyLiked
            val newLikeCount = if (newLiked) currentLikeCount + 1 else currentLikeCount - 1

            val updates = mutableMapOf<String, Any>()

            if (newLiked) {
                // Add like - explicitly set boolean true
                updates["userLikes/${currentUser.uid}/$postId"] = true
                updates["posts/$postId/likeCount"] = newLikeCount
                database.reference.updateChildren(updates)
                    .addOnSuccessListener { onResult(newLiked, newLikeCount) }
                    .addOnFailureListener { e -> onError(e) }
            } else {
                // Remove like - delete the node completely
                userLikeRef.removeValue()
                    .addOnSuccessListener {
                        database.getReference("posts/$postId/likeCount")
                            .setValue(newLikeCount)
                            .addOnSuccessListener { onResult(newLiked, newLikeCount) }
                            .addOnFailureListener { e -> onError(e) }
                    }
                    .addOnFailureListener { e -> onError(e) }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.toException())
        }
    })
}
```

#### Important syntax

- `onResult: (Boolean, Int) -> Unit`:
  - A **function type**.
  - The callback takes two parameters: `Boolean` (new liked state) and `Int` (new likeCount).

- `userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener { ... })`:
  - Creates an **anonymous object** implementing `ValueEventListener`.
  - `override fun onDataChange(...)` is called once with current data.

- `snapshot.exists()`:
  - True if `userLikes/userId/postId` exists.
  - If it exists, the user **already liked** the post.

- `val newLiked = !isCurrentlyLiked`:
  - `!` is logical **NOT**.
  - Toggles true ↔ false.

- `val newLikeCount = if (newLiked) currentLikeCount + 1 else currentLikeCount - 1`:
  - **Inline if-expression**.
  - Returns an `Int`.

**Logic summary:**

- If **user is liking**:
  - Set `userLikes/userId/postId = true`.
  - Increase `posts/postId/likeCount`.
- If **user is unliking**:
  - `removeValue()` on `userLikes/userId/postId`.
  - Decrease `posts/postId/likeCount`.

---

### c) Checking Which Posts Current User Liked

```kotlin
fun checkUserLikedPosts(
    postIds: List<String>,
    onResult: (Map<String, Boolean>) -> Unit,
    onError: (Throwable) -> Unit
) {
    val currentUser = auth.currentUser
    if (currentUser == null) {
        onError(Exception("Must be logged in to check likes"))
        return
    }

    val userLikesRef = database.getReference("userLikes/${currentUser.uid}")
    userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val likedPosts = mutableMapOf<String, Boolean>()
            postIds.forEach { postId ->
                val childSnapshot = snapshot.child(postId)
                val value = childSnapshot.getValue()
                // Only count as liked if it's explicitly boolean true
                likedPosts[postId] = (value is Boolean) && (value == true)
            }
            onResult(likedPosts)
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.toException())
        }
    })
}
```

- Reads `userLikes/{currentUserUid}` **only for the logged-in user**.
- Fills a map `postId -> true/false`.
- Used by `HomeViewModel` to compute `isLikedByCurrentUser`.

**Exam tip:** Per-user metadata pattern:

```text
mainCollection/{itemId}      // shared data
userSpecific/{userId}/{itemId} = true // per-user state
```

---

## 3. HomeViewModel.kt — State Management for Likes and Liked Tweets

Key fields:

```kotlin
private val _postState = MutableStateFlow<PostState>(PostState.Loading)
val postState: StateFlow<PostState> = _postState.asStateFlow()

private val _postsWithLikeState = MutableStateFlow<List<PostWithLikeState>>(emptyList())
val postsWithLikeState: StateFlow<List<PostWithLikeState>> = _postsWithLikeState.asStateFlow()

private val _showLikedOnly = MutableStateFlow(false)
val showLikedOnly: StateFlow<Boolean> = _showLikedOnly.asStateFlow()
```

### Syntax

- `MutableStateFlow<T>`: A state container that can be **updated**.
- `StateFlow<T>`: Read-only view exposed to the UI.
- `.asStateFlow()`: Exposes only the immutable version outside the ViewModel.

**Exam tip:** ViewModel pattern:

```kotlin
private val _state = MutableStateFlow(InitialState)
val state: StateFlow<StateType> = _state.asStateFlow()
```

### Refreshing Like States Per User

```kotlin
fun refreshLikeStates() {
    repository.fetchAllPosts(
        onResult = { allPosts ->
            val postIds = allPosts.map { it.id }
            println("DEBUG: Found ${allPosts.size} posts, checking likes for user")

            repository.checkUserLikedPosts(
                postIds = postIds,
                onResult = { likedMap ->
                    val likedCount = likedMap.values.count { it == true }
                    println("DEBUG: User has liked $likedCount posts: ${likedMap.filter { it.value }.keys}")

                    val postsWithLikes = allPosts.map { post ->
                        val isLiked = likedMap[post.id] ?: false
                        PostWithLikeState(
                            post = post,
                            isLikedByCurrentUser = isLiked
                        )
                    }
                    _postsWithLikeState.value = postsWithLikes
                    filterPostsBasedOnLikedState()
                },
                onError = { exception ->
                    println("DEBUG: Error checking user likes: ${exception.message}")
                    val postsWithLikes = allPosts.map { post ->
                        PostWithLikeState(post = post, isLikedByCurrentUser = false)
                    }
                    _postsWithLikeState.value = postsWithLikes
                    filterPostsBasedOnLikedState()
                }
            )
        },
        onError = { exception ->
            println("DEBUG: Error fetching posts: ${exception.message}")
            _postState.value = PostState.Error("Failed to refresh posts")
            _postsWithLikeState.value = emptyList()
        }
    )
}
```

#### Flow of logic

1. Fetch **all posts** from `posts`.
2. For those ids, call `checkUserLikedPosts`.
3. Build a `PostWithLikeState` list with `isLikedByCurrentUser` set correctly.
4. Store it in `_postsWithLikeState`.
5. Call `filterPostsBasedOnLikedState()` to decide what to show.

### Toggling View Mode: All vs Liked

```kotlin
fun showLikedPosts() {
    _showLikedOnly.value = true
    refreshLikeStates() // Refresh to get current user's likes
}

fun showAllPosts() {
    _showLikedOnly.value = false
    filterPostsBasedOnLikedState()
}

private fun filterPostsBasedOnLikedState() {
    val allPostsWithLikes = _postsWithLikeState.value
    val showLiked = _showLikedOnly.value

    println("DEBUG: Filtering posts - showLikedOnly: $showLiked, total posts: ${allPostsWithLikes.size}")

    if (showLiked) {
        // Show only liked posts
        val likedPosts = allPostsWithLikes.filter { it.isLikedByCurrentUser }
        println("DEBUG: Filtered to ${likedPosts.size} liked posts")

        if (likedPosts.isNotEmpty()) {
            _postState.value = PostState.Success(likedPosts.map { it.post })
        } else {
            _postState.value = PostState.Empty
        }
    } else {
        // Show all posts
        println("DEBUG: Showing all ${allPostsWithLikes.size} posts")
        if (allPostsWithLikes.isNotEmpty()) {
            _postState.value = PostState.Success(allPostsWithLikes.map { it.post })
        } else {
            _postState.value = PostState.Empty
        }
    }
}
```

- `_showLikedOnly` controls whether we are in **Home** or **Liked Tweets** mode.
- `filterPostsBasedOnLikedState` changes `postState` based on that flag.

**Exam tip:** Use a Boolean flag + filter function to switch between different views of the **same data** without duplicating UI code.

### Updating State After Like Toggle

```kotlin
fun toggleLike(postId: String, currentLikeCount: Int) {
    viewModelScope.launch {
        repository.toggleLike(
            postId = postId,
            currentLikeCount = currentLikeCount,
            onResult = { newLiked, newLikeCount ->
                // Update the posts with like state
                val currentPostsWithLikes = _postsWithLikeState.value
                val updatedPostsWithLikes = currentPostsWithLikes.map { postWithLike ->
                    if (postWithLike.post.id == postId) {
                        postWithLike.copy(
                            post = postWithLike.post.copy(likeCount = newLikeCount),
                            isLikedByCurrentUser = newLiked
                        )
                    } else {
                        postWithLike
                    }
                }
                _postsWithLikeState.value = updatedPostsWithLikes

                // Also update the original posts state
                val currentState = _postState.value
                if (currentState is PostState.Success) {
                    val updatedPosts = currentState.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(likeCount = newLikeCount)
                        } else {
                            post
                        }
                    }
                    _postState.value = PostState.Success(updatedPosts)
                    filterPostsBasedOnLikedState()
                }
            },
            onError = { exception ->
                println("Error toggling like: ${exception.message}")
            }
        )
    }
}
```

- `viewModelScope.launch { ... }`:
  - Starts a coroutine tied to the ViewModel lifecycle.
- `copy(...)` on data classes:
  - Makes a new object with some fields changed.

**Pattern:** Update both:
- The raw `Post` list (for main state).
- The `PostWithLikeState` list (for UI like styling).

---

## 4. HomeScreen.kt — UI for Likes and Liked Tweets

Important parts:

### Collecting State

```kotlin
val postState by viewModel.postState.collectAsState()
val seedState by viewModel.seedState.collectAsState()
val showLikedOnly by viewModel.showLikedOnly.collectAsState()
```

- `collectAsState()` turns a `StateFlow` into Compose state.
- `by` uses **property delegation** so you can write `postState` instead of `postState.value` in Composable code.

### Drawer Menu with "Liked Tweets"

```kotlin
NavigationDrawerItem(
    label = { Text(if (showLikedOnly) "Liked Tweets" else stringResource(R.string.drawer_home)) },
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
        label = { Text("Liked Tweets") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            viewModel.showLikedPosts()
        }
    )
}
```

- When **not** in liked mode:
  - Menu shows: Home (selected) + Liked Tweets.
  - Tapping "Liked Tweets" calls `showLikedPosts()`.
- When in liked mode:
  - Main item label becomes "Liked Tweets".
  - Tapping it calls `showAllPosts()` to go back.

### Changing the Top Bar Title

```kotlin
TopAppBar(
    title = { Text(if (showLikedOnly) "Liked Tweets" else stringResource(R.string.home_title)) },
    navigationIcon = { ... }
)
```

- Uses the same Boolean flag to show the correct title.

### Filtering Displayed Posts in the UI

```kotlin
when (postState) {
    is PostState.Loading -> ...
    is PostState.Empty -> ...
    is PostState.Error -> ...
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
```

- This is the **final safety** to ensure that in liked mode:
  - Only `PostWithLikeState` with `isLikedByCurrentUser == true` are shown.

**Exam tip:** Even if you filter in the ViewModel, it’s often a good idea to **also filter in the UI** to avoid mistakes and keep the behavior obvious.

---

## 5. Firebase Rules — posts and userLikes

### posts Rules (Simplified for This Feature)

```json
"posts": {
  ".read": true,
  "$postId": {
    ".write": "auth != null",
    ".validate": "newData.hasChildren(['id','authorId','authorName','handle','text','likeCount','timestamp'])",
    "id": { ".validate": "newData.isString()" },
    "authorId": { ".validate": "newData.isString()" },
    "authorName": { ".validate": "newData.isString()" },
    "handle": { ".validate": "newData.isString()" },
    "text": { ".validate": "newData.isString()" },
    "likeCount": { ".validate": "newData.isNumber()" },
    "timestamp": { ".validate": "newData.isNumber()" }
  }
}
```

### userLikes Rules (Per-User Likes)

```json
"userLikes": {
  "$userId": {
    ".read": "$userId === auth.uid",
    ".write": "$userId === auth.uid",
    "$postId": {
      ".validate": "newData.isBoolean()"
    }
  }
}
```

- Users can only read/write `userLikes` for **their own `uid`**.
- Each liked post is just `true`.

**Exam tip:**

- Use a separate node for user-specific data (like bookmarks, likes), with rules locking it to `auth.uid`.
- Keep the main `posts` collection free of per-user flags.

---

## Final Mental Model

1. **Storage:**
   - `posts` holds the tweets + total like count.
   - `userLikes/{userId}/{postId} = true` marks individual user likes.

2. **Fetching:**
   - Fetch all posts.
   - Fetch `userLikes` for current user.
   - Combine them into `PostWithLikeState`.

3. **UI:**
   - Normal mode: show all posts.
   - Liked mode: filter `PostWithLikeState` by `isLikedByCurrentUser == true`.

4. **Updating:**
   - Toggle like updates both `userLikes` and `posts/likeCount`.
   - ViewModel updates local state so UI reacts immediately.

**Exam tip:** Whenever you need per-user actions on shared content (likes, favorites, stars):

- **Do NOT** put a `liked: Boolean` directly in the main document.
- **Do** create a side collection keyed by `userId`, then use it to compute per-user UI state.

If you can explain and reproduce this pattern, you can implement Twitter/Instagram-style likes in any exam or project.
