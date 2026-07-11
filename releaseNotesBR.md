[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

$${\color{red}Warning}$$
To minimize the risk of problems, JAR and EXE files must be placed in their own directories.
When updating, you can reuse the same folder.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-07-11 (BR)
- New fix for unresponsive Linux.
  - Custom species folders won't be scanned deeper than 8 levels. This is to avoid infinite loops due to symbolic links.
  - If custom species folders are set to jar or exe folder, subfolders will not be scanned. This is in case the jar file has not been placed in its own folder.
  - The default for the custom species folder will be set to "CustomSpecies". If another folder has already been selected, the choice will be kept.

26-07-10 (BR)
- New tools to track Unresponsive Linux + possible fix.
- Possible Fix for Linux infinite loop.
  - Could be a directory link looping back to itself.
  - Added directory loop detection when parsing the custom species folder.
- New debug tools to track Linux Mint and Arch crash.

26-07-09 (BR)
- New debug tools to track Linux Mint crash.

26-07-05 (BR)
- New option to prevent governors to send transports to besieged colonies, even if the troops are able to sneak in. As sometime it is preferable to conserve its population to strengthen the defenses of the colonies close to the front.
- Empire Status panel:
  - Fixed display of lists not scrolling all the way to the bottom when there were a high number of empires.
  - Fixed the empire names of the top boxes dripping onto those of the bottom boxes when scrolling through the bottom boxes.
- Fixed misplaced scrollbar in the Manage Diplomats subpanel.


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
