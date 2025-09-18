-- CONVENTIONAL SUPABASE CHAT SCHEMA - 2025 STANDARD APPROACH
-- Following consultant's report: Simple, clean, leverages native Supabase features
-- Safe deployment: Handles existing tables gracefully

-- Users/Profiles (extends Supabase auth.users) - Use existing if present
CREATE TABLE IF NOT EXISTS profiles (
    id UUID REFERENCES auth.users PRIMARY KEY,
    display_name TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add display_name column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'profiles' AND column_name = 'display_name') THEN
        ALTER TABLE profiles ADD COLUMN display_name TEXT;
    END IF;
END $$;

-- Check existing rooms table structure and adapt
DO $$ 
DECLARE
    rooms_id_type text;
BEGIN
    -- Get the data type of existing rooms.id column
    SELECT data_type INTO rooms_id_type 
    FROM information_schema.columns 
    WHERE table_name = 'rooms' AND column_name = 'id';
    
    -- If rooms table doesn't exist, create with TEXT id (simple approach)
    IF rooms_id_type IS NULL THEN
        CREATE TABLE rooms (
            id TEXT PRIMARY KEY DEFAULT 'general',
            name TEXT NOT NULL DEFAULT 'General Chat',
            soft_cap INTEGER DEFAULT 8,
            hard_cap INTEGER DEFAULT 12,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
        );
        
        -- Insert default general room
        INSERT INTO rooms (id, name) VALUES ('general', 'General Chat');
        
    -- If existing rooms has BIGINT id, use that structure instead
    ELSIF rooms_id_type = 'bigint' THEN
        -- Don't recreate - use existing rooms table structure
        -- Insert a numeric room if general doesn't exist
        INSERT INTO rooms (id, name, soft_cap, hard_cap) 
        SELECT 1, 'General Chat', 8, 12
        WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE name = 'General Chat');
        
    -- If existing rooms has TEXT id, ensure general room exists  
    ELSE
        INSERT INTO rooms (id, name) VALUES ('general', 'General Chat')
        ON CONFLICT (id) DO UPDATE SET 
            name = EXCLUDED.name,
            soft_cap = COALESCE(rooms.soft_cap, 8),
            hard_cap = COALESCE(rooms.hard_cap, 12);
    END IF;
END $$;

-- Messages (core chat functionality) - Adapt to existing rooms structure
DO $$
DECLARE
    rooms_id_type text;
BEGIN
    -- Get the data type of rooms.id column
    SELECT data_type INTO rooms_id_type 
    FROM information_schema.columns 
    WHERE table_name = 'rooms' AND column_name = 'id';
    
    -- Create messages table with appropriate room_id type
    IF rooms_id_type = 'bigint' THEN
        -- Use BIGINT room_id to match existing rooms table
        CREATE TABLE IF NOT EXISTS messages (
            id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
            user_id UUID REFERENCES profiles(id) NOT NULL,
            room_id BIGINT REFERENCES rooms(id) DEFAULT 1,
            content TEXT NOT NULL CHECK (char_length(content) <= 200),
            created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
        );
    ELSE
        -- Use TEXT room_id (our preferred simple approach)
        CREATE TABLE IF NOT EXISTS messages (
            id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
            user_id UUID REFERENCES profiles(id) NOT NULL,
            room_id TEXT REFERENCES rooms(id) DEFAULT 'general',
            content TEXT NOT NULL CHECK (char_length(content) <= 200),
            created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
        );
    END IF;
END $$;

-- Simple index for performance (create if not exists)
CREATE INDEX IF NOT EXISTS messages_room_created_idx ON messages(room_id, created_at DESC);
CREATE INDEX IF NOT EXISTS messages_created_idx ON messages(created_at DESC);

-- Enable RLS (safe to run multiple times)
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;

-- Drop existing policies first to avoid conflicts
DROP POLICY IF EXISTS "Users can view all profiles" ON profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON profiles;
DROP POLICY IF EXISTS "Users can insert own profile" ON profiles;
DROP POLICY IF EXISTS "Users can view all rooms" ON rooms;
DROP POLICY IF EXISTS "Users can view all messages" ON messages;
DROP POLICY IF EXISTS "Users can insert own messages" ON messages;

-- Basic RLS policies
CREATE POLICY "Users can view all profiles" ON profiles FOR SELECT USING (true);
CREATE POLICY "Users can update own profile" ON profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can insert own profile" ON profiles FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can view all rooms" ON rooms FOR SELECT USING (true);
CREATE POLICY "Users can view all messages" ON messages FOR SELECT USING (true);
CREATE POLICY "Users can insert own messages" ON messages FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Note: Room insertion handled above in conditional logic

-- EXTENSION 1: Ephemeral message cleanup (30 minutes)
-- Simple function to delete old messages
CREATE OR REPLACE FUNCTION cleanup_old_messages()
RETURNS void AS $$
BEGIN
    DELETE FROM messages 
    WHERE created_at < NOW() - INTERVAL '30 minutes';
END;
$$ LANGUAGE plpgsql;

-- EXTENSION 2: Dynamic room creation (simple quirky name generator)
CREATE OR REPLACE FUNCTION generate_simple_room_name()
RETURNS TEXT AS $$
DECLARE
    adjectives TEXT[] := ARRAY['Cozy', 'Peaceful', 'Friendly', 'Curious', 'Dreamy', 'Wild'];
    nouns TEXT[] := ARRAY['Chat', 'Corner', 'Lounge', 'Hangout', 'Space', 'Room'];
    adj TEXT;
    noun TEXT;
BEGIN
    adj := adjectives[floor(random() * array_length(adjectives, 1)) + 1];
    noun := nouns[floor(random() * array_length(nouns, 1)) + 1];
    RETURN adj || ' ' || noun;
END;
$$ LANGUAGE plpgsql;