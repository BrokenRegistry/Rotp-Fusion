[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

$${\color{red}Warning}$$
To minimize the risk of problems, JAR and EXE files must be placed in their own directories.
When updating, you can reuse the same folder.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-10-09 (BR)
- Extended the maximum screen size percentage to attempt to resolve a player's issue.

26-10-09 (Xilmi)
- Faster AI-ships now kite slower ships
- Assign more weight to maneuverability
  - Under Moo1 ruleset maneuverability is quite a bit bigger and AI would often design ships with only 1 combat-speed

26-10-09 (BR)
- Updated Advisor for Ecology allocation and Defense Allocation...

26-10-08 (BR)
- Fixed possible crash on new colony.
- Fixed null pointer exception due to concurrent modification while computing the total production.
- New option to make transport ships awesome again.
- Fixed the Gauntlet event disrupting colony reserve management.
  - The enormous loss of income resulting from this event triggered a reaction from the governor who attempted a readjustment of allocations, intended in principle to keep the colony clean and deliver the promised ship... This readjustment was not planned for events of this origin, nor of such magnitude, nor so early in the progress of the turn...
- Fixed reference point for reporting population growth or loss. It's not normal that after the Gauntlet event, all the arrows are green!
- Partial deactivation of left widgets during turn processing: If the player loses a colony while a widget is interrogating it, this could crash the game...
- Minor improvements to the governor and advisor.

26-10-06 (BR)
- Using the "Transfer" button will now mark the budget as invalid, the budget will then be updated, based on the player's settings.
- Incoming transport will be taken into account for subsidies.
- Improved advisor prevision for ecology spending.


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
