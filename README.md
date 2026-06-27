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


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
