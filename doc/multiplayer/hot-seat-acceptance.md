# Hot-seat execution ledger

Plan: `docs/superpowers/plans/2026-10-01-local-hot-seat.md`; spec: `docs/superpowers/specs/2026-10-01-local-hot-seat-design.md`.

User approved the design and selected native execution. October 2, 2026: implementation started.

Current status: COMPLETE on October 2, 2026. All eight tasks, native desktop acceptance, regression, independent review fixes and the launchable delivery artifact are verified.

## Rulings and pre-flight

- Ruling: continue in the existing checkout, including its uncommitted Phase 1 work, as required by the approved plan. A clean worktree would omit that dependency. No commits, resets, merge or publication. Cost if wrong: integration depends on preserving this working-tree context.
- Ruling: keep the Windows execution ledger here and logs under `target/hotseat-*`, rather than using Bash-only commit-range scripts. The approved plan forbids commits and depends on uncommitted code; review the working files and logged evidence instead. Cost if wrong: history must be traced manually through the ledger.
- Tasks 1→2/4/7: immutable snapshots/revisions drive privacy, finish and recovery; pending handoff destination must be serialized.
- Tasks 2→3/4/5/7: UI cover must exist before startup/load and all owner changes; viewer activation never changes controllers or AI settings.
- Tasks 3→4/7: named assignments and scheduler are installed before exposing hot-seat UI; startup notifications require delivery before planning.
- Tasks 4→5/6/7: worker, future callbacks and inbox share one lifecycle; unsafe synchronous choices cannot be saved.
- Tasks 6→7: human offers and frozen global governor policies must survive restore; per-colony policies remain separate model state.
- Tasks 1–7→8: full regression, graphical acceptance, packaged artifact and one independent review are required for completion.

## Tasks

- [x] Task 1: persisted seat scheduling.
- [x] Task 2: viewer and privacy cover.
- [x] Task 3: human setup.
- [x] Task 4: worker, round progression and reports.
- [x] Task 5: graphical decisions.
- [x] Task 6: orders and human diplomacy.
- [x] Task 7: save/recovery.
- [x] Task 8: end-to-end acceptance and artifact.

## Evidence

Implementation is integrated with normal setup and play. All acceptance gates passed; dated entries below retain the implementation history.

### Targeted implementation evidence (October 2)

- Task 1 scheduler: five tests pass (`target/hotseat-state-green.log`), after missing-type RED. Session field installed; full regression pending.
- Task 2 viewer/privacy: four tests pass with scheduler tests (`target/hotseat-view-green.log`, 9 total), including held-key/stale-button behavior and separate map views. Map-view test reproduced cross-seat position loss before the fix. Broader menu/report privacy and graphical acceptance remain pending.
- Task 3 setup: three validation tests and two real-engine startup tests pass (`target/hotseat-startup-green.log`, 5 total), after missing-API RED. Named immutable assignments and metadata are installed before startup saves. Invalid assignment leaves existing galaxy intact; normal startup clears hot-seat metadata. Slot editor added; normal setup entry awaits controller integration.
- Ruling: retain the existing recovery fixture unchanged and introduce an equivalent hot-seat fixture for new tests, avoiding unrelated changes to established Phase 1 tests. Cost if wrong: duplicated bootstrap maintenance.
- Ruling: connect the setup editor's Start action only after the worker/providers exist, so no incomplete playable entry point is exposed. Cost if wrong: setup UI verification occurs later in this pass.
- Tasks 3-5 integration: normal human-slot editor, covered startup, worker, report inboxes and eight graphical decision families implemented. `target/hotseat-decisions-green.log` passes 8 targeted UI/engine cases; `target/hotseat-midpoint-regression.log` passes all 97 tests. Setup layout and comprehensive lifecycle/recovery verification remain pending.
- Task 6 in progress: reproduced finished-seat research mutation (`target/hotseat-orders-red.log`) and enemy transport clearing (`target/hotseat-transport-red.log`). Guards pass both real UI cases plus turn regression (`target/hotseat-orders-green.log`, 2 tests). Extending ownership guards and human diplomacy next.

