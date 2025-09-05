-- Core RPC function for dynamic room creation and navigation
-- This implements the "conveyor system" from DYNAMIC_MAIN_ROOM_CREATION.md

CREATE OR REPLACE FUNCTION rpc_next_room(p_from_seq INTEGER DEFAULT 0)
RETURNS JSON AS $$
DECLARE
    next_room RECORD;
    new_room_seq INTEGER;
    new_room_name TEXT;
    current_user_id UUID;
BEGIN
    -- Get current user
    current_user_id := auth.uid();
    IF current_user_id IS NULL THEN
        RETURN json_build_object('error', 'Not authenticated');
    END IF;

    -- Find the next available room after p_from_seq
    SELECT * INTO next_room
    FROM public.rooms
    WHERE seq > p_from_seq 
    AND status = 'active'
    AND live_user_count < hard_cap
    ORDER BY seq ASC
    LIMIT 1;

    -- If we found a room, check if we should create a new tail room
    IF FOUND THEN
        -- If this room is getting close to capacity, create a new tail room
        IF next_room.live_user_count >= next_room.soft_cap THEN
            -- Check if there's already a newer room (tail room)
            IF NOT EXISTS (
                SELECT 1 FROM public.rooms 
                WHERE seq > next_room.seq 
                AND status = 'active'
            ) THEN
                -- Create new tail room
                new_room_seq := nextval('room_seq_number');
                new_room_name := generate_quirky_room_name();
                
                INSERT INTO public.rooms (seq, name, status, soft_cap, hard_cap, live_user_count)
                VALUES (new_room_seq, new_room_name, 'active', 8, 12, 0);
            END IF;
        END IF;

        RETURN json_build_object(
            'room_id', next_room.id,
            'room_seq', next_room.seq,
            'room_name', next_room.name,
            'user_count', next_room.live_user_count,
            'soft_cap', next_room.soft_cap,
            'hard_cap', next_room.hard_cap,
            'end_of_flow', false
        );
    ELSE
        -- No rooms found - either no rooms exist or we're at the end
        -- Check if ANY rooms exist
        IF NOT EXISTS (SELECT 1 FROM public.rooms WHERE status = 'active') THEN
            -- No rooms exist, create the first one
            new_room_seq := nextval('room_seq_number');
            new_room_name := generate_quirky_room_name();
            
            INSERT INTO public.rooms (seq, name, status, soft_cap, hard_cap, live_user_count)
            VALUES (new_room_seq, new_room_name, 'active', 8, 12, 0)
            RETURNING * INTO next_room;

            RETURN json_build_object(
                'room_id', next_room.id,
                'room_seq', next_room.seq,
                'room_name', next_room.name,
                'user_count', next_room.live_user_count,
                'soft_cap', next_room.soft_cap,
                'hard_cap', next_room.hard_cap,
                'end_of_flow', false,
                'new_room', true
            );
        ELSE
            -- We've reached the end of available rooms
            RETURN json_build_object(
                'end_of_flow', true,
                'message', '🌊 You''ve reached calm waters. All rooms are currently full!'
            );
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to join a room (updates presence and user count)
CREATE OR REPLACE FUNCTION rpc_join_room(p_room_id BIGINT)
RETURNS JSON AS $$
DECLARE
    room_record RECORD;
    current_user_id UUID;
BEGIN
    current_user_id := auth.uid();
    IF current_user_id IS NULL THEN
        RETURN json_build_object('error', 'Not authenticated');
    END IF;

    -- Get room info
    SELECT * INTO room_record
    FROM public.rooms
    WHERE id = p_room_id AND status = 'active';

    IF NOT FOUND THEN
        RETURN json_build_object('error', 'Room not found or inactive');
    END IF;

    -- Check capacity
    IF room_record.live_user_count >= room_record.hard_cap THEN
        RETURN json_build_object('error', 'Room is full');
    END IF;

    -- Remove user from any other rooms first
    DELETE FROM public.room_presence WHERE user_id = current_user_id;
    
    -- Update user counts for rooms they left
    UPDATE public.rooms 
    SET live_user_count = (
        SELECT COUNT(*) FROM public.room_presence 
        WHERE room_id = rooms.id
    );

    -- Add user to new room
    INSERT INTO public.room_presence (room_id, user_id)
    VALUES (p_room_id, current_user_id)
    ON CONFLICT (room_id, user_id) DO UPDATE 
    SET last_seen = NOW();

    -- Update room user count
    UPDATE public.rooms 
    SET live_user_count = live_user_count + 1,
        last_activity = NOW()
    WHERE id = p_room_id;

    RETURN json_build_object(
        'success', true,
        'room_id', p_room_id,
        'room_name', room_record.name
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to leave a room
CREATE OR REPLACE FUNCTION rpc_leave_room(p_room_id BIGINT)
RETURNS JSON AS $$
DECLARE
    current_user_id UUID;
BEGIN
    current_user_id := auth.uid();
    IF current_user_id IS NULL THEN
        RETURN json_build_object('error', 'Not authenticated');
    END IF;

    -- Remove user from room
    DELETE FROM public.room_presence 
    WHERE room_id = p_room_id AND user_id = current_user_id;

    -- Update room user count
    UPDATE public.rooms 
    SET live_user_count = (
        SELECT COUNT(*) FROM public.room_presence 
        WHERE room_id = p_room_id
    )
    WHERE id = p_room_id;

    RETURN json_build_object('success', true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get current room for a user
CREATE OR REPLACE FUNCTION rpc_get_user_current_room()
RETURNS JSON AS $$
DECLARE
    room_record RECORD;
    current_user_id UUID;
BEGIN
    current_user_id := auth.uid();
    IF current_user_id IS NULL THEN
        RETURN json_build_object('error', 'Not authenticated');
    END IF;

    SELECT r.* INTO room_record
    FROM public.rooms r
    JOIN public.room_presence rp ON r.id = rp.room_id
    WHERE rp.user_id = current_user_id
    AND r.status = 'active';

    IF FOUND THEN
        RETURN json_build_object(
            'room_id', room_record.id,
            'room_seq', room_record.seq,
            'room_name', room_record.name,
            'user_count', room_record.live_user_count,
            'soft_cap', room_record.soft_cap,
            'hard_cap', room_record.hard_cap
        );
    ELSE
        RETURN json_build_object('error', 'User not in any room');
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
