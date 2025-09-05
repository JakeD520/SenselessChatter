DO NOT USE MOCK DATA METHODS.  THIS IS FOR PRODUCTION>

# Anti-Parasocial Chat App - Development Concept Package

## 🎯 Core Mission Statement
**Create authentic, ephemeral conversations that replicate classic AOL chatroom vibes while eliminating modern social media's parasocial performance layer.**

---

## 🚫 What We Are NOT Building
- **NO** user profiles or "about me" sections
- **NO** follower/following systems
- **NO** like/reaction buttons on individual messages
- **NO** message history persistence beyond ephemeral limits
- **NO** user verification badges or status symbols
- **NO** algorithmic content feeds or recommendations
- **NO** permanent usernames or identity building
- **NO** public user directories or search

---

## ✅ What We ARE Building

### Core Philosophy
- **Ephemeral First**: All conversations fade naturally
- **Anonymous Flexibility**: Users control their identity moment-to-moment
- **Discovery Over Algorithm**: Serendipitous exploration, not targeted content
- **Collective Experience**: Shared moments over individual performance
- **Simplicity**: Minimal friction, maximum authentic interaction

---

## 🔧 Technical Feature Requirements

### User Identity System
```
- Simple account creation (email/password only)
- Dynamic alias changing capability
- No persistent user profiles
- No avatar uploads or customization
- Alias can be changed per session/room visit
```

### Chat Room Architecture
```
- Platform-curated rooms with creative names
- Dynamic room population (users come and go)
- No room search functionality
- Swipe-based room discovery interface
- Rooms persist, but conversations within them expire
```

### Message System Specifications
```
- 200 character maximum per message
- Anti-spam cooldown timer between posts
- Messages expire after: 50 total messages OR 30 minutes (whichever first)
- No message editing or deletion by users
- No message threading or replies
- Text-only messages (no image/video sharing)
```

### Emote Burst System
```
- Room-wide emotional expressions
- NOT attached to individual messages
- Triggered by users but affects entire chat room
- Visual burst effect across the room interface
- Examples: 🎉 celebration, 😂 laughter, 💀 shock, etc.
```

### Privacy Controls
```
- Individual user muting (client-side filtering)
- No blocking (ephemeral nature reduces need)
- No reporting system (messages expire quickly)
- Private DM capability between users
```

### Private Communication
```
- Direct messages between users
- Invite-only private group chats
- Same ephemeral rules apply (50 messages OR 30 minutes)
- Private chats discoverable via swipe navigation
```

---

## 🎨 User Interface Specifications

### Main Chat Interface
- Clean, minimal design inspired by classic IRC/AOL
- Focus on text conversation
- Alias displayed clearly but not prominently
- Message timestamps but no "read" indicators
- Emote burst overlay effects

### Navigation System
- **Primary**: Main chat room view
- **Swipe Right**: Discover other chat rooms
- **Swipe Left**: Access private DMs and group chats
- **No tabs** or complex navigation menus
- **No search bars** or room directories

### Onboarding Flow
1. Create account (email/password)
2. Choose initial alias
3. Immediately enter a random chat room
4. Brief overlay tutorial on key features
5. No extended setup or profile creation

---

## 🔄 Core User Journey

### Typical Session Flow
1. **Open App** → Land in a random active chat room
2. **Read Context** → See last ~20 messages for room vibe
3. **Choose Alias** → Set or change current display name
4. **Participate** → Chat with 200-char limit, cooldown between posts
5. **React Collectively** → Trigger emote bursts for room-wide expression
6. **Discover** → Swipe to find other rooms organically
7. **Connect Privately** → DM interesting users if desired
8. **Leave Naturally** → Close app, conversations continue without you

---

## 📱 Android Compose Implementation Notes

### State Management Priorities
- Real-time message synchronization via Supabase
- Ephemeral message cleanup (automated deletion)
- Room population tracking (who's currently active)
- Anti-spam cooldown state per user
- Emote burst coordination across clients

### Supabase Schema Considerations
```sql
-- Messages table with TTL
messages (
  id, 
  room_id, 
  user_alias, 
  content, 
  created_at,
  expires_at  -- Auto-cleanup trigger
)

-- Active rooms (persistent)
chat_rooms (
  id,
  name,
  created_at,
  is_active
)

-- Temporary user presence
user_presence (
  user_id,
  room_id, 
  alias,
  last_seen,
  -- Auto-expire after inactivity
)
```

---

## 🎯 Success Metrics (Internal)
- **Time spent in conversations** (not scrolling)
- **Number of room discoveries** per session
- **Emote burst participation** rate
- **Return visit frequency** (daily active users)
- **Private conversation initiation** rate

**NOT tracking**: Followers, likes, shares, or any vanity metrics

---

## 🚨 Development Guardrails

### Always Remember
- **Ephemeral First**: Nothing should persist beyond stated limits
- **No Identity Building**: Resist features that create persistent personas
- **Anti-Performance**: Avoid anything that encourages "content creation"
- **Discovery Focus**: Random exploration, not algorithmic suggestions
- **Collective Over Individual**: Shared experiences trump personal branding

### Red Flags to Avoid
- User profiles creeping into the design
- Message persistence beyond ephemeral limits
- Any form of user ranking or reputation system
- Features that encourage "influencer" behavior
- Complex navigation that breaks the simple swipe model

---

## 💡 Future Considerations (Phase 2+)
- Voice chat rooms with same ephemeral principles
- Location-based regional chat rooms
- Themed temporary events or special rooms
- Desktop web version maintaining same UX principles
- Realtime MULTILANGUAGE interpretation of messages
---

**Remember: We're building the anti-Instagram, the anti-Twitter. This is about rediscovering genuine human connection without the performance anxiety of modern social media.**