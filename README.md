# Rivvr - Real-time Flow Communication App

A modern Android application for real-time messaging and conversation flows, built with Jetpack Compose and Supabase.

## 🚀 Features

- **Real-time Authentication** - Secure email/password authentication via Supabase
- **Flow Rooms** - Create and participate in conversation flows
- **Live Messaging** - Send and receive ripples (messages) in real-time
- **Material Design 3** - Modern, accessible UI with dark/light theme support
- **Offline-Ready** - Graceful error handling and retry mechanisms

## 🏗️ Architecture

Rivvr follows Clean Architecture principles with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │     Domain      │    │      Data       │
│                 │    │                 │    │                 │
│ • Compose UI    │◄──►│ • Use Cases     │◄──►│ • Repositories  │
│ • ViewModels    │    │ • Models        │    │ • Supabase API  │
│ • Navigation    │    │ • Interfaces    │    │ • Local Cache   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Modules

- **`:app`** - Android application and UI layer
- **`:core:models`** - Shared domain models (Profile, Flow, Ripple)
- **`:data:api`** - Repository interfaces and contracts
- **`:data:impl-supabase`** - Supabase implementation of data layer
- **`:data:impl-stub`** - Local stub implementation for testing

## 🛠️ Tech Stack

- **Language**: Kotlin 2.2.0
- **UI Framework**: Jetpack Compose with Material 3
- **Backend**: Supabase (PostgreSQL, Auth, Realtime)
- **HTTP Client**: Ktor 3.2.2 with OkHttp engine
- **Build System**: Gradle 8.6.0 with Version Catalogs
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

## 📋 Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **Java**: JDK 17 or newer
- **Android SDK**: API level 35
- **Supabase Project**: With database and authentication configured

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone <your-repo-url>
cd Rivvr
```

### 2. Configure Supabase

Create your Supabase project and update `gradle.properties`:

```properties
RIVVR_SUPABASE_URL=https://your-project.supabase.co
RIVVR_SUPABASE_ANON=your-anon-key-here
```

### 3. Database Setup

Run this SQL in your Supabase SQL editor to create the required tables:

```sql
-- Create profiles table
CREATE TABLE public.profiles (
    id UUID REFERENCES auth.users(id) PRIMARY KEY,
    email TEXT NOT NULL,
    display_name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create flows table  
CREATE TABLE public.flows (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    created_by UUID REFERENCES public.profiles(id) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create ripples table
CREATE TABLE public.ripples (
    id BIGSERIAL PRIMARY KEY,
    flow_id BIGINT REFERENCES public.flows(id) ON DELETE CASCADE,
    sender_user_id UUID REFERENCES public.profiles(id) NOT NULL,
    text TEXT,
    media_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.flows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ripples ENABLE ROW LEVEL SECURITY;

-- Create RLS policies (see MASTER_RECORD.md for complete policies)
```

### 4. Build and Run

```bash
# Build the app
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

## 📱 Usage

1. **Sign Up**: Create a new account with email and password
2. **Explore Flows**: View your conversation flows on the main screen
3. **Join Conversations**: Tap on a flow to see ripples (messages)
4. **Send Ripples**: Participate in real-time conversations
5. **Profile**: Manage your account and sign out

## 🔧 Development

### Build Commands

```bash
# Clean build
./gradlew clean

# Run tests
./gradlew test

# Build all variants
./gradlew build

# Install debug version
./gradlew installDebug
```

### Code Style

This project follows [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and uses:

- **ktlint** for code formatting
- **detekt** for static analysis
- **Compose guidelines** for UI development

## 🗄️ Database Schema

### Profiles
- Links to Supabase Auth users
- Stores display name and avatar
- Auto-created on signup

### Flows
- Conversation rooms/channels
- Owned by creating user
- Supports metadata and settings

### Ripples
- Individual messages in flows
- Supports text and media
- Real-time synchronization

## 🔐 Security

- **Row Level Security (RLS)** enabled on all tables
- **Authentication** handled by Supabase Auth
- **Authorization** through database policies
- **API Keys** stored securely in build configuration

## 🚨 Troubleshooting

### Common Issues

**Build Fails with HttpTimeout Error**
- Ensure Ktor version is 3.2.2 to match Supabase 3.2.2

**Network Permission Error**
- Verify INTERNET permission is in AndroidManifest.xml

**Email Verification Issues**
- Check Supabase Auth URL configuration
- Consider disabling email confirmation for development

**Database Connection Issues**
- Verify Supabase URL and keys in gradle.properties
- Check RLS policies allow your operations

## 📖 Documentation

- **MASTER_RECORD.md** - Current project status and technical details
- **Architecture Decision Records** - In `/docs/adr/` (when created)
- **API Documentation** - Generated from code comments

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- **Supabase** for the excellent backend-as-a-service platform
- **JetBrains** for Kotlin and excellent developer tools
- **Google** for Jetpack Compose and Android development platform

---

**Status**: ✅ Production Ready | **Last Updated**: September 4, 2025
