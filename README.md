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

26-06-15 (BR)
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

26-06-11 (BR)
- Improved the responsiveness of the search field.
- Fixed an issue that prevented the search field from accepting uppercase letters.
- Fixed an issue where the search field was obscuring part of the guide's pop-up window.

26-06-10 (BR)
- A new option now allows you to view all technologies in the technology allocation panel. This means you no longer need to open the user manual to find out the level of an unknown technology.

26-06-09 (BR)
- Two new subpanels have been created. They group together the options for restoring the MoO1 rules.
  - One is located in the galaxy settings panel
  - And the other is in the rules settings panel.
- Removed the Fixe (attempt) of 26-06-03: useless.
- Added constant in SpyConfessionIncident.java to improve readability.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
