# Local Hot-Seat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking. Native execution with one final independent review is recommended for this plan.

**Goal:** Deliver a launchable desktop game in which two or more local humans can plan, privately hand over the computer, answer decisions, resolve turns, and save/resume a campaign with optional AI opponents.

**Architecture:** Add a persisted hot-seat scheduler and a transient desktop controller around the existing Phase 1 roster and turn/decision adapters. Reuse local model-backed strategic screens with an explicit active-viewer operation, input guards and private handoffs; do not use debug player swapping. One simulation worker resolves turns, while Swing displays owner-specific decisions and reports.

**Tech Stack:** Java 17, existing Swing UI, Maven, JUnit Jupiter, existing atomic `.rotp` serialization. No new runtime dependencies.

**Spec:** [Approved local hot-seat design](../specs/2026-10-01-local-hot-seat-design.md).

## Global Constraints

- "one shared computer; two or more local humans alongside optional AI opponents; fixed seat order; automatic ship and ground combat for this milestone."
- "A shared galaxy turn resolves only after every surviving human finishes planning."
- "Human assignments remain stable throughout the campaign."
- "No console input or automatic answers substitute for human decisions."
- "Existing model-backed screens remain local-only; later remote-client work still needs the planned filtered views and command gateway."
- "No LAN transport, remote clients, manual tactical combat, headless startup, seat passwords or general mid-call crash recovery in this milestone."
- "A packaged/launchable build matching the changes is part of delivery, not merely compiled source."
- Preserve the existing uncommitted Phase 1 implementation and acceptance work. Do not reset, clean, replace the checkout, or commit unrelated changes. No commit, merge, publication or deployment is part of this authorization.
- Native Java 17 is the validation runtime. Tests use temporary settings/saves and isolated JVMs; UI-dependent tests require a graphics environment.
- Treat this as a focused local milestone. Do not mark the full Phase 2 command-gateway/private-view gate complete.

## Review Focus

1. Held Enter, double click or a callback from an earlier seat must not acknowledge the next handoff or resolve twice. Pin in Tasks 1, 2 and 4.
2. Load/Continue, dialogs, report history and keyboard shortcuts must not reveal the previous viewer before acknowledgement. Pin in Tasks 2 and 7.
3. Three humans, nonadjacent human empire IDs, or an eliminated current seat must retain valid order and skip only ineligible seats. Pin in Tasks 1, 3 and 8.
4. Human-to-human offers and synchronous invasion decisions must wait for the right person without an AI reply, deadlock or invented default choice. Pin in Tasks 5 and 6.
5. A failed write/load or interrupted resolution must leave the last safe save usable and must not resume a half-applied phase. Pin in Tasks 4 and 7.

## Existing seams and file ownership

`GameSession.nextTurn()` intentionally rejects a controller roster. Both `MainButtonPanel` and `MapOverlayNone` call it; `RotPUI` also has a benchmark route, and `VIPConsole` has a mutation route. All must be classified rather than patching only the visible button.

`Galaxy.swapPlayerEmpire(int)` exchanges AI settings and debug state. Never call it for hot-seat activation. `MainUI` stores selected/hovered sprites and map focus in session variables; resetting only the map's position is insufficient.

`InProcessMatchDriver` already supports owner providers for Council, research, colonization and diplomacy. Bombardment, espionage and sabotage use synchronous adapters. `StrictInProcessNotificationSink` supports recipient callbacks and deferred decisions. The new controller must preserve their phase/retry semantics.

New production files, grouped by responsibility:

| Files | Responsibility |
|---|---|
| `src/rotp/multiplayer/hotseat/HotSeatState.java`, `HotSeatSnapshot.java` | Persisted scheduling; immutable public snapshots and transition revisions |
| `HotSeatSetup.java` in that package | Validated named human assignments, roster creation |
| `HotSeatReport.java`, `HotSeatInbox.java` in that package | Serializable recipient reports and acknowledgement IDs |
| `HotSeatController.java`, `HotSeatDecisions.java` in that package | Transient worker, legal input, decision rendezvous and provider installation |
| `HotSeatDiplomacy.java` in that package | Owned human offer submission, supported-action checks and queued replies |
| `HotSeatPersistence.java` in that package | Planning/resolution save classification, atomic writes and guarded restore |
| `src/rotp/ui/multiplayer/HotSeatSetupPanel.java` | Names and human assignments within normal setup |
| `HotSeatPrivacyPane.java`, `HotSeatDesktop.java` in that package | Full-window handoff, viewer activation, cache reset and guarded navigation |
| `HotSeatDecisionPanels.java`, `HotSeatReportsPanel.java`, `HotSeatResultPanel.java` in that package | Legal choice UI, recipient reports and per-human final results |

