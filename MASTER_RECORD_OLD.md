# RIVVR PROJECT - MASTER RECORD
*Last Updated: September 5, 2025*

## 📋 P### **✅ Bottom Tabs Imp#### **Tab 3: 💬 Private Chat** 
- **Status**: ✅ Screen created with feature preview
- **Purpose**: Direct messages with users discovered in main rooms
- **Features**: Same expiry policy, 200-char limit, no conversation historyntation** (4-Tab System - Left to Right)
1. **🔒 Private Room** - Secure end-to-end encrypted conversations
2. **🏠 Main Room** - Primary swipe-based chat discovery (START DESTINATION)
3. **💬 Private Chat** - Direct messages with discovered users  
4. **⚙️ Dashboard** - Session stats, privacy settings, account actionsT STATUS: **FOUNDATION COMPLETE - FEATURE DEVELOPMENT PHASE**

### 🎯 **CURRENT STATE**
- ✅ **Build System**: Working with Java 17, Gradle 8.6.0, Kotlin 2.2.0
- ✅ **Dependencies**: Supabase 3.2.2 + Ktor 3.2.2 (HttpTimeout issue RESOLVED)
- ✅ **Network Permissions**: INTERNET + ACCESS_NETWORK_STATE configured
- ✅ **Authentication**: Real Supabase auth (NO MOCK DATA as per requirement)
- ✅ **Database**: Basic SQL schema implemented and working
- ✅ **App Installation**: Successfully builds and installs on Android
- 🔄 **Email Verification**: Needs Supabase dashboard configuration (localhost:3000 redirect issue)

---

## � **FEATURE IMPLEMENTATION STATUS** (Based on MUST_READ.md)

### ✅ **COMPLETED FEATURES**

#### User Identity System
- ✅ **Simple account creation** (email/password only) - AuthScreen.kt
- ❌ **Dynamic alias changing** - NOT IMPLEMENTED YET
- ✅ **No persistent user profiles** - Only basic profile data stored
- ✅ **No avatar uploads** - No avatar functionality implemented
- ❌ **Alias per session/room** - NOT IMPLEMENTED YET

#### Authentication Flow
- ✅ **Email/password signup/signin** - Real Supabase integration
- ✅ **No extended profile setup** - Direct to app after auth
- ❌ **Immediate random room entry** - Currently shows flow list instead

### 🔄 **IN PROGRESS / PARTIAL FEATURES**

#### Chat Room Architecture  
- 🔄 **Platform-curated rooms** - Basic flow structure exists, needs creative naming
- ❌ **Dynamic room population** - No presence tracking yet
- ✅ **No room search** - Currently no search implemented
- ❌ **Swipe-based discovery** - Using bottom tabs instead (NEEDS REWORK)
- 🔄 **Persistent rooms, ephemeral messages** - Rooms persist, message expiry NOT implemented

#### Database Schema
- 🔄 **Basic structure** - flows/ripples tables exist
- ❌ **Message TTL/Expiration** - No auto-cleanup implemented
- ❌ **User presence tracking** - No real-time presence system
- ❌ **Ephemeral limits** - No 50 message/30 minute cleanup

### ❌ **NOT YET IMPLEMENTED**

#### Core Missing Features
- ❌ **200 character message limit** - No limit implemented
- ❌ **Anti-spam cooldown** - No rate limiting
- ❌ **Message expiration** (50 msgs OR 30 min)
- ❌ **Emote burst system** - Major feature missing
- ❌ **Swipe navigation** - Using tabs instead (CONFLICTS with spec)
- ❌ **Real-time messaging** - Basic data loading only
- ❌ **Private DMs** - No private messaging
- ❌ **Room discovery** - No random room assignment
- ❌ **Alias changing** - Static display names

