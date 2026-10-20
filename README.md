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

26-10-20 (BR)
- Fixed an issue where "system discovery", "Colonize", "Combat", or "Bombard" windows would sometimes display the panorama of the previous planet.

26-10-19 (BR)
- Fixed an very old bug in the "next turn" waste computing.
  - Waste is now added at the time of production calculation, before industry can change the robotic level and distort the value calculated later by ecology.
- The governor will check for Rich-Artefact type planest and will not apply conflicting request.

26-10-18 (BR)
- Fixed Random Event Gauntlet Relic: Transports and Rebels will also be impacted.
- Council UI: Fixed the animation of diplomats.
- Empires UI: Fixed the animation of diplomats, which only moved when the mouse was constantly moving...
  - The same species will no longer be synchronized.
  - The selected diplomat will speak with the player.
- Design UI: fixed design boxes display refresh.

26-10-17 (BR)
- Load/save user option button: Right click to open the file chooser.
- File chooser: Fixed button text and color for French speaking players.

26-10-15 (BR)
- Galaxy Map: Disabled the action of the left icons when they should not be used.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)


