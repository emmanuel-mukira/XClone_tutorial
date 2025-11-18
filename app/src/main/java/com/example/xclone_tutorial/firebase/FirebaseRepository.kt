package com.example.xclone_tutorial.firebase

import com.example.xclone_tutorial.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance("https://xclone-tutorial-default-rtdb.europe-west1.firebasedatabase.app")
    private val auth = FirebaseAuth.getInstance()
    private val postsRef = database.getReference("posts")

    fun fetchPostById(id: String, onResult: (Post?) -> Unit, onError: (Throwable) -> Unit) {
        postsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                onResult(post)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        })
    }

    fun fetchAllPosts(onResult: (List<Post>) -> Unit, onError: (Throwable) -> Unit) {
        postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                snapshot.children.forEach { childSnapshot ->
                    childSnapshot.getValue(Post::class.java)?.let { post ->
                        posts.add(post)
                    }
                }
                // Sort by timestamp descending (newest first)
                onResult(posts.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        })
    }

    fun seedPosts(count: Int, onDone: () -> Unit, onError: (Throwable) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError(Exception("Must be logged in to seed posts"))
            return
        }

        val christianTweets = listOf(
            "The Lord is my shepherd; I shall not want. He makes me lie down in green pastures. Psalm 23:1-2",
            "For God so loved the world that he gave his one and only Son. John 3:16",
            "I can do all things through Christ who strengthens me. Philippians 4:13",
            "Trust in the Lord with all your heart and lean not on your own understanding. Proverbs 3:5",
            "Be still and know that I am God. Psalm 46:10",
            "The joy of the Lord is my strength. Nehemiah 8:10",
            "Let everything that has breath praise the Lord. Psalm 150:6",
            "God is our refuge and strength, an ever-present help in trouble. Psalm 46:1",
            "Love one another as I have loved you. John 13:34",
            "The Lord is close to the brokenhearted. Psalm 34:18",
            "Walk by faith, not by sight. 2 Corinthians 5:7",
            "Grace and peace be yours in abundance. 2 Peter 1:2",
            "The Lord will fight for you; you need only to be still. Exodus 14:14",
            "His mercies are new every morning. Lamentations 3:22-23",
            "Seek first the kingdom of God. Matthew 6:33"
        )
        
        val authors = listOf(
            "David Thompson" to "@davidthompson",
            "Sarah Miller" to "@sarahmiller", 
            "Pastor James Wilson" to "@pastorjames",
            "Mary Johnson" to "@maryjohnson",
            "Rev. Michael Brown" to "@revmichael",
            "Emma Davis" to "@emmadavis",
            "Father Robert Garcia" to "@fatherrobert",
            "Lisa Anderson" to "@lisanderson",
            "Bishop John Martinez" to "@bishopjohn",
            "Rachel White" to "@rachelwhite",
            "Elder Thomas Lee" to "@elderthomas",
            "Jennifer Taylor" to "@jentaylor",
            "Deacon Mark Robinson" to "@deaconmark",
            "Amanda Clark" to "@amandaclark",
            "Brother Daniel Lewis" to "@brotherdaniel"
        )
        
        val authorData = mapOf(
            "author_001" to ("David Thompson" to "@davidthompson"),
            "author_002" to ("Sarah Miller" to "@sarahmiller"),
            "author_003" to ("Pastor James Wilson" to "@pastorjames"),
            "author_004" to ("Mary Johnson" to "@maryjohnson"),
            "author_005" to ("Rev. Michael Brown" to "@revmichael"),
            "author_006" to ("Emma Davis" to "@emmadavis"),
            "author_007" to ("Father Robert Garcia" to "@fatherrobert"),
            "author_008" to ("Lisa Anderson" to "@lisanderson"),
            "author_009" to ("Bishop John Martinez" to "@bishopjohn"),
            "author_010" to ("Rachel White" to "@rachelwhite"),
            "author_011" to ("Elder Thomas Lee" to "@elderthomas"),
            "author_012" to ("Jennifer Taylor" to "@jentaylor"),
            "author_013" to ("Deacon Mark Robinson" to "@deaconmark"),
            "author_014" to ("Amanda Clark" to "@amandaclark"),
            "author_015" to ("Brother Daniel Lewis" to "@brotherdaniel")
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
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { e -> onError(e) }
    }
    
    fun toggleLike(postId: String, currentLikeCount: Int, onResult: (Boolean, Int) -> Unit, onError: (Throwable) -> Unit) {
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
                            // Then update the like count
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
    
    fun checkUserLikedPosts(postIds: List<String>, onResult: (Map<String, Boolean>) -> Unit, onError: (Throwable) -> Unit) {
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
}
