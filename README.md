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

26-11-09 (BR)
- Fixed technologies that cost nothing when restarting.
- Renaming of certain function names that are confusing (Pct -> Ratio)

26-11-06 (Frank Zago)
- Fix stale transport slider hitbox on existing transport.
- Fix infinite scrolling in system history pane and add scrollbar.

26-11-06 (BR)
- Fixed an issue where "filler stars" were "mixed up" and incorrectly identified by the optimization process for the construction of large galaxies.
- Fixed occasional crash in ground battle.
- Fixed mouse click sometime requiring mouse being moved to act. (in council and galaxy setup)

26-11-03 (Frank Zago)
- Optimization of transport duration calculations in the "Mass Transport" window:
 - The duration is calculated once and then stored, which speeds up sorting. This makes a noticeable difference for routes passing through nebulae in giant galaxies.

26-11-03 (BR)
- Added informative text inside BitmapGalaxyExample.zip
- Ship combat prompt: Corrected the French text that was overflowing.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)


