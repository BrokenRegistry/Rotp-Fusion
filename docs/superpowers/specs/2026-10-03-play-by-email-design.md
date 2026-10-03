# Play by email (save passing) — design

Approved plan (user, 2026-10-03): option A from the play-by-email discussion. Sequential turns on top of
hot seat; players pass the hot-seat save file. Honor system beyond a per-player PIN.

## Mode

`GameSession` gains one serializable field, `PlayByEmail playByEmail` (null for hot seat and single
player). Its presence is the mode. `GameSession.startHotSeatGame(options, setup, boolean playByEmail)`
creates it. Setup panel gets a **Use Play by Email** button beside **Use Hot Seat**;
`SetupGalaxyUI` passes the flag through. `HotSeatSetup` is unchanged.

`PlayByEmail` (package `rotp.multiplayer.pbem`) holds:
- `matchLabel` — `PBEM-yyyyMMdd-HHmm` at creation; prefixes turn-file names.
- `Map<String, StandingOrders> orders` — per player ID, defaults on creation.
- `Map<String, PinHash> pins` — per player ID; absent until first unlock.

## Standing orders

`StandingOrders(Bombard bombard, Frame frame, Sabotage sabotage)` — serializable record.
- `Bombard { NEVER, ALWAYS, AT_WAR, AT_WAR_NOT_INVADING }`, default `AT_WAR`.
- `Frame { NEVER, WHEN_POSSIBLE }`, default `NEVER`.
- `SabotageTarget { FACTORIES, MISSILES, REBELS }`, default `FACTORIES`.

`StandingOrderRules` — pure functions, unit-tested without a game:
- `bombard(orders, atWar, transportsInTransit, targetAllowed)` → `SKIP | BOMBARD | TARGET_BOMBARD`.
  Rule passes → `TARGET_BOMBARD` if transports are in transit and target bombing is allowed
  (matches the single-player auto path), else `BOMBARD`.
- `sabotage(orders, suggestedSystemId, legal)` → preferred action at the suggested system if legal,
  else its first legal system, else the next action in FACTORIES, MISSILES, REBELS order; null cancels.
- Espionage tech: owner's `ai().scientist().mostDesirableTech(candidates)` (what the AI uses).
  Framing when `WHEN_POSSIBLE` and frameable list non-empty: `spyMasterAI().suggestToFrame(...)`.

`HotSeatController.attachProviders()`: in PBEM mode bombing/espionage/sabotage providers apply the
seat's standing orders immediately instead of `mission(...)` prompts. Hot seat is unchanged.

Per-computer settings must not leak into another player's empire. In PBEM mode
`AI.promptForBombardment` ignores the match-wide `autoBombard*` setting so standing orders decide.
`autoColonize` is saved with the game, so it stays a shared match setting. The `Remnants.cfg`-backed
options that change simulation (rally combat, rally losses, AI aggressiveness, ship-based missiles,
harmless-colony targeting) are frozen with the governor policies in `HotSeatPolicies`.

## Turn files and the send step

`PbemFlow` (EDT) decides what a privacy handoff means in PBEM mode. The controller keeps a transient
`localPlayer` (who last unlocked on this computer since load).
- Handoff to `localPlayer` → behave as hot seat (no cover needed for decisions; planning still covers).
- Handoff to someone else, with a `localPlayer` set → write the turn file
  `<saveDir>/<matchLabel>-T<turn 3 digits>-for-<sanitized name>.rotp` via `HotSeatPersistence.save`,
  then show the **Send** screen: "Turn saved as FILE. Send it to NAME." Buttons: **Return to menu**
  (close controller, main game menu) and **NAME is here** (show PIN screen).
- No `localPlayer` (fresh load) → PIN screen for the owner.

Finish Player Turn in PBEM opens the **Finish turn** screen first: the player's standing orders as
three dropdowns, **Send turn** (stores orders, then `finishPlayerTurn`) and **Back**.

Eliminated or finished-match reports are acknowledged silently in PBEM; the result panel still shows.

Decisions not safe to save (bombard/espionage/sabotage) never prompt in PBEM, so every handoff in
PBEM happens at a recoverable boundary. Assert this: a PBEM handoff at an unsafe point is a bug →
fail resolution with the existing error screen.

## PIN

`PinHash(salt, hash)` — PBKDF2WithHmacSHA256, 16-byte salt, 100k iterations; 4–12 characters.
`HotSeatPrivacyPane` gets a PIN variant: set-PIN (enter twice) when absent, else enter-PIN with an
error line on mismatch. Enter key submits. Setting a PIN mutates `PlayByEmail`; it is saved with the
next save.

## Build check

`BuildStamp.current()` = `Rotp.releaseId` + ` ` + `build.date` from `/build.properties`.
For PBEM saves `writeSessionAtomically` adds a second zip entry `PlayByEmail.properties`
(`build=...`) after `GameSession.dat` (the generic loader reads the first entry). The load path
checks that entry before deserializing; on mismatch it throws with
`PBEM_BUILD_MISMATCH | This turn was made with build %1; you have build %2.` Hot-seat saves without
the entry load as before.

## Tests

Unit (no game): `StandingOrderRules`, `PinHash`, turn-file naming, `BuildStamp` mismatch check.
Integration (`rotp.integration`): PBEM match where Alice finishes → file for Bob written and the
controller stops; load as Bob with PIN → plan → finish → resolution → file for next owner; standing
orders resolve a bombard/espionage/sabotage without prompting; autoBombard/autoColonize user prefs
do not change the outcome; build-mismatch file refused. Manual: three-round two-player game
passing files between two save folders.
