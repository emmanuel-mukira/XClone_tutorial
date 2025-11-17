package com.example.xclone_tutorial.firebase

import com.example.xclone_tutorial.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val posts = db.collection("posts")

    fun fetchPostById(id: String, onResult: (Post?) -> Unit, onError: (Throwable) -> Unit) {
        posts.document(id).get()
            .addOnSuccessListener { doc ->
                val post = doc.toObject(Post::class.java)
                onResult(post)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun seedPosts(count: Int, onDone: () -> Unit, onError: (Throwable) -> Unit) {
        val batch = db.batch()
        repeat(count) { index ->
            val id = "seed_${index + 1}"
            val p = Post(
                id = id,
                authorName = "John Doe $id",
                handle = "@johndoe · ${index + 1}h",
                text = "Seeded post #${index + 1}",
                likeCount = (index + 1) * 3,
                timestamp = System.currentTimeMillis() - index * 60_000L
            )
            val ref = posts.document(id)
            batch.set(ref, p, SetOptions.merge())
        }
        batch.commit()
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { e -> onError(e) }
    }
}
