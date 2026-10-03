# Play by email

Two or more people play one game from their own computers by passing a turn file. Players take turns in a fixed order, like [hot seat](hot-seat.md), but each one plays at home. AI empires may fill the other slots.

## Before you start

- **Everyone must run the same game build.** A turn file records the build that wrote it. Opening it with a different build stops with a message naming both builds.
- **Decide how files travel.** Either email the `.rotp` file as an attachment, or point every player's save folder (in the game's settings) at a shared Dropbox, OneDrive or similar folder. With a shared folder, new turns simply appear.

## Start a match

1. One player chooses **New Game**, picks a race and goes to galaxy setup.
2. Set the number of opponents, then click **Players: Single Player** near the bottom left.
3. Check **Human** for each person's empire slot and type each person's name. Leave other slots unchecked for AI. Click **Use Play by Email**.
4. Start the game. The first player listed chooses a PIN and plays the first turn.

## Taking a turn

1. Open the turn file with **Load Game**. The file name says whose turn it is, for example `PBEM-20261003-1405-T012-for-Bob.rotp`.
2. Enter your PIN. The first time you open one of your turns, you choose it (4 to 12 characters). Keep it to yourself. It cannot be reset.
3. Read your reports, then plan with the normal screens.
4. Click **Finish Player Turn**. Review your standing orders (below) and click **Send turn**.
5. The game saves the next file in your save folder and says who it is for. Send it, then click **Return to menu**. If that person is sitting next to you, click **(name) is here** instead.

When the last player in a round finishes, that computer also resolves the turn for everyone, including the AI empires. If resolution raises a choice for another player, such as a research pick, a Council vote, a colonization prompt or a diplomatic offer, the game saves a file for that player and waits for them. Choices for the player at the keyboard appear straight away.

## Standing orders

Nobody is at the keyboard when bombing, spy theft or sabotage happens in the middle of a turn, so each player sets these in advance on the **Finish turn** screen. They stay in force until changed.

| Choice | Options | Starts as |
| --- | --- | --- |
| Bombard enemy colonies | Never / Always / Only at war / At war, unless my troops are landing | Only at war |
| After spying, frame another empire | Never / When possible | Never |
| Sabotage target | Factories / Missile bases / Incite rebellion | Factories |

When troops are landing and targeted bombing is allowed, bombing spares population for the invasion. Spies always steal the technology the game rates most useful. If the chosen sabotage target has no legal system, the next one in the list is used. If none is legal, the mission is cancelled. The game-wide **auto-bombard** setting does not apply in these matches.

## Fixed rules

A few settings are normally stored per computer: rally points joining combat, rally combat losses, AI aggressiveness, ship-based missiles and ignoring harmless colonies. The match fixes them at creation, together with the shared governor policies, so every computer resolves turns the same way. Loading a match applies its values to your game settings.

## Limits

- **Honor system.** The PIN stops casual peeking. Every file still contains the whole galaxy, so a determined player could read it. A player who resolves a turn could also reload and play it differently. Play with people you trust.
- Players who are eliminated do not receive their final reports. Everyone sees the result screen when the match ends.
- Space battles resolve automatically, as in hot seat. The [hot-seat action table](hot-seat-actions.md) lists what else is supported.