Paths named only by basename below refer to these packages. Existing files should receive focused integrations; do not reorganize the large legacy UI as a separate project.

## Build and test commands

Use this shell setup from the repository root (the JDK already exists from Phase 1):

```powershell
$env:JAVA_HOME = Join-Path (Get-Location) 'target/phase1-tools/jdk-17.0.20.1+1'
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
$hotSeatMaven = 'C:/Users/david/AppData/Local/Temp/rotp-phase0-tools/apache-maven-3.9.16/bin/mvn.cmd'
```

The common focused command is:

```powershell
& $hotSeatMaven '-Pphase1-acceptance' '-Dmaven.antrun.skip=true' '-Dmaven.resources.skip=true' '-Dtest=HotSeatStateTest' test
```

Replace the test selector with the class specified by each task. Run without `-Dmaven.resources.skip=true` after resource/label edits. Do not run `clean`: the portable JDK lives under `target`. Inspect Maven's actual result if output is redirected, and keep failure logs for reproduced defects. Split integration classes by concern instead of exceeding the existing 180-second per-JVM timeout.

## Task 1: Persisted seat scheduling and repeat-safe transitions

**Files:** Create `HotSeatState.java`, `HotSeatSnapshot.java`; modify `src/rotp/model/game/GameSession.java`; create `tests/rotp/multiplayer/HotSeatStateTest.java`.

**Interfaces:**

```java
// HotSeatState, Serializable, explicit serialVersionUID
enum Stage { HANDOFF, PLANNING, RESOLVING, DECISION, FINISHED, ERROR }
HotSeatState(List<String> orderedPlayerIds, int turn);
HotSeatSnapshot snapshot();
boolean confirmHandoff(long expectedRevision);
boolean finishPlanning(String playerId, long expectedRevision, Set<String> livingPlayers);
void beginDecision(String ownerPlayerId, String decisionId, boolean recoverable);
boolean completeDecision(String ownerPlayerId, String decisionId, long expectedRevision);
void completeResolution(int turn, Set<String> livingPlayers);
void finishMatch();
void failResolution();
void coverForLoad();
// HotSeatSnapshot, Serializable; immutable defensive copies
record HotSeatSnapshot(int turn, long revision, HotSeatState.Stage stage,
    String ownerPlayerId, Set<String> finishedPlayers, String pendingDecisionId,
    boolean recoverable) implements Serializable {}
// GameSession; null state means not a hot-seat match
HotSeatState hotSeatState();
```

- [x] Write `HotSeatStateTest` with a minimal two-seat progression and repeat rejection:

```java
var state = new HotSeatState(List.of("alice", "bob"), 1);
assertTrue(state.confirmHandoff(state.snapshot().revision()));
long click = state.snapshot().revision();
assertTrue(state.finishPlanning("alice", click, Set.of("alice", "bob")));
assertEquals(HotSeatState.Stage.HANDOFF, state.snapshot().stage());
assertEquals("bob", state.snapshot().ownerPlayerId());
assertFalse(state.finishPlanning("alice", click, Set.of("alice", "bob")));
assertTrue(state.confirmHandoff(state.snapshot().revision()));
assertTrue(state.finishPlanning("bob", state.snapshot().revision(), Set.of("alice", "bob")));
assertEquals(HotSeatState.Stage.RESOLVING, state.snapshot().stage());
```

