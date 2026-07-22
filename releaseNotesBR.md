[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

$${\color{red}Warning}$$
To minimize the risk of problems, JAR and EXE files must be placed in their own directories.
When updating, you can reuse the same folder.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-07-22 (BR)
- Fixed an issue where the governor spending analysis could be started concurently by a screen refresh and a mouse click... Resulting in colony display glitch.
- Added options to set the limit of fund accumulation. (Absolute and relative)
  - When funds collected exceed funds spent, at what point does the governor stop collecting?.
  -The maximum of the absolute and relative value will be collected.
- Fixed the description of fund limits: it is always the maximum of the absolute and relative value.
  - Then you may choose your favorite method, and set the other to 0...
- Shipyard: If there is a building limit and the amount of BC is already enough to build them (due to new technology), you no longer need to spend a tick to get your ships.

26-07-21 (BR)
- Colony spending sliders: Press Alt to view expenses in BC.

26-07-20 (BR)
- Governor's Funding: New option to prioritize research funding into plague and supernova random events.
- Coin icon: added a separate level of transparency for each species.

26-07-19 (BR)
- In parallel with the permanent fund transfer, it is now possible to establish a budget and modify it. New tasks may be assigned to the governor to collect funds and distribute them.
- New Governor option for ho to allocate subsidies: (Gold coin on the side of the Governor button)
  - Metal color: Follow global instructions.
  - Gold color: Allocate subsidies.
  - Red arc (right click): Can draw from the player's reserve.
  - Uncircled: active until the player deactivates it.
  - Magenta circle: Active while an emergency is activated.
  - Uncircled: active until the player deactivates it.
  - Green circle: Active as long as the colony has not reached the development limit.
  - Blue circle: Active as long as a blue priority is present. (With the exception of research and shipsyard, as they do not deactivate automatically)
- New Budget tab in the Colony panel, to review the governor's choices and refine the budget for yourself.
- Right-clicking the Mandate button will open the Governor's Advanced Panel, providing access to the new funding options:
  - Activation and deactivation of the collection of funds
  - Amounts to be collected (absolute or relative)
  - Reserve for the player (absolute or relative)
  - Development limit of a colony for receiving subsidies.


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
