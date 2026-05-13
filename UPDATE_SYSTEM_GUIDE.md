# Update System & About Screen Implementation Guide

## Overview
This comprehensive update system provides:
- ✅ **Soft Updates**: Optional updates displayed as a banner at the top
- ✅ **Force Updates**: Mandatory updates that happen automatically without user permission
- ✅ **About Screen**: Shows app version, last update date, app description, and manual update option
- ✅ **Firebase Remote Config Integration**: Instant remote control of update settings

---

## Architecture

### 1. **Data Models** (`domain/models/UpdateInfo.kt`)
```kotlin
data class UpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val updateType: UpdateType,  // NONE, SOFT, FORCE
    val updateUrl: String,
    val changeLog: String,
    val lastUpdateDate: Long,
    val forceMinVersion: String?
)

enum class UpdateType {
    NONE,       // No update
    SOFT,       // Show banner with option to dismiss
    FORCE       // Auto-update silently
}
```

### 2. **Repository** (`domain/repository/UpdateRepository.kt`)
Handles all update-related logic:
- Fetching update info from Firebase
- Comparing versions
- Downloading/installing updates
- Storing update state

### 3. **Implementation** (`data/repository/UpdateRepositoryImpl.kt`)
- Uses Firebase Remote Config
- Implements semantic versioning comparison
- Handles SharedPreferences for tracking viewed updates

### 4. **ViewModel** (`presentation/update/UpdateViewModel.kt`)
- Manages update state (soft available, force available, etc.)
- Handles user interactions (accept/dismiss updates)
- Auto-starts force updates on app launch

---

## UI Components

### 1. **SoftUpdateBanner** (`ui/update/SoftUpdateBanner.kt`)
**Where it appears**: Top of MainScreen

```kotlin
@Composable
fun SoftUpdateBanner(
    updateInfo: UpdateInfo,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit,
    isUpdating: Boolean = false
)
```

**Features**:
- Slides in from top
- Shows version and changelog
- "Update" and "Later" buttons
- Loading indicator while updating

### 2. **AboutScreen** (`ui/about/AboutScreen.kt`)
**Navigation**: Menu item in left drawer

**Components**:
1. **Header**: Back button + "About" title
2. **App Info Card**: Current version + last update date
3. **About Text Section**: Full Jamey Aebersold quote explaining app purpose
4. **Update Button**: Manual update option if available

```kotlin
@Composable
fun AboutScreen(
    onBackPressed: () -> Unit,
    updateViewModel: UpdateViewModel
)
```

### 3. **Updated LeftDrawer** (`ui/leftdrawer/LeftDrawer.kt`)
Added "About" menu item:
```kotlin
// In buildMenuItems():
items.add(MenuItem("About", Icons.Default.Info, onAboutClick))
```

---

## Firebase Remote Config Setup

### Required Keys
Configure these in Firebase Console:

```json
{
  "app_latest_version": "1.1",
  "app_update_url": "https://play.google.com/store/apps/details?id=com.example.jazzlibraryktroomjpcompose",
  "app_changelog": "Bug fixes and performance improvements",
  "app_force_min_version": "1.0",  // If current < this, force update
  "app_last_update_timestamp": 1715500000000
}
```

### How It Works
- **app_latest_version**: Latest app version available
- **app_force_min_version**: If set, versions below this MUST update immediately
- **app_update_url**: Where to download (usually Play Store link)
- **app_changelog**: What's new description for banner
- **app_last_update_timestamp**: Unix timestamp of last release

---

## Integration Steps

### Step 1: Update MainActivity.kt
```kotlin
import com.example.jazzlibraryktroomjpcompose.ui.main.RootScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JazzLibraryKTRoomJPComposeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScreen()  // Changed from MainScreen()
                }
            }
        }
    }
}
```

### Step 2: Update MainScreen.kt
```kotlin
@Composable
fun MainScreen(onAboutClick: () -> Unit = {}) {
    // ... existing code ...
    
    // In LeftDrawer callback:
    LeftDrawer(
        isOpen = leftDrawerState == DrawerState.OPEN,
        onClose = { viewModel.closeLeftDrawer() },
        onRefreshClick = { /* existing */ },
        onClearHistoryClick = { /* existing */ },
        onLoginClick = { /* existing */ },
        onAboutClick = onAboutClick  // Pass through
    )
}
```

