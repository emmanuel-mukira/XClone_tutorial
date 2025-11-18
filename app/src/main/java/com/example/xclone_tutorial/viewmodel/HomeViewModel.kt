package com.example.xclone_tutorial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xclone_tutorial.firebase.FirebaseRepository
import com.example.xclone_tutorial.model.Post
import com.example.xclone_tutorial.model.PostWithLikeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for HomeScreen
 * Handles post fetching and seeding logic
 */
class HomeViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    // UI State for posts
    private val _postState = MutableStateFlow<PostState>(PostState.Loading)
    val postState: StateFlow<PostState> = _postState.asStateFlow()
    
    private val _postsWithLikeState = MutableStateFlow<List<PostWithLikeState>>(emptyList())
    val postsWithLikeState: StateFlow<List<PostWithLikeState>> = _postsWithLikeState.asStateFlow()
    
    private val _showLikedOnly = MutableStateFlow(false)
    val showLikedOnly: StateFlow<Boolean> = _showLikedOnly.asStateFlow()

    // UI State for seeding
    private val _seedState = MutableStateFlow<SeedState>(SeedState.Idle)
    val seedState: StateFlow<SeedState> = _seedState.asStateFlow()

    init {
        // Load all posts when ViewModel is created
        fetchAllPosts()
    }
    
    fun refreshLikeStates() {
        // First, get all posts from the original source
        repository.fetchAllPosts(
            onResult = { allPosts ->
                val postIds = allPosts.map { it.id }
                println("DEBUG: Found ${allPosts.size} posts, checking likes for user")
                
                // Then check which posts are liked by current user
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
                        // If we can't check likes, show posts without like state
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

    /**
     * Fetch all posts
     */
    fun fetchAllPosts() {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            repository.fetchAllPosts(
                onResult = { posts ->
                    if (posts.isNotEmpty()) {
                        _postState.value = PostState.Success(posts)
                        // Check which posts are liked by current user
                        val postIds = posts.map { it.id }
                        repository.checkUserLikedPosts(
                            postIds = postIds,
                            onResult = { likedMap ->
                                val postsWithLikes = posts.map { post ->
                                    PostWithLikeState(
                                        post = post,
                                        isLikedByCurrentUser = likedMap[post.id] ?: false
                                    )
                                }
                                _postsWithLikeState.value = postsWithLikes
                                filterPostsBasedOnLikedState()
                            },
                            onError = { exception ->
                                // If we can't check likes, show posts without like state
                                val postsWithLikes = posts.map { post ->
                                    PostWithLikeState(post = post, isLikedByCurrentUser = false)
                                }
                                _postsWithLikeState.value = postsWithLikes
                                filterPostsBasedOnLikedState()
                            }
                        )
                    } else {
                        _postState.value = PostState.Empty
                        _postsWithLikeState.value = emptyList()
                    }
                },
                onError = { exception ->
                    _postState.value = PostState.Error(exception.message ?: "Failed to load posts")
                    _postsWithLikeState.value = emptyList()
                }
            )
        }
    }

    /**
     * Seed multiple posts to Firestore
     */
    fun seedPosts(count: Int = 10) {
        viewModelScope.launch {
            _seedState.value = SeedState.Loading
            repository.seedPosts(
                count = count,
                onDone = {
                    _seedState.value = SeedState.Success(count)
                    // Refresh posts after seeding
                    fetchAllPosts()
                },
                onError = { exception ->
                    _seedState.value = SeedState.Error(exception.message ?: "Failed to seed posts")
                }
            )
        }
    }

    /**
     * Reset seed state (e.g., to hide success message)
     */
    fun resetSeedState() {
        _seedState.value = SeedState.Idle
    }
    
    fun fetchLikedTweets() {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            _postsWithLikeState.value = emptyList()
            
            // First get all posts to check which ones are liked
            repository.fetchAllPosts(
                onResult = { allPosts ->
                    val postIds = allPosts.map { it.id }
                    
                    // Then check which posts are liked by current user
                    repository.checkUserLikedPosts(
                        postIds = postIds,
                        onResult = { likedMap ->
                            val likedPosts = allPosts.filter { post ->
                                likedMap[post.id] == true
                            }
                            
                            if (likedPosts.isNotEmpty()) {
                                _postState.value = PostState.Success(likedPosts)
                                val postsWithLikes = likedPosts.map { post ->
                                    PostWithLikeState(
                                        post = post,
                                        isLikedByCurrentUser = true
                                    )
                                }
                                _postsWithLikeState.value = postsWithLikes
                            } else {
                                _postState.value = PostState.Empty
                                _postsWithLikeState.value = emptyList()
                            }
                        },
                        onError = { exception ->
                            _postState.value = PostState.Error("Failed to check liked posts")
                            _postsWithLikeState.value = emptyList()
                        }
                    )
                },
                onError = { exception ->
                    _postState.value = PostState.Error("Failed to load posts")
                    _postsWithLikeState.value = emptyList()
                }
            )
        }
    }
    
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
                    // Could show error message, but for now just log
                    println("Error toggling like: ${exception.message}")
                }
            )
        }
    }
}

/**
 * Sealed class representing post loading states
 */
sealed class PostState {
    object Loading : PostState()
    object Empty : PostState()
    data class Success(val posts: List<Post>) : PostState()
    data class Error(val message: String) : PostState()
}

/**
 * Sealed class representing seed operation states
 */
sealed class SeedState {
    object Idle : SeedState()
    object Loading : SeedState()
    data class Success(val count: Int) : SeedState()
    data class Error(val message: String) : SeedState()
}
