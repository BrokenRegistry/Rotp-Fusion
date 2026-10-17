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

26-10-17 (BR)
- Load/save user option button: Right click to open the file chooser.
- File chooser: Fixed button text and color for French speaking players.

26-10-15 (BR)
- Galaxy Map: Disabled the action of the left icons when they should not be used.

26-10-14 (BR)
- Multi-player Hot-Seat
  - Improved player identification text quality. 
  - Anti-cheat: The number of turns since the last reload is now displayed on the left side of the Year (Turn).
  - Diplomacy UI: Fixed the "Continue" doing nothing.
  - All Notification UI: Reduced the effect of bug security blocking player responses, when a problem was identified and the player was not expected to provide responses: The player will still be able to respond and continue their game. (And report the bug if it was real)
- Fixed auto flag not always recognizing asteroids.
- Fixed Pulsar Damage
  - It was wrongly applied the same damage to all groups of ships, while the moO1 rules say that each group has its own random roll, and since the RotP manual doesn't mention any changes to the original rules, each group's damage is now random.

26-10-12 (BR)
- New advisor TTV and ROI content while hovering Production field.
- Fixed Advisor display glitch with colonies in rebellion.
- In Progress: Centralization of the production estimate in the next turn, to calculate it only once for all categories. And offer the possibility to select "Old Way" or "New Way". (In the "Old Way", transport must be scanned only for ecological allocation, while in the "New Way" they must be scanned for each category... But by centralizing, once for all categories is enough!)


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)