- [x] Run the focused command; record the missing-type/API failure before implementation.
- [x] Implement transitions with synchronized methods, monotonic revisions, defensive collections and strict stage/owner validation. Store the destination behind a handoff so confirmation enters either planning or a pending decision. A rejected transition makes no change. Completing a decision returns to resolution, not an extra planning turn.
- [x] Add three-seat, duplicate-ID/empty-roster, eliminated-current-seat, all-humans-lost, wrong-owner, stale decision and serialization tests. Serialize through `ObjectOutputStream`/`ObjectInputStream`; compare snapshots, then continue the round on both originals and restores. `coverForLoad()` must increment the revision, preserve pending work, and cover any private stage.
- [x] Verify the focused class passes. Persist the scheduler in `GameSession`, but do not add desktop entry points until the subsequent tasks exist.

## Task 2: Viewer activation and an opaque input-owning handoff

**Files:** Create `HotSeatDesktop.java`, `HotSeatPrivacyPane.java`; modify `src/rotp/model/galaxy/Galaxy.java`, `src/rotp/ui/RotPUI.java`, `src/rotp/ui/main/MainUI.java`; create `tests/rotp/multiplayer/HotSeatTestFixture.java`, `HotSeatPrivacyTest.java`.

**Interfaces:**

```java
// Galaxy: viewer change only; requires a rostered human empire
void activateHotSeatViewer(int empireId);
// HotSeatDesktop: all methods enforce the Swing event thread
void cover(HotSeatSnapshot snapshot, Runnable confirm);
void activateViewer(GameSession session, String playerId);
void clearPrivateUi();
boolean isCovered();
// Test fixture, package rotp.multiplayer; real engine, not mocks
static void initialize(Path temporaryDirectory);
static GameSession start(int opponents, int... humanEmpireIds);
static void onEdt(Runnable action);
```

- [x] Extract the existing deterministic hidden-frame bootstrap into `HotSeatTestFixture` without changing `InProcessRecoveryTest` semantics. `initialize` uses the existing seed 136, Low graphics, temporary config/save directories and stopped animation timer. `start(3, 0, 2, 3)` creates three humans and one AI using `startGame(options, registry)`. Enable engine classes only with `rotp.integration=true`.
- [x] Add the viewer test before implementing the method:

```java
GameSession game = HotSeatTestFixture.start(2, 0, 1);
int ai0 = game.galaxy().empire(0).selectedAI;
int ai1 = game.galaxy().empire(1).selectedAI;
long expectedRng = new rotp.util.Rand(941L).nextLong();
rotp.Rotp.rand(new rotp.util.Rand(941L));
game.galaxy().activateHotSeatViewer(1);
assertEquals(1, game.galaxy().player().id);
assertEquals(ai0, game.galaxy().empire(0).selectedAI);
assertEquals(ai1, game.galaxy().empire(1).selectedAI);
assertFalse(game.galaxy().empire(0).isAIControlled());
assertFalse(game.galaxy().empire(1).isAIControlled());
assertEquals(expectedRng, rotp.Rotp.rand().nextLong());
```

- [x] Run `HotSeatPrivacyTest` and confirm failure. Implement activation by setting the galaxy viewer and `Empire.updatePlayerId`, refreshing derived viewer caches, and leaving roster, AI choices, debug swap flags and simulation RNG unchanged.
- [x] Implement an opaque root-level privacy component above all game panels. Clear/hide legacy dialogs and governor windows before covering; route navigation centrally through the hot-seat desktop guard. Store each seat's map center/scale and selected **system ID**, not shared sprite references. Clear clicked/hovered sprites, range caches, overlays and unscoped session UI variables before activation. Reset lazy report/design/race/technology panels from the new owner before uncovering.
- [x] Add actual Swing event tests: Escape/F1/map/menu shortcuts cannot uncover; a double click cannot acknowledge a newly displayed handoff; a held Enter requires key release and a fresh press. Test return from Save/Load/Game menu, secondary dialogs, and stale callbacks from a prior UI generation. Reset per-session UI generations on load/new game.
- [x] Verify with seeded private system names and technology lists for two owners: no old selections/tooltips/report history are visible after switch. Run the focused class; record a graphical privacy smoke check for Task 8.

## Task 3: Normal setup with named human slots

**Files:** Create `HotSeatSetup.java`, `HotSeatSetupPanel.java`; modify `src/rotp/ui/game/SetupGalaxyUI.java`, `src/rotp/model/game/GameSession.java`, English labels under `src/rotp/lang/en`; create `tests/rotp/multiplayer/HotSeatSetupTest.java`.