### Integrated implementation evidence

- Task 6: human diplomacy acceptance/refusal covers all five supported offers, duplicates, wrong owner and private replies (10 cases, `target/hotseat-diplomacy-green.log`). Frozen shared governor policy reproduced then fixed. Mixed-owner bulk governor mutation reproduced then fixed. Real design callback reproduced a foreign rename; `target/hotseat-action-matrix-green.log` now passes design, spy, rally, research and transport checks for both owners, plus stale/finished and bulk guards.
- Task 7: five recovery tests pass, including partial-round planning state/RNG/reports, a saved real research prompt, malformed/unsupported input, failed serialization preserving the original save, and a failed decision restoring the last safe prompt. With order checks, `target/hotseat-final-boundaries.log`: 6 passed, zero failures.
- Task 8: five-round two-human and three-human/one-AI scenarios, nonadjacent IDs, eliminated viewer, all-human loss, military and Council endings pass (`target/hotseat-outcomes.log`, 4 tests). Participant-only combat report regression passes (`target/hotseat-report-orders-green.log`).
- Ruling: extend the existing direct UI validation pattern to bulk transport and rally callbacks as part of the ownership audit; these remain local desktop guards, not a remote authorization gateway. Cost if wrong: an unsupported UI path may need a further local guard.

### Full native verification

- `target/run-hotseat-tests.ps1 -Log target/hotseat-full-regression.log`: **118 tests passed**, zero failures/errors/skips (October 2, 10:34).
- `tests/compare-single-player.ps1`: ordinary turns, research, colonization, automatic combat, diplomacy, military/Council victories and subsequent RNG values match original commit `1dfe8bfb17cecffcb4ed8b99ded4179f63d26f41`. Log: `target/hotseat-single-player-comparison.log`; detailed evidence: `target/single-player-comparison-f23ecd9c408b4640b765b640ec8f87c2`.
- `git -c core.safecrlf=false diff --check`: passed after removing one added trailing space.
- Full shaded artifact build and graphical smoke are in progress. One fresh independent reviewer is reviewing the working tree; no review verdict yet.

### Independent review and fixes

The one fresh reviewer reported four Important findings, no Critical/Minor findings, and no declined judgments. All four were reproduced in `target/hotseat-review-red.log` and fixed in one pass. `target/hotseat-review-green.log`: 19 tests passed.

- Full glass panes now consume background mouse, motion and wheel events. `blankCoverCapturesMouseDragAndWheelBeforeTheUnderlyingMap` dispatches actual Swing mouse events: three leaked before, zero after.
- Old map keyboard callbacks now validate viewer generation/owner before any action. The real old N callback previously finished Bob's turn; it now leaves his snapshot unchanged.
- Council ruling text names the elected empire and explains acceptance and defiance. The graphical decision test now verifies the leader's visible name.
- Hot-seat offer submission and replies share eligibility checks. Breaking trade before a pact reply, or breaking a pact before an alliance reply, now invalidates the offer and gives both humans an explanatory report instead of creating a treaty.
- Full post-fix regression and fresh packaged desktop acceptance are underway. The initial full asset package built successfully (`target/hotseat-package.log`).

### Final build and regression evidence

- Post-review full suite: **121/121 passed**, zero failures/errors/skips, `target/hotseat-final-regression.log` (10:50).
- Final compiled single-player/RNG comparison: **PASS**, `target/hotseat-final-single-player.log`, evidence directory `target/single-player-comparison-cab6b2ea68ba4a7d8c3504e793ce73dc`.
- Final package: **BUILD SUCCESS**, `target/hotseat-final-package.log` (10:54). The full asset copy from the first successful build was retained; updated labels were copied before the final build. No `clean` or media conversion was run.
- Full artifact: `F:\Projects-PCGames\Rotp-Fusion\target\rotp-Fusion-2026-10-01.jar`.
- SHA-256: `6F581FA446D5DBBF4CE9566E1D76FCFE97763CF52A41256DCCFFC1687817D6F9`.
- Manifest: `Main-Class: rotp.RotpGovernor`, Java 17. Archive verified: 15,692 entries, 4,960 JPG/PNG images, 74 WAV/OGG audio resources, and `HotSeatController.class`.
- The isolated playtest copy has the identical SHA-256. Native Java 17 process launched from `target/hotseat-playtest`; normal setup, Alice/Bob assignment, distinct colony orders, Alice scout deployment, research allocation, duplicate Finish clicks and Bob's normal planning save were exercised. Final-build cold Continue and three-round playthrough are still in progress.

