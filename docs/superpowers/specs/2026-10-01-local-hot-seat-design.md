# Local hot-seat multiplayer

Status: approved October 1 and implemented/verified October 2, 2026. The native local hot-seat milestone is complete. See `doc/multiplayer/hot-seat-acceptance.md` for verification and the launchable artifact.

## Goal and assumptions

The user wants actual local hot-seat play, following Phase 1's successful two-human engine acceptance. Success means starting a game from the normal desktop UI, assigning at least two human empires, issuing meaningful orders for each, handing over the computer privately, resolving turns, and saving and resuming the campaign.

Recommended assumptions: one shared computer; two or more local humans alongside optional AI opponents; fixed seat order; automatic ship and ground combat for this milestone. A shared galaxy turn resolves only after every surviving human finishes planning. Humans may need to pass the computer again for decisions during resolution.

This is trusted local play. The handoff screen protects against accidental disclosure through the UI; it is not authentication or protection against someone inspecting local files. Human assignments remain stable throughout the campaign.

## Alternatives and recommendation

1. **Focused desktop hot-seat controller (recommended).** Reuse existing strategic screens, Phase 1 ownership, decision routing and recovery. Add explicit viewer activation, seat progression, private handoffs and graphical decision providers. This reaches playable local sessions without building networking first. Existing model-backed screens remain local-only; later remote-client work still needs the planned filtered views and command gateway.
2. **Complete the full Phase 2 gateway first.** Move every supported screen to isolated planning and filtered client views before hot-seat play. This offers the strongest foundation for LAN but is a substantially larger prerequisite to playing locally.
3. **Use the debug player-swap command.** Rejected: `Galaxy.swapPlayerEmpire` changes AI assignments and debug options and does not manage private screen state, readiness or owned prompts.

The recommendation is a local milestone alongside the roadmap, not completion of Phase 2's two-client isolation gate.

## Player experience

- Normal new-game setup exposes Hot Seat and allows assigning human seats to empire slots, with player names and the existing race configuration. Require at least two human seats and valid unique assignments before Start. Other slots retain their AI choices. The ordinary single-player setup remains available.
- Startup shows an opaque handoff screen identifying the first player. Only an explicit Continue reveals that player's map. The active player's name/empire stays visible in normal play.
- Each player uses the existing map and management screens. End Turn becomes Finish Player Turn in hot-seat mode. Finishing locks that seat against further edits for this round and shows the next handoff. Double clicks and keyboard repeats must not skip a seat or resolve twice.
- After the last surviving human finishes, the game resolves one galaxy turn. The shared display remains covered while switching owners or performing simulation work.
- Research, colonization, Council and diplomacy choices show the owning player's handoff before displaying the decision. Bombardment, espionage and sabotage also require the correct human's actual choice. No console input or automatic answers substitute for human decisions.
- Combat is automatic. Results, scouting, technology discoveries, construction and alerts are delivered only to their owning human. Read-only reports may be queued for the next appropriate viewing session; decisions pause resolution until answered.
- An eliminated seat stops receiving planning turns. Elimination of the currently viewed empire does not end other humans' play. The finished-match screen lists each human's outcome.
- Save/Load and Continue preserve who has finished, whose handoff or decision is next, pending reports, and the simulation position. Loading a hot-seat game opens the handoff screen before showing private content.

## Architecture and invariants

### Session and turn controller

Add persisted hot-seat session state alongside `ControllerRegistry`: seat order, round number, finished seats, active/pending seat, and planning/resolution/handoff/finished state. Use a focused controller to validate transitions. The registry remains authoritative for human versus AI ownership.

Planning uses the existing local model-backed order semantics in seat order. This milestone does not promise simultaneous isolated planning snapshots or ready/unready revision editing. Movement and production still advance together only once per galaxy turn. Human-to-human diplomacy must queue owned responses rather than invoking an AI response or displaying the other player's screen without a handoff.

Route both button and keyboard end-turn paths through this controller. The ordinary `GameSession.nextTurn()` rejects rostered sessions, so hot-seat resolution must drive the extracted Phase 1 turn path instead.

### Viewer activation and privacy