**Interfaces:**

```java
record HotSeatSetup(List<Assignment> humans) implements Serializable {
    record Assignment(String playerId, String displayName, int empireId) implements Serializable {}
    ControllerRegistry registry(int empireCount);
}
// GameSession, separate overload so existing startGame(options, registry) stays valid
void startHotSeatGame(IGameOptions options, HotSeatSetup setup);
HotSeatSetup hotSeatSetup();
```

- [x] Write tests for named, nonadjacent assignments and fail-fast validation:

```java
var setup = new HotSeatSetup(List.of(
    new HotSeatSetup.Assignment("a", "Alice", 0),
    new HotSeatSetup.Assignment("b", "Bob", 2)));
assertTrue(setup.registry(3).isHumanControlled(2));
assertTrue(setup.registry(3).isAIControlled(1));
assertThrows(IllegalArgumentException.class, () -> setup.registry(2));
```

- [x] Run `HotSeatSetupTest` and confirm failure. Implement immutable assignments, nonblank trimmed names, unique player/empire IDs, at least two humans, and valid empire counts. Stable IDs are separate from display names. Validation happens before creating a galaxy or replacing an existing session.
- [x] Add a normal Hot Seat setup control with per-slot Human/AI assignment and player name. Preserve race selection and existing AI settings. If opponent count changes, flag invalid assignments and disable Start until corrected; never silently remove a human. Disable incompatible autoplay/debug/ironman modes with a visible explanation. Use existing label fallback conventions, not hardcoded English throughout widgets.
- [x] `startHotSeatGame` creates the roster before galaxy generation, saves setup/scheduler state, and immediately covers startup. Do not show a first-player intro/map before the handoff. Single-player and console-harness startup retain their existing behavior.
- [x] Add validation tests for blank/duplicate names or IDs, duplicate empire assignments, two humans/no AI, three humans/one AI, cancelled setup and returning to single-player mode. Verify the tests and inspect layout at the existing supported desktop scale settings.

## Task 4: One worker, one shared turn, recipient report inboxes

**Files:** Create `HotSeatController.java`, `HotSeatDecisions.java`, `HotSeatReport.java`, `HotSeatInbox.java`, `HotSeatReportsPanel.java`; modify `GameSession.java`, `src/rotp/ui/main/MainButtonPanel.java`, `src/rotp/ui/main/overlay/MapOverlayNone.java`, `RotPUI.java`; create `tests/rotp/multiplayer/HotSeatTurnTest.java`, `HotSeatInboxTest.java`.

**Interfaces:**

```java
// HotSeatController; transient, one instance per live hot-seat session
HotSeatController(GameSession session, HotSeatDesktop desktop, HotSeatDecisions decisions);
void start(); // attach providers, drain startup decisions, then first planning handoff
boolean finishPlayerTurn(String owner, long revision);
boolean canEdit(int empireId);
void resume();
void close(); // cancel worker/pending UI, invalidate callback generation
// HotSeatDecisions: typed CompletableFuture responses
CompletableFuture<Integer> councilVote(PendingDecision decision);
CompletableFuture<Boolean> councilRuling(PendingDecision decision);
CompletableFuture<String> research(PendingResearchDecision decision);
CompletableFuture<Boolean> colonize(PendingColonizationDecision decision);
CompletableFuture<Boolean> diplomacy(PendingDiplomacyDecision decision);
CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> bombardment(BombardmentDecision decision);
CompletableFuture<InProcessEspionageDecisionAdapter.Choice> espionage(EspionageDecision decision);
CompletableFuture<InProcessSabotageDecisionAdapter.Choice> sabotage(SabotageDecision decision);
// Serializable report: recipient-formatted strings/IDs only, no UI/model objects
record HotSeatReport(long id, int turn, int recipientEmpireId,
    String kind, String title, List<String> lines) implements Serializable {}
// HotSeatInbox, persisted by GameSession; IDs monotonic across saves
long append(int turn, int owner, String kind, String title, List<String> lines);
List<HotSeatReport> unread(int owner);
boolean acknowledge(int owner, long reportId);
```

- [x] Add inbox tests before implementation:

