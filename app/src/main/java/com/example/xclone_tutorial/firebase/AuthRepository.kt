package com.example.xclone_tutorial.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signUp(
        name: String,
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    // Update display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    
                    user.updateProfile(profileUpdates)
                        .addOnSuccessListener { onSuccess(user) }
                        .addOnFailureListener { e -> onError(e) }
                } ?: onError(Exception("User creation failed"))
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { onSuccess(it) }
                    ?: onError(Exception("Login failed"))
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun logout() {
        auth.signOut()
    }
}
