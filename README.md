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

26-09-10 (BR)
- Budget Transfer Panel:
  - Added "Max Capital" options.
  - Replaced Yellow selection box with yellow text.
  - Fixed the flickering planet.
- Added little marker for the the first 1/6th of each bar that applies a 25% bonus to the conversion from BCs to research points.
- DNA Workshop:
  - Fixed tech settings counted twice in malus too.
  - removed annoying beep.
    - (It was a false positive error detection, when updating the file list; as for a moment the selection was set to "null")
- Tech Panel: Improved display of total research by removing rounding errors.
- Improved display of "Production per factory". (Now, up to two decimal places.)
- Fixed potentially suboptimal industry allocation when terraforming.

26-09-09 (BR)
- Fixed label Key with empty contents.

26-09-08 (BR)
- Added another security to try to prevent the crash when a combat end due to max turn limit.
- Restored the minor fix as it was not the problem. (That I can't reproduce)
- Removed a minor fix from 26-08-22 who may crash the game...
- DNA Factory: Added some synchronization protection on save, set as Player, and exit to prevent file saving corruption.
- Added a tools to detect duplicate labels...
- Fixed a few duplicated labels!

26-09-07 (BR)
- Improved help for options "Retreat Destination", "Hyper Comm Retreat", and “Retreat toward enemy”.
- DNA Lab: 
  - Fixed tech settings counted twice.
  - Added avatar to original species
- Fixed label typo
- Refresh budget for ungoverned colonies too.
  - The optimal value and remaining funds will be up to date.
- Scouted System interface: moved the flags a little away from the “Next” button.
- All interfaces with flag:
  - removed the sound while pressing a key modifier.
  - added Auto-Flg function ("home")
- Adjusted some advice bubble location.

26-09-05 (BR)
- All interfaces displaying the star system flags:
  - will also have the auto-flag key functional (Home)
  - wil not emit beep when holding "Shift", "Ctrl", or "Alt"

26-09-05 (BR)
- Scouted System: Fixed Flag linked to the wrong System.
- Minor Advice location adjustment. 

26-09-04 (BR)
- Extension of the advisor, to be able to call it on demand, this in order to display help on the element under the cursor.
  - On the Galaxy map, including some overlay windows.
  - On the colony panel.
  - Press "F1" to toggle. (Former help panel still available with "Ctrl-F1")
  - Options for Avatar size, text size, and to restore the former help panels.
  - This is a work in progress.
- Upgraded help panels to be able to display HTML text.
- Fixed potential sources of memory leak.
- Fixed potential null pointer exceptions.
- Clicking on the Empire name in the system panel will now take you to the Diplomacy panel and select the related empire. (It was already supposed to do this, but it wasn't fully functional!)


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
