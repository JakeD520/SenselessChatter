-- DYNAMIC ROOM CREATION MIGRATION
-- Transform existing flows table into rooms table for conveyor system

-- First, let's create the new rooms table structure
CREATE TABLE public.rooms (
    id BIGSERIAL PRIMARY KEY,
    seq INTEGER NOT NULL UNIQUE, -- Sequential room number for ordering
    name TEXT NOT NULL, -- Quirky room names like "The Neighborhood Wanderers"
    status TEXT DEFAULT 'active', -- 'active', 'archived', 'full'
    soft_cap INTEGER DEFAULT 8, -- Preferred max users (triggers new room creation)
    hard_cap INTEGER DEFAULT 12, -- Absolute max users
    live_user_count INTEGER DEFAULT 0, -- Current active users
    message_count INTEGER DEFAULT 0, -- Messages in this room
    created_at TIMESTAMPTZ DEFAULT NOW(),
    last_activity TIMESTAMPTZ DEFAULT NOW()
);

-- Create sequence for room numbering
CREATE SEQUENCE IF NOT EXISTS room_seq_number START 1;

-- Enable RLS
ALTER TABLE public.rooms ENABLE ROW LEVEL SECURITY;

-- Create policies - all authenticated users can view and join rooms
CREATE POLICY "Authenticated users can view all active rooms" ON public.rooms
FOR SELECT USING (auth.role() = 'authenticated' AND status = 'active');

-- Create room_presence table for tracking who's in what room
CREATE TABLE public.room_presence (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT REFERENCES public.rooms(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) NOT NULL,
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    last_seen TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(room_id, user_id)
);

-- Enable RLS
ALTER TABLE public.room_presence ENABLE ROW LEVEL SECURITY;

-- Policies for presence
CREATE POLICY "Users can view presence in active rooms" ON public.room_presence
FOR SELECT USING (auth.role() = 'authenticated');

CREATE POLICY "Users can manage their own presence" ON public.room_presence
FOR ALL USING (user_id = auth.uid());

-- Rename ripples.flow_id to ripples.room_id and update references
ALTER TABLE public.ripples RENAME COLUMN flow_id TO room_id;

-- Update ripples policies to work with rooms
DROP POLICY "Users can view ripples in flows they created" ON public.ripples;
DROP POLICY "Users can create ripples in flows they created" ON public.ripples;

CREATE POLICY "Users can view ripples in active rooms" ON public.ripples
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.rooms
        WHERE rooms.id = ripples.room_id
        AND rooms.status = 'active'
    )
);

CREATE POLICY "Users can create ripples in rooms they're present in" ON public.ripples
FOR INSERT WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.room_presence
        WHERE room_presence.room_id = ripples.room_id
        AND room_presence.user_id = auth.uid()
    )
);

-- Update indexes
DROP INDEX IF EXISTS idx_ripples_flow_id;
CREATE INDEX idx_ripples_room_id ON public.ripples(room_id);
CREATE INDEX idx_rooms_seq ON public.rooms(seq);
CREATE INDEX idx_rooms_status ON public.rooms(status);
CREATE INDEX idx_room_presence_user ON public.room_presence(user_id);
CREATE INDEX idx_room_presence_room ON public.room_presence(room_id);

-- Quirky room name generator arrays
CREATE OR REPLACE FUNCTION generate_quirky_room_name()
RETURNS TEXT AS $$
DECLARE
    adjectives TEXT[] := ARRAY[
        'Wandering', 'Sleepy', 'Curious', 'Midnight', 'Daytime', 'Cozy', 'Restless',
        'Dreamy', 'Caffeinated', 'Spontaneous', 'Whimsical', 'Lazy', 'Energetic',
        'Peaceful', 'Chaotic', 'Gentle', 'Wild', 'Quiet', 'Loud', 'Mysterious',
        'Friendly', 'Shy', 'Bold', 'Calm', 'Excited', 'Thoughtful', 'Playful'
    ];
    
    nouns TEXT[] := ARRAY[
        'Nappers', 'Wanderers', 'Dreamers', 'Thinkers', 'Chatters', 'Observers',
        'Explorers', 'Creators', 'Listeners', 'Storytellers', 'Philosophers', 
        'Night Owls', 'Early Birds', 'Coffee Drinkers', 'Tea Sippers', 'Book Readers',
        'Music Lovers', 'Art Makers', 'Game Players', 'Food Enthusiasts', 'Plant Parents',
        'Dog Walkers', 'Cat Whisperers', 'Rain Watchers', 'Star Gazers', 'Cloud Spotters'
    ];
    
    phrases TEXT[] := ARRAY[
        'Cake for Breakfast', 'Pizza at Midnight', 'Socks and Sandals', 'Dancing in Rain',
        'Singing in Showers', 'Talking to Plants', 'Collecting Sunsets', 'Chasing Rainbows',
        'Finding Four-Leaf Clovers', 'Counting Sheep', 'Building Blanket Forts', 
        'Making Paper Airplanes', 'Watching Paint Dry', 'Organizing Sock Drawers',
        'Alphabetizing Snacks', 'Memorizing Clouds', 'Perfecting Pancakes', 
        'Discovering New Colors', 'Inventing New Words', 'Time Traveling Thoughts'
    ];
    
    selected_adjective TEXT;
    selected_noun TEXT;
    selected_phrase TEXT;
    name_type INTEGER;
BEGIN
    -- Randomly choose between "The [Adjective] [Noun]" or just "[Phrase]"
    name_type := floor(random() * 3) + 1;
    
    IF name_type <= 2 THEN
        -- "The [Adjective] [Noun]" format
        selected_adjective := adjectives[floor(random() * array_length(adjectives, 1)) + 1];
        selected_noun := nouns[floor(random() * array_length(nouns, 1)) + 1];
        RETURN 'The ' || selected_adjective || ' ' || selected_noun;
    ELSE
        -- "[Phrase]" format
        selected_phrase := phrases[floor(random() * array_length(phrases, 1)) + 1];
        RETURN selected_phrase;
    END IF;
END;
$$ LANGUAGE plpgsql;
