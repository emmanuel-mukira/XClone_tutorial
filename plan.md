# XClone_tutorial — Coursework Plan

## Overview
- **Goal**: Build three small Android deliverables aligned to lecturer requirements.
- **Phases** map 1:1 to assessment bullets:
  - **Phase 1**: Jetpack Compose — Static social timeline (Twitter/X-like)
  - **Phase 2**: RecyclerView — Simple list app rendering multiple posts
  - **Phase 3**: Firebase Realtime Database — Basic read/write of posts (+ optional Auth)
- **Submission**: Each phase zipped separately with short demo video and code.
 

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Phase 1), XML + RecyclerView (Phase 2)
- **Backend**: Firebase Realtime Database (Phase 3), optional Firebase Auth
- **Min SDK**: 24 or as per template
- **Build**: Gradle (KTS if project uses it)

## Repository Structure (proposed)
- **app**: Main Android app module
  - `ui/compose/` — Compose screen(s) for Phase 1
  - `recycler/` — RecyclerView Activity, Adapter, ViewHolder, item XML for Phase 2
  - `firebase/` — Data models, repository, and UI for Phase 3
  - `model/` — Shared data classes (e.g., `Post`)
  - `theme/` — Compose theme
- **plan.md** — This plan
- **/submission/** — Folder to place per-phase zips and demo videos

## Phase 1 — Jetpack Compose (Static Screen)
- **Goal**: One functional UI page with a top bar, navigation drawer, and a single Tweet card (no list yet).
- **Status**: Step 1 completed (project runs on emulator).

### Step-by-step tasks (actionable)
- **Step 1 — Project setup [Done]**
  - Create/confirm Empty Compose Activity project (Kotlin) builds and runs.
  - Verify Compose Preview renders default screen.
  - Acceptance: App runs on emulator without errors.

- **Step 2 — Basic structure**
  - Create `ui/home/HomeScreen.kt`.
  - Implement `Scaffold` with `TopAppBar` (Material 3) and empty `content`.
  - Add `@Preview(showBackground = true)` for `HomeScreen`.
  - Acceptance: White page with a top bar title when running.

- **Step 3 — Navigation drawer**
  - Wrap `Scaffold` in `ModalNavigationDrawer` with `drawerContent`.
  - Items: Home, Profile, Settings, Logout (static; no navigation logic yet).
  - Use `rememberDrawerState` + `rememberCoroutineScope` to open via top bar menu icon.
  - Acceptance: Swipe or tap menu to open drawer and see items.

- **Step 4 — Tweet UI**
  - Create `ui/components/TweetCard.kt` with `@Composable fun TweetCard()`.
  - Layout: Avatar circle (placeholder) + name/handle/time row + multi-line text + actions row (comment/retweet/like/share with `Icons.Default.*`).
  - Add `@Preview` for `TweetCard`.
  - Acceptance: Tweet card looks clean and balanced in preview.

- **Step 5 — Combine**
  - Place `TweetCard()` into `HomeScreen` `content` with padding and spacing.
  - Acceptance: App shows top bar, one tweet card, and a working drawer.

- **Step 6 — Polish**
  - Tweak typography, spacing, and icon tint; verify dark mode.
  - Ensure content adapts on different screen sizes.
  - Acceptance: UI approximates a real X post page; no crashes.

### Acceptance Criteria (Phase 1)
- Drawer opens/closes and lists items.
- Top bar visible; one static Tweet card displayed.
- Material 3 styling; light/dark theme OK; no crashes.
- Code organized under `ui/home`, `ui/components`, `theme`, `model` (if used).

### Deliverables (Phase 1)
- Zipped code for Phase 1 only + short demo video (<= 2 minutes) showing the drawer, top bar, and tweet card.

 

## Milestones & Timeboxing (suggested)
- **Day 1**: Phase 1 UI and drawer complete; record demo.
- **Day 2**: Phase 2 RecyclerView list with clicks; record demo.
- **Day 3–4**: Phase 3 Firebase setup, read and write; optional Auth; record demo.

## Testing & Demo
- **Manual checks**: Rotation, dark mode, small/large font sizes.
- **Devices**: Run on emulator API 29+; note device used in video.
- **Demo video tips**: Show build/run, key interactions, and brief code walkthrough (files only).

## Packaging for Submission
- Create per-phase zip under `/submission/`:
  - `phase1_compose.zip` — code + `phase1_demo.mp4`
  - `phase2_recyclerview.zip` — code + `phase2_demo.mp4`
  - `phase3_firebase.zip` — code + `phase3_demo.mp4`
- Include a short README in each zip with run steps.

## Risks & Mitigations
- **Firebase setup delays**: Prepare dependencies early; verify SHA-1 if using Auth.
- **UI polish time**: Lock scope to required elements; avoid feature creep.
- **Video recording issues**: Use Android Studio emulator recording or OS screen recorder.

## Next Actions
- Implement Phase 1 Compose screen and drawer.
- Create RecyclerActivity, item XML, and adapter for Phase 2.
- Set up Firebase project and wire read/write in Phase 3.
 

## Phase 2 — RecyclerView (Simple List App)
- **Scope**
  - Implement a classic RecyclerView screen that renders a list of 10 static posts.
  - Use XML item layout and ViewBinding.
  - `PostAdapter` + `PostViewHolder`. Optional `ListAdapter` with `DiffUtil`.
  - Click listeners: like/comment/share -> show `Toast` with item position.
  - Provide a dedicated `RecyclerActivity` with toolbar back navigation.
- **Acceptance Criteria**
  - Smooth scrolling list; correct view recycling; no layout jitter.
  - Item visuals approximate the Compose post from Phase 1.
  - Clicks trigger expected Toasts; no crashes.
- **Deliverables**
  - Zipped code for Phase 2 only + short demo video (<= 2 minutes) showing list and clicks.

## Phase 3 — Firebase Realtime Database (Basic Read/Write)
- **Scope**
  - Add Firebase to the project:
    - Create Firebase project, register Android app, download `google-services.json` into [app/](cci:7://file:///home/emmanuel/AndroidStudioProjects/XClone_tutorial/app:0:0-0:0).
    - Add Gradle dependencies and `google-services` plugin.
  - Data model (example):
    ```json
    posts: {
      postId: {
        id: string,
        authorName: string,
        handle: string,
        text: string,
        likeCount: number,
        timestamp: number
      }
    }
    ```
  - Features:
    - Read: Fetch posts from `/posts` and display in RecyclerView.
    - Write: Simple "New Post" form (author, text) that pushes to DB.
    - Optional: Email/Password Auth to gate writing.
  - Rules (dev/testing): permissive read/write for demo; note risks; tighten for final if time.
- **Acceptance Criteria**
  - App reads existing posts on launch and renders them in the list.
  - Creating a post writes to DB and appears in list on success.
  - No hard-coded secrets in code; `google-services.json` excluded from public VCS if needed.
- **Deliverables**
  - Zipped code for Phase 3 only + short demo video (<= 3 minutes) showing read and write.

## Milestones & Timeboxing (suggested)
- **Day 1**: Phase 1 UI and drawer complete; record demo.
- **Day 2**: Phase 2 RecyclerView list with clicks; record demo.
- **Day 3–4**: Phase 3 Firebase setup, read and write; optional Auth; record demo.

## Testing & Demo
- **Manual checks**: Rotation, dark mode, small/large font sizes.
- **Devices**: Run on emulator API 29+; note device used in video.
- **Demo video tips**: Show build/run, key interactions, and brief code walkthrough (files only).

## Packaging for Submission
- Create per-phase zip under `/submission/`:
  - `phase1_compose.zip` — code + `phase1_demo.mp4`
  - `phase2_recyclerview.zip` — code + `phase2_demo.mp4`
  - `phase3_firebase.zip` — code + `phase3_demo.mp4`
- Include a short README in each zip with run steps.

## Risks & Mitigations
- **Firebase setup delays**: Prepare dependencies early; verify SHA-1 if using Auth.
- **UI polish time**: Lock scope to required elements; avoid feature creep.
- **Video recording issues**: Use Android Studio emulator recording or OS screen recorder.

## Next Actions
- Implement Phase 1 Compose screen and drawer.
- Create RecyclerActivity, item XML, and adapter for Phase 2.
- Set up Firebase project and wire read/write in Phase 3.