# In-process turn seam

## Launch the Phase 1 console exercise on Windows

From the repository root in PowerShell, build the full JAR and launch it with the `phase1` argument:

```powershell
& 'C:\Users\david\AppData\Local\Temp\rotp-phase0-tools\apache-maven-3.9.16\bin\mvn.cmd' '-Dmaven.antrun.skip=true' '-DskipTests' package
java -Xmx4g -jar 'target\rotp-Fusion-2026-09-30.jar' phase1
```

Keep the PowerShell window open. The harness starts a tiny galaxy with `human-1` on empire 0, `human-2` on empire 1, and empire 2 controlled by AI. Enter `turn` to advance one turn, `status` to inspect the match result, or `quit` to exit. Decision prompts and notices print the owning player ID. The game window initializes the existing engine; use the PowerShell console for this exercise. This is one process with two simulated human providers, not a LAN game or a second playable UI. The full JAR is required when the media conversion step is skipped; the mini JAR omits the original image assets.

`GameSession.advanceInProcessTurn()` is an experimental entry point for a rostered match. Install a `StrictInProcessNotificationSink` and any synchronous decision providers the match may encounter before calling it. The entry point runs complete named phases and returns a `TurnCheckpoint` at an owned decision, at the end of the turn, or when the match has ended.

Create two `PlayerSeat` entries with distinct player IDs and empire IDs before `startGame(options, registry)`. The factory now gives every human empire its initial colony allocations and first decision pass. Startup notices stay queued until the in-process sink is attached. Use `StrictInProcessNotificationSink.deferredDecisions(...)` with separate information providers keyed by player ID. `InProcessMatchDriver(session, seatDecisionsByPlayerId).advanceOneTurn()` then routes deferred Council, research, colonization, and diplomacy replies to the owning provider and returns after one turn. It requires a provider for every human seat. The map keys are player IDs, not empire IDs. Attach synchronous espionage, sabotage, and bombardment adapters separately when those events are possible.

For a Council barrier, inspect `TurnCheckpoint.councilVote()` or `councilRuling()`. Submit the owner's reply with `DecisionRouter.submitCouncilVote(...)` or `submitCouncilRuling(...)`, then call `advanceInProcessTurn()` again. The router advances intervening AI choices. The phase cursor remains at `COUNCIL` while waiting, so movement and production are not repeated by this in-memory continuation.

The strict sink sends scouted system IDs, constructed ship counts, read-only technology results, and diplomatic messages to providers keyed by the owning player ID. It applies accepted or declined trade, peace, pact, alliance, and joint-war offers through the existing diplomat methods. `StrictInProcessNotificationSink.deferredDecisions(...)` instead leaves research, colonization, and diplomatic offers for separate replies. Read `researchChoices()`, `colonizationChoices()`, and `diplomacyChoices()` from the checkpoint. Submit research through `ResearchDecisionRouter`, colonization through `GameSession.answerColonizationDecision(...)`, and an offer through `GameSession.answerDiplomacyDecision(...)`; then advance again. The answer methods return false for stale or wrong-owner replies. Espionage still needs a synchronous provider.

Bombardment also needs a synchronous `InProcessBombardmentDecisionAdapter` before a rostered turn reaches invasions. Its provider chooses skip, full bombardment, or target bombardment for the attacking human. The strict sink's bombardment provider receives a result for each human attacker or defender after the invasion phase; use the sink constructor or `deferredDecisions(...)` overload that accepts bombardment providers. An automatic bombardment does not call the attack-choice provider.

Sabotage needs a synchronous `InProcessSabotageDecisionAdapter` for a human spy owner. Its decision lists legal systems for factories, missile bases, and rebellion; a null choice cancels the mission. The strict sink's sabotage provider receives that owner's mission result after the invasion phase. Install both providers before advancing into that phase.

Delivery runs after the model phase and removes each successful notice. If a missing provider rejects a notice, attach it and call `advanceInProcessTurn()` again; completed model work is not repeated. Notices created by a deferred reply are delivered before the next model phase. An exception inside model work, or a provider that mutates state before throwing, is not safely retryable yet.

Rostered alerts are available as serialized `AlertRecord` values through `GameSession.alertRecordsForEmpire(empireId)`. The records contain only that empire's recipient text and system ID; the single-player alert objects remain on the desktop path.

`TurnCheckpoint` is a model snapshot, not a general recoverable save envelope. `saveDecisionBarrier(file)` accepts a completed phase with at least one pending Council, research, colonization, or diplomatic decision and no transient notifications or alerts. Rostered alerts are stored as records and may be present in that save. `restoreDecisionBarrier(file)` restores that trusted local save, checks the exact pending checkpoint and colony fleet references, and restores the simulation RNG. The caller then reattaches the strict sink and synchronous providers, submits the owner-specific response, and advances again. The older `saveCouncilBarrier` and `restoreCouncilBarrier` entry points remain restricted to Council decisions. This guarded restart path is exercised by the automated recovery scenarios below. Partially answered Council rulings and completed final-war coalitions are covered by the acceptance suite. Active phases, pending notification delivery, and ordinary desktop saves are not supported restart points. The normal desktop turn loop remains the single-player path.

