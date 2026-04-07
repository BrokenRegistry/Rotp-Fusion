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

26-04-07 (BR)
- DNA Workshop: Fixed a display issue where elements overlapped at certain specific resolutions.
- The “War View” panel is now available for all colonies.
- The Shift-F7 and Shift-F8 shortcuts let you scroll through the alien colonies to which you are sending troops.
- The Ctrl-F7 and Ctrl-F8 shortcuts let you browse the war enemy colonies to which you are sending armed ships or troops.
- The Ctrl-Shift-F7 and Ctrl-Shift-F8 shortcuts let you cycle through all the alien colonies to which you are sending armed ships or troops. (This will include all targets of armed scouts.)
- Pressing the F7 or F8 keys will open the “War View” panel.

26-04-05 (BR)
- New option to select MoO1 rules for the miniaturization of the Battle Scanners and the Reserve Fuel Tanks.
- Ship Design UI: The "Size" of ship components is replaced by the space they occupy once powered by the engines; the same applies to their cost.

26-04-03 (BR)
- Ship Combat: Fixed "retreat all" button acting wrong.
- Ship Combat prompt: The “Smart-Resolve” button will once again display only “Smart-Resolve,” but if the action is “immediate withdrawal,” a pop-up window will explain why. If the reason is that the alien is not considered an enemy, pressing ‘W’ will start a war and update the “Smart-Resolve” button's action. (As always, hovering over the flag icon will provide more information about our relations.)
- Fixed “Smart-Resolve” popup text for Monsters.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
