# MVVM Architecture Implementation

## Why MVVM? (Answering Your Questions)

### 1. String Resource Issue - Your Solution Was Right!

**The Problem:**
```kotlin
// ❌ This doesn't work - stringResource() is @Composable
onError = { e ->
    errorMessage = e.message ?: stringResource(R.string.auth_error)
}
```

**Your Suggested Solution:**
```kotlin
// ✅ Get string in Composable scope, use in callback
@Composable
fun LoginScreen() {
    val authErrorString = stringResource(R.string.auth_error)
    
    Button(onClick = {
        authRepo.login(
            onError = { e ->
                errorMessage = e.message ?: authErrorString  // Works!
            }
        )
    })
}
```

**Even Better with ViewModel:**
```kotlin
// ✅✅ ViewModel handles the error, UI just displays it
viewModel.login(email, password)

// In ViewModel:
onError = { exception ->
    _authState.value = AuthState.Error(exception.message ?: "Login failed")
}

// In UI:
if (authState is AuthState.Error) {
    Text((authState as AuthState.Error).message)
}
```

### 2. Why ViewModel for Logic?

**Before (Logic in Composable):**
```kotlin
@Composable
fun LoginScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authRepo = remember { AuthRepository() }
    
    Button(onClick = {
        isLoading = true
        errorMessage = null
        authRepo.login(
            email, password,
            onSuccess = {
                isLoading = false
                onLoginSuccess()
            },
            onError = { e ->
                isLoading = false
                errorMessage = e.message
            }
        )
    })
}
```

**Problems:**
- ❌ Business logic mixed with UI
- ❌ State management scattered
- ❌ Hard to test (need to render UI)
- ❌ State lost on configuration change (rotation)
- ❌ Repository created on every recomposition

**After (Logic in ViewModel):**
```kotlin
// ViewModel
class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(
                email, password,
                onSuccess = { _authState.value = AuthState.Success(it) },
                onError = { _authState.value = AuthState.Error(it.message) }
            )
        }
    }
}

// UI
@Composable
fun LoginScreen(viewModel: AuthViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsState()
    
    Button(onClick = { viewModel.login(email, password) })
    
    when (authState) {
        is AuthState.Loading -> /* Show loading */
        is AuthState.Error -> /* Show error */
        is AuthState.Success -> /* Navigate */
    }
}
```

**Benefits:**
- ✅ Clear separation: UI renders, ViewModel handles logic
- ✅ Easy to test ViewModel without UI
- ✅ State survives configuration changes
- ✅ Repository created once
- ✅ Cleaner, more maintainable code

### 3. MVVM Architecture Benefits

**M**odel - **V**iew - **V**iew**M**odel

```
┌─────────────┐
│    View     │  (Composables)
│  (UI Layer) │  - Renders UI
│             │  - Handles user input
│             │  - Observes state
└──────┬──────┘
       │ observes state
       │ calls functions
┌──────▼──────┐
│  ViewModel  │  (Business Logic)
│             │  - Manages UI state
│             │  - Handles business logic
│             │  - Survives config changes
└──────┬──────┘
       │ calls
       │ receives data
┌──────▼──────┐
│ Repository  │  (Data Layer)
│             │  - Fetches data
│             │  - Firebase calls
│             │  - Data transformation
└─────────────┘
```

## Implementation Details

### 1. AuthViewModel

**Purpose:** Handle all authentication logic

```kotlin
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    // Private mutable state (only ViewModel can modify)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    
    // Public immutable state (UI can only observe)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {  // Coroutine tied to ViewModel lifecycle
            _authState.value = AuthState.Loading
            authRepository.signUp(
                name, email, password,
                onSuccess = { _authState.value = AuthState.Success(it) },
                onError = { _authState.value = AuthState.Error(it.message ?: "Failed") }
            )
        }
    }
}
```

**Key Concepts:**

**StateFlow:**
- Observable state holder
- Always has a value
- UI can collect and react to changes
- Like LiveData but for Kotlin coroutines

**viewModelScope:**
- Coroutine scope tied to ViewModel lifecycle
- Automatically cancelled when ViewModel is cleared
- No memory leaks!

**Sealed Class for State:**
```kotlin
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
```
- Type-safe state representation
- Compiler ensures you handle all cases
- Clear, explicit states

### 2. HomeViewModel

**Purpose:** Handle post fetching and seeding

```kotlin
class HomeViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    
    private val _postState = MutableStateFlow<PostState>(PostState.Loading)
    val postState: StateFlow<PostState> = _postState.asStateFlow()
    
    private val _seedState = MutableStateFlow<SeedState>(SeedState.Idle)
    val seedState: StateFlow<SeedState> = _seedState.asStateFlow()
    
    init {
        fetchPost("seed_1")  // Load initial data
    }
    
    fun fetchPost(postId: String) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            repository.fetchPostById(
                id = postId,
                onResult = { post ->
                    _postState.value = if (post != null) {
                        PostState.Success(post)
                    } else {
                        PostState.Error("Post not found")
                    }
                },
                onError = { _postState.value = PostState.Error(it.message ?: "Failed") }
            )
        }
    }
}
```

