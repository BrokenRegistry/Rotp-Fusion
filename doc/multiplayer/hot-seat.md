# Local hot-seat play

Two or more people share one computer and take turns controlling separate empires. AI empires may fill the other slots. This is local play; it does not add network multiplayer.

To play from separate computers by passing a turn file, see [play by email](play-by-email.md).

## Start a match

For this verified Windows checkout, run from `F:\Projects-PCGames\Rotp-Fusion`:

```powershell
& .\target\phase1-tools\jdk-17.0.20.1+1\bin\javaw.exe -Xmx3g -jar .\target\rotp-Fusion-2026-10-01.jar
```

The full JAR includes the game media. Saves and settings default to its folder. Keep the JAR and your saves in a writable location. A separately installed Java 17+ can also launch it.

1. Launch the full game JAR with Java 17 or newer.
2. Choose **New Game**, select your race, and continue to galaxy setup.
3. Set the number of opponents, then click **Players: Single Player** near the bottom left.
4. Check **Human** for at least two empire slots and give each person a unique name. Leave the other slots unchecked for AI control. Click **Use Hot Seat**.
5. Start the game. Pass the computer to the named player and click **Continue** on the privacy screen.

If you change the opponent count after assigning players, reopen the player editor to check the assignments. Hot seat requires autoplay, debug autorun and ironman load restrictions to be off.

## Play a round

The current player's name appears at the top of the galaxy map. Issue orders using the normal colony, fleet, design, research and diplomacy screens. Click **Finish Player Turn** when ready. The game hides the map and names the next person. Only that person should acknowledge the handoff.

The galaxy advances once after every living human finishes. AI empires act during that shared resolution. Decisions may then request a handoff to the person who owns the choice; one handoff covers all of that person's waiting choices. Choices use the game's own screens: the Galactic Council, research selection, colonize and bombard prompts, diplomatic offers, espionage (including framing) and sabotage.

Each player's reports play on the game's own screens after their handoff, before planning unlocks: discoveries, scouted systems, ships built, diplomatic messages, bombardment and sabotage results. Automatically resolved battles and invasions, which have no single-player screen, appear in a summary. Eliminated players no longer submit turns, but can still receive their final reports.

Each seat keeps a separate map position and selected system. Do not use debug viewer switching; it is disabled for these matches.

## Saves and recovery

Use the normal menu's **Save Game** during planning. Loading or continuing a hot-seat save always opens a privacy handoff first. Finished seats, pending orders, reports, player names and the random stream are saved.

Council, research, colonization and diplomacy prompts have a **Save checkpoint** button, which writes `HotSeat-Decision.rotp`. Bombardment, espionage and sabotage occur inside an active model phase: finish those choices before saving.

`HotSeat-LastSafe.rotp` and the recent save are refreshed at supported boundaries. If a worker operation fails, the covered error screen offers **Restore last safe checkpoint** or **Return to menu**. Restore may require repeating orders or choices since that checkpoint; it never resumes a half-applied phase.

## Supported actions

See [the action table](hot-seat-actions.md). Battles resolve automatically. Human-to-human technology barter, counteroffers and threats are unavailable. Shared governor/game policy settings, plus the per-computer rally combat, rally loss, AI aggressiveness, ship-based missile and harmless-colony settings, are fixed when the match starts; per-colony governors, locks and ship requests remain available.

## Build and acceptance

The exact verified artifact, test results and desktop acceptance status are recorded in [the acceptance ledger](hot-seat-acceptance.md).
