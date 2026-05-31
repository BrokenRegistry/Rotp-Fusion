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

26-05-31 (BR)
- Fixed a few rare null pointer exceptions that were reported to me.
  - An unexplained missing image key will no longer cause the image manager to crash.
  - An unexplained missing image will no longer cause the ground battle panel to crash.
  - Under highly unlikely circumstances, certain keyboard actions that clear a critical variable will no longer cause the game to crash.
- Fixes related to space combat:
  - Asteroids were sometimes displayed at the wrong position. (The one of the previous battle)
  - Ship initiative is now correctly affected by technology nullifiers and warp dissipators.
  - Missiles already launched are now affected when the ships that launched them are hit by technology nullifiers.
  - Values in ship info bubble are now displayed without the extra decimal place.
- Initiative and maneuverability have been added to ship information bubble.

26-05-29 (BR)
- Fixe: Ships destroyed by reaction fire continued to fire using their special weapons.
- Fixed a potential issue with battles ending when the maximum number of turns is reached: it was incomplete.

26-05-28 (BR)
- Fixes (attempt) to prevent a display offset bug.
- Fix in “MoO1 Asteroids”: Missiles were slowing down above the asteroids (this was a forgotten debugging feature!)


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