### Native desktop acceptance (October 2, 11:00-11:16)

- Two humans (Alice/Silicoid and Bob/Human) plus two AI opponents, Small galaxy. Distinct colony spending, governor toggles, Alice's split scout deployment and research allocation were entered through the normal screens.
- Bob's Turn 1 planning save survived application exit and a cold Continue. The first screen was Bob's opaque handoff; Escape did not expose the map. Background clicks and scrolling remained covered at Alice's next handoff.
- All eleven initial research choices (five Alice, six Bob) were answered using their real graphical buttons after named handoffs. No console or automatic human responses were used.
- Three shared rounds reached Turn 4; Alice's finish handed to Bob without advancing the galaxy, and Bob's finish advanced it once. Both seats retained distinct map positions, homeworlds and spending allocations.
- Normal Save Game and Load Game restored the Turn 4 campaign behind Alice's handoff. Window resizing from 80% to 75% survived restart. A fourth shared round reached Turn 5 and displayed Alice's private scouting report for Datol; acknowledging it opened Alice's map with the arrived scout.
- Other decision families, combat reports and the broader action table are covered by the automated graphical/engine tests, not claimed as naturally encountered during this short campaign. The three playtest stderr logs were empty.
- This playthrough found that transient maintenance caches deserialize to zero, temporarily overstating planning income until the next model recalculation. The recovery regression now compares all three seats' maintenance and net income immediately after load: `target/hotseat-maintenance-red.log` reproduced expected 0.20814158 versus actual 0.0. Hot-seat restoration now invalidates the derived economic caches before revealing planning.

### Delivery regression

- Final: fixed all four Important review findings with RED-to-GREEN regression evidence; none deferred or declined. No Minor findings.
- Final: fixed the native-playthrough maintenance preview finding with `planningSavePreservesPartialRoundReportsOrdersAndRandomStream` RED-to-GREEN evidence. Full suite **121/121 passed**, zero failures/errors/skips, `target/hotseat-delivery-regression.log` (11:20).
- Fresh single-player gameplay/RNG comparison: **PASS**, `target/hotseat-delivery-single-player.log`; evidence directory `target/single-player-comparison-7d5d9796739f4fa181756588b24d764b`.
- The earlier package hash above identifies the pre-cache-fix playthrough build; the delivery package below supersedes it.

### Verified delivery artifact

- `F:\Projects-PCGames\Rotp-Fusion\target\rotp-Fusion-2026-10-01.jar`, SHA-256 **`38C594232081DE0B325B65B652B087129DDF5FC4BB2992704ECABFDE103A2E5D`**.
- `target/hotseat-delivery-package.log`: BUILD SUCCESS, 11:22. Java 17 manifest/main class and all 15,692 archive entries verified, including 4,960 images, 74 audio resources and the hot-seat controller.
- Identically hashed playtest copy launched in native Java 17. Cold Continue restored Alice's Turn 5 handoff, persisted private scouting report and map. Production correctly showed net 59/gross 71 and 21 RP, matching the pre-save values. `delivery-visible-stderr.log` remained empty. The test game was closed after verification.
- `git -c core.safecrlf=false diff --check`: passed. Existing main checkout and uncommitted Phase 1 work preserved; no commit, merge or publication. The generated EXE/Windows ZIP were not independently exercised; the verified deliverable is the full JAR.
- Player launch/setup instructions: `doc/multiplayer/hot-seat.md`. Supported actions and restrictions: `doc/multiplayer/hot-seat-actions.md`. All eight task contracts are complete; this delivers local hot-seat play, not the later network multiplayer milestone.
