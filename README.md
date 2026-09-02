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

26-09-02 (BR)
- Combat panel: Added a level of security in the final ship retreat display, to prevent crash.
- Fixed fund raising acting on ungoverned colony. (May be an option to allow it will follow)

26-08-26 (BR)
- Fixed Planets Bombardments not always being added on the diplomatic list.

26-08-23 (BR)
- Fixed a display bug in the transport panel, which could display, with a negative time, a transport previously arrived at its destination.

26-08-22 (BR)
- Fixed the issue where the player could not threaten other empires.
  - This was due to the player's diplomat AI selection. In fact, some AI does not bother the player with incessant threats and deactivates this functionality. The new selected AI will not hesitate to offer this option again.
- When a planet with an artifact is discovered, and the technology found is an ongoing research, the new choice become final, as the current allocation is given to it. To avoid this inconvenience, the current search will only be returned if no other tech is available.
- Fixed the attacker not always retreating when the fight reached the maximum number of turns.
- Fixed an incorrect identification of the missiles' source; the AI did not realize they were coming from the planet and was trying to dodge them instead of approaching the planet to bomb it.

26-08-20 (BR)
- Fixed Development limit default and limit value: from 100% & 100% to 50% & 80%
  - Development limit: Maximum development level defining a colony as new.
  - Colonies above this limit will not receive funding as "new Colonies"
  - Colonies below this limit will not be taxed as they could receive funds as “new colonies”. (Which would result in wasted funds as taxation involves 50% losses)


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
