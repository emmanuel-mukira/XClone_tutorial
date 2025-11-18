# Updated Database Rules for Per-User Like System

## New Rules for Individual User Likes

Replace your current Firebase Realtime Database rules with this updated version:

```json
{
  "rules": {
    "posts": {
      ".read": true,
      "$postId": {
        ".write": "auth != null && (!data.exists() || data.child('authorId').val() === auth.uid)",
        ".validate": "newData.hasChildren(['id','authorId','authorName','handle','text','likeCount','timestamp'])",
        "id": { ".validate": "newData.isString()" },
        "authorId": { ".validate": "newData.isString() && newData.val() === auth.uid" },
        "authorName": { ".validate": "newData.isString()" },
        "handle": { ".validate": "newData.isString()" },
        "text": { ".validate": "newData.isString()" },
        "likeCount": { ".validate": "newData.isNumber()" },
        "timestamp": { ".validate": "newData.isNumber()" }
      }
    },
    "userLikes": {
      "$userId": {
        ".read": "$userId === auth.uid",
        ".write": "$userId === auth.uid",
        "$postId": {
          ".validate": "newData.isBoolean() || newData.isString()"
        }
      }
    }
  }
}
```

## Key Changes

### 1. Removed 'liked' Field from Posts
```json
".validate": "newData.hasChildren(['id','authorId','authorName','handle','text','likeCount','timestamp'])",
```
- Removed 'liked' from required fields
- Posts no longer track individual user likes

### 2. Added userLikes Node
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

**Security Features:**
- Users can only read their own likes (`$userId === auth.uid`)
- Users can only write their own likes (`$userId === auth.uid`)
- Each like is stored as boolean: `userLikes/userId/postId = true`

## Database Structure

### Posts Node
```
posts/
  seed_1/
    id: "seed_1"
    authorId: "author_001"
    authorName: "David Thompson"
    handle: "@davidthompson · 1h"
    text: "The Lord is my shepherd..."
    likeCount: 42
    timestamp: 1700000000000
```

### userLikes Node
```
userLikes/
  currentUserUid/
    seed_1: true    // User liked this post
    seed_3: true    // User liked this post
    seed_2: null    // User didn't like this post (or was removed)
```

## How It Works

### When User Likes a Post:
1. Check if `userLikes/userId/postId` exists
2. If not exists → Add like:
   - Set `userLikes/userId/postId = true`
   - Increment `posts/postId/likeCount`
3. If exists → Remove like:
   - Set `userLikes/userId/postId = null`
   - Decrement `posts/postId/likeCount`

### When Different User Views Posts:
1. Fetch all posts (same for everyone)
2. Check their own `userLikes/userId/` node
3. Show red heart only for posts they personally liked
4. See updated like counts from all users' actions

## Steps to Update

1. Go to Firebase Console → Realtime Database → Rules
2. Replace existing rules with the JSON above
3. Click "Publish"
4. Clear existing data (optional, to remove old liked fields)

## Test It

1. **User A logs in** → Likes post 1 → Sees red heart, count = 4
2. **User B logs in** → Sees post 1 with outline heart, count = 4
3. **User B likes post 1** → Sees red heart, count = 5
4. **User A logs back in** → Still sees red heart, count = 5

Each user now has their own like state while sharing the same like count!