#### UI/UX Implementation Status
- ✅ **4-tab navigation system** - Private Room | Main Room | Private Message | Dashboard
- ✅ **Mobile-optimized strategy** - Swipe for main chat discovery, tabs for meta functions  
- ✅ **Material 3 NavigationBar** - Proper icons and navigation state management
- ✅ **MainRoom as start destination** - Primary chat discovery area
- 🔄 **Swipe gesture implementation** - Tab navigation working, swipe discovery pending
- ❌ **Direct room entry after auth** - Still shows tab interface first

---

## 🎨 **REFINED NAVIGATION STRATEGY** (Mobile-Optimized)

### **Primary Interface: Swipe-Based Main Chat**
- **Swipe Left/Right**: Discover different main chat rooms (ephemeral, public)
- **Main View**: Full-screen chat with minimal UI
- **Direct Entry**: Land in random room immediately after auth

### **✅ Bottom Tabs Implementation** (4-Tab System - Left to Right)
1. **� Private Room** - Secure end-to-end encrypted conversations
2. **🏠 Main Room** - Primary swipe-based chat discovery (START DESTINATION)
3. **� Private Message** - Direct messages with discovered users  
4. **⚙️ Dashboard** - Session stats, privacy settings, account actions

### **Implemented Tab Functionality**

#### **Tab 1: 🔒 Private Room**
- **Status**: ✅ Screen created with feature preview
- **Purpose**: End-to-end encrypted intimate conversations
- **Features**: Auto-expiry, no history, anti-parasocial design

#### **Tab 2: 🏠 Main Room** (PRIMARY)
- **Status**: ✅ Screen created, set as start destination
- **Purpose**: Primary ephemeral chat experience with swipe discovery
- **Features**: Anti-parasocial messaging preview, sign-out capability
- **Next**: Implement swipe gestures for room discovery

#### **Tab 3: � Private Message** 
- **Status**: ✅ Screen created with feature preview
- **Purpose**: Direct messages with users discovered in main rooms
- **Features**: Same expiry policy, 200-char limit, no conversation history

#### **Tab 4: ⚙️ Dashboard**
- **Status**: ✅ Screen created with session stats and controls
- **Features**: Session stats (0/50 messages), privacy settings, sign-out
- **Room Info**: Current room name, approximate user count

#### **Tab 3: 🔒 Private**
- **Private Group Chats**: Invite-only rooms (same ephemeral rules)
- **Create Private Room**: Generate invite links
- **Active Private Rooms**: Switch between private group conversations
- **Same Rules**: 50 message OR 30 minute expiry applies

#### **Tab 4: 📱 DMs**
- **Direct Messages**: 1-on-1 private conversations
- **User Discovery**: From main chat interactions
- **Ephemeral**: Same expiry rules as main chat
- **No Persistence**: No long-term conversation history

---

## 🏗️ **UPDATED ARCHITECTURE PLAN**

```
app/ui/
├── MainActivity.kt                    ✅ Entry point  
├── RivvrApp.kt                       🔄 Needs tab system update
└── screens/
    ├── AuthScreen.kt                 ✅ Email/password (keep)
    ├── PrivateRoomScreen.kt          ❌ NEW - Tab 1: Private group chats
    ├── MainRoomScreen.kt             🔄 NEW - Tab 2: Swipe-based public rooms + chat
    ├── PrivateMessageScreen.kt       ❌ NEW - Tab 3: 1-on-1 DM conversations  
    └── DashboardScreen.kt            ❌ NEW - Tab 4: Alias + occupants + mute controls
```

### **Remove These Screens**
- ❌ **FlowsScreen.kt** - Replace with direct MainRoomScreen entry
- ❌ **FeedScreen.kt** - Not in refined spec
- ❌ **ProfileScreen.kt** - Replace with DashboardScreen

---

## 🎯 **REFINED NAVIGATION STRATEGY** (Mobile-Optimized)

### **Primary Interface: Swipe-Based Main Chat**
- **Swipe Left/Right**: Discover different main chat rooms (ephemeral, public)
- **Main View**: Full-screen chat with minimal UI
- **Direct Entry**: Land in random room immediately after auth

