# Phase 0 architecture findings

Inspected at `1dfe8bfb` on 2026-09-30. This is a feasibility audit for the [multiplayer plan](../../docs/superpowers/plans/2026-09-30-multiplayer.md), not a claim that the engine can already run headless or with two human players.

## Current control and turn flow

`model/game/GameSession.java` is a process-wide singleton. It stores a `Galaxy`, starts `nextTurnProcess()` in a thread, autosaves, runs galaxy phases, and repeatedly asks `RotPUI` to show notifications and screens. Prompt handling uses a static `suspendNextTurn` flag and a sleep loop, so the Java call stack and thread are the effective turn cursor. Saving that call stack is impossible with the current `GameSession` serialization.

`model/galaxy/Galaxy.java` stores one `playerEmpire`. Its debug `swapPlayerEmpire()` changes `Empire.PLAYER_ID` and the old/new empires' selected AI values. This is a local-perspective tool, not an independent controller roster. `model/empires/Empire.java` implements `isPlayer()` using static `PLAYER_ID` and `isPlayerControlled()` as the opposite of AI control. These concepts must be separated: an empire can be human-controlled even when its owner is not the local viewer.

`GameSession` has static notification, alert, and scratch collections. They have no recipient key. `processNotifications()` sends a single collected list to `RotPUI`. This makes both wrong-recipient prompts and information leaks likely if a server simply exposes the present session to two clients.

## Headless probe and blockers

The desktop entry point `Rotp.main()` constructs a `JFrame` before it creates a game session; this immediately requires a display. `GameSession.startGame()` calls `galaxy().startGame()`, and `Galaxy.startGame()` processes notifications and advice through UI helpers. `GameSession.nextTurnProcess()` calls `RotPUI` throughout, including before simulation advances, during result delivery, and at completion. `RotPUI` pauses the thread for numerous decisions and acknowledgments. These code paths establish that a server cannot safely call the current desktop entry point or `nextTurn()` under `java.awt.headless=true`.

The headless probe `java -Djava.awt.headless=true -cp target/classes rotp.Rotp` terminated with `java.awt.HeadlessException` at `Rotp.java:112`, before any game turn began. The practical extraction seam is between authoritative simulation and a turn interaction adapter: keep the ordered galaxy phase calls, but express any human input as an owned, persisted pending decision; route reports through recipient-specific events; and move map repaint/save-state calls into the desktop adapter. A small `GameSession`/`Galaxy` shim can preserve single-player behavior while this seam is introduced.

`GameSession.startGame()` and `GalaxyFactory.newGalaxy()` also load species/options and write saves. A headless turn probe therefore needs a way to build or load a small fixture without desktop initialization and without saving to the user's normal game directory. The current source does not provide one.

## Randomness and persistence

`Galaxy` owns `galRandom` (`Rand`) and installs it into `Rotp` during load validation when persistent RNG is enabled. `Rand` wraps serializable `SerialRandom`; the new baseline test checks equal seeds and continuation after serialization. Other random calls, time-derived IDs, and `GameSession` static state still require an audit before asserting deterministic replay. The server should restore the authoritative RNG state from checkpoints rather than ask clients to regenerate the same world.

`GameSession.saveSession()` writes a ZIP entry containing a Java-serialized full session. That is useful as existing single-player persistence evidence, but must not be used as an untrusted network payload or a client view. Multiplayer needs its own versioned checkpoint envelope and filtered client records.

## Build findings

The POM compiles from `src` for Java 17 and originally defined JUnit version properties without test dependencies or a separate test root. Phase 0 adds `tests`, JUnit Jupiter, and Surefire. The normal Maven lifecycle invokes an Ant media conversion during `compile`; on Windows it needs repository `oggenc.exe` and `cwebp.exe` on `PATH`. For a focused test run, `-Dmaven.antrun.skip=true -Dmaven.resources.skip=true` avoids converting and copying thousands of assets. A release build still needs the tools.

This workstation initially had JDK 26 and no Maven command on `PATH`. Maven 3.9.16 was downloaded to a temporary directory and SHA-512 checked against Apache's published checksum. The compiler now uses `release=17`, which checks the Java 17 API surface when Maven runs under JDK 26. A native JDK 17 run remains useful before release.

The focused command is `mvn clean test -Dmaven.antrun.skip=true -Dmaven.resources.skip=true`. It completed on JDK 26 with `release 17`, compiled 831 production files and one test file, and ran two passing tests. The ordinary baseline `mvn test` compiled production source but failed at media conversion when `oggenc` and `cwebp` were not on `PATH`. For the future wire protocol, pin `com.fasterxml.jackson:jackson-bom:2.22.3` when adding JSON serialization in Phase 3; [Jackson 2.x supports Java 8 and later](https://github.com/FasterXML/jackson-databind), and [the BOM version is published on Maven Central](https://central.sonatype.com/artifact/com.fasterxml.jackson/jackson-bom/versions). It is deliberately not a dependency yet because Phase 0 has no wire payload.

## Phase 1 extraction order

1. Add a controller registry keyed by empire ID; leave `Galaxy.player()` for local display until all model branches have an explicit acting-empire context.
2. Route AI/human checks through that registry in the turn scheduler. Do not use `swapPlayerEmpire()` to run another human empire's turn.
3. Split `nextTurnProcess()` at prompt barriers into explicit resumable phases. Treat information-only notices as queued reports and interactive notices as decisions.
4. Replace static notification scratch with per-match, per-recipient state, preserving event order.
5. Provide a desktop adapter that answers a local player's decisions using the existing panels; add a non-graphical adapter for an in-process two-human harness.
6. Record before/after semantic outcomes from the scenario catalogue, then run the same fixtures through both adapters to detect simulation drift.

The highest uncertainty is the number of player-relative branches below `Empire`, `Galaxy`, and combat; a targeted `player()`/`isPlayer()` audit is required as each action family is moved. This is likely the schedule driver before networking can begin.
