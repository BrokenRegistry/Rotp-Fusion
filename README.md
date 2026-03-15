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

26-03-15 (BR)
- New options tool feature to call outside method without creating subclasses.
  - Implemented as test to existing options, and relocated them where they are used. (StarSystem and GalaxyMapPanel)
- New option to add a bit of transparency to the flight path.
- New Option to disable flight path to small transports.

26-03-12 (BR)
- Fixed player's invasion transport displayed on the map, but not on the transport panel. This when "Only display warships" was selected.
- Mass transport Panel: Transport paths are now hidden when "Hide all flight path" is selected.
  - On thousands stars galaxies, they were masking almost everything.

26-03-10 (BR)
- The names of the difficulty levels have been changed to be more realistic and less judgmental. The default value for new players is set to 75% (moderate).

26-03-09 (BR)
- Fixed Invasion loss to rebel message (Typo in the Key)
- Updated the JRE in the windows.zip file to 25.0.2-10
- Fixed Star System marked as targeted by Monster to early.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
