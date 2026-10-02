# Consolidated Phase 1 acceptance

Plan: `docs/superpowers/plans/2026-09-30-multiplayer.md`, Phase 1. Authorized October 1 to complete the remaining coverage in one pass.

## Work checklist

- [x] Partial decisions and multi-turn checkpoint continuation.
- [x] Trade, peace, pact, alliance and joint-war accept/refuse/stale offers.
- [x] Recipient isolation for scouting, research, construction, combat, bombardment, espionage, sabotage and alerts.
- [x] Council final-war finishes, alliance victory, and human elimination combinations.
- [x] Original-revision single-player research, colonization, combat, diplomacy and victory comparisons.
- [x] Native Java 17 compilation and execution, consolidated verification and review.

## Execution decisions

- Continue in the existing authorized checkout, preserving all existing uncommitted Phase 1 work. No commit, merge or publication is requested.
- Tests exercise the existing safe phase boundaries. Synchronous invasion decisions are checked for ownership and outcomes; serializing an active call stack is outside the agreed design.
- Existing implemented behavior may pass its new coverage immediately. Production fixes require a reproduced failure before correction.
- Use deterministic small fixtures and real engine operations. Explicitly record where scheduling or event setup is arranged; this is an acceptance matrix, not exhaustive game-state enumeration.
- Pre-flight: all areas share engine singletons and providers. Keep execution sequential within a JVM, use isolated processes for baseline revisions, and temporary settings/save directories.

## Evidence and findings

### Coverage added

| Area | Acceptance evidence |
|---|---|
| Recovery | Partially answered research and Council rulings; five-turn continuation; all supported decision barriers; production, allocation and RNG state across real save/reload |
| Diplomacy | Trade, peace, pact, alliance and joint war, each accepted, refused and stale; wrong-owner and duplicate replies; resulting treaties and restored outcomes |
| Recipients | Separate human providers for scouting, technology, construction, automatic combat, bombardment, espionage, sabotage and alerts; missing-provider delivery retry does not repeat earlier notices or model work |
| Endings | Military and alliance victory, No Alliances, local-viewer elimination, all humans eliminated with multiple AIs alive, peaceful Council and completed final-war coalitions |
| Single player | Original-revision overlay and separate JVMs: ordinary turns, research completion/next choice, colonization, automatic combat, diplomacy, military and Council endings, plus RNG continuation |
| Runtime | Native Temurin Java 17 compilation and tests, using a portable JDK under `target/phase1-tools`; no system Java installation changed |

The fixtures explicitly arrange Council scheduling, offers, fleet/transport positions, projects and economic conditions, then run real engine operations. They compare a selected gameplay projection and random continuation, not every possible save field or a full campaign.

### Review and reproduced failures

One final read-only review reported four important findings and no critical findings. Each important finding was reproduced locally before its correction:

1. Unassigned AI bombardment threw because no human seat existed. The adapter now follows controller ownership, including unassigned and explicitly rostered AI attackers against human or AI defenders. Reproduction: `target/phase1-ai-bomb-red.log`.
2. Peaceful Council acceptance incorrectly awarded victory to every accepting empire. Outcomes now preserve the leader/prior-alliance rules and No Alliances. Actual final-war victories still reward the surviving winning coalition. Reproduction: `target/phase1-council-red.log`. The additional Realms Beyond check initially expected a defying prior ally to win; source tracing established that this mode also enables `noAllianceCouncil()`, so the correct original outcome is loss. The test expectation was corrected; no alliance-victory exception was added.
3. Checkpoints lost pending production/assessment work. Persisted state now includes completed colony research projects, reallocation and new-order flags, pre-production spy-budget deferral, the prior income comparison, research points awaiting the research phase, and the current ship-production estimate. An older save without that estimate computes it lazily. Reproductions: `target/phase1-recovery-red.log` and `target/phase1-recovery-fixes.log`.
4. Persistent RNG and randomized leader personalities let load normalization consume the saved simulation stream. In-process restore now normalizes with an isolated copy, then installs the untouched checkpoint stream. The ordinary desktop loading path remains unchanged. Reproduction: `target/phase1-options-red.log`.

The spy-budget reproducer also exposed a separate ownership bug: the second human could receive opponent AI components. Rostered humans now receive player automation components independently of the viewing empire. The same behavioral test first failed before saving, then failed only after restoring until the pending-budget flag was preserved (`target/phase1-human-ai-red.log`).

### Scope rulings

- The canonical Phase 1 exit gate is an **in-process** two-human decision harness. These tests still initialize hidden Swing components and require a desktop graphics environment. The earlier Phase 0 report's proposed non-graphical bootstrap is superseded by the canonical Phase 5 headless-server gate; this pass does not claim headless execution.
- Human providers exercise decisions, not a complete strategic-order editor or second playable client. Orders/private views belong to Phase 2; networking and first playable LAN follow afterward.
- Only completed phases with delivered notifications are recoverable. Bombardment, espionage and sabotage choices remain synchronous; failure inside active model work requires restoring a prior safe checkpoint.
- Recipient-safe advisor/GNN presentation, exhaustive private client views, packaging and release validation remain later-phase work. No remote game, arbitrary crash recovery, or exhaustive gameplay equivalence is claimed.

### Final verification

- `mvn -Pphase1-acceptance -Dmaven.antrun.skip=true -Dmaven.resources.skip=true test`: **79 passed, 0 failures, 0 errors, 0 skipped**. It compiled 879 production sources and ran on Temurin **17.0.20.1**. Evidence: `target/phase1-acceptance-verified.log`, completed October 1 at 19:59:52 America/Denver, and `target/surefire-reports` (including the recorded Java home/version).
- `git -c core.safecrlf=false diff --check`: passed.
- `tests/compare-single-player.ps1`: **passed** against `1dfe8bfb17cecffcb4ed8b99ded4179f63d26f41`. All scripted gameplay snapshots and subsequent RNG values match. Evidence: `target/phase1-baseline-verified.log` and `target/single-player-comparison-dae743eb291d46fb915d17765e831cd1/{baseline,current}/state.txt`. The Council fixture disables debug autorun before checking that victory stops play; debug autorun intentionally continues with surviving opponents in both revisions.
- A final native Java 17 `test-compile` also passed after the probe adjustment (`target/phase1-baseline-compile.log`). No gameplay changes followed the successful 79-case run.

**Result: the agreed Phase 1 in-process acceptance scope is complete.** All six work areas are covered, the review's important findings are resolved, and there are no remaining blockers within that scope. Phase 2 orders/private client views is the next development phase. The later-phase limits above remain explicit. No commit, merge or publication was performed.
