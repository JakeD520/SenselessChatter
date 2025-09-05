# RIVVR SUPABASE SQL SCHEMA

## Current Schema (Legacy)
*Note: This schema is being migrated to the dynamic room system*

-- Add indexes for better query performance
CREATE INDEX idx_flows_created_by ON public.flows(created_by);
CREATE INDEX idx_flows_created_at ON public.flows(created_at DESC);
CREATE INDEX idx_ripples_flow_id ON public.ripples(flow_id);
CREATE INDEX idx_ripples_created_at ON public.ripples(created_at DESC);

-- Create ripples table
CREATE TABLE public.ripples (
id BIGSERIAL PRIMARY KEY,
flow_id BIGINT REFERENCES public.flows(id) ON DELETE CASCADE,
sender_user_id UUID REFERENCES public.profiles(id) NOT NULL,
text TEXT,
media_url TEXT,
created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE public.ripples ENABLE ROW LEVEL SECURITY;

-- Create policies
CREATE POLICY "Users can view ripples in flows they created" ON public.ripples
FOR SELECT USING (
EXISTS (
SELECT 1 FROM public.flows
WHERE flows.id = ripples.flow_id
AND flows.created_by = auth.uid()
)
);

CREATE POLICY "Users can create ripples in flows they created" ON public.ripples
FOR INSERT WITH CHECK (
EXISTS (
SELECT 1 FROM public.flows
WHERE flows.id = ripples.flow_id
AND flows.created_by = auth.uid()
)
);

-- Create flows table
CREATE TABLE public.flows (
id BIGSERIAL PRIMARY KEY,
name TEXT NOT NULL,
created_by UUID REFERENCES public.profiles(id) NOT NULL,
created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE public.flows ENABLE ROW LEVEL SECURITY;

-- Create policies
CREATE POLICY "Users can view flows they created" ON public.flows
FOR SELECT USING (created_by = auth.uid());

CREATE POLICY "Users can create flows" ON public.flows
FOR INSERT WITH CHECK (created_by = auth.uid());

CREATE POLICY "Users can update their own flows" ON public.flows
FOR UPDATE USING (created_by = auth.uid());

-- Create profiles table (extends Supabase auth.users)
CREATE TABLE public.profiles (
id UUID REFERENCES auth.users(id) PRIMARY KEY,
email TEXT NOT NULL,
display_name TEXT,
avatar_url TEXT,
created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS (Row Level Security)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Create policies
CREATE POLICY "Users can view own profile" ON public.profiles
FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON public.profiles
FOR UPDATE USING (auth.uid() = id);

-- Create trigger to auto-create profile when user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO public.profiles (id, email, display_name)
VALUES (NEW.id, NEW.email, NEW.raw_user_meta_data->>'display_name');
RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

---

## 🚀 NEW DYNAMIC ROOM SYSTEM

**Migration Status**: Ready to deploy
**Migration Files**: 
- `sql_migrations/001_dynamic_rooms.sql` - Core room schema and quirky name generator
- `sql_migrations/002_room_rpc_functions.sql` - RPC functions for room navigation

### Key Features
- **Dynamic Room Creation**: Rooms created on-demand with quirky names
- **Conveyor System**: Sequential room navigation (swipe right = next room)
- **Capacity Management**: Soft caps (8 users) trigger new room creation, hard caps (12) prevent overcrowding
- **Quirky Names**: "The Wandering Dreamers", "Cake for Breakfast", "Midnight Nappers", etc.
- **Presence Tracking**: Real-time user count and room occupancy

### RPC Functions
- `rpc_next_room(from_seq)` - Get next available room, create new tail if needed
- `rpc_join_room(room_id)` - Join a specific room with capacity checks
- `rpc_leave_room(room_id)` - Leave current room and update counts
- `rpc_get_user_current_room()` - Get user's current room info

### To Deploy
1. Run both migration files in Supabase SQL Editor
2. Update Android client to use room-based navigation
3. Test auto-room creation on first login

