# ViewModels & State Management - Exam Study Guide

## Part 1: Understanding ViewModels and State

---

## What is a ViewModel?

A **ViewModel** is a class that:
- Manages UI state
- Survives configuration changes (screen rotation)
- Handles business logic
- Communicates with repositories
- Lives longer than the UI (Composables)

**Why use it?** Separates UI from logic, making code testable and maintainable.

---

## AuthViewModel.kt — Authentication State Manager

### Purpose
Manages login and sign-up state for the entire authentication flow.

### Full Code

```kotlin
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    val currentUser: FirebaseUser?
        get() = authRepository.getCurrentUser()
    
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val cleanName = name.trim()
            val cleanEmail = email.trim().lowercase()
            val cleanPassword = password.trim()
            authRepository.signUp(
                name = cleanName,
                email = cleanEmail,
                password = cleanPassword,
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                },
                onError = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
            )
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val cleanEmail = email.trim().lowercase()
            val cleanPassword = password.trim()
            authRepository.login(
                email = cleanEmail,
                password = cleanPassword,
                onSuccess = { user ->
                    _authState.value = AuthState.Success(user)
                },
                onError = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }
    
    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

### Line-by-Line Breakdown

#### 1. ViewModel Inheritance
```kotlin
class AuthViewModel : ViewModel() {
```
- `class` = Define a new class
- `AuthViewModel` = Class name
- `: ViewModel()` = Inherits from Android's ViewModel class
- `ViewModel()` provides lifecycle awareness

**Exam tip:** Always extend `ViewModel()` for state management classes.

---

#### 2. Repository Instance
```kotlin
private val authRepository = AuthRepository()
```
- `private` = Only accessible within this class
- `val` = Immutable reference (can't reassign)
- `authRepository` = Instance of AuthRepository
- `= AuthRepository()` = Create new instance

**Why?** ViewModel delegates data operations to Repository.

---

#### 3. Private Mutable State
```kotlin
private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
```
- `private` = Internal use only
- `val` = Reference is immutable
- `_authState` = Underscore prefix indicates private
- `MutableStateFlow<AuthState>` = Observable state container
- `<AuthState>` = Generic type parameter
- `(AuthState.Idle)` = Initial state value

**What is StateFlow?**
- Like LiveData but for Kotlin coroutines
- Always has a current value
- Emits updates to collectors
- Hot stream (always active)

**Exam tip:** Use `_` prefix for private mutable state.

---

#### 4. Public Immutable State
```kotlin
val authState: StateFlow<AuthState> = _authState.asStateFlow()
```
- `val` = Public immutable property
- `authState` = What UI observes
- `: StateFlow<AuthState>` = Type annotation (read-only)
- `= _authState.asStateFlow()` = Convert to read-only

**Why two properties?**
- `_authState` = ViewModel can modify
- `authState` = UI can only read
- Encapsulation: UI can't accidentally change state

**Exam tip:** Always expose read-only StateFlow to UI.

---

#### 5. Current User Property
```kotlin
val currentUser: FirebaseUser?
    get() = authRepository.getCurrentUser()
```
- `val` = Property
- `currentUser` = Property name
- `: FirebaseUser?` = Nullable type (might be null if not logged in)
- `get() =` = Custom getter
- Returns current user from repository

**Exam tip:** Use custom getters for computed properties.

---

#### 6. Sign Up Function
```kotlin
fun signUp(name: String, email: String, password: String) {
```
- `fun` = Function declaration
- `signUp` = Function name
- `(name: String, ...)` = Parameters with types

---

#### 7. Coroutine Scope
```kotlin
viewModelScope.launch {
```
- `viewModelScope` = Coroutine scope tied to ViewModel lifecycle
- `.launch` = Start a new coroutine
- `{ }` = Coroutine body (lambda)

**What is viewModelScope?**
- Automatically cancelled when ViewModel is destroyed
- Prevents memory leaks
- Tied to ViewModel lifecycle

**Exam tip:** Always use `viewModelScope` in ViewModels, never `GlobalScope`.

---

#### 8. Set Loading State
```kotlin
_authState.value = AuthState.Loading
```
- `_authState.value` = Access current value
- `=` = Assignment
- `AuthState.Loading` = New state value

**Effect:** UI sees Loading state and shows progress indicator.

---

#### 9. Input Sanitization
```kotlin
val cleanName = name.trim()
val cleanEmail = email.trim().lowercase()
val cleanPassword = password.trim()
```
- `trim()` = Remove leading/trailing whitespace
- `lowercase()` = Convert to lowercase
- Prevents "badly formatted" errors

**Exam tip:** Always sanitize user input before sending to backend.

---

#### 10. Repository Call with Callbacks
```kotlin
authRepository.signUp(
    name = cleanName,
    email = cleanEmail,
    password = cleanPassword,
    onSuccess = { user ->
        _authState.value = AuthState.Success(user)
    },
    onError = { exception ->
        _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
    }
)
```
- Named parameters for clarity
- `onSuccess = { user -> }` = Lambda for success callback
- `onError = { exception -> }` = Lambda for error callback
- `exception.message ?: "Sign up failed"` = Elvis operator (use message or fallback)

**Exam tip:** Use named parameters for functions with multiple callbacks.

---

#### 11. Sealed Class for State
```kotlin
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

**What is sealed class?**
- Limited set of subclasses
- All subclasses defined in same file
- Compiler knows all possible types
- Perfect for state representation

**Types of subclasses:**
- `object` = Singleton, no data (Idle, Loading)
- `data class` = Holds data (Success has user, Error has message)

**Exam tip:** Use sealed classes for state. Compiler ensures you handle all cases in `when`.

---

## HomeViewModel.kt — Post State Manager

### Purpose
Manages post fetching and seeding operations for the home feed.

### Full Code

```kotlin
class HomeViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _postState = MutableStateFlow<PostState>(PostState.Loading)
    val postState: StateFlow<PostState> = _postState.asStateFlow()
    
    private val _seedState = MutableStateFlow<SeedState>(SeedState.Idle)
    val seedState: StateFlow<SeedState> = _seedState.asStateFlow()
    
    init {
        fetchAllPosts()
    }
    
    fun fetchAllPosts() {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            repository.fetchAllPosts(
                onResult = { posts ->
                    if (posts.isNotEmpty()) {
                        _postState.value = PostState.Success(posts)
                    } else {
                        _postState.value = PostState.Empty
                    }
                },
                onError = { exception ->
                    _postState.value = PostState.Error(exception.message ?: "Failed to load posts")
                }
            )
        }
    }
    
    fun seedPosts(count: Int = 10) {
        viewModelScope.launch {
            _seedState.value = SeedState.Loading
            repository.seedPosts(
                count = count,
                onDone = {
                    _seedState.value = SeedState.Success(count)
                    fetchAllPosts()
                },
                onError = { exception ->
                    _seedState.value = SeedState.Error(exception.message ?: "Failed to seed posts")
                }
            )
        }
    }
    
    fun resetSeedState() {
        _seedState.value = SeedState.Idle
    }
}

sealed class PostState {
    object Loading : PostState()
    object Empty : PostState()
    data class Success(val posts: List<Post>) : PostState()
    data class Error(val message: String) : PostState()
}

sealed class SeedState {
    object Idle : SeedState()
    object Loading : SeedState()
    data class Success(val count: Int) : SeedState()
    data class Error(val message: String) : SeedState()
}
```

### Key Differences from AuthViewModel

#### 1. Two Separate States
```kotlin
private val _postState = MutableStateFlow<PostState>(PostState.Loading)
private val _seedState = MutableStateFlow<SeedState>(SeedState.Idle)
```

**Why two states?**
- `postState` = For displaying posts
- `seedState` = For seeding feedback
- Independent concerns
- UI can react to each separately

**Exam tip:** Use separate states for independent operations.

---

#### 2. Init Block
```kotlin
init {
    fetchAllPosts()
}
```
- `init` = Initialization block
- Runs when ViewModel is created
- Automatically loads posts on screen open

**Exam tip:** Use `init` for automatic data loading.

---

#### 3. Empty State
```kotlin
sealed class PostState {
    object Loading : PostState()
    object Empty : PostState()  // NEW
    data class Success(val posts: List<Post>) : PostState()
    data class Error(val message: String) : PostState()
}
```

**Why Empty state?**
- Distinguish between loading and no data
- Show appropriate message: "No posts yet" vs "Loading..."

---

#### 4. List Instead of Single Item
```kotlin
data class Success(val posts: List<Post>) : PostState()
```
- `List<Post>` = Multiple posts
- Not just one post
- For scrollable feed

---

#### 5. Refresh After Seeding
```kotlin
onDone = {
    _seedState.value = SeedState.Success(count)
    fetchAllPosts()  // Refresh posts
}
```
- After seeding completes, fetch all posts again
- Ensures UI shows newly seeded posts

**Exam tip:** Chain operations when one depends on another.

---

## Key Patterns for Exams

### Pattern 1: StateFlow Declaration
```kotlin
private val _state = MutableStateFlow<StateType>(InitialState)
val state: StateFlow<StateType> = _state.asStateFlow()
```
- Private mutable for ViewModel
- Public immutable for UI
- Always provide initial state

---

### Pattern 2: Coroutine Launch
```kotlin
fun doSomething() {
    viewModelScope.launch {
        _state.value = State.Loading
        // async operation
        _state.value = State.Success(data)
    }
}
```
- Use `viewModelScope.launch`
- Set Loading before operation
- Update state with result

---

### Pattern 3: Sealed Class State
```kotlin
sealed class MyState {
    object Loading : MyState()
    data class Success(val data: Data) : MyState()
    data class Error(val message: String) : MyState()
}
```
- Use `object` for states without data
- Use `data class` for states with data
- Compiler ensures all cases handled

---

### Pattern 4: Input Sanitization
```kotlin
val cleanInput = input.trim().lowercase()
```
- Always clean user input
- Prevents formatting errors

---

## Summary: ViewModel Responsibilities

✅ **Manage UI state** (Loading, Success, Error)  
✅ **Handle business logic** (validation, sanitization)  
✅ **Call repository methods** (don't access Firebase directly)  
✅ **Survive configuration changes** (screen rotation)  
✅ **Provide data to UI** (via StateFlow)  

❌ **Don't** access Android framework classes (Context, Resources)  
❌ **Don't** hold references to Views or Composables  
❌ **Don't** make direct Firebase calls  

**Exam tip:** ViewModel = State + Logic. UI = Observe + Render.
