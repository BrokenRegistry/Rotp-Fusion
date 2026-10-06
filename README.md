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

26-10-06 (BR)
- Incoming transport will be taken into account for subsidies.
- Improved advisor prevision for ecology spending.

26-10-05 (Legendmaster)
- Multiplayer setup: let the player roster scroll with many empires

26-10-05 (BR)
- Fixed the responsivity of the search field in the option panel...
  - The galaxy map and sprites were still displayed behind the options panel, temporarily stealing focus and eating keystrokes.

26-10-04 (Legendmaster)
- New multiplayer feature (Play by E-Mail).

26-10-03 (BR)
- Fixed Spy Overspend OFF "creating money"
- French translation for Hot-Seat.
- added Rotp colors to the multiplayer button in the galaxy panel.

26-10-03 (DM)
- New multiplayer feature (Hot-seat).

26-10-03 (BR)
- New ships display options:
  - No transports.
  - No ships.
- Transport icon size is now linked to transport size.

26-10-02 (BR)
- Transport synchronization will now be remembered independently for invasions and migrations.
- Added the number of flags setting inside the flag sub-panel.

26-10-01 (BR)
- DNA Workshop: New button on the side of the directory selection button to copy GMO species samples to the selected directory.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)