```java
var inbox = new HotSeatInbox();
long id = inbox.append(1, 0, "SCOUT", "Scouting", List.of("Alice's discovery"));
assertTrue(inbox.unread(1).isEmpty());
assertFalse(inbox.acknowledge(1, id));
assertEquals(1, inbox.unread(0).size());
assertTrue(inbox.acknowledge(0, id));
assertFalse(inbox.acknowledge(0, id));
```

- [x] Write `HotSeatTurnTest` with controllable `CompletableFuture` providers. Assert initial notification delivery does not advance a turn; first Finish only changes seat; last Finish starts exactly one worker and one galaxy turn. Repeated finish calls during resolution return false. Record real fleet movement, production and both seat callbacks.
- [x] Run both classes to confirm failure. Implement the worker with a single-thread executor and an atomic running flag. Only worker code advances model phases; UI input is disabled during resolution. Startup drains initial notifications and decisions without advancing a new round. Use existing driver behavior/phase boundaries; do not duplicate model phase operations.
- [x] Install `StrictInProcessNotificationSink.deferredDecisions` with per-owner inbox append providers and the three synchronous invasion adapters. Copy actual values before model objects change. Bridge existing alerts and automatic combat/invasion result notices into the inbox without exposing unrelated participants' data. Successful append followed by sink consumption is one worker operation; failed delivery must not replay an already consumed notice.
- [x] Route main button and map keyboard next-turn actions through `finishPlayerTurn`; block benchmark and VIP turn routes in hot-seat mode. Display Finish Player Turn plus current player name. Guard old callbacks with controller generation plus snapshot revision. Preserve the ordinary desktop branch.
- [x] Add this worker-only rendezvous to `HotSeatController`; the panel implementations return immediately with a future and schedule their visual work on the EDT. Register the owned pending state before this call, route its answer through the existing validator afterward, and clear pending state only on acceptance:

```java
private <T> T awaitAnswer(java.util.function.Supplier<CompletableFuture<T>> request)
        throws InterruptedException, java.util.concurrent.ExecutionException {
    if (SwingUtilities.isEventDispatchThread())
        throw new IllegalStateException("Resolution must run on the worker");
    return request.get().get();
}
```

Cancellation, interruption and exceptional completion propagate to the worker's failure/close path. Restore the interrupt flag on interruption, cover the UI, and do not convert failure into an accept/refuse/skip choice. Null is valid only for Council abstention and deliberate sabotage cancellation, as defined by their adapters.

- [x] Add tests for cancelled window/session, provider failure, owner eliminated during resolution, and incoming notification while a different owner is active. Verify no second worker, no editable map during model work, no lost report after serialization, and no continuation after an active-phase exception.

## Task 5: Real graphical decisions for every enabled prompt

**Files:** Create `HotSeatDecisionPanels.java`; integrate `HotSeatDesktop.java`, `HotSeatController.java`; create `tests/rotp/multiplayer/HotSeatDecisionTest.java`.

**Interfaces:** `HotSeatDecisionPanels implements HotSeatDecisions` from Task 4. Each method returns a future completed only by that owner's explicit legal action. The controller owns a fresh UI generation and persisted decision ID; panels do not advance the model themselves.

- [x] Write graphical tests with a real panel and incomplete future, for example:

```java
CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> answer =
    panels.bombardment(new BombardmentDecision("b", 1, 2, systemId, false, 0));
assertFalse(answer.isDone());
assertTrue(desktop.isCovered());
// Dispatch a fresh Continue action on the EDT, then the Skip button action.
// Assert the resulting future is SKIP and no bombardment was applied by the panel.
```

