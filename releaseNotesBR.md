[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

$${\color{red}Warning}$$
To minimize the risk of problems, JAR and EXE files must be placed in their own directories.
When updating, you can reuse the same folder.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-09-10 (BR)
- DNA Workshop: removed annoying beep.
  - (It was a false positive error detection, when updating the file list; as for a moment the selection was set to "null")
- Tech Panel: Improved display of total research by removing rounding errors.
- Improved display of "Production per factory". (Now, up to two decimal places.)
- Fixed potentially suboptimal industry allocation when terraforming.

26-09-09 (BR)
- Fixed label Key with empty contents.

6-09-08 (BR)
- Added another security to try to prevent the crash when a combat end due to max turn limit.
- Restored the minor fix as it was not the problem. (That I can't reproduce)
- Removed a minor fix from 26-08-22 who may crash the game...
- DNA Factory: Added some synchronization protection on save, set as Player, and exit to prevent file saving corruption.
- Added a tools to detect duplicate labels...
- Fixed a few duplicated labels!

26-09-07 (BR)
- Improved help for options "Retreat Destination", "Hyper Comm Retreat", and “Retreat toward enemy”.
- DNA Lab: 
  - Fixed tech settings counted twice.
  - Added avatar to original species
- Fixed label typo
- Refresh budget for ungoverned colonies too.
  - The optimal value and remaining funds will be up to date.
- Scouted System interface: moved the flags a little away from the “Next” button.
- All interfaces with flag:
  - removed the sound while pressing a key modifier.
  - added Auto-Flg function ("home")
- Adjusted some advice bubble location.

26-09-05 (BR)
- Scouted System: Fixed Flag linked to the wrong System.
- Minor Advice location adjustment. 

26-09-04 (BR)
- Extension of the advisor, to be able to call it on demand, this in order to display help on the element under the cursor.
  - On the Galaxy map, including some overlay windows.
  - On the colony panel.
  - Press "F1" to toggle. (Former help panel still available with "Ctrl-F1")
  - Options for Avatar size, text size, and to restore the former help panels.
  - This is a work in progress.
- Upgraded help panels to be able to display HTML text.
- Fixed potential sources of memory leak.
- Fixed potential null pointer exceptions.
- Clicking on the Empire name in the system panel will now take you to the Diplomacy panel and select the related empire. (It was already supposed to do this, but it wasn't fully functional!)


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
