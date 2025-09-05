Recommended MVP: Server-driven Conveyor
Core rules (keeps things smooth)

Capacity threshold: HARD_CAP (e.g., 120) and soft cap (e.g., 90).

Creation trigger: If user requests “next” while tail room ≥ soft cap (or energy high), create one new room at the tail.

Hysteresis (anti-thrash): Only create another new room if the tail’s average over the last N minutes ≥ soft cap and message rate ≥ X/min.

End-of-flow clamp: If tail has 0 users for Y minutes and message rate ~0, do not create new rooms; return end_of_flow: true.

TTL & cleanup: If a room is idle >= 30 min and users == 0, mark archived. Periodic job deletes old ones (or keeps a tiny “seed” set warm).

Pre-warm on spikes: If global concurrency spikes, precreate 1–3 tail rooms (hidden) so /next_room is instant.

Minimal data model (Supabase-friendly)

rooms

id uuid pk

seq bigint unique (monotonic conveyor index)

status text (‘active’, ‘archived’)

soft_cap int, hard_cap int

created_at timestamptz, archived_at timestamptz

room_stats (aggregated per minute)

room_id uuid fk

ts timestamptz

user_count int

msg_rate int

composite pk (room_id, ts)

room_presence

room_id uuid fk

user_id uuid

joined_at timestamptz

left_at timestamptz null

(You can also maintain a materialized view or a cached counter for live headcount.)

Key API endpoints

GET /next_room?from=seq

Input: current seq (or null on first open)

Logic:

If from is null, return lowest seq active room (or the current “main”).

Calculate candidate = smallest active seq > from.

If none, evaluate tail: if tail meets creation rules → create seq = tail+1; else return {end_of_flow: true}.

Return {room_id, seq, live_counts, energy}.

GET /prev_room?from=seq

Client can mostly use its local backstack, but expose this in case the app restarts.

POST /events/presence

Body: {room_id, action: "join"|"leave"}

Server updates room_presence, live headcount cache, and rolls stats.

POST /rooms/create (internal)

Only the conveyor manager calls this when rules say “add tail.”

Creation & end-of-flow logic (pseudocode)
function next_room(from_seq):
if from_seq is null:
return room with min(seq) where status='active'

candidate = min(seq) where seq > from_seq and status='active'
if candidate exists:
return candidate

tail = max(seq) where status='active'
if should_extend(tail):
new = create_room(seq = tail + 1)
return new

return { end_of_flow: true }

function should_extend(tail_seq):
stats = last_5_minutes_stats(tail_seq)
return (
stats.avg_user_count >= soft_cap ||
(stats.avg_user_count >= soft_cap*0.8 && stats.avg_msg_rate >= MSG_RATE_MIN)
)

Client behavior (crucial polish)

Backstack: Keep a local stack of {room_id, seq} so swiping left is instant and offline-safe.

End-of-flow UX: When end_of_flow: true, show a tasteful stopper (e.g., “You’ve reached the calm waters”) and offer “Jump to Fresh Room” (which calls /next_room with from=null) or “Switch Topic”.

Latency hiding: Optimistically preload next_room when a user pauses on the current room (debounced).

Loop prevention: Never locally “wrap around.” Always ask server for the next seq.

Anti-abuse & safety

Rate limit /next_room per user (e.g., 10/sec burst, 30/min) to block swipe-spam.

Bot flood protection: If a spike is from suspicious sources, temporarily raise the soft cap or slow creation.

RLS: Rooms are generally public-read; presence writes must match auth.uid().

Cleanup policies

Archival: Idle + empty → archive. Keep last N rooms unarchived so back-swipes still work for a bit (or allow back into archived rooms for 5 minutes).

Compression: Periodic job re-numbers seq only if you really need to (I wouldn’t; monotonically increasing seq is fine).

“Make it real” quick start (Supabase)

Use PostgREST RPC or an Edge Function for /next_room so you can run the creation logic server-side (Node or Deno).

Maintain a live headcount per room in Redis or Postgres advisory locks + room_live_counts table (updated on join/leave).

A CRON (Supabase scheduled function) runs every minute to:

Roll minute aggregates into room_stats

Archive stale rooms

Pre-warm tails during spikes

Nice extras (when you’re ready)

Energy sort inside each conveyor segment (e.g., tiny reordering windows to keep hot rooms adjacent).

Micro-branching (“tributaries”): If a topic diverges, the server spawns a side-branch conveyor; a subtle affordance lets users hop into that tributary without losing backstack.

Soft seating: Prefer placing newcomers into rooms with 60–80% capacity to keep vibes lively but not chaotic.