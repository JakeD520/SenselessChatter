# 🚀 Roadmap Report: Dynamic Main Room Creation

## 🎯 Context

The goal is to implement a **server-driven conveyor system** for Rivvr’s chat rooms, ensuring users always have fresh spaces to join without looping endlessly or creating unnecessary empties. This aligns with Rivvr’s **ephemeral-first philosophy** and **swipe-based navigation** outlined in the core concept package【47†MUST\_READ.md†L10-L22】【49†MUST\_READ.md†L49-L63】.

---

## 🗺️ Roadmap Phases

### **Phase 1 – Foundation (MVP Ready)**

* **Schema Setup**

    * `rooms` table with `seq`, `status`, `soft_cap`, `hard_cap`, `live_user_count`.
    * `room_presence` for join/leave tracking.
* **RPC Function**: `rpc_next_room(p_from_seq)`

    * Returns the next active room.
    * Creates a new tail room if capacity is hot.
    * Returns `{ end_of_flow: true }` if tail is cold.
* **Edge Function Wrapper**

    * Thin TypeScript Deno function that invokes RPC.
    * Client calls only this endpoint.
* **Client Integration**

    * Swipe-right → invoke `next-room`.
    * Local backstack for swipe-left.
    * “End of Flow” UX message when capped.

**Deliverables:** SQL migration, Edge Function scaffold, Android Compose client integration.

---

### **Phase 2 – Scaling & Stability**

* **Concurrency Safety**

    * Advisory lock inside `rpc_next_room`.
    * Cooldown (e.g., 5s) on room creation.
* **Spare Tail Rooms**

    * Pre-warm 1–2 extra rooms when usage spikes.
    * Garbage-collect unused empties.
* **Presence Updates**

    * Lightweight `rpc_bump_presence(p_room, delta)`.
    * Minute rollups reconcile drift in `live_user_count`.

**Deliverables:** hardened SQL function, cron-based cleanups, rate-limiting in Edge Function.

---

### **Phase 3 – UX Enhancements**

* **Energy-Based Flow**

    * Creation rules use both user count & message rate.
    * Tail must be “lively” before spawning new rooms.
* **End-of-Flow UI**

    * Thematic stopper (e.g., “🌊 You’ve reached calm waters”).
    * CTA: “Jump to Fresh Room” → resets to seq=lowest.
* **Latency Hiding**

    * Pre-fetch next room while user lingers.

**Deliverables:** Energy metrics, pre-fetch hooks, client animations.

---

### **Phase 4 – Operational Maturity**

* **Archival System**

    * Idle empty rooms auto-archived after 30m.
    * Archived rooms hidden from next-room calls.
* **Load Monitoring**

    * Supabase metrics + logs for room creation frequency.
    * Alerts if room churn is too high/low.
* **Spike Handling**

    * Scheduled job adjusts `prewarm_spares` dynamically.

**Deliverables:** archival jobs, dashboards, alerting policies.

---

## ⚖️ Success Metrics

* Room creation frequency stays within stable band (not thrashing).
* Swipe latency < 200ms on average.
* % of users hitting “end of flow” < 5%.
* Average occupancy per room \~60–80% of capacity.

---

## 🚨 Guardrails (from MUST\_READ.md)

* **Ephemeral First**: Don’t persist rooms/messages beyond stated limits.
* **No Loops**: Ensure forward-only conveyor with natural end.
* **Collective > Individual**: Don’t add features that encourage “room ownership.”
* **Simplicity**: Keep UX swipe-driven, no complex menus【48†MUST\_READ.md†L89-L104】.

---

## ✅ Next Steps

1. Write SQL migration for `rooms` + `rpc_next_room`.
2. Deploy Edge Function with Supabase service role key.
3. Wire Android Compose swipe gestures to Edge Function.
4. Instrument logs for room creation events.

---

**Summary:** This roadmap takes Rivvr’s **main room conveyor** from a basic MVP into a production-grade, scalable, ephemeral system that matches the anti-parasocial mission【50†MUST\_READ.md†L243-L259】.