- [x] Run `HotSeatDecisionTest` and confirm failure. Build owner-labeled dialogs on the EDT behind the handoff. Use lists of legal IDs from decision records. UI labels resolve from the active owner's knowledge; panels must not enumerate secret data from the whole galaxy.
- [x] Implement Council candidates/abstain and accept/defy, research legal technologies, colonize/decline, diplomacy accept/refuse, bombard/skip/allowed target bombing, espionage tech plus optional legal framing, and sabotage action/system/cancel. Do not show unavailable choices or supply a default response when the dialog closes.
- [x] Submit deferred replies through `DecisionRouter`, `ResearchDecisionRouter` and the existing `GameSession.answer*Decision` methods. Synchronous adapters validate their returned choice as they already do. Illegal/stale replies leave the decision pending, display a concise explanation, and rebuild from current legal state; they never restart completed model work.
- [x] Parameterize tests over both human owners and all eight methods. Exercise wrong-owner callback, obsolete generation, double submit, window close, target bombard disabled, illegal technology/frame/target, and deliberate sabotage cancellation. Use existing real mission fixtures to verify model outcomes once, not only future completion.
- [x] Verify handoffs between two decisions in one resolution, including a player who already finished planning. Check that closing a decision neither silently answers nor reveals the previous map; exiting the session explicitly cancels the worker and preserves the prior safe save.

## Task 6: Active-owner strategic controls and human diplomacy

**Files:** Create `HotSeatDiplomacy.java`; modify `FleetPanel.java`, `RallyPointPanel.java`, `TransportPanel.java`, `TransportDeploymentPanel.java`, `EmpireColonySpendingPane.java` under `src/rotp/ui/main`; `ShipRelocationSprite.java`, `SystemTransportSprite.java` under `src/rotp/ui/sprites`; `MultiColonySpendingPane.java`, `TransferReserveUI.java` under `src/rotp/ui/planets`; `AllocateTechUI.java`, `DesignUI.java`, `ConfirmCreateUI.java`, `ConfirmScrapUI.java` in their existing UI packages; `ManageSpiesUI.java`, `RacesDiplomacyUI.java`; `GovernorOptionsPanel.java`; diplomacy menu classes under `src/rotp/ui/diplomacy`; relevant model entry points identified by `doc/multiplayer/action-inventory.md`; create `tests/rotp/multiplayer/HotSeatOrdersTest.java`, `HotSeatDiplomacyTest.java`.

**Interfaces:**

```java
// HotSeatDiplomacy; actor is explicit, never inferred from the currently open dialog
enum Offer { TRADE, PEACE, PACT, ALLIANCE, JOINT_WAR }
enum Result { QUEUED, WRONG_OWNER, ILLEGAL, DUPLICATE }
Result submit(String playerId, long revision, Offer offer, int recipientEmpireId,
              int tradeLevel, Integer jointWarTargetId);
```

- [x] Create an action-coverage table in `doc/multiplayer/hot-seat-actions.md` from every inventory row. Include mouse, keyboard, bulk-edit and alternate entry paths; list exact model mutation sites. Initially mark rows unverified, then replace each with test/evidence and supported or visibly disabled status during this task. Essential fleet/colony/research/design/spy/governor controls cannot all be disabled to satisfy the table.
- [x] Add parameterized real-engine tests for each enabled family using both owners. Capture both empires before invoking an actual UI action: legal active-owner changes succeed; enemy/finished-owner/stale-panel changes leave both unchanged. Exercise invalid allocations, exhausted reserves, unreachable destinations and nonexistent entity IDs without partial edits.
- [x] Implement UI mutation guards using the active controller and the entity owner. Keep the authoritative worker's automatic operations separate; a UI phase guard must not prevent AI/model resolution. For methods callable from multiple interfaces, use an explicit UI wrapper with owner and generation rather than relying solely on disabled buttons:

```java
if (session().hotSeatState() != null && !controller.canEdit(entityOwnerId)) {
    misClick();
    return;
}
// Existing model validation and mutation follow only after this check.
```