**Why Two Separate States?**
- `postState`: For displaying posts
- `seedState`: For seeding operation feedback
- Independent concerns, independent states
- UI can react to each separately

### 3. Updated UI (LoginScreen)

**Before:**
```kotlin
@Composable
fun LoginScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authRepo = remember { AuthRepository() }
    
    // Manual state management, callback hell
}
```

**After:**
```kotlin
@Composable
fun LoginScreen(viewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    
    // Handle success
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.resetState()
                onLoginSuccess()
            }
            else -> {}
        }
    }
    
    // Show error
    if (authState is AuthState.Error) {
        Text((authState as AuthState.Error).message)
    }
    
    val isLoading = authState is AuthState.Loading
    
    Button(
        onClick = { viewModel.login(email, password) },
        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
    )
}
```

**Key Changes:**
1. **viewModel parameter:** Injected, testable
2. **collectAsState():** Observes StateFlow, triggers recomposition
3. **LaunchedEffect:** Side effects (navigation) when state changes
4. **Declarative UI:** UI is a function of state

### 4. Shared ViewModel (MainActivity)

```kotlin
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()  // Created once
    
    when (currentScreen) {
        Screen.SignUp -> SignUpScreen(
            viewModel = authViewModel  // Same instance
        )
        Screen.Login -> LoginScreen(
            viewModel = authViewModel  // Same instance
        )
        Screen.Home -> HomeScreen(
            onLogout = { authViewModel.logout() }  // Same instance
        )
    }
}
```

**Why Share?**
- Both SignUp and Login use same auth logic
- State persists when switching screens
- Single source of truth

## Comparison: Before vs After

### Before (No MVVM)

```kotlin
@Composable
fun LoginScreen() {
    // ❌ State management in UI
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // ❌ Repository in UI
    val authRepo = remember { AuthRepository() }
    
    Button(onClick = {
        // ❌ Business logic in UI
        isLoading = true
        errorMessage = null
        authRepo.login(
            email, password,
            onSuccess = {
                isLoading = false
                onLoginSuccess()
            },
            onError = { e ->
                isLoading = false
                errorMessage = e.message  // ❌ String handling issue
            }
        )
    })
    
    // ❌ Manual state rendering
    if (isLoading) { /* loading UI */ }
    errorMessage?.let { /* error UI */ }
}
```

**Problems:**
- Mixed concerns (UI + logic)
- Hard to test
- State lost on rotation
- String resource issues
- Callback hell

### After (MVVM)

```kotlin
// ViewModel (testable, reusable)
class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(
                email, password,
                onSuccess = { _authState.value = AuthState.Success(it) },
                onError = { _authState.value = AuthState.Error(it.message ?: "Failed") }
            )
        }
    }
}

// UI (simple, declarative)
@Composable
fun LoginScreen(viewModel: AuthViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsState()
    
    Button(onClick = { viewModel.login(email, password) })
    
    when (authState) {
        is AuthState.Loading -> CircularProgressIndicator()
        is AuthState.Error -> Text((authState as AuthState.Error).message)
        is AuthState.Success -> LaunchedEffect(Unit) { onLoginSuccess() }
        else -> {}
    }
}
```

**Benefits:**
- ✅ Clear separation
- ✅ Easy to test
- ✅ State survives rotation
- ✅ No string issues
- ✅ Clean, readable code

## Testing Benefits

### Testing ViewModel (Easy!)

```kotlin
@Test
fun `login with valid credentials should emit Success state`() = runTest {
    // Arrange
    val viewModel = AuthViewModel()
    
    // Act
    viewModel.login("test@example.com", "password123")
    
    // Assert
    val state = viewModel.authState.value
    assertTrue(state is AuthState.Success)
}
```

### Testing UI Without ViewModel (Hard!)

```kotlin
@Test
fun `login screen shows loading when logging in`() {
    // Need to:
    // - Render entire Composable
    // - Mock Firebase
    // - Simulate button click
    // - Wait for async operations
    // - Check UI state
    // Much more complex!
}
```

## Key Takeaways for Exams

### 1. MVVM Pattern
```
View (UI) → observes → ViewModel (Logic) → calls → Repository (Data)
```

### 2. ViewModel Basics
```kotlin
class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow(InitialState)
    val state: StateFlow = _state.asStateFlow()
    
    fun doSomething() {
        viewModelScope.launch {
            _state.value = NewState
        }
    }
}
```

### 3. UI Observing State
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is Loading -> /* loading UI */
        is Success -> /* success UI */
        is Error -> /* error UI */
    }
}
```

### 4. Sealed Classes for State
```kotlin
sealed class MyState {
    object Loading : MyState()
    data class Success(val data: Data) : MyState()
    data class Error(val message: String) : MyState()
}
```

## Summary

Your questions were spot-on! MVVM architecture:

1. ✅ **Solves string resource issue** - Handle errors in ViewModel, display in UI
2. ✅ **Separates concerns** - UI renders, ViewModel handles logic
3. ✅ **Makes code easier to understand** - Clear responsibilities
4. ✅ **Industry standard** - What you'll see in real Android projects
5. ✅ **Testable** - Test logic without UI
6. ✅ **Lifecycle-aware** - State survives configuration changes

This is the **proper way** to build Android apps with Jetpack Compose!
