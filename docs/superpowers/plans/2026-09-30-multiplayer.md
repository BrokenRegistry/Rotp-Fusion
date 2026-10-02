# Multiplayer Implementation Plan

> **For agentic workers:** Use `superpowers:executing-plans` to implement an approved phase task by task. Checkboxes track deliverables. This is a phased project plan; refine each phase into small implementation changes using the evidence and interfaces established by its prerequisites.

**Goal:** Let friends control separate empires in one Rotp-Fusion game over LAN, then support reliable servers that anyone can host.

**Architecture:** One authoritative Java server owns the simulation, randomness, turn progression, and saves. Desktop clients submit validated orders and receive only their empire's permitted information. Extract reusable simulation and decision boundaries from the existing game; preserve a local adapter for single-player play.

**Tech stack:** Existing Java 17, Maven, and Swing; a versioned, length-prefixed JSON protocol over TCP, with encrypted transport for Internet deployment. Configure a maintained JSON dependency and JUnit test execution in Phase 0, checking compatibility with the existing build before pinning versions.

**Design basis:** The design and assumptions in this document implement [upstream request #136](https://github.com/BrokenRegistry/Rotp-Fusion/issues/136). Repository inspected at `1dfe8bfb17cecffcb4ed8b99ded4179f63d26f41`. This document is a proposal, not authorization to implement every phase.

## Scope and assumptions

The separate [local hot-seat milestone](2026-10-01-local-hot-seat.md) reuses Phase 1 for shared-computer play. See the [player guide](../../../doc/multiplayer/hot-seat.md) and [acceptance ledger](../../../doc/multiplayer/hot-seat-acceptance.md). It does not complete Phase 2's remote client isolation or command gateway.

- First playable milestone: two humans, optional AI opponents, small galaxies, existing desktop clients, direct LAN connection, and automatic tactical combat resolution.
- Players plan strategic orders simultaneously. Resolution begins when all active humans commit their plans. Mid-turn decisions are explicitly routed to their owners and may pause resolution.
- LAN release grows to 2–4 humans after the two-player milestone passes. Larger counts require separate performance measurements.
- Initial sessions require matching game builds, rules, and protocol versions. The host chooses and locks gameplay options before start.
- Public matchmaking, host migration, late joining as a new empire, spectators, and browser play are outside the initial release. Reconnecting to an existing seat is included.
- The host is trusted and can inspect server state. Ordinary clients receive filtered information and cannot submit orders for another empire.
- Single-player saves remain supported. Multiplayer uses a separately versioned save envelope; old builds are not expected to load multiplayer saves.
- Phase estimates are rough person-weeks for a developer familiar with Java and this codebase. They include validation and documentation but are not delivery promises.

## Existing code and implications

| Location | Observation | Planned treatment |
|---|---|---|
| `src/rotp/model/game/GameSession.java` | Singleton session, static notifications, UI calls inside turn processing, Java object serialization | Extract turn coordination, decision routing, and persistence adapters; retain single-player facade |
| `src/rotp/model/galaxy/Galaxy.java` | One `playerEmpire`; debug swapping also changes AI control | Separate viewing perspective from controller ownership; do not use swapping as the multiplayer scheduler |
| `src/rotp/model/empires/Empire.java` | Global player identity and human/AI branches | Audit every controller branch and player-dependent rule for explicit acting empire |
| `src/rotp/model/combat/ShipCombatManager.java` | Combat behavior and results depend on the current player | Introduce explicit participant context; automatic resolution first, routed tactical decisions later |
| `src/rotp/ui/notifications/` | Research, colonization, bombardment, spying, diplomacy, and voting prompts | Split immutable messages from decisions that require a response |
| `src/rotp/ui/main/FleetPanel.java` and `src/rotp/model/galaxy/Ships.java` | UI directly mutates fleets | Route actions through a command gateway and authoritative validation |
| `src/rotp/util/Base.java`, `src/rotp/Rotp.java`, `src/rotp/ui/RotPUI.java` | Shared access to session, player, randomness, and UI | Audit dependencies; isolate host simulation from graphics initialization |
| `pom.xml` | Production source root is `src`; JUnit version properties exist but no test suite was found in this assessment | Add a separate test root outside `src`, test dependencies, and a working Maven test lifecycle |

## Architectural choices

| Approach | Tradeoff | Decision |
|---|---|---|
| Authoritative host with orders and private views | Requires command and view boundaries; centralizes rules and recovery | Recommended foundation for LAN and self-hosting |
| All clients simulate the same full galaxy | Requires deterministic execution everywhere and reveals hidden state | Do not use for the planned release |
| Pass whole saves between players | Useful for an experimental hotseat or play-by-email fork, but does not solve simultaneous orders or mid-turn prompts | Not the project foundation |

One server process hosts one match initially. The hosting desktop connects through the same client interface as remote players. Single-player uses local implementations of the same command and decision contracts where practical. Avoid changing unrelated model classes solely to remove every singleton; remove globals that prevent correct simulation ownership and headless execution.

Clients render a read model plus their own uncommitted orders. Do not deserialize a complete server `GameSession` into clients and then hide fields in the UI. The protocol uses explicit data records and identifiers, never Java object deserialization of client input. Client prediction is limited to reversible planning previews; only server results change authoritative state.

## Core contracts and turn rules

Introduce the following concepts in `src/rotp/multiplayer/`; use existing entity IDs where stable, and add persisted IDs where they are not:

- `PlayerSeat`: stable player ID, empire ID, controller type, and connection status. Authentication derives the acting player; messages cannot nominate another seat.
- `OrderBatch`: match ID, turn number, unique request ID, expected plan revision, and an ordered list of typed orders. Orders cover the supported action inventory below.
- `CommandResult`: accepted/rejected, resulting plan revision, and structured validation errors. Applying a repeated request ID must not execute it again.
- `PendingDecision`: stable decision ID, owning seat, phase, legal choices, and any public timeout policy. Its resolution is persisted.
- `PlayerView`: match/turn/view revision, own empire data, visible or last-known foreign information, public facts, and notifications for this seat.
- `MatchCheckpoint`: authoritative simulation state and random state, controller roster, locked options, turn phase/cursor, committed plans, outstanding decisions, and request deduplication data.

State progression: `LOBBY → PLANNING → RESOLVING → AWAITING_DECISIONS → RESOLVING → PLANNING`, with terminal `FINISHED` and recoverable `PAUSED` states. A turn may encounter multiple decision barriers. Persist the cursor and apply-once markers at each barrier so resuming does not repeat production, movement, combat, or rewards.

During planning, each player edits an isolated plan against the same start-of-turn state. Ready commits a particular plan revision. Unready is permitted only before resolution starts. A plan change invalidates that player's ready flag. Lock all committed plans atomically before applying them; then use a documented stable empire and order sequence that preserves existing resolution rules. Revalidate references that may have become invalid, returning a specific outcome rather than silently retargeting an order.

## Phase 0 — Feasibility and behavior inventory (1–2 person-weeks)

**Outcome:** A reproducible baseline and evidence that the required engine boundaries can be extracted.

**Files:** Inspect the existing files listed above; modify `pom.xml`; create `tests/rotp/multiplayer/SimulationBaselineTest.java`, `doc/multiplayer/architecture.md`, and `doc/multiplayer/action-inventory.md`.

- [ ] Establish a clean Java 17 build and a separate test source root. Confirm Maven actually discovers and runs the new tests; the existing JUnit properties alone are insufficient.
- [ ] Record every player action family and its mutation entry points: fleets/splitting/rallying, transports, colony budgets/builds/reserve spending, governors, research allocation/selection, ship design/construction/scrapping, espionage, diplomacy, council votes, colonization, bombardment, and combat choices. Include alternate UI and VIP console paths.
- [ ] Classify each action as a planning order, resolution decision, automatic rule, or display-only action. Record ownership, visibility, phase restrictions, and save implications for each.
- [ ] Audit `player()`, `PLAYER_ID`, human/AI checks, UI access during simulation, random generators, and static session collections. Identify blockers for two human empires and headless resolution.
- [ ] Capture small baseline scenarios with fixed inputs: AI turn, research completion, colonization, combat, diplomacy, and victory. Compare normalized gameplay outcomes, excluding presentation state and timestamps.
- [ ] Probe a small automatic turn without opening a graphics window; identify exactly which dependencies prevent it. Keep any probe code isolated until promoted into the tested engine seam.
- [ ] Produce the action coverage table and revise effort estimates based on the audit. Choose and pin protocol/test dependencies compatible with the baseline build.

**Exit gate:** Baseline tests run; the action inventory has owners and phase assignments; a concrete extraction path exists. Resolve headless and multi-human blockers before expanding network work.

## Phase 1 — Multiple humans and resumable turns (3–6 person-weeks)

**Outcome:** One process can simulate two human-controlled empires without treating either as an AI or relying on global player swapping.

**Create:** `src/rotp/multiplayer/session/PlayerSeat.java`, `ControllerRegistry.java`, `src/rotp/multiplayer/turn/TurnCoordinator.java`, `DecisionRouter.java`, `PendingDecision.java`, and `TurnCheckpoint.java`.

**Modify:** `GameSession.java`, `Galaxy.java`, `Empire.java`, affected model decision sites, and local notification adapters.

- [x] Introduce controller ownership independently from local viewing perspective. Preserve governors as explicitly enabled automation without granting them general control of another human's empire.
- [x] Extract the existing turn sequence into named phases with an explicit cursor. Preserve current order of movement, production, combat, diplomacy, and research.
- [x] Replace UI-blocking decision calls with owner-specific requests and responses. Provide a single-player adapter that opens the existing panels.
- [x] Partition notifications and alerts by recipient. Route council votes and diplomatic choices to each human participant; compute game outcome per seat as well as for the whole match.
- [x] Make paused resolution serializable and resumable at defined safe points. Never rely on saving a live Java thread or call stack.
- [x] Add `ControllerRegistryTest`, `TurnCoordinatorTest`, and `DecisionRouterTest` under `tests/rotp/multiplayer/`: two humans retain control; AI still acts; prompts reach the correct seat; duplicate responses have no effect; restoring a decision barrier does not repeat the preceding phase.
- [x] Run baseline single-player scenarios and correct gameplay changes introduced by the extraction.

**Exit gate:** An in-process harness drives two independent human decision providers through research, colonization, diplomacy, automatic combat, and a game-ending condition. Neither provider consumes the other's notifications.

**October 1 acceptance:** Complete for the in-process scope. 79 tests passed on native Java 17; original-revision single-player scenarios match. Router rejection/recovery coverage is in `InProcessRecoveryTest` rather than a separate `DecisionRouterTest`. See [the acceptance ledger](../../../doc/multiplayer/phase1-acceptance.md) for evidence, review fixes and explicit later-phase limits.

## Phase 2 — Orders and private client views (3–6 person-weeks)

**Outcome:** All supported strategic actions cross a validated boundary, and each client can render only its own information.

**Create:** `src/rotp/multiplayer/orders/OrderBatch.java`, `OrderService.java`, `CommandResult.java`, typed order records grouped by action family, `src/rotp/multiplayer/view/PlayerView.java`, `PlayerViewBuilder.java`, and `src/rotp/multiplayer/client/GameGateway.java` with local and remote adapters.

**Modify:** Mutation paths found in Phase 0, beginning with `FleetPanel.java` and `Ships.java`; colony, research, ship design, diplomacy, spying, and governor controls; shared UI read access.

- [ ] Define typed orders and ownership/phase validation for every action selected for the first playable milestone. Route all interactive mutation entry points through `GameGateway`.
- [ ] Implement isolated planning state, plan revisions, ready/unready, request deduplication, and atomic plan locking. Validate numeric limits, entity ownership, range, affordability, and legal targets on the server.
- [ ] Return explicit rejection results without partial application of an invalid batch. A subsequent corrected batch must still be accepted.
- [ ] Build player views from existing visibility/knowledge rules, retaining last-known observations where the game supports them. Filter nested fields, logs, event payloads, and errors as well as map objects.
- [ ] Adapt screens to read the client view and planning overlay. Disable any unsupported action visibly in multiplayer; list every such restriction in the action inventory.
- [ ] Add `OrderServiceTest` and `PlayerViewTest`: enemy orders rejected, invalid allocation rejected, duplicate request harmless, stale plan detected, concurrent commits lock once, hidden ships/tech/colonies absent from serialized payloads, and prior scouting ages correctly.
- [ ] Add in-process integration coverage for the complete strategic action inventory and private notifications.

**Exit gate:** Two client adapters independently plan and view one match through the gateway; neither has direct access to the authoritative galaxy. The UI migration is demonstrated, not assumed from the existence of DTOs.

## Phase 3 — First playable LAN milestone (2–4 person-weeks)

**Outcome:** Two desktop clients can play a constrained match across a LAN, with automatic battles.

**Create:** `src/rotp/multiplayer/protocol/MessageEnvelope.java`, `ProtocolCodec.java`, `src/rotp/multiplayer/server/MatchServer.java`, `src/rotp/multiplayer/client/NetworkClient.java`, and `src/rotp/ui/multiplayer/LobbyUI.java`.

**Modify:** Main menu navigation, startup/session selection, ready button, and status/decision presentation.

- [ ] Implement framed messages with strict maximum frame size, schema validation, protocol version, match ID, request ID, and response correlation. Select limits using small and large projected-view fixtures; enforce them before allocation.
- [ ] Add Host and Join by address, lobby seat assignment, host-selected rules, and readiness display. Reject mismatched builds or rules before starting the match.
- [ ] Have the host's own client use the same gateway and protocol rules as guests. Run model mutations through one server execution queue; keep network callbacks off the Swing event thread.
- [ ] Wire planning, view updates, decision prompts, rejection messages, and automatic battle reports end to end. On a connection loss, pause that seat's participation and show a clear connection state.
- [ ] Add `ProtocolCodecTest` and `LanMatchTest`: fragmented frames, multiple frames per read, oversized/malformed payloads, duplicate messages, protocol mismatch, and two clients committing the same turn.
- [ ] Conduct a scripted two-machine playthrough with AI opponents, scouting, expansion, research, diplomacy, and automatic combat. Exercise every enabled action family and complete at least one small match.

**Exit gate / first playable:** Two separate machines finish the scripted small match without wrong-player prompts, divergent authoritative state, or UI access to hidden data. This is an experimental LAN build; release reliability follows.

## Phase 4 — Saves, reconnects, and LAN release (2–4 person-weeks)

**Outcome:** A 2–4 player campaign survives interrupted connections and server restarts.

**Create:** `src/rotp/multiplayer/persistence/MatchCheckpointStore.java`, `src/rotp/multiplayer/session/ReconnectService.java`, recovery fixtures, and `doc/multiplayer/lan-guide.md`.

**Modify:** Save/load adapters, lobby, network client, turn coordinator, and checkpoint metadata.

- [ ] Persist simulation/random state, all plans and revisions, seats, phase cursor, decision responses, protocol/build/rules metadata, and deduplication state. Write checkpoints atomically and retain a last-known-good copy.
- [ ] Bind reconnect credentials to a seat; reconnect through a full private snapshot plus outstanding decisions. Do not trust a player name as proof of seat ownership.
- [ ] Recover from disconnect before ready, after ready, after an accepted command whose response was lost, and while resolving a decision. Replayed messages cannot spend resources twice.
- [ ] Default to pausing for missing humans. An explicit host action may replace a disconnected human with AI; advertise this to all remaining players and persist the transition. No silent takeover.
- [ ] Verify old single-player saves still load. Unsupported multiplayer save versions fail with an actionable message rather than partially loading.
- [ ] Add `CheckpointRecoveryTest` and `ReconnectTest`: restore each barrier, crash around checkpoint publication, reject wrong-seat credentials, and resume after lost acknowledgments. Compare semantic simulation state before and after recovery.
- [ ] Run 2-, 3-, and 4-human sessions, mixed AI games, elimination, reconnect, and save/reload campaigns. Document supported rules and known restrictions.

**Exit gate:** A campaign resumes after a deliberate server stop during planning and at a pending decision, without missing or repeating a turn effect. Ship the first supported LAN release.

## Phase 5 — Dedicated self-hosted server (2–4 person-weeks)

**Outcome:** A user can run a match server without a desktop or display and connect over the Internet.

**Create:** `src/rotp/multiplayer/server/ServerMain.java`, `ServerConfig.java`, distribution packaging, and `doc/multiplayer/server-guide.md`.

**Modify:** Build packaging, simulation bootstrapping, transport configuration, and host administration controls.

- [ ] Add a headless entry point accepting bind address, port, data directory, and match configuration. Remove remaining mandatory display/audio initialization from that startup path.
- [ ] Provide secure transport and server identity verification for Internet use. Authenticate joining and reconnecting players; bound message sizes, connection counts, and command rates.
- [ ] Provide start/load/save, pause/resume, seat management, and graceful shutdown controls. Keep secrets and private empire data out of ordinary logs.
- [ ] Package a runnable server JAR and document Java requirements, TCP port forwarding/firewalls, backups, version matching, and certificate setup. LAN discovery and automatic router configuration remain optional conveniences.
- [ ] Add `HeadlessServerTest` and `ServerAccessTest`; start with `-Djava.awt.headless=true`, load and advance a saved match, reject unauthorized administration, and exercise malformed/oversized input without terminating the server.
- [ ] Have a fresh installation host a match from the guide and reconnect after a process restart. Measure server memory, turn latency, view size, and reconnect time on agreed small and medium fixtures; publish measurements and practical limits.

**Exit gate:** A person using the packaged artifact and guide can host and restore a match on a machine without a graphical display. No hosted service is required.

## Phase 6 — Full multiplayer gameplay and tactical battles (4–8 person-weeks)

**Outcome:** Manual battles and remaining Fusion gameplay features work with multiple humans.

**Create:** `src/rotp/multiplayer/combat/BattleCoordinator.java`, tactical command/view records, and battle recovery tests.

**Modify:** `ShipCombatManager.java`, combat UI, relevant `CombatStack` classes, decision routing, and action families still marked restricted.

- [ ] Give each battle explicit participants and a persisted action cursor. The server validates whose tactical turn it is, legal movement, weapons, targets, and retreat actions.
- [ ] Route tactical views only to participants. Queue battles sequentially for the first manual-combat release; show other players a waiting status without exposing private battle details.
- [ ] Preserve automatic resolution as a match rule. Define participant disconnect behavior as pause, with an explicit host decision to switch to automatic resolution if desired.
- [ ] Extend reconnect/checkpoints to tactical boundaries. Duplicate fire or retreat commands must not apply twice, including after a server restart.
- [ ] Close the Phase 0 action inventory: human-to-human treaties and tech exchange, spies, votes, bombardment, colonization conflicts, custom species, governor combinations, and victory/elimination. Each option is either covered or clearly disabled with a reason.
- [ ] Add `BattleCoordinatorTest`, `BattleRecoveryTest`, and focused interaction scenarios: human/human, human/AI, multiple empires at one system, retreats, colony destruction, and interrupted combat.

**Exit gate:** Two humans fight a manual battle, reconnect during it, and resume the strategic turn correctly. The declared multiplayer feature matrix has no untested enabled action family.

## Phase 7 — Optional browser client (separate project; estimate after scope study)

**Outcome:** Play through a browser against the existing server.

- [ ] Inventory screens and interactions needed for a complete browser game; assess asset/font delivery, accessibility, touch support, and map rendering.
- [ ] Add a browser-compatible transport adapter sharing the same message schemas, validation, permissions, and match rules. Browser sessions need an explicit authentication and origin policy.
- [ ] Build an initial vertical slice: join, private map, colony inspection, fleet orders, ready, and a new turn. Use it to measure the cost of the remaining UI before committing to a full port.
- [ ] Port research, ship design, diplomacy, intelligence, notifications, saves/reconnect UI, and optional tactical combat. Verify desktop and browser clients can participate in the same match.

**Exit gate:** Release only the scope explicitly supported by the browser feature matrix. A browser interface is not required to deliver LAN or self-hosted desktop multiplayer.

## Dependency and release schedule

| Milestone | Required phases | Cumulative rough effort |
|---|---|---|
| Engine supports multiple humans | 0–1 | 4–8 person-weeks |
| First playable LAN prototype | 0–3 | 9–18 person-weeks |
| Supported LAN campaigns | 0–4 | 11–22 person-weeks |
| Dedicated self-hosting | 0–5 | 13–26 person-weeks |
| Manual combat and broader Fusion coverage | 0–6 | 17–34 person-weeks |
| Browser client | Separate follow-on | Estimate from browser vertical slice |

These ranges refine the earlier high-level assessment: the inspected global player assumptions, direct UI mutations, and mid-turn prompts make a useful LAN milestone more involved than adding a transport. Re-estimate at Phase 0 and Phase 2 exits. Interface/protocol documentation can proceed alongside UI adaptation once contracts are stable; core simulation changes should land in dependency order.

## Validation, review, and delivery rules

- Each implementation change introduces or updates a focused behavioral test, demonstrates the target behavior, and runs relevant single-player regressions. Avoid tests that merely repeat getters or serialization implementation details.
- Use `mvn test` after Phase 0 establishes discovery. Use the documented project packaging command for release candidates. Build/toolchain corrections discovered during execution should be reviewed separately from gameplay changes.
- Keep changes small enough to review: control ownership, turn phase extraction, each order family, view filtering, transport, recovery, and tactical control are separate change sets.
- Keep multiplayer behind an explicit experimental entry until the Phase 4 gate. Preserve the single-player route and retain a working build at every phase boundary.
- Carry fixtures for ordinary turns, pending decisions, hidden information, multiplayer saves, and old single-player saves through every release.
- Require a human playthrough before each playable release. Automated simulations alone cannot establish that every required prompt and screen is usable.

## Review focus and highest risks

| Risk | Required evidence / owning phase |
|---|---|
| Global player state grants AI control or sends a prompt to the wrong human | Two independent controllers and per-seat prompt tests, Phase 1 |
| A private field leaks through a nested view, notification, or error | Inspect serialized wire payloads with seeded hidden facts, Phase 2 |
| Late ready/unready or stale revisions change a locked turn | Concurrent plan/commit tests and a single resolution transition, Phases 2–3 |
| Lost acknowledgments or recovery repeat a resource spend, reward, or battle action | Duplicate request and checkpoint restart tests, Phases 4 and 6 |
| A rare UI prompt stops a headless server indefinitely | Decision inventory coverage and headless saved-game scenarios, Phases 1 and 5 |

The largest uncertainty is adapting the existing UI to a filtered read model while removing global-player assumptions from rules. Treat the Phase 2 working two-client adapter demonstration as a firm gate before promising a LAN release date.

## Recommended first implementation increment

Execute Phase 0 first. Its concrete deliverables are a verified build/test baseline, the complete action-and-decision inventory, and an extraction proposal supported by a small headless-turn probe. Use that evidence to finalize Phase 1's detailed changes before implementation begins.