`saveInProcessCheckpoint(file)` and `restoreInProcessCheckpoint(file)` also accept a completed phase boundary without a pending decision. They use the same transient-message guard, exact cursor comparison, canonical colony references, and RNG restoration. Reattach all transient providers after restore, then call `advanceInProcessTurn()` to continue from the next phase. A crash inside active model work still has no safe continuation point.

Call `advanceInProcessPhase()` to stop after one named model phase and its queued notices have been delivered. The returned `TurnCheckpoint.lastCompletedPhase()` names the boundary where `saveInProcessCheckpoint(file)` may run. If that phase produced an owned decision, answer it before advancing to the next phase. After a failure inside active model work, restore the most recent saved boundary; the partially executed in-memory phase cannot be replayed safely.

`InProcessMatchDriver` now advances through these individual phase boundaries. Its optional boundary observer receives each checkpoint before the next phase or owned reply and may write a guarded checkpoint. After a reply at the last phase creates another notice, `deliverInProcessNotifications()` drains it without starting a second turn. The driver uses that operation before returning the completed turn.

Rostered turns currently omit advisor and GNN news panels because their text is built using the local desktop player's perspective. The simulation code that would produce those notices still runs. Recipient-safe news is part of the remaining notification extraction.

## Automated Phase 1 acceptance scenarios

Run from the repository root in a desktop graphics environment, with a native JDK 17 on PATH. This acceptance run used the portable JDK below:

```powershell
$env:JAVA_HOME = Join-Path (Get-Location) 'target/phase1-tools/jdk-17.0.20.1+1'
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
& 'C:\Users\david\AppData\Local\Temp\rotp-phase0-tools\apache-maven-3.9.16\bin\mvn.cmd' '-Pphase1-acceptance' '-Dmaven.antrun.skip=true' test
```

This copies game assets and runs the unit tests plus 72 `InProcessRecoveryTest` cases (79 tests total). After assets have been copied, add `-Dmaven.resources.skip=true` for subsequent code-only runs. Use the acceptance profile so engine singletons and hidden Swing components are isolated in their own JVM. Ordinary `mvn test` leaves the desktop-dependent integration class disabled.

The suite uses temporary settings and save directories, a fixed galaxy seed, and distinct human decision providers. It compares real saved/restored phase boundaries and research, colonization, diplomacy, Council-vote, and Council-ruling barriers against uninterrupted execution. Council cases cover acceptance and defiance with rebels, immediate victory, Realms Beyond, and no-alliance settings. It also exercises automatic ship combat, ground capture leading to a military finish, and continued play after the viewing empire loses. Assertions compare selected gameplay state and the next RNG value; they are not a complete save-field equivalence proof.

After a fresh Maven test build of the current sources, run `& tests/compare-single-player.ps1` to compare three desktop turns, research completion and its next choice, colonization, automatic combat, diplomacy, military victory and a separate Council victory with original revision `1dfe8bfb17cecffcb4ed8b99ded4179f63d26f41`. The script needs `java`, `javac`, and `git` on PATH. It exports original versions of modified tracked Java sources under `target`, compiles an original-class overlay using the existing build and dependencies, and launches each probe in a separate JVM with a 180-second limit. Each run retains logs and state snapshots in its printed evidence directory. Rerun Maven after source edits: the script uses existing compiled classes and does not detect stale builds. The overlay is intended for this known baseline and change set, not arbitrary historical revisions. No checkout or index changes are made. Animations are disabled and startup/options/galaxy randomness is seeded; debug presentation suppresses interactive prompts and GNN. The check compares selected gameplay state and sixteen subsequent RNG values after each scripted ending, not every game field or every branch in the scenario catalogue.

The suite also covers partial responses, five-turn continuation, all five offer families with accept/refuse/stale replies, recipient isolation, AI bombardment, alliance and Council endings, and pending colony/research/empire work across recovery. It exposed and now guards lost production state, incorrect AI ownership, Council outcomes and persistent RNG consumption during load. See [the acceptance ledger](phase1-acceptance.md) for findings, evidence and scope limits.

## Manual Phase 1 acceptance pass (developer checklist)

Use a small galaxy with two human seats and at least one AI. Record each provider invocation with its player ID, decision ID, empire ID, and turn. Check that neither provider receives an event for the other seat. Drive both empires through a research selection, colonization, a diplomatic offer and reply, a Council vote or ruling, automatic ship and ground combat, and a match ending. For each decision type, try the other seat's ID and a repeated decision ID through the router; both must leave the model unchanged. At one deferred decision, save with `saveDecisionBarrier`, restore with `restoreDecisionBarrier`, reattach providers, answer it, and confirm the preceding production and movement do not occur again. Compare a single-player turn using the same options and input for unexpected rule changes. The in-process driver does not submit strategic orders or provide a client UI; those belong to Phase 2.
