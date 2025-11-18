# Firebase Realtime Database Security Rules Guide

## Production Rules (Recommended - Now Implemented!)

The app now uses Firebase Realtime Database with Authentication. Here are production-ready rules:

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

✅ **These rules are secure and ready for production!**

The rules file is saved at `database.rules.json` in your project root.

## What These Rules Do

1. **Read posts**: Anyone can read (public timeline)
   - `.read: true` - No authentication required for reading

2. **Create posts**: Only authenticated users can create
   - `auth != null` - User must be logged in
   - `!data.exists()` - Allows creation of new posts
   - Validates `authorId` matches the authenticated user's UID

3. **Update/Delete posts**: Only the author can modify their own posts
   - `data.child('authorId').val() === auth.uid` - Existing post's authorId must match current user

4. **Validation**: Ensures all required fields are present
   - `newData.hasChildren([...])` - Checks for required fields
   - `authorId` must equal the authenticated user's UID

## How to Update Rules

1. Go to Firebase Console → **Realtime Database** → Rules
2. Copy the rules from `database.rules.json` file in your project
3. Paste and click "Publish"
4. Wait 1-2 minutes for propagation

## Testing Your Rules

After updating to production rules:
1. Go to Firebase Console → **Realtime Database** → Rules
2. Copy the rules from `database.rules.json` file in your project
3. Paste and click "Publish"
4. Wait 1-2 minutes for propagation
5. Sync Gradle in Android Studio
6. Run the app
7. Sign up with name, email, and password
8. Press "Seed posts" button
9. Should see "Seeded 10 posts" message
10. App will automatically load and display seed_1 post

## Firebase Console Setup

Make sure you've enabled:
1. **Authentication** → Email/Password provider
2. **Realtime Database** → Created in your preferred region
3. **Rules** → Updated with production rules above

## Realtime Database vs Firestore

You're now using **Realtime Database** which:
- ✅ Stores data as JSON tree
- ✅ Real-time synchronization
- ✅ Simpler structure
- ✅ Good for simple data models
- ✅ Lower cost for small apps

**Key Differences:**
- Firestore: Document-based, better for complex queries
- Realtime Database: JSON tree, better for real-time updates
