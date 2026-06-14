CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Links to auth.users
    first_name TEXT,
    last_name TEXT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone_number TEXT,
    profile_picture TEXT,
    guess_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE group_members (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    group_id UUID REFERENCES groups(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'MEMBER',
    settings_notification BOOLEAN DEFAULT TRUE,
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    
    PRIMARY KEY (user_id, group_id)
);

CREATE TABLE games (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id UUID REFERENCES groups(id) ON DELETE CASCADE,
    nb_rounds INT NOT NULL,
    round_duration_minutes BIGINT NOT NULL,
    current_round_index INT DEFAULT 0,
    status TEXT NOT NULL, -- 'CREATED', 'PLAYING', 'FINISHED'
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    date_begin TIMESTAMPTZ NOT NULL,
    date_end TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE rounds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID REFERENCES games(id) ON DELETE CASCADE,
    index INT NOT NULL,
    status TEXT NOT NULL, -- 'CREATED', 'PLAYING', 'REVEALED', 'FINISHED'
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL
);

CREATE TABLE pictures (
    id TEXT PRIMARY KEY,
    round_id UUID REFERENCES rounds(id) ON DELETE CASCADE,
    publisher_id UUID REFERENCES users(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    latitude FLOAT8 NOT NULL,
    longitude FLOAT8 NOT NULL,
    description TEXT,
    revealed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE guesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    round_id UUID REFERENCES rounds(id) ON DELETE CASCADE,
    player_id UUID REFERENCES users(id) ON DELETE CASCADE,
    picture_id TEXT REFERENCES pictures(id) ON DELETE CASCADE,
    latitude FLOAT8 NOT NULL,
    longitude FLOAT8 NOT NULL,
    distance_meters FLOAT8 NOT NULL,
    guess_score INT NOT NULL,
    guessed_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE game_scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID REFERENCES games(id) ON DELETE CASCADE,
    player_id UUID REFERENCES users(id) ON DELETE CASCADE,
    score INT DEFAULT 0,
    rank INT,
    date_last_update TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (game_id, player_id)
);

CREATE TABLE round_scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    round_id UUID REFERENCES rounds(id) ON DELETE CASCADE,
    player_id UUID REFERENCES users(id) ON DELETE CASCADE,
    score INT DEFAULT 0,
    UNIQUE (round_id, player_id)
);

CREATE TABLE player_stats (
    player_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_score INT DEFAULT 0,
    games_played INT DEFAULT 0,
    wins INT DEFAULT 0
);

CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    player_1_id UUID REFERENCES users(id) ON DELETE CASCADE,
    player_2_id UUID REFERENCES users(id) ON DELETE CASCADE,
    status TEXT NOT NULL, -- 'PENDING', 'ACCEPTED'
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (player_1_id, player_2_id)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    group_id UUID REFERENCES groups(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    date_sent TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE reactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    player_id UUID REFERENCES users(id) ON DELETE CASCADE,
    picture_id TEXT REFERENCES pictures(id) ON DELETE CASCADE,
    message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
    emoji TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT chk_reaction_target CHECK (
        (picture_id IS NOT NULL AND message_id IS NULL) OR 
        (picture_id IS NULL AND message_id IS NOT NULL)
    )
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    player_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    content TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE user_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    player_id UUID REFERENCES users(id) ON DELETE CASCADE,
    setting_key TEXT NOT NULL,
    setting_value TEXT,
    
    UNIQUE(player_id, setting_key)
);


CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER SET search_path = public
AS $$
DECLARE
  base_username TEXT;
  final_username TEXT;
BEGIN
  base_username := COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1));
  final_username := base_username;
  
  WHILE EXISTS (SELECT 1 FROM public.users WHERE username = final_username) LOOP
    final_username := base_username || '_' || substr(md5(random()::text), 1, 4);
  END LOOP;

  INSERT INTO public.users (id, username, email)
  VALUES (
    NEW.id,
    final_username,
    NEW.email
  );
  RETURN NEW;
END;
$$;


CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

CREATE POLICY "Allow logged-in users to search" 
ON public.users 
FOR SELECT 
USING (auth.role() = 'authenticated');

CREATE POLICY "Users can read their own friendships" 
ON public.friendships FOR SELECT 
USING (auth.uid() = player_1_id OR auth.uid() = player_2_id);

CREATE POLICY "Users can insert their own friend requests" 
ON public.friendships FOR INSERT 
WITH CHECK (auth.uid() = player_1_id);

CREATE POLICY "Users can update requests sent to them" 
ON public.friendships FOR UPDATE 
USING (auth.uid() = player_2_id)
WITH CHECK (auth.uid() = player_2_id);

