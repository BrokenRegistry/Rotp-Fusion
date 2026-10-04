# Play by Email Implementation Plan

> Executed inline in the authoring session. Spec: `docs/superpowers/specs/2026-10-03-play-by-email-design.md`.

**Goal:** Save-passing play by email on top of hot seat.

**Architecture:** New package `rotp.multiplayer.pbem` holds pure rules (standing orders, PIN hashing,
file naming, build stamp) plus the `PlayByEmail` match state. `HotSeatController` consults it for
providers and handoffs; UI changes live in `rotp.ui.multiplayer`.

**Tech:** Java 17, Swing, JUnit 5. Unit tests: `mvn test -Dmaven.antrun.skip=true -Dmaven.resources.skip=true`.
Integration: add `-Pphase1-acceptance` (sets `rotp.integration=true`).

---

- [x] **1. Rules (TDD, unit):** `StandingOrders`, `StandingOrderRules` (bombard, sabotage), `PinHash`,
  `TurnFiles.fileName(label, turn, name)`, `BuildStamp` (current + `requireMatch`). Tests in
  `tests/rotp/multiplayer/pbem/*Test.java`. Commit.
- [x] **2. Match state:** `PlayByEmail` (label, orders, pins). `GameSession.playByEmail` field, getter,
  `startHotSeatGame(options, setup, boolean)`; reset in `startGame`/stop. Setup panel button +
  `SetupGalaxyUI` plumbing; button label shows "(Play by Email)". Commit.
- [x] **3. Machine-local settings:** PBEM guard in `AI.promptForBombardment` and colonize path; audit
  other cfg-backed options read by model code. Integration test: prefs set to always-bomb /
  auto-colonize do not bypass PBEM routing. Commit.
- [x] **4. Standing-order providers:** in `attachProviders`, PBEM providers apply rules; espionage via
  scientist/spymaster AI. Integration test driving each adapter with a PBEM match. Commit.
- [x] **5. Build stamp in saves:** extra zip entry for PBEM saves; check before deserialize in both load
  paths (`loadSession` and `restoreHotSeatEnvelope`); label `PBEM_BUILD_MISMATCH`. Test. Commit.
- [x] **6. PIN screen:** PIN variant of `HotSeatPrivacyPane`; `HotSeatDesktop.cover` uses it in PBEM.
  Test set / wrong / right PIN. Commit.
- [x] **7. Send flow:** controller `localPlayer`; on handoff to another player write turn file and show
  Send screen; Finish-turn screen with standing orders; silent drop of eliminated reports.
  Integration test: two-player round trip via files with fresh loads. Commit.
- [x] **8. Docs + full test run + manual three-round game.** Player guide `doc/multiplayer/play-by-email.md`.