### Step 3: Add SoftUpdateBanner to MainScreen
```kotlin
@Composable
fun MainScreen(onAboutClick: () -> Unit = {}) {
    val updateViewModel: UpdateViewModel = hiltViewModel()
    val updateState by updateViewModel.updateState.collectAsState()
    val isUpdating by updateViewModel.isUpdating.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Show banner if soft update available
        if (updateState is UpdateState.SoftUpdateAvailable) {
            SoftUpdateBanner(
                updateInfo = (updateState as UpdateState.SoftUpdateAvailable).info,
                onUpdateClick = {
                    updateViewModel.performUpdate((updateState as UpdateState.SoftUpdateAvailable).info)
                },
                onDismiss = {
                    updateViewModel.dismissSoftUpdate()
                },
                isUpdating = isUpdating
            )
        }
        
        // Rest of MainScreen content...
    }
}
```

---

## Update Flow Diagrams

### Soft Update Flow
```
App Launch
    ↓
UpdateViewModel.checkForUpdates()
    ↓
Firebase Remote Config Fetch
    ↓
Compare Versions
    ↓
Type = SOFT?
    ├─ YES → Show SoftUpdateBanner
    │        ├─ User clicks "Update" → Open Play Store
    │        └─ User clicks "Later" → Dismiss (don't show again)
    └─ NO → Continue
```

### Force Update Flow
```
App Launch
    ↓
UpdateViewModel.checkForUpdates()
    ↓
Firebase Remote Config Fetch
    ↓
Check: currentVersion < forceMinVersion?
    ├─ YES → UpdateType.FORCE
    │        ↓
    │        Auto-call performUpdate()
    │        ↓
    │        Silent redirect to Play Store
    │        (No banner, no dialog, just opens)
    └─ NO → Continue normally
```

### About Screen Flow
```
User clicks "About" in left drawer
    ↓
Navigate to AboutScreen
    ↓
Show:
  - Current version (from package info)
  - Last update date (from Firebase)
  - Jamey Aebersold quote
  - "Install Update" button (if update available)
    ↓
User clicks back → Return to MainScreen
```

---

## Testing

### Test Case 1: Soft Update
1. Set in Firebase: `app_latest_version` = higher than current
2. Don't set `app_force_min_version`
3. Expected: Banner appears at top
4. Action: Click "Update" → Opens Play Store
5. Action: Click "Later" → Banner disappears

### Test Case 2: Force Update
1. Set in Firebase: `app_force_min_version` = higher than current
2. Expected: No banner, Play Store opens automatically on app launch
3. Note: User cannot dismiss this

### Test Case 3: About Screen
1. Open left drawer
2. Click "About"
3. Verify:
   - Version number displays
   - Update date displays
   - Full quote is visible (scrollable)
   - Back button works
   - "Install Update" button appears if update available

### Test Case 4: No Update Available
1. Set in Firebase: `app_latest_version` = same as current
2. Expected: No banner, no update button
3. About screen shows version normally

---

## Key Features

| Feature | Behavior |
|---------|----------|
| **Soft Update Banner** | Appears at top, dismissible, shows what's new |
| **Force Update** | No UI interference, silently opens Play Store |
| **Version Tracking** | Current version from system, update date from Firebase |
| **About Screen** | Full app history/purpose, accessible via drawer |
| **Manual Update** | Users can manually update from About screen if behind |
| **Real-time Control** | Update settings controlled via Firebase (no app rebuild needed) |

---

## File Structure Created

```
app/src/main/java/com/example/jazzlibraryktroomjpcompose/
├── domain/
│   ├── models/
│   │   └── UpdateInfo.kt                    (Data models)
│   └── repository/
│       └── UpdateRepository.kt              (Interface)
├── data/
│   └── repository/
│       └── UpdateRepositoryImpl.kt           (Firebase implementation)
├── presentation/
│   └── update/
│       └── UpdateViewModel.kt               (Update state management)
├── ui/
│   ├── update/
│   │   └── SoftUpdateBanner.kt              (Banner UI)
│   ├── about/
│   │   └── AboutScreen.kt                   (About screen)
│   ├── leftdrawer/
│   │   └── LeftDrawer.kt                    (UPDATED: Added About item)
│   └── main/
│       ├── RootScreen.kt                    (NEW: Navigation root)
│       └── ScreenNavigationViewModel.kt     (NEW: Screen navigation)
└── di/
    └── UpdateModule.kt                      (Hilt dependency injection)
```

---

## Summary

This implementation provides a **production-ready update system** with:
- ✅ Instant remote updates via Firebase
- ✅ Seamless soft updates (user chooses)
- ✅ Silent force updates (admin control)
- ✅ Beautiful About screen with app history
- ✅ Version tracking and update information
- ✅ Back handler support for About screen
- ✅ Full integration with existing architecture

Ready for deployment! 🚀