- [x] Audit shared governor options. `GovernorOptions` reads global `IGovOptions` parameters, so separate instances alone would not isolate policies. In this milestone, freeze those shared policy parameters at the match's setup defaults and visibly disable the shared governor-policy editor during hot-seat play. Preserve per-colony governor enable/disable, allocation locks and requests stored on the colony. Disable alternate settings/VIP paths to the frozen parameters too. Add a two-seat test with different colony governor toggles, run production, save/reload and verify one seat cannot change the other's toggle or the frozen policy. Record this specific restriction; per-seat editing of global governor policies is outside this first version.
- [x] Queue the five supported human-to-human offers as persisted owned decisions. `DiplomacyTreatyMenu` currently calls `diplomatAI().receiveOffer*` directly; intercept human recipients before that route. Revalidate contact, treaty eligibility, trade amount, joint-war target and seat revision when queued and answered. AI recipients keep their ordinary diplomat path. Apply each accepted offer once through the existing model action path and send each participant its own result.
- [x] Test each offer with accept/refuse/stale cases, including a recipient who has already finished planning. Do not change a treaty until acceptance. One-way war/treaty-break and aid actions must notify the correct recipient. Disable unsupported human technology barter/counteroffers, threats, debug switching, autoplay, manual tactical controls and VIP mutation with visible explanatory text; retain their existing single-player behavior. Record every restriction in the action table and player guide.
- [x] Run both focused classes and check all action-table rows. Add tests for shared UI settings that affect gameplay (especially governor/spy budgets) instead of assuming changing `PLAYER_ID` isolates them.

## Task 7: Planning saves, decision saves and safe load/Continue

**Files:** Create `HotSeatPersistence.java`; modify `GameSession.java`, `HotSeatController.java`, `src/rotp/ui/game/SaveGameUI.java`, `LoadGameUI.java`, `GameUI.java`; create `tests/rotp/multiplayer/HotSeatRecoveryTest.java`.

**Interfaces:**

```java
// HotSeatPersistence, using existing atomic GameSession save machinery
static boolean canSave(GameSession session);
static long save(GameSession session, File destination) throws Exception;
static GameSession load(File source) throws Exception;
```

- [x] Write recovery tests before implementation. Save after Alice finishes, while Bob edits, and at a partially answered Council/research barrier. Compare scheduler, reports, orders, selected model state and the next RNG values against uninterrupted play:

```java
HotSeatSnapshot savedState = game.hotSeatState().snapshot();
HotSeatPersistence.save(game, file);
long expectedRandom = rotp.Rotp.rand().nextLong();
GameSession restored = HotSeatPersistence.load(file);
assertEquals(savedState.finishedPlayers(), restored.hotSeatState().snapshot().finishedPlayers());
assertEquals(HotSeatState.Stage.HANDOFF, restored.hotSeatState().snapshot().stage());
assertEquals(expectedRandom, rotp.Rotp.rand().nextLong());
```

- [x] Run `HotSeatRecoveryTest` and confirm failure. Serialize setup, state, frozen match governor defaults, per-seat view state, inbox, deferred planning offers and authoritative RNG with the session. Per-colony governor choices already live on colonies. Save planning state through an explicit planning envelope; use Phase 1 guards for completed resolution phases and decision barriers. Distinguish the envelope kind with a versioned enum rather than inferring it from the currently displayed panel.
- [x] Reuse Phase 1 scratch-RNG load normalization so validation cannot consume the saved stream. Reattach transient controllers/providers before resuming and install the privacy cover before any main-panel selection. Normal Load and Continue must inspect hot-seat metadata before ordinary desktop normalization/autosaving. Harness saves without hot-seat metadata remain harness saves, not an assumed two-player campaign.
- [x] Guard all manual/automatic save entry points against active model work and synchronous mission choices. Automatic save requests during unsafe work wait for the next safe boundary; manual save shows "Finish this choice before saving." Save/Load worker coordination cannot deadlock against an EDT dialog.
- [x] Maintain a last-safe checkpoint before resolution and at supported boundaries. Failures cover the UI and offer return to menu or restore-last-safe-save. Never call `advanceInProcessPhase()` again on a failed active phase. Cancel transient callbacks before replacing sessions.
- [x] Inject a failed write and malformed/unsupported save. Verify the original file remains readable, the prior active session/RNG/options survive a failed load, and the UI remains covered. Test ordinary single-player save loading, persistent RNG with randomized personalities, three seats with one eliminated, and closed/reopened application through the normal Continue route.
- [x] Verify the focused class and record save restrictions in the player guide.

## Task 8: Outcomes, end-to-end acceptance and playable artifact

**Files:** Create `HotSeatResultPanel.java`, `tests/rotp/multiplayer/HotSeatAcceptanceTest.java`, `doc/multiplayer/hot-seat.md`, `doc/multiplayer/hot-seat-acceptance.md`; finish integrations in `GameSession.java`, `RotPUI.java`, `GameUI.java`; update roadmap links without marking Phase 2 complete.

