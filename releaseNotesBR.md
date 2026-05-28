[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-05-28 (BR)
- Fixes (attempt) to prevent a display offset bug.
- Fix in “MoO1 Asteroids”: Missiles were slowing down above the asteroids (this was a forgotten debugging feature!)

26-05-25 (BR)
- The option to automate space battles, added on May 26, 2023, has been moved to the remnant.cfg file. (You may need to reset your preferences.)
- During a space battle, a ship targeted by missiles will display a missile warning in the information panel, and incoming missiles will be highlighted with an animation.

26-05-23 (BR)
- Fixed a bug that caused retreating ships to leave the victorious faction in certain cases.
  - This occurred when the retreating ships had never engaged in combat and none of the ships that had participated in the battle had retreated.
- Fixed weapon action text that overflows the boxes.
- New option to tell the combat automation to not target a colony that doesn't have missile bases.
  - Later, when selecting the automation, you can change the default value. You can force a “Yes” by pressing the “Shift” key, or force a ‘No’ by pressing the “Control” key.

26-05-21 (Xilmi)
- Fix in retreat logic
  - Ships that still can attack enemies with specials that deal no damage but not anything else will no longer want to stay in combat.
- Bio-bomber-fix
  - Bio-bombers shouldn't retreat if they can inflict damage to colonies with missile-bases.


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
