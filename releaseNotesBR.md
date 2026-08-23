[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

$${\color{red}Warning}$$
To minimize the risk of problems, JAR and EXE files must be placed in their own directories.
When updating, you can reuse the same folder.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-08-23 (BR)
- Fixed a display bug in the transport panel, which could display, with a negative time, a transport previously arrived at its destination.

26-08-22 (BR)
- Fixed the issue where the player could not threaten other empires.
  - This was due to the player's diplomat AI selection. In fact, some AI does not bother the player with incessant threats and deactivates this functionality. The new selected AI will not hesitate to offer this option again.
- When a planet with an artifact is discovered, and the technology found is an ongoing research, the new choice become final, as the current allocation is given to it. To avoid this inconvenience, the current search will only be returned if no other tech is available.
- Fixed the attacker not always retreating when the fight reached the maximum number of turns.
- Fixed an incorrect identification of the missiles' source; the AI did not realize they were coming from the planet and was trying to dodge them instead of approaching the planet to bomb it.

26-08-20 (BR)
- Fixed Development limit default and limit value: from 100% & 100% to 50% & 80%
  - Development limit: Maximum development level defining a colony as new.
  - Colonies above this limit will not receive funding as "new Colonies"
  - Colonies below this limit will not be taxed as they could receive funds as “new colonies”. (Which would result in wasted funds as taxation involves 50% losses)


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
