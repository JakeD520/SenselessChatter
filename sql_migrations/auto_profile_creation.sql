-- AUTO-PROFILE CREATION: Add trigger to create profiles automatically
-- Run this in Supabase SQL Editor after the nuclear reset

-- Create function to handle new user signups
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, display_name)
    VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data->>'display_name', 'Anonymous User'));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create trigger on auth.users table
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Also create profiles for any existing authenticated users
INSERT INTO public.profiles (id, display_name)
SELECT id, COALESCE(raw_user_meta_data->>'display_name', 'User')
FROM auth.users
WHERE id NOT IN (SELECT id FROM public.profiles);

-- Verify what we created
SELECT 'Profiles created:' as status;
SELECT id, display_name, created_at FROM public.profiles;