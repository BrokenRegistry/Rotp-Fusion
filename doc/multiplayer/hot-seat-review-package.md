# Native hot-seat final review package

Review the working tree, including untracked source/tests; there are deliberately no task commits. Baseline HEAD is `1dfe8bfb17cecffcb4ed8b99ded4179f63d26f41`. Existing uncommitted Phase 1 in-process multiplayer work is a dependency and must be preserved.

Spec: `docs/superpowers/specs/2026-10-01-local-hot-seat-design.md`.
Plan: `docs/superpowers/plans/2026-10-01-local-hot-seat.md`.
Ledger and rulings: `doc/multiplayer/hot-seat-acceptance.md`.
Player guide/actions: `doc/multiplayer/hot-seat.md`, `doc/multiplayer/hot-seat-actions.md`.

Primary new implementation: `src/rotp/multiplayer/hotseat/`, `src/rotp/ui/multiplayer/`. Integrations are in `GameSession`, `Galaxy`, `RotPUI`, setup/menu/save, direct UI order callbacks, diplomacy menus, report producers and frozen shared governor parameters. Tests are `tests/rotp/multiplayer/HotSeat*Test.java` plus the inherited Phase 1 suite.

Fresh full regression: `target/hotseat-full-regression.log`, 118 tests, zero failures/errors/skips. Original single-player comparison and packaged desktop smoke are being completed by the implementer; do not duplicate them or claim their result early.

Review especially ownership, stale callbacks, cover/keyboard privacy, transient worker cancellation, safe save/load boundaries and all eight decisions. This is local shared-computer play, not Phase 2 network authority. Review is read-only; do not alter code, run competing Maven builds, change Git state or dispatch other agents. Report concrete Critical/Important/Minor findings with file/line, trigger and impact. List every behavior declined for scope reasons, or explicitly say none.
