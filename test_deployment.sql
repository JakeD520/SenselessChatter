-- Check if tables exist and their structure
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_schema = 'public' 
AND table_name IN ('rooms', 'room_presence', 'ripples')
ORDER BY table_name, ordinal_position;

-- Check if any rooms exist
SELECT COUNT(*) as room_count FROM public.rooms;

-- Check if RPC functions exist
SELECT routine_name, routine_type 
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name LIKE 'rpc_%'
ORDER BY routine_name;

-- Test the quirky name generator (this doesn't require auth)
SELECT generate_quirky_room_name() as sample_room_name_1;
SELECT generate_quirky_room_name() as sample_room_name_2;
SELECT generate_quirky_room_name() as sample_room_name_3;

-- Check room sequence
SELECT nextval('room_seq_number') as next_room_sequence;

-- NOTE: rpc_next_room(0) requires authentication, so it will return {"error": "Not authenticated"}
-- This is expected behavior and confirms the RPC functions are working correctly!
