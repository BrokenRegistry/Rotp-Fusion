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

26-09-13 (BR)
- New option to Restore the original MoO1 ship combat maneuver power requirement.
- Fixed the budget algorithm which was going astray due to negative fundraising...
- Fixed help tooltip for maximum production transfer from 50% to 100%.
  - This in all languages, hoping that it won't produce too many grammatical errors...

26-09-12 (BR)
- Fixed a potential concurrent exception.
- Fixed occasional crash issue when selecting a stolen tech.
- The colony development limit will now be based on the colony industry capacity.
- Budget Max Absolute Capital can now be set to a smaller amount.
- The accrued production on the old ship can now bw displayed in the military tab of the colonies panel.
  - "F12" to toggle.
- Tech Panel: Holding "Shift" while equalizing allocations will only equalize unlocked fields that already have an allocation.
- Improved labels files validation tool.

26-09-11 (BR)
- New option to disable Map animations (while keeping high graphic quality)
  - Useful for huge maps.

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


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