Introduce a dedicated viewer activation operation, separate from debug player swapping. It updates the desktop's viewing empire without transferring AI settings or changing controller ownership. Preserve per-seat map position and selection where safe; clear cached overlays, dialogs, hover details, reports, pending UI callbacks and histories that could expose the previous seat. Refresh visibility-dependent screens before revealing the new view.

The handoff screen must cover the whole application and capture mouse and keyboard input. Escape, shortcuts, help overlays and back navigation cannot reveal a previous player's content. Debug empire switching, autoplay and VIP mutation paths must be disabled in hot-seat mode unless explicitly adapted to the same ownership checks.

Audit all enabled strategic controls against the active seat: fleets and rally points, transports, colony allocations and reserve/tax controls, research, designs, spies, diplomacy and governor settings. Preserve existing legal-target rules and reject edits to another seat's assets. Unsupported controls must be visibly disabled with a brief explanation and recorded in the action inventory; silent no-ops are unacceptable.

### Decisions, events and threading

Graphical providers consume Phase 1 decision records and submit through the existing owner/decision-ID validation. Adapt existing panels when their owner assumptions can be removed; otherwise use focused local decision dialogs. Recheck owner and decision ID when a callback returns so a stale panel cannot affect a different seat.

One simulation worker owns resolution. Swing changes run on the event-dispatch thread. The UI must remain responsive while the worker waits for a human. Existing synchronous invasion providers may wait for a UI response on the worker; they must never block the Swing event thread or pretend to be serializable mid-call.

Queue recipient-specific read-only reports as persisted data, not Swing components or transient legacy notification objects. Repeated delivery attempts must not repeat model effects or consume another seat's reports. Existing viewer-specific GNN/advisor panels remain suppressed until adapted.

### Save and recovery

Support saving during planning and at the existing recoverable decision/phase boundaries. Persist the hot-seat scheduler and report state atomically with the game and RNG. Restore transient UI/providers before continuing. Planning saves do not masquerade as an in-progress resolution checkpoint.

During an active synchronous choice or model operation, disable save with a user-facing instruction to finish the choice first. On resolution failure, retain a recoverable prior checkpoint, show an actionable error behind the privacy screen, and never retry partially applied model work. Ordinary single-player saves remain compatible; test-harness rostered saves without hot-seat metadata are not silently converted.

## Likely integration points

New focused classes belong under `rotp.multiplayer.hotseat` and `rotp.ui.multiplayer`. Integrate with `SetupGalaxyUI`, `MainButtonPanel`, keyboard end-turn handlers, `RotPUI`/`MainUI`, `GameSession`, a dedicated galaxy viewer operation, and the existing turn/decision adapters. Persist per-seat UI state separately from simulation ownership. Avoid unrelated game-rule changes.

## Acceptance gate

1. From the normal UI, create two human empires and an AI, without a console command. Both humans can issue fleet, colony, research, design, spy and diplomatic orders supported by the milestone.
2. Complete several rounds with actual handoffs. Finishing the first human does not advance the galaxy; finishing the last advances it once. Repeated clicks, keys and stale callbacks are harmless.
3. Exercise the strategic action inventory and every enabled prompt family for both humans, including human-to-human diplomacy. Verify ownership and visible disabled states for any restriction.
4. Seed distinct private information and check maps, reports, overlays, hover text, navigation history and keyboard paths before and after handoffs. One player's notifications never appear for the other.
5. Save/reload after the first player finishes, during another player's planning, and at a recoverable decision. Restore the same seat, orders, pending reports, game state and RNG continuation without repeated effects.
6. Cover elimination of the viewing player, AI turns, automatic combat and a completed match. Verify the correct per-seat outcomes and continued play for survivors.
7. Run native Java 17 unit/integration checks, the existing Phase 1 suite and original-revision single-player comparisons. Perform a real graphical hot-seat smoke test and provide exact launch/setup steps. A packaged/launchable build matching the changes is part of delivery, not merely compiled source.

## Scope boundary

No LAN transport, remote clients, manual tactical combat, headless startup, seat passwords or general mid-call crash recovery in this milestone. The full Phase 2 gateway/private-view requirements remain open. The milestone is complete only when local humans can actually play through the supported desktop flow; a coordinator and tests alone do not satisfy it.
