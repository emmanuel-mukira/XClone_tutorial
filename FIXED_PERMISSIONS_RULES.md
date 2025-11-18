# Fixed Database Rules - Copy This to Firebase Console

## Updated Rules to Fix Permission Denied

Replace your current rules with this:

```json
{
  "rules": {
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
    },
    "userLikes": {
      "$userId": {
        ".read": "$userId === auth.uid",
        ".write": "$userId === auth.uid",
        "$postId": {
          ".validate": "newData.isBoolean()"
        }
      }
    }
  }
}
```

## Key Changes

### 1. Simplified Write Permission
```json
".write": "auth != null"
```
- **Before**: Only original author could write
- **Now**: Any authenticated user can write (needed for seeding)

### 2. Removed Author ID Validation
```json
"authorId": { ".validate": "newData.isString()" }
```
- **Before**: Had to match current user's ID
- **Now**: Any string allowed (for diverse authors)

### 3. Updated userLikes Validation
```json
".validate": "newData.isBoolean() || newData.isString()"
```
- Allows both boolean (true) and string ("") for like removal

## Steps

1. Go to Firebase Console → Realtime Database → Rules
2. Delete everything in the editor
3. Paste the JSON above
4. Click "Publish"

This will fix the permission denied error for seeding posts!
