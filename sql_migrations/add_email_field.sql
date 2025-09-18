-- ADD EMAIL FIELD: Update clean schema to include email field for compatibility
-- Run this in Supabase SQL Editor

-- Add email field to profiles table
ALTER TABLE public.profiles 
ADD COLUMN IF NOT EXISTS email TEXT;

-- Update the auto-profile creation function to include email
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, display_name)
    VALUES (
        NEW.id, 
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'display_name', 'Anonymous User')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Update existing profiles to include email from auth.users
UPDATE public.profiles 
SET email = auth_users.email
FROM auth.users auth_users
WHERE public.profiles.id = auth_users.id;

-- Verify the update
SELECT 'Updated profiles:' as status;
SELECT id, email, display_name, created_at FROM public.profiles;