**Interfaces:** `HotSeatResultPanel.show(MatchOutcome outcome, HotSeatSetup setup)` displays every human's result. No legacy single-player loss panel may replace an ongoing rostered match.

- [x] Write end-to-end tests that use production hot-seat controllers and deterministic human provider responses to complete five rounds, then an arranged military or Council ending. Verify each seat's real orders and choices, AI activity, one advancement per round, eliminated-seat skipping and per-seat outcomes.
- [x] Include two humans/no AI, three humans/one AI with nonadjacent IDs, local viewer eliminated first, all humans lost while AIs survive, and late reports for an eliminated human. Deliver final private reports through an owner handoff before the public per-seat result screen. A report must not reveal unobserved opponent technology or private orders.
- [x] Implement the result route and run the focused class. Then perform one full native Java 17 validation pass:

```powershell
& $hotSeatMaven '-Pphase1-acceptance' '-Dmaven.antrun.skip=true' test
& tests/compare-single-player.ps1
git -c core.safecrlf=false diff --check
```

Expected: existing 79 Phase 1 cases plus new hot-seat cases pass with zero failures; original single-player gameplay/RNG comparisons match. Do not run the baseline probe against stale classes. Fix reproduced failures and rerun the affected checks; rerun the full suite after gameplay changes.

- [x] Package after tests pass. Preserve assets and use the full shaded JAR (the mini JAR omits original images when conversion is skipped):

```powershell
& $hotSeatMaven '-Dmaven.antrun.skip=true' '-DskipTests' package
Get-ChildItem target -Filter 'rotp-*.jar' | Select-Object Name,Length,LastWriteTime
```

Select the actual full artifact emitted by this invocation, verify manifest/resources, record its absolute path and SHA-256, and launch it with the native Java 17 executable. Do not guess the versioned filename or deliver an older JAR. Do not publish a release.

- [x] Perform a real graphical smoke test from that artifact: New Game → Hot Seat → Alice/Bob plus an AI; deploy a scout/change colony and research spending for Alice; finish; verify privacy; issue different orders for Bob; resolve; answer real prompted choices; save on Bob's planning turn; close/reload; finish three rounds. Exercise report panels, keyboard handoff protection, window resize, menu navigation and the supported action table. Use the computer-use skill before UI automation; if the environment cannot operate Swing, report that specific limitation and provide a concrete manual checklist rather than claiming a UI pass.
- [x] Obtain the one final independent review required by native execution, focused on ownership, privacy, lifecycle and save boundaries. Resolve important findings with regression evidence. Do not spawn implementation agents unless the selected execution method calls for them.
- [x] Write exact launch/setup instructions, supported actions and visible restrictions, save rules, artifact path, test counts and graphical evidence in the two docs. Mark the milestone complete only if the graphical playthrough and launchable artifact are verified. Otherwise identify the outstanding acceptance item precisely; tests and a scheduler alone do not mean hot-seat play is ready.

## Plan self-review and execution handoff

Spec mapping: setup → Task 3; seat planning and turn order → Tasks 1/4/6; handoff privacy → Task 2; every prompt and reports → Tasks 4/5/6; saves and failures → Task 7; elimination/outcomes and playable artifact → Task 8. The five Review Focus items each have explicit tests above.

The public contracts are declared at their owning tasks. Existing Phase 1 decision record types/adapters retain their real names and response types. `HotSeatTestFixture` is introduced before engine tests depend on it. UI code is not assumed to be private merely because the viewer ID changed. A focused local guard is not presented as a remote security boundary.

Execution recommendation: **Native** (implement sequentially in this session, one final independent review). The eight tasks share session lifecycle and Swing interfaces, so keeping implementation context together reduces integration churn. Subagent-driven execution remains an available alternative with per-task review and higher context overhead.

Status: COMPLETE, October 2, 2026. Native implementation, 121 passing tests, original single-player/RNG comparison, independent review fixes, four-round graphical playthrough, save/Continue/Load and corrected artifact reload are verified. See `doc/multiplayer/hot-seat-acceptance.md` for exact evidence, rulings and artifact hash. Work remains uncommitted in the approved checkout.