CREATE POLICY "Users can delete their friendships"
ON public.friendships FOR DELETE
USING (auth.uid() = player_1_id OR auth.uid() = player_2_id);

-- RLS for Groups
ALTER TABLE public.groups ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can create groups" 
ON public.groups FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can view groups they belong to" 
ON public.groups FOR SELECT 
USING (
    EXISTS (
        SELECT 1 FROM public.group_members 
        WHERE group_members.group_id = groups.id 
        AND group_members.user_id = auth.uid()
    )
);

-- RLS for Group Members
ALTER TABLE public.group_members ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can add members to groups" 
ON public.group_members FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can view group memberships" 
ON public.group_members FOR SELECT 
USING (auth.role() = 'authenticated');

CREATE POLICY "Users can leave or remove members" 
ON public.group_members FOR DELETE 
USING (auth.role() = 'authenticated');

-- RLS for Games
ALTER TABLE public.games ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert games" 
ON public.games FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can view games they belong to" 
ON public.games FOR SELECT 
USING (auth.role() = 'authenticated');

CREATE POLICY "Users can update active games" 
ON public.games FOR UPDATE 
USING (auth.role() = 'authenticated');

-- RLS for Rounds
ALTER TABLE public.rounds ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert rounds" 
ON public.rounds FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can view rounds" 
ON public.rounds FOR SELECT 
USING (auth.role() = 'authenticated');

CREATE POLICY "Users can update rounds" 
ON public.rounds FOR UPDATE 
USING (auth.role() = 'authenticated');

-- RLS for Game Scores
ALTER TABLE public.game_scores ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert game scores" 
ON public.game_scores FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can view game scores" 
ON public.game_scores FOR SELECT 
USING (auth.role() = 'authenticated');

CREATE POLICY "Users can update their scores" 
ON public.game_scores FOR UPDATE 
USING (auth.role() = 'authenticated');

-- RLS for Round Scores
ALTER TABLE public.round_scores ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert round scores" 
ON public.round_scores FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can view round scores" 
ON public.round_scores FOR SELECT 
USING (auth.role() = 'authenticated');

CREATE POLICY "Users can update round scores" 
ON public.round_scores FOR UPDATE 
USING (auth.role() = 'authenticated');

-- 1. Create the storage bucket for pictures (if it doesn't already exist)
insert into storage.buckets (id, name, public)
values ('pictures', 'pictures', true)
on conflict (id) do nothing;

-- 2. Allow ANY authenticated user to upload images to the 'pictures' bucket
create policy "Allow authenticated uploads"
on storage.objects for insert
to authenticated
with check ( bucket_id = 'pictures' );

-- 3. Allow ANYONE to view/read the pictures
create policy "Allow public viewing"
on storage.objects for select
to public
using ( bucket_id = 'pictures' );

-- 4. Make sure your database 'pictures' table also allows inserts
-- Enable RLS on the pictures table (if not already enabled)
alter table public.pictures enable row level security;

-- Enable RLS
alter table public.pictures enable row level security;

-- Allow players to INSERT pictures ONLY if they are in the game
create policy "Allow players to insert pictures"
on public.pictures for insert
to authenticated
with check (
  exists (
    select 1 from public.rounds r
    join public.game_scores gs on gs.game_id = r.game_id
    where r.id = round_id
    and gs.player_id = auth.uid()
  )
);

-- Allow players to SELECT/VIEW pictures ONLY if they are in the game
create policy "Allow players to view pictures"
on public.pictures for select
to authenticated
using (
  exists (
    select 1 from public.rounds r
    join public.game_scores gs on gs.game_id = r.game_id
    where r.id = round_id
    and gs.player_id = auth.uid()
  )
);

-- Enable RLS
alter table public.guesses enable row level security;

-- Allow players to INSERT guesses ONLY if they are in the game
create policy "Allow players to insert guesses"
on public.guesses for insert
to authenticated
with check (
  exists (
    select 1 from public.rounds r
    join public.game_scores gs on gs.game_id = r.game_id
    where r.id = round_id
    and gs.player_id = auth.uid()
  )
);

-- Allow players to SELECT/VIEW guesses ONLY if they are in the game
create policy "Allow players to view guesses"
on public.guesses for select
to authenticated
using (
  exists (
    select 1 from public.rounds r
    join public.game_scores gs on gs.game_id = r.game_id
    where r.id = round_id
    and gs.player_id = auth.uid()
  )
);

-- Allow users to update their own profile only
CREATE POLICY "Users can update own profile" 
ON public.users FOR UPDATE 
USING (auth.uid() = id);


CREATE POLICY "Users can delete their friendships"
ON public.friendships FOR DELETE
USING (auth.uid() = player_1_id OR auth.uid() = player_2_id);