### **Bottom Tabs for Meta Functions** (4-Tab System - Left to Right)
1. **� Private Room** - Private group chats (invite-only, ephemeral rules)
2. **💬 Main Room** - Current public room with swipe discovery  
3. **� Private Message** - Direct 1-on-1 conversations
4. **⚙️ Dashboard** - Alias changing + room occupants + mute controls

### **Tab Functionality Breakdown**

#### **Tab 1: 🔒 Private Room**
- **Purpose**: Invite-only group conversations
- **Features**: Create/join private rooms, same 50 msg/30 min expiry
- **UI**: List of active private rooms, tap to enter
- **Room Management**: Generate invite links, leave rooms

#### **Tab 2: 💬 Main Room** (Primary Focus)
- **Purpose**: Primary ephemeral public chat experience
- **Navigation**: Horizontal swipe to discover other public rooms
- **Features**: 200-char limit, auto-expiry, emote bursts
- **UI**: Minimal, full-screen chat interface
- **Default**: This is where users land after auth

#### **Tab 3: 📱 Private Message**
- **Purpose**: Direct 1-on-1 private conversations
- **User Discovery**: From main room or private room interactions
- **Features**: Same ephemeral rules (50 msg/30 min expiry)
- **UI**: List of active DM conversations

#### **Tab 4: ⚙️ Dashboard**
- **Current Alias**: Large, prominent, tap to change (per-room basis)
- **Room Occupants**: Live list of users in current room (main or private)
- **Mute Controls**: Toggle mute/unmute for specific users
- **Room Info**: Current room name, user count, room type
- **Settings**: Basic app preferences

#### **Tab 2: 👥 Status** 
- **Current Alias**: Tap to change (per-room basis)
- **Room Occupants**: Live list of who's in current main room
- **Mute Controls**: Client-side filtering of specific users
- **Room Info**: Current room name, approximate user count

#### **Tab 3: 🔒 Private**
- **Private Group Chats**: Invite-only rooms (same ephemeral rules)
- **Create Private Room**: Generate invite links
- **Active Private Rooms**: Switch between private group conversations
- **Same Rules**: 50 message OR 30 minute expiry applies

#### **Tab 4: 📱 DMs**
- **Direct Messages**: 1-on-1 private conversations
- **User Discovery**: From main chat interactions
- **Ephemeral**: Same expiry rules as main chat
- **No Persistence**: No long-term conversation history

---

## 🔄 **IMPLEMENTATION PRIORITY UPDATE**

The navigation clarification significantly improves our implementation path:

### ✅ **What This Solves:**
- **Mobile-first design** that respects screen real estate
- **Maintains anti-parasocial core** while adding practical functionality
- **Evolutionary approach** from current tab system
- **Clear separation** between ephemeral main chat and meta functions

### 🎯 **Updated Phase 1 Priorities:**
1. **Transform current tabs** into the 4-tab system described above
2. **MainChatScreen** with swipe discovery replaces FlowsScreen
3. **StatusScreen** with alias changing replaces ProfileScreen
4. **Direct room entry** on app launch

---

## 🏗️ **CURRENT ARCHITECTURE** (Needs Alignment)

```
app/
├── MainActivity.kt                    ✅ Entry point
├── ui/
│   ├── RivvrApp.kt                   🔄 Has auth but wrong navigation pattern
│   └── screens/
│       ├── AuthScreen.kt             ✅ Email/password auth working
│       ├── FlowsScreen.kt            🔄 Shows flow list (should be direct room entry)
│       ├── FeedScreen.kt             ❌ Not in spec - should be room discovery
│       └── ProfileScreen.kt          ❌ Conflicts with "no profiles" requirement
```

---

## 🗄️ **DATABASE SCHEMA STATUS**

### ✅ **Implemented Tables**
```sql
-- Basic structure exists
profiles (id, email, display_name, avatar_url, created_at)
flows (id, name, created_by, created_at) 
ripples (id, flow_id, sender_user_id, text, media_url, created_at)
```

### ❌ **MISSING - Required for Ephemeral System**
```sql
-- Need to add these fields/tables
ALTER TABLE ripples ADD COLUMN expires_at TIMESTAMPTZ;
CREATE TABLE user_presence (user_id, room_id, alias, last_seen);
CREATE TABLE emote_bursts (room_id, emote_type, triggered_by, created_at);

-- Auto-cleanup triggers for 50 message limit
-- Auto-cleanup triggers for 30 minute expiry
-- Presence tracking triggers
```

---

## � **CRITICAL MISALIGNMENTS WITH SPEC**

### 1. **Navigation Pattern**
- **Current**: Bottom tabs (Flows, Feed, Profile)
- **Required**: Swipe-only navigation (rooms left/right, DMs up/down)
- **Impact**: Fundamental UX change needed

### 2. **User Experience Flow**
- **Current**: Auth → Flow list → Select flow
- **Required**: Auth → Immediate random room entry
- **Impact**: Complete onboarding rework needed

### 3. **Profile System**
- **Current**: Has ProfileScreen with persistent data
- **Required**: No profiles, dynamic aliases only
- **Impact**: Remove ProfileScreen, add alias changing

### 4. **Message System**
- **Current**: Persistent messages with no limits
- **Required**: 200 char limit, ephemeral expiry
- **Impact**: Message UI rework + database cleanup system

---

## 🎯 **PRIORITY IMPLEMENTATION ROADMAP**

### **Phase 1: Core Compliance (CRITICAL)**
1. **Remove bottom tabs** → Implement swipe navigation
2. **Remove ProfileScreen** → Add in-room alias changing  
3. **Implement message limits** → 200 characters
4. **Add message expiration** → 50 count OR 30 minute cleanup
5. **Direct room entry** → Skip flow list, land in random room

### **Phase 2: Essential Features**
1. **Anti-spam cooldown** → Rate limiting between messages
2. **Real-time messaging** → Live updates via Supabase realtime
3. **Emote burst system** → Room-wide emotional expressions
4. **Room discovery** → Swipe to explore other rooms
5. **User presence** → Track who's currently in rooms

### **Phase 3: Advanced Features**
1. **Private DMs** → Direct messaging system
2. **Room curation** → Creative room names and themes
3. **Auto-room assignment** → Random room selection on login

---

## � **TECHNICAL DEBT TO ADDRESS**

### Database Schema Updates Needed
```sql
-- Message expiration system
ALTER TABLE ripples ADD COLUMN expires_at TIMESTAMPTZ;
CREATE INDEX idx_ripples_expires_at ON ripples(expires_at);

-- User presence tracking  
CREATE TABLE user_presence (
    user_id UUID REFERENCES profiles(id),
    room_id BIGINT REFERENCES flows(id),
    current_alias TEXT NOT NULL,
    last_seen TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (user_id, room_id)
);

-- Emote burst system
CREATE TABLE emote_bursts (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT REFERENCES flows(id),
    emote_type TEXT NOT NULL,
    triggered_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### UI/UX Rework Required
- Replace NavigationBar with swipe gestures
- Remove ProfileScreen entirely
- Redesign FlowsScreen as direct room view
- Add alias changing overlay in chat rooms
- Implement character counter for messages

---

## 📱 **BUILD COMMANDS** (Unchanged)
```bash
.\gradlew clean assembleDebug installDebug
```

---

## 🎯 **NEXT SESSION PRIORITIES**
1. **Database schema updates** for ephemeral features
2. **Remove bottom navigation** → Implement swipe pattern
3. **Add message character limits** and display
4. **Implement basic real-time messaging**
5. **Create random room assignment on login**

### 📝 **CRITICAL REALIZATION**
Our current implementation is a **basic chat app foundation** but doesn't align with the **anti-parasocial, ephemeral vision** in MUST_READ.md. We need significant UX changes to match the AOL chatroom vibe and ephemeral-first philosophy.

---
*This record should be updated at the end of each development session*
