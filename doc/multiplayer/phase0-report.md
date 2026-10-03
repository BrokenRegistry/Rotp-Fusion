# Phase 0 report

Source baseline: `1dfe8bfb` (Fusion 2026-09-30). The [project plan](../../docs/superpowers/plans/2026-09-30-multiplayer.md) remains the roadmap. See [architecture findings](architecture.md) and [action inventory](action-inventory.md) for the evidence.

## Completed

- Added a separate `tests` source root, JUnit Jupiter 6.1.1, and Surefire 3.5.4; tests are discovered by Maven.
- Compiled production code using the Java 17 API surface (`release=17`) and ran `mvn clean test -Dmaven.antrun.skip=true -Dmaven.resources.skip=true`: 831 source files compiled; 2 tests passed.
- Characterized seeded random stream consistency and continuation after Java serialization. This covers the generator used for persistent galaxy randomness, not a full simulated turn.
- Mapped major player action families, result notifications, and blocking decisions, including VIP console paths that could bypass a graphical command gateway.
- Identified singleton/`PLAYER_ID`/AI-control coupling, global notification state, turn-thread suspension, and UI dependencies at the desktop entry and inside resolution.
- Ran `java -Djava.awt.headless=true -cp target/classes rotp.Rotp`; it fails with `HeadlessException` at `Rotp.java:112` while constructing the frame. This is an expected feasibility result.

## Findings that change the next phase

- A headless server cannot call the existing `Rotp.main()` or unmodified `GameSession.nextTurn()`. Phase 1 must first provide a non-graphical session bootstrap and a decision boundary.
- Fixed-input full-turn comparison fixtures could not be captured in Phase 0 because game creation, turn execution, and prompt handling use UI and normal save paths. The six scenarios in `action-inventory.md` are the required fixture catalogue; Phase 1 should make them executable as it extracts the turn seam. Do not claim deterministic multiplayer replay from the two RNG tests alone.
- The action table is a family-level inventory. Before declaring an action supported in Phase 2, trace and test all graphical, keyboard, governor, and VIP console entry paths for that family.
- Normal packaging additionally invokes asset conversion. On Windows, make the bundled `oggenc.exe` and `cwebp.exe` available on `PATH`; focused simulation tests can skip those steps. A native JDK 17 run remains a release gate.

## Readiness decision

Phase 1 may proceed with controller ownership and turn/decision extraction. Its exit gate must include the missing full-turn baseline scenarios and an actual two-human non-graphical harness. Do not start networking on the strength of the current RNG-only test baseline.

## October 1 acceptance update

The statements above record the initial findings. The subsequent [Phase 1 acceptance](phase1-acceptance.md) supplies real-engine two-human decision and recovery coverage, original-revision comparisons across the scenario families, and native Java 17 validation. Refer to that ledger for the final result and evidence.

The canonical plan's Phase 1 gate requires an **in-process** harness; its Phase 5 gate requires fully headless startup. The initial proposal above to require a non-graphical bootstrap in Phase 1 is superseded by that scope. Current tests still initialize hidden Swing components and require a desktop graphics environment. Networking remains gated by the subsequent orders/private-views work; this is not a LAN release.
