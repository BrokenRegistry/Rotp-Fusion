# Multiplayer New Game Flow Implementation Plan

> Executed inline in the authoring session. Approved plan (user, 2026-10-03): mode screen first, host-only
> game settings, per-player Select Your Avatar, play-by-email invitation round, display-only settings
> during matches. Per-player governor policies are deferred.

**Architecture:** `rotp.multiplayer.setup` holds the draft match (mode, roster, per-seat factions) and
the invitation file. Seat empires are created like the player's (`SpeciesFactory` seat override).
`SetupRaceUI` gains a seat mode that edits one seat's faction through the player fields, swapping the
host's values out and back.

**Tests:** `-Pphase1-acceptance` integration tests in `tests/rotp/multiplayer/`, unit tests in
`tests/rotp/multiplayer/setup/`.

---

- [ ] **1. Display-only settings during matches.** New `MultiplayerDisplayOptions` sub UI built from the
  visual, zoom, menu-preference, GNN/pop-up filter, help, flag and name groups. `GameUI.goToSettings`,
  `BaseCompactOptionsUI.start` and the governor window open it (or stay blocked for gameplay/debug
  screens) during a hot-seat or play-by-email match. Labels en/fr. Test: opening is allowed for display,
  refused for governor/debug.
- [ ] **2. Seat factions in galaxy creation.** `SeatFaction(race, leaderName, homeWorldName, colorId)` per
  human seat beyond the host; `SpeciesFactory` builds those aliens like the player species with fixed
  color, removing used colors. Unit + integration test: empires get chosen race, names, colors.
- [ ] **3. Mode screen.** `NewGameModeUI` (Single / Hot Seat / Play by Email / Back) after New Game. Mode
  stored in `MatchDraft`. Galaxy screen: single player hides the players button; multiplayer shows
  "Players: N humans" editing names (host slot 0 always human, at least two humans), no mode buttons.
- [ ] **4. Per-player avatar screens (hot seat).** `SetupRaceUI` seat mode: banner with player name, Next
  returns the seat faction, Cancel returns to galaxy setup, presets/custom species/ship set hidden,
  colors taken by earlier seats refused. After galaxy Start: privacy handoff then avatar screen per
  other human, then the match starts with seat factions.
- [ ] **5. Play-by-email invitation round.** After galaxy Start in email mode: save invitation file
  (options, roster, factions so far, host PIN not yet, build stamp) for next human; Load Game routes
  invitation files; invitee sets PIN, picks faction, sends on; last invitee's computer builds the
  galaxy and sends turn 1 to the first player.
- [ ] **6. Docs, French labels, full suite, JAR.**
