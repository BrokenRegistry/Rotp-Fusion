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

26-03-09 (BR)
- Updated the JRE in the windows.zip file to 25.0.2-10
- Fixed Star System marked as targeted by Monster to early.

26-03-08 (BR)
- Fixed being unable to redirect fleets to their originating star.
- Tentative to fix a bug that should not happen... No feature change.
- Fixed search tools not working on the Rules panel.

26-03-07 (BR)
- Removed unused files to reduce the .Jar and .Zip files.
- New option: When formatting decimal numbers, use either the local Java setting or the formatting associated with the language selected in the game.
  - Main Panel --> Settings --> User Interface --> Setup UI Preferences --> Change Language Format (Default = No)

26-03-06 (BR)
- Mass transport panel: when the panel popup, stop the back ground animations when the refresh time is greater than 100ms.

26-03-05 (BR)
- Fixed possible Crash on Game Over panel.

26-03-04 (BR)
- Restoration of an original feature: The name of an empire is only randomized starting with the second of the same type.
- Galaxy map: new fixes for mouse responsiveness issues on maps containing thousands of stars.
  - (Continuation) Not all sub-panel and sub-sub-panels were fixed in the previous fixes.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
