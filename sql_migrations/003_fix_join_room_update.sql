-- Fix the rpc_join_room function to properly handle room user count updates
-- The issue was an UPDATE without WHERE clause that PostgreSQL correctly rejected

CREATE OR REPLACE FUNCTION rpc_join_room(p_room_id BIGINT)
RETURNS JSON AS $$
DECLARE
    room_record RECORD;
    current_user_id UUID;
    old_room_ids BIGINT[];
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

    -- Get list of rooms user was in before removing them
    SELECT ARRAY(
        SELECT room_id FROM public.room_presence 
        WHERE user_id = current_user_id
    ) INTO old_room_ids;

    -- Remove user from any other rooms first
    DELETE FROM public.room_presence WHERE user_id = current_user_id;
    
    -- Update user counts for rooms they left (now with proper WHERE clause)
    UPDATE public.rooms 
    SET live_user_count = (
        SELECT COUNT(*) FROM public.room_presence 
        WHERE room_id = rooms.id
    )
    WHERE id = ANY(old_room_ids);

    -- Add user to new room
    INSERT INTO public.room_presence (room_id, user_id)
    VALUES (p_room_id, current_user_id)
    ON CONFLICT (room_id, user_id) DO UPDATE 
    SET last_seen = NOW();

    -- Update room user count for the new room
    UPDATE public.rooms 
    SET live_user_count = (
        SELECT COUNT(*) FROM public.room_presence 
        WHERE room_id = p_room_id
    ),
    last_activity = NOW()
    WHERE id = p_room_id;

    RETURN json_build_object(
        'success', true,
        'room_id', p_room_id,
        'room_name', room_record.name
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
