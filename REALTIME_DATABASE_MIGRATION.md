# Firebase Realtime Database Migration Guide

## What Changed

We've migrated from **Firestore** to **Firebase Realtime Database** as requested.

## Changes Made

### 1. Gradle Dependencies

**Before (Firestore):**
```kotlin
implementation("com.google.firebase:firebase-firestore-ktx")
```

**After (Realtime Database):**
```kotlin
implementation("com.google.firebase:firebase-database-ktx")
```

### 2. FirebaseRepository API

**Before (Firestore API):**
```kotlin
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
}
```

**After (Realtime Database API):**
```kotlin
class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance()
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
}
```

### 3. Security Rules

**Before (Firestore Rules):**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /posts/{postId} {
      allow read: if true;
      allow create: if request.auth != null;
    }
  }
}
```

**After (Realtime Database Rules):**
```json
{
  "rules": {
    "posts": {
      "$postId": {
        ".read": true,
        ".write": "auth != null && (!data.exists() || data.child('authorId').val() === auth.uid)"
      }
    }
  }
}
```

## Key API Differences

### Data Structure

**Firestore:**
- Document-based (like MongoDB)
- Collections contain documents
- Documents contain fields
- Path: `/posts/seed_1`

**Realtime Database:**
- JSON tree structure
- References point to nodes
- Nodes contain key-value pairs
- Path: `/posts/seed_1`

### Reading Data

**Firestore:**
```kotlin
db.collection("posts").document(id).get()
    .addOnSuccessListener { doc ->
        val post = doc.toObject(Post::class.java)
    }
```

**Realtime Database:**
```kotlin
database.getReference("posts").child(id)
    .addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val post = snapshot.getValue(Post::class.java)
        }
        override fun onCancelled(error: DatabaseError) { }
    })
```

### Writing Data

**Firestore (Batch):**
```kotlin
val batch = db.batch()
batch.set(ref, data)
batch.commit()
```

**Realtime Database (Multi-path Update):**
```kotlin
val updates = mutableMapOf<String, Any>()
updates["posts/$id"] = data
database.reference.updateChildren(updates)
```

## Security Rules Syntax

### Firestore Rules

```
match /posts/{postId} {
  allow read: if condition;
  allow write: if condition;
}
```

- Uses `request.auth` for authentication
- Uses `request.resource.data` for new data
- Uses `resource.data` for existing data

### Realtime Database Rules

```json
"posts": {
  "$postId": {
    ".read": "condition",
    ".write": "condition",
    ".validate": "condition"
  }
}
```

- Uses `auth` for authentication
- Uses `newData` for new data
- Uses `data` for existing data
- Uses `$variable` for wildcards

## Production Rules Explained

```json
{
  "rules": {
    "posts": {
      "$postId": {
        ".read": true,
        ".write": "auth != null && (!data.exists() || data.child('authorId').val() === auth.uid)",
        ".validate": "newData.hasChildren(['id', 'authorId', 'authorName', 'handle', 'text', 'likeCount', 'timestamp'])",
        "authorId": {
          ".validate": "newData.val() === auth.uid"
        }
      }
    }
  }
}
```

### Breaking Down the Rules:

**1. Read Rule:**
```json
".read": true
```
- Anyone can read posts (public timeline)
- No authentication required

**2. Write Rule:**
```json
".write": "auth != null && (!data.exists() || data.child('authorId').val() === auth.uid)"
```
- `auth != null` - User must be logged in
- `!data.exists()` - Allows creating new posts
- `data.child('authorId').val() === auth.uid` - For updates, authorId must match current user
- **Result:** Users can create their own posts and update only their own posts

**3. Validation Rule:**
```json
".validate": "newData.hasChildren(['id', 'authorId', ...])"
```
- Ensures all required fields are present
- Prevents incomplete data

**4. AuthorId Validation:**
```json
"authorId": {
  ".validate": "newData.val() === auth.uid"
}
```
- Ensures `authorId` always matches the authenticated user
- Prevents users from impersonating others

## How to Update Rules in Firebase Console

### Step 1: Go to Realtime Database
1. Open Firebase Console
2. Select your project
3. Click **Realtime Database** (not Firestore!)
4. Click **Rules** tab

### Step 2: Replace Rules
1. Copy the rules from `database.rules.json`
2. Paste into the Firebase Console
3. Click **Publish**

### Step 3: Verify
Your rules should look like this in the console:
```json
{
  "rules": {
    "posts": {
      "$postId": {
        ".read": true,
        ".write": "auth != null && (!data.exists() || data.child('authorId').val() === auth.uid)",
        ".validate": "newData.hasChildren(['id', 'authorId', 'authorName', 'handle', 'text', 'likeCount', 'timestamp'])",
        "authorId": {
          ".validate": "newData.val() === auth.uid"
        }
      }
    }
  }
}
```

## Data Structure in Realtime Database

Your data will be stored as:

```json
{
  "posts": {
    "seed_1": {
      "id": "seed_1",
      "authorId": "user_uid_here",
      "authorName": "John Doe",
      "handle": "@johndoe · 1h",
      "text": "Seeded post #1",
      "likeCount": 3,
      "timestamp": 1700000000000
    },
    "seed_2": {
      "id": "seed_2",
      "authorId": "user_uid_here",
      "authorName": "John Doe",
      "handle": "@johndoe · 2h",
      "text": "Seeded post #2",
      "likeCount": 6,
      "timestamp": 1699999940000
    }
  }
}
```

## Testing the Migration

### 1. Sync Gradle
```
File → Sync Project with Gradle Files
```

### 2. Update Rules in Firebase Console
- Go to Realtime Database → Rules
- Copy from `database.rules.json`
- Publish

### 3. Run the App
```
Build → Rebuild Project
Run app
```

### 4. Test Flow
1. Sign up with name, email, password
2. Press "Seed posts" button
3. Should see "Seeded 10 posts" message
4. App loads and displays seed_1 post

### 5. Verify in Firebase Console
- Go to Realtime Database → Data
- You should see `posts` node with `seed_1` through `seed_10`

## Common Issues and Solutions

### Issue 1: "Permission denied"
**Cause:** Rules not updated or still set to deny all

**Solution:**
```json
// Your current rules (deny all):
{
  "rules": {
    ".read": false,
    ".write": false
  }
}

// Replace with production rules from database.rules.json
```

### Issue 2: "Database not found"
**Cause:** Realtime Database not created in Firebase Console

**Solution:**
1. Go to Firebase Console
2. Click "Realtime Database"
3. Click "Create Database"
4. Choose region
5. Start in **test mode** (we'll update rules after)

### Issue 3: Data not appearing
**Cause:** Wrong database reference or rules blocking

**Solution:**
1. Check Firebase Console → Realtime Database → Data
2. Verify rules are published
3. Check app logs for errors

## Advantages of Realtime Database for This Project

✅ **Simpler structure** - JSON tree is easier to understand
✅ **Real-time sync** - Changes propagate instantly
✅ **Lower cost** - Better pricing for small apps
✅ **Easier rules** - Simpler syntax than Firestore
✅ **Good for learning** - Clearer data model

## When to Use Firestore Instead

Use Firestore if you need:
- Complex queries (multiple filters, sorting)
- Better offline support
- Automatic scaling
- Document-based structure
- More flexible data model

For this coursework project, **Realtime Database is perfect!**

## Summary

✅ Migrated from Firestore to Realtime Database
✅ Updated all API calls
✅ Created production-ready security rules
✅ Rules allow authenticated writes and public reads
✅ Data structure remains the same (Post model)
✅ ViewModel and UI code unchanged (abstraction works!)

The migration is complete and ready to test!
