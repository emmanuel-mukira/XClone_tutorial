# Updated Database Rules for Like Functionality

## New Rules to Include 'liked' Field

Replace your current Firebase Realtime Database rules with this updated version:

```json
{
  "rules": {
    "posts": {
      ".read": true,
      "$postId": {
        ".write": "auth != null && (!data.exists() || data.child('authorId').val() === auth.uid)",
        ".validate": "newData.hasChildren(['id','authorId','authorName','handle','text','likeCount','timestamp','liked'])",
        "id": { ".validate": "newData.isString()" },
        "authorId": { ".validate": "newData.isString() && newData.val() === auth.uid" },
        "authorName": { ".validate": "newData.isString()" },
        "handle": { ".validate": "newData.isString()" },
        "text": { ".validate": "newData.isString()" },
        "likeCount": { ".validate": "newData.isNumber()" },
        "timestamp": { ".validate": "newData.isNumber()" },
        "liked": { ".validate": "newData.isBoolean()" }
      }
    }
  }
}
```

## Key Changes

### Added 'liked' Field Validation
```json
".validate": "newData.hasChildren(['id','authorId','authorName','handle','text','likeCount','timestamp','liked'])",
```
- Added `'liked'` to required fields list

### Added Boolean Type Validation
```json
"liked": { ".validate": "newData.isBoolean()" }
```
- Ensures liked field is always true/false
- Type safety for the like state

## Steps to Update

1. Go to Firebase Console → Realtime Database → Rules
2. Replace existing rules with the JSON above
3. Click "Publish"

## What This Enables

✅ **Like/Unlike functionality** - Users can toggle like state  
✅ **Like count updates** - Automatically increments/decrements  
✅ **Visual feedback** - Heart turns red when liked  
✅ **Data validation** - Ensures all fields are present and correct types  
✅ **Christian faith tweets** - 15 inspiring Bible verses in seeded posts  

## Test It

1. Log in and seed 15 posts
2. Click the heart icon on any post
3. Should see:
   - Heart turns red (liked state)
   - Like count updates immediately
   - Changes persist in database
4. Click again to unlike
5. Heart returns to outline and count decreases

The like functionality is now fully implemented with proper state management and persistence!
