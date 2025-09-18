-- NUCLEAR RESET: Complete database scorched earth
-- WARNING: This will delete EVERYTHING in the public schema
-- Only run this if you want to start completely from scratch

-- ============================================
-- STEP 1: DROP EVERYTHING NUCLEAR STYLE
-- ============================================

-- Drop all views first
DO $$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT viewname FROM pg_views WHERE schemaname = 'public') LOOP
        EXECUTE 'DROP VIEW IF EXISTS public.' || quote_ident(r.viewname) || ' CASCADE';
    END LOOP;
END $$;

-- Drop all functions/procedures
DO $$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT routine_name, routine_type FROM information_schema.routines WHERE routine_schema = 'public') LOOP
        EXECUTE 'DROP ' || r.routine_type || ' IF EXISTS public.' || quote_ident(r.routine_name) || ' CASCADE';
    END LOOP;
END $$;

-- Drop all tables (this will cascade drop all policies, triggers, indexes)
DO $$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        EXECUTE 'DROP TABLE IF EXISTS public.' || quote_ident(r.tablename) || ' CASCADE';
    END LOOP;
END $$;

-- Drop all sequences
DO $$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public') LOOP
        EXECUTE 'DROP SEQUENCE IF EXISTS public.' || quote_ident(r.sequence_name) || ' CASCADE';
    END LOOP;
END $$;

-- Drop all types
DO $$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT typname FROM pg_type WHERE typnamespace = 'public'::regnamespace AND typtype = 'e') LOOP
        EXECUTE 'DROP TYPE IF EXISTS public.' || quote_ident(r.typname) || ' CASCADE';
    END LOOP;
END $$;

-- ============================================
-- STEP 2: CREATE CLEAN CONVENTIONAL SCHEMA
-- ============================================

-- Profiles table (simple, follows auth.users structure)
CREATE TABLE public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    display_name TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Simple rooms table with TEXT IDs
CREATE TABLE public.rooms (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Simple messages table
CREATE TABLE public.messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    room_id TEXT REFERENCES public.rooms(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE NOT NULL,
    content TEXT NOT NULL CHECK (length(trim(content)) > 0 AND length(content) <= 500),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Essential indexes
CREATE INDEX idx_messages_room_time ON public.messages (room_id, created_at DESC);
CREATE INDEX idx_messages_user ON public.messages (user_id);

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Simple RLS policies
CREATE POLICY "Profiles are readable by authenticated users" 
    ON public.profiles FOR SELECT 
    USING (auth.role() = 'authenticated');

CREATE POLICY "Users can update own profile" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid() = id);

CREATE POLICY "Rooms are readable by authenticated users" 
    ON public.rooms FOR SELECT 
    USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can create rooms" 
    ON public.rooms FOR INSERT 
    WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Messages are readable by authenticated users" 
    ON public.messages FOR SELECT 
    USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can send messages" 
    ON public.messages FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

-- Create sample room
INSERT INTO public.rooms (id, name) VALUES ('general', 'General Chat');

-- Enable realtime
ALTER publication supabase_realtime ADD TABLE public.messages;
ALTER publication supabase_realtime ADD TABLE public.rooms;
ALTER publication supabase_realtime ADD TABLE public.profiles;

-- ============================================
-- VERIFICATION
-- ============================================

-- Show what we created
SELECT 'Tables created:' as status;
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

SELECT 'Functions remaining:' as status;
SELECT routine_name FROM information_schema.routines WHERE routine_schema = 'public';

SELECT 'Sample data:' as status;
SELECT * FROM public.rooms;