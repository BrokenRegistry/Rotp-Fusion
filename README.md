 Remnants of the Precursors

Remnants of the Precursors is a Java-based modernization of the original Master of Orion game from 1993. <br/>

### Fusion version
### Mixt of of Xilmi Fusion with Modnar new races
### With BrokenRegistry Options Manager. <br/>
... and some more features

Summary of the differences of Fusion-Mod to the base-game:
        [https://www.reddit.com/r/rotp/comments/x2ia8x/differences_between_fusionmod_and_vanillarotp/](https://www.reddit.com/r/rotp/comments/x2ia8x/differences_between_fusionmod_and_vanillarotp/) <br/>

Description of the different AI-options in Fusion-Mod:
        [https://www.reddit.com/r/rotp/comments/xhsjdr/some_more_details_about_the_different_aioptions/](https://www.reddit.com/r/rotp/comments/xhsjdr/some_more_details_about_the_different_aioptions/) <br/>

The decription of the additions/changes by Modnar can be found there: <br/>
	[https://github.com/modnar-hajile/rotp/releases](https://github.com/modnar-hajile/rotp/releases) <br/>


### To build and run locally:

On Debian / Ubuntu:

```
sudo apt install vorbis-tools
sudo apt install webp
mvn clean package -Dmaven.javadoc.skip=true
java -jar target/rotp-<timestamp>-mini.jar
```

On Fedora:

```
sudo dnf install libwebp-tools vorbis-tools
mvn clean package -Dmaven.javadoc.skip=true
java -jar target/rotp-<timestamp>-mini.jar
```

# Other Links
[Official website](https://www.remnantsoftheprecursors.org/) <br/>
[Community subreddit](https://www.reddit.com/r/rotp/) <br/>
[Download build](https://rayfowler.itch.io/remnants-of-the-precursors)


## What's New

26-06-18 (BR)
- Ship Combat:
  - A validation check is now performed after each ship movement to ensure that the turn is properly completed in the event of the ship's destruction.
  - Finalized the fix for the initiative bug.
    - The ships' action sequence is now dynamic as well, so that it corresponds to the initiative levels communicated to the AIs.

26-06-16 (BR)
- Tentative to make mini.jar compatible wit arm64.
- Added some diagnostic logs to try to pinpoint an old crash issue affecting Linux.
- New option to select how the ship repulsors works.
  - As in Moo1: You can access the cells adjacent to the repulsors, which are not highlighted.
  - As in Original RotP: Access to the cells adjacent to the repulsors is prohibited.
  - Intermediate: You can access the cells adjacent to the repulsors, which will be highlighted.

26-06-15 (BR)
- Fixed a bug where the Orion Guardian's ship captain did not check whether their ship had been destroyed during movement, and added a safety measure for all monster ship captains.
- Fixed an issue with the text display for the Meklar rebellion advisor.
- Improvements to space monsters and guardian monsters:
  - They will now be updated in real time when you change their level in the options panel.
  - The choice between MoO1 monsters and RotP monsters has been replaced by a probability value indicating whether it is one or the other. (100% = always MoO1; 0% = always RotP)
  - Guardian pirate levels are half those of space pirates and are based on the level of the nearest Empire.
  - New option to always allow the fleet to retreat when fighting a monster.

26-06-13 (BR)
- Repulsor fix: They now work the same way as in MoO1.
  - Applies only to the player. The AI will continue to avoid repulsor cells.
  - Ships may be repelled multiple times, but not indefinitely. They will stop if they pass through the same spot again.
- Fleet Deployment UI: fixed an issue with arrows clickable area being smaller than hover detection.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
