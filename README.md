# Remnants of the Precursors

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

25-09-26 (BR)
- Minor change to reduce the risk of concurrent error.

25-09-24 (BR)
- New Options tool to reset remote variable and to add images in option panels.
- New Dynamic difficulty customization.
  - Turn at which the dynamic difficulty level is at 50% of its potential.
  - Number of turns required for dynamic difficulty to increase from 50% to 75% of its potential.
  - Dynamic evolution model:
    - Unfair to the Player (default)
      - The player's reinforcement is limited, unlike that of the AIs.
    - Unfair to the AI
      - The AI's reinforcement is limited, unlike that of the player.
    - Fair and limited
      - Player and AI reinforcement is limited. (No correction When the power difference is high)
    - Fair and unlimited
      - Player and AI reinforcement is unlimited.
    - End the game quickly
      - Increase the production gap.
- New parameter to reduce (or increase!?) the Spaces Pirates level.
    

### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

### [Reverse Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)


## [To-Do list](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/TodoList.md)

?