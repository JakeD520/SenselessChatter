-- CLEAN AUTH RESET: Delete all existing auth users and start fresh
-- Run this in Supabase SQL Editor to completely reset authentication

-- ============================================
-- STEP 1: DELETE ALL EXISTING AUTH DATA
-- ============================================

-- Delete all profiles first (to avoid foreign key constraints)
DELETE FROM public.profiles;

-- Delete all auth users (this will cascade to sessions, etc.)
DELETE FROM auth.users;

-- Delete any remaining auth sessions
DELETE FROM auth.sessions;

-- Delete refresh tokens
DELETE FROM auth.refresh_tokens;

-- ============================================
-- STEP 2: VERIFY CLEAN STATE
-- ============================================

-- Check that everything is clean
SELECT 'Auth users remaining:' as status, COUNT(*) as count FROM auth.users;
SELECT 'Profiles remaining:' as status, COUNT(*) as count FROM public.profiles;
SELECT 'Sessions remaining:' as status, COUNT(*) as count FROM auth.sessions;

-- ============================================
-- STEP 3: READY FOR FRESH SIGNUPS
-- ============================================

-- The auto-profile creation trigger is already in place
-- New users will automatically get profiles created when they sign up

SELECT 'Ready for fresh authentication!' as status;