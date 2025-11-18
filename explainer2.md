# Explainer 2 — Firebase Authentication Integration

This document explains the Firebase Authentication code added to the app, breaking down syntax and concepts so you can understand and reproduce it in an exam.

---

## Table of Contents
1. [Gradle Dependencies](#gradle-dependencies)
2. [Data Models](#data-models)
3. [AuthRepository](#authrepository)
4. [SignUpScreen](#signupscreen)
5. [LoginScreen](#loginscreen)
6. [MainActivity Navigation](#mainactivity-navigation)
7. [Updated FirebaseRepository](#updated-firebaserepository)
8. [Firestore Security Rules](#firestore-security-rules)

---

## Gradle Dependencies

### Location: `app/build.gradle.kts`

```kotlin
// Firebase BOM, Firestore, and Auth
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
```

**What this does:**
- `platform("com.google.firebase:firebase-bom:33.5.1")`: BOM (Bill of Materials) manages Firebase library versions so they're compatible
- `firebase-firestore-ktx`: Kotlin extensions for Firestore database
- `firebase-auth-ktx`: Kotlin extensions for Firebase Authentication
- `firebase-analytics-ktx`: Firebase Analytics (optional but recommended)

**Syntax to remember:**
- `implementation()` adds a library to your project
- `-ktx` suffix means Kotlin extensions (better API for Kotlin)
- BOM version controls all Firebase library versions

---

## Data Models

### Location: `model/Post.kt`

```kotlin
data class Post(
    val id: String = "",
    val authorId: String = "",  // NEW: Links post to user
    val authorName: String = "",
    val handle: String = "",
    val text: String = "",
    val likeCount: Int = 0,
    val timestamp: Long = 0L
)
```

**What changed:**
- Added `authorId: String = ""` field
- This stores the Firebase user's unique ID (UID)
- Used for security rules to verify ownership

**Why default values?**
- Firebase's `getValue(Post::class.java)` needs a no-arg constructor
- Default values (`= ""`, `= 0`) provide that constructor automatically

**Exam tip:** Always add default values to data classes used with Firebase

---

## AuthRepository

### Location: `firebase/AuthRepository.kt`

```kotlin
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
```

### Line-by-line breakdown:

**1. Get Firebase Auth instance:**
```kotlin
private val auth = FirebaseAuth.getInstance()
```
- `FirebaseAuth.getInstance()`: Gets the singleton Firebase Auth object
- `private val`: Class-level property, initialized once

**2. Get current user:**
```kotlin
fun getCurrentUser(): FirebaseUser? = auth.currentUser
```
- Returns `FirebaseUser?` (nullable) - null if not logged in
- Single-expression function (no braces needed)

**3. Sign up function:**
```kotlin
fun signUp(
    name: String,
    email: String,
    password: String,
    onSuccess: (FirebaseUser) -> Unit,
    onError: (Exception) -> Unit
)
```
- Takes 3 strings for user input
- Takes 2 callbacks: `onSuccess` and `onError`
- `(FirebaseUser) -> Unit`: Function type that takes FirebaseUser, returns nothing
- Callbacks let the UI know when async operation completes

**4. Create user:**
```kotlin
auth.createUserWithEmailAndPassword(email, password)
    .addOnSuccessListener { result ->
        // Success code
    }
    .addOnFailureListener { e -> onError(e) }
```
- Firebase returns a `Task` (async operation)
- `.addOnSuccessListener`: Runs when creation succeeds
- `.addOnFailureListener`: Runs when creation fails
- `result` contains the created user

**5. Update display name:**
```kotlin
val profileUpdates = UserProfileChangeRequest.Builder()
    .setDisplayName(name)
    .build()

user.updateProfile(profileUpdates)
```
- Builder pattern: construct object step-by-step
- `.setDisplayName(name)`: Sets user's display name
- `.build()`: Creates the final request object
- `updateProfile()`: Another async operation

**6. Elvis operator:**
```kotlin
result.user?.let { user ->
    // Use user
} ?: onError(Exception("User creation failed"))
```
- `?.let`: Only runs if `result.user` is not null
- `?:`: Elvis operator - runs right side if left is null
- Exam tip: This is Kotlin's null-safety pattern

**7. Login function:**
```kotlin
auth.signInWithEmailAndPassword(email, password)
```
- Similar to sign up but simpler (no profile update needed)
- Returns existing user if credentials match

**8. Logout:**
```kotlin
fun logout() {
    auth.signOut()
}
```
- Simple one-liner to sign out current user

### Key concepts for exams:
1. **Async operations**: Firebase uses callbacks (success/error)
2. **Null safety**: Use `?.let` and `?:` for nullable types
3. **Builder pattern**: Step-by-step object construction
4. **Callbacks**: Pass functions as parameters to handle results

---

## SignUpScreen

### Location: `ui/auth/SignUpScreen.kt`

```kotlin
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authRepo = remember { AuthRepository() }
    
    // UI code...
}
```

### Key Compose concepts:

**1. Composable function:**
```kotlin
@Composable
fun SignUpScreen(...)
```
- `@Composable`: Annotation marking this as a Compose UI function
- Can call other `@Composable` functions
- Automatically recomposes when state changes

**2. State management:**
```kotlin
var name by remember { mutableStateOf("") }
```
- `mutableStateOf("")`: Creates observable state with initial value `""`
- `remember`: Preserves state across recompositions
- `by`: Kotlin property delegate - lets you use `name` instead of `name.value`
- When `name` changes, UI automatically updates

**3. Nullable state:**
```kotlin
var errorMessage by remember { mutableStateOf<String?>(null) }
```
- `<String?>`: Explicitly nullable type
- Starts as `null`, can be set to error message string

**4. Remember non-state objects:**
```kotlin
val authRepo = remember { AuthRepository() }
```
- `remember` without `mutableStateOf`: Creates object once, reuses it
- Prevents creating new `AuthRepository` on every recomposition

**5. Column layout:**
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    // Children stacked vertically
}
```
- `Column`: Vertical layout (like LinearLayout vertical)
- `Modifier`: Chain of transformations
- `.fillMaxSize()`: Take all available space
- `.padding(24.dp)`: Add 24dp padding on all sides
- `horizontalAlignment`: Center children horizontally
- `verticalArrangement`: Center children vertically

**6. TextField:**
```kotlin
OutlinedTextField(
    value = name,
    onValueChange = { name = it },
    label = { Text(stringResource(R.string.name)) },
    modifier = Modifier.fillMaxWidth(),
    enabled = !isLoading
)
```
- `value = name`: Current text value (state)
- `onValueChange = { name = it }`: Callback when user types
- `it`: Lambda parameter (the new text)
- `label`: Floating label composable
- `enabled = !isLoading`: Disable when loading

**7. Password field:**
```kotlin
OutlinedTextField(
    value = password,
    onValueChange = { password = it },
    visualTransformation = PasswordVisualTransformation(),
    // ...
)
```
- `PasswordVisualTransformation()`: Hides text with dots

**8. Conditional rendering:**
```kotlin
errorMessage?.let {
    Text(
        text = it,
        color = MaterialTheme.colorScheme.error
    )
}
```
- `?.let`: Only renders if `errorMessage` is not null
- `it`: The non-null error message

**9. Button with callback:**
```kotlin
Button(
    onClick = {
        isLoading = true
        errorMessage = null
        authRepo.signUp(
            name = name,
            email = email,
            password = password,
            onSuccess = {
                isLoading = false
                onSignUpSuccess()
            },
            onError = { e ->
                isLoading = false
                errorMessage = e.message ?: stringResource(R.string.auth_error)
            }
        )
    },
    enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
) {
    Text(if (isLoading) stringResource(R.string.signing_up) else stringResource(R.string.sign_up))
}
```

**Breaking down the onClick:**
1. Set `isLoading = true` (shows loading state)
2. Clear previous error: `errorMessage = null`
3. Call `authRepo.signUp()` with:
   - User input: `name`, `email`, `password`
   - Success callback: Stop loading, navigate to home
   - Error callback: Stop loading, show error message
4. `e.message ?: stringResource(...)`: Use error message or fallback

**Button enabled logic:**
```kotlin
enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
```
- Only enabled when:
  - Not currently loading
  - All fields have text (not blank)

**Dynamic button text:**
```kotlin
Text(if (isLoading) stringResource(R.string.signing_up) else stringResource(R.string.sign_up))
```
- Ternary-like: `if (condition) value1 else value2`
- Shows "Signing up…" when loading, "Sign Up" otherwise

### Exam tips for Compose:
1. **State**: Use `remember { mutableStateOf() }` for UI state
2. **Layouts**: `Column` (vertical), `Row` (horizontal), `Box` (stack)
3. **Modifiers**: Chain with `.modifier1().modifier2()`
4. **Callbacks**: Pass lambda functions for events
5. **Conditional UI**: Use `if`, `when`, or `?.let`

---

## LoginScreen

### Location: `ui/auth/LoginScreen.kt`

LoginScreen is nearly identical to SignUpScreen, but:
- No `name` field (only email and password)
- Calls `authRepo.login()` instead of `authRepo.signUp()`
- Different navigation text

**Key difference:**
```kotlin
authRepo.login(
    email = email,
    password = password,
    onSuccess = { onLoginSuccess() },
    onError = { e -> 
        errorMessage = e.message ?: stringResource(R.string.auth_error)
    }
)
```

Same pattern, different function. This is code reuse!

---

## MainActivity Navigation

### Location: `MainActivity.kt`

```kotlin
@Composable
fun AppNavigation() {
    val authRepo = remember { AuthRepository() }
    var currentScreen by remember { mutableStateOf(
        if (authRepo.getCurrentUser() != null) Screen.Home else Screen.Login
    ) }

    when (currentScreen) {
        Screen.SignUp -> SignUpScreen(
            onSignUpSuccess = { currentScreen = Screen.Home },
            onNavigateToLogin = { currentScreen = Screen.Login }
        )
        Screen.Login -> LoginScreen(
            onLoginSuccess = { currentScreen = Screen.Home },
            onNavigateToSignUp = { currentScreen = Screen.SignUp }
        )
        Screen.Home -> HomeScreen(
            onLogout = {
                authRepo.logout()
                currentScreen = Screen.Login
            }
        )
    }
}

enum class Screen {
    SignUp, Login, Home
}
```

### Breaking it down:

**1. Enum for screens:**
```kotlin
enum class Screen {
    SignUp, Login, Home
}
```
- `enum`: Fixed set of values
- Represents the 3 possible screens
- Type-safe navigation (can't misspell)

**2. Initial screen logic:**
```kotlin
var currentScreen by remember { mutableStateOf(
    if (authRepo.getCurrentUser() != null) Screen.Home else Screen.Login
) }
```
- Check if user is already logged in
- If yes → start at `Screen.Home`
- If no → start at `Screen.Login`
- This is "remember me" functionality

**3. When expression (like switch):**
```kotlin
when (currentScreen) {
    Screen.SignUp -> SignUpScreen(...)
    Screen.Login -> LoginScreen(...)
    Screen.Home -> HomeScreen(...)
}
```
- `when`: Kotlin's switch statement
- Checks `currentScreen` value
- Renders corresponding screen composable

**4. Navigation via callbacks:**
```kotlin
SignUpScreen(
    onSignUpSuccess = { currentScreen = Screen.Home },
    onNavigateToLogin = { currentScreen = Screen.Login }
)
```
- Change `currentScreen` state to navigate
- Compose automatically recomposes with new screen
- No fragments, no navigation component - just state!

**5. Logout flow:**
```kotlin
HomeScreen(
    onLogout = {
        authRepo.logout()
        currentScreen = Screen.Login
    }
)
```
- Call `authRepo.logout()` to sign out
- Navigate back to login screen

### Exam pattern: State-based navigation
1. Create enum for screens
2. Use `mutableStateOf` for current screen
3. Use `when` to render current screen
4. Pass callbacks to change screen state

---

## Updated FirebaseRepository

### Location: `firebase/FirebaseRepository.kt`

```kotlin
class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val posts = db.collection("posts")

    fun seedPosts(count: Int, onDone: () -> Unit, onError: (Throwable) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError(Exception("Must be logged in to seed posts"))
            return
        }

        val batch = db.batch()
        repeat(count) { index ->
            val id = "seed_${index + 1}"
            val p = Post(
                id = id,
                authorId = currentUser.uid,  // User's unique ID
                authorName = currentUser.displayName ?: "Anonymous",
                handle = "@${currentUser.displayName?.replace(" ", "")?.lowercase() ?: "user"} · ${index + 1}h",
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
```

### Key changes:

**1. Get current user:**
```kotlin
val currentUser = auth.currentUser
if (currentUser == null) {
    onError(Exception("Must be logged in to seed posts"))
    return
}
```
- Check authentication before writing
- Early return if not logged in
- Prevents unauthorized writes

**2. Use user data:**
```kotlin
authorId = currentUser.uid,
authorName = currentUser.displayName ?: "Anonymous",
```
- `currentUser.uid`: Unique user ID (never changes)
- `currentUser.displayName`: Name from sign up
- `?: "Anonymous"`: Fallback if name is null

**3. Generate handle:**
```kotlin
handle = "@${currentUser.displayName?.replace(" ", "")?.lowercase() ?: "user"} · ${index + 1}h"
```
- `?.replace(" ", "")`: Remove spaces (null-safe)
- `?.lowercase()`: Convert to lowercase (null-safe)
- `?: "user"`: Fallback if name is null
- String template: `${}` embeds expressions

**4. Batch write:**
```kotlin
val batch = db.batch()
repeat(count) { index ->
    val ref = posts.document(id)
    batch.set(ref, p, SetOptions.merge())
}
batch.commit()
```
- `batch`: Group multiple writes into one operation
- More efficient than individual writes
- All succeed or all fail (atomic)

### Exam tip: Always check auth before writes
```kotlin
val currentUser = auth.currentUser ?: run {
    onError(Exception("Not authenticated"))
    return
}
```

---

## Firestore Security Rules

### Location: `firestore.rules`

```
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /posts/{postId} {
      allow read: if true;
      
      allow create: if request.auth != null 
                    && request.resource.data.authorId == request.auth.uid;
      
      allow update, delete: if request.auth != null 
                            && resource.data.authorId == request.auth.uid;
    }
    
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### Breaking down the rules:

**1. Version declaration:**
```
rules_version = '2';
```
- Always start with this
- Specifies rules syntax version

**2. Service and database:**
```
service cloud.firestore {
  match /databases/{database}/documents {
```
- `service cloud.firestore`: These are Firestore rules
- `match /databases/{database}/documents`: Match all databases

**3. Posts collection:**
```
match /posts/{postId} {
```
- Matches any document in `posts` collection
- `{postId}`: Variable for document ID

**4. Read rule:**
```
allow read: if true;
```
- Anyone can read posts (public timeline)
- `if true`: Always allowed

**5. Create rule:**
```
allow create: if request.auth != null 
              && request.resource.data.authorId == request.auth.uid;
```
- `request.auth != null`: User must be logged in
- `request.resource.data`: The data being written
- `.authorId == request.auth.uid`: Author ID must match user's ID
- `&&`: Both conditions must be true

**6. Update/Delete rule:**
```
allow update, delete: if request.auth != null 
                      && resource.data.authorId == request.auth.uid;
```
- `resource.data`: Existing document data
- Only the author can modify their posts

**7. Deny all else:**
```
match /{document=**} {
  allow read, write: if false;
}
```
- `{document=**}`: Matches any path (wildcard)
- `if false`: Always denied
- Default deny for security

### Exam pattern: Security rules
1. Start with `rules_version = '2';`
2. Match collections: `match /collection/{docId}`
3. Check auth: `request.auth != null`
4. Verify ownership: `resource.data.authorId == request.auth.uid`
5. Default deny: `if false`

---

## Summary: Key Exam Concepts

### 1. Firebase Auth Pattern
```kotlin
auth.createUserWithEmailAndPassword(email, password)
    .addOnSuccessListener { result -> /* success */ }
    .addOnFailureListener { e -> /* error */ }
```

### 2. Compose State
```kotlin
var state by remember { mutableStateOf(initialValue) }
```

### 3. Callbacks
```kotlin
fun doSomething(
    onSuccess: (Result) -> Unit,
    onError: (Exception) -> Unit
)
```

### 4. Null Safety
```kotlin
value?.let { /* use it */ } ?: fallback
```

### 5. Navigation
```kotlin
var screen by remember { mutableStateOf(Screen.Login) }
when (screen) {
    Screen.Login -> LoginScreen { screen = Screen.Home }
    Screen.Home -> HomeScreen { screen = Screen.Login }
}
```

### 6. Security Rules
```
allow operation: if condition;
```

---

## Practice Exercise

Try writing from memory:
1. A simple login function with email/password
2. A Compose screen with TextField and Button
3. Basic Firestore security rule for authenticated writes

This will help cement the patterns for your exam!
