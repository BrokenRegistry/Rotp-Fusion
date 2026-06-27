[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-06-27 (BR)
- Double clicking on the war view planet image will now also recenter the map.
- New option to disable the auto war view on F7/F8 activation. (User Interface --> Visual Options)

26-06-26 (BR)
- Fixed a bug occurring when double clicking on the planet image to recenter the map, when done with totally improbable timing.

26-06-25 (BR)
- Fixed Modnar species missing their special abilities. (Removed a forgotten debug line)
- Fixed Missile range English description (some range were wrong)
- Fixed Event messages and UI display, when related to a system unknown to the player.

26-06-18 (frojas)
- Changed the vorbis library to something that is actively supported.

26-06-18 (BR)
- Fixed an issue where the governor did not always comply with the request not to build a shield in the absence of a missile base.
  - This could occur after the construction of the requested number of ships was complete.
- Ship Combat:
 - Reactivation of safe retreat in Xilmi's AIs when threatened to be completely disabled by warp-dissipater.
 - A validation check is now performed after each ship movement to ensure that the turn is properly completed in the event of the ship's destruction.
  - Finalized the fix for the initiative bug.
    - The ships' action sequence is now dynamic as well, so that it corresponds to the initiative levels communicated to the AIs.


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
