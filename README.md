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

26-03-20 (BR)
- New Option for Custom Species:
  - They now have Attack Confidence and Defense confidence.
    - This value will multiply the Confidence of the AI (or the Player) in the rules setting.
  - Prefixes and suffixes are no longer available, as they are redundant under new custom species naming system.

26-03-19 (BR)
- Improved Main setting responsivity by disabling background Game Panel refresh.
- Working Transport Flight Path will be shown even if the display threshold is not null.

26-03-18 (BR)
- Fixed stargate auto-setting!
- Build Limit can't be set higher than 1 in the colony panel too.

26-03-17 (BR)
- Colony Panel: New Stargate column.

26-03-16 (BR)
- Release.yml: Updated deprecated Node.js 20 to Node.js 24
- Stargates:
  - Build Limit can't be set higher than 1.
  - Reset Build Limit once the stargate is completed.
  - Fixed smart max (left-click will not overspend anymore).
- An image of the Stargate will also appear next to the planet in the colonies panel.
- New option that automatically starts fleet battles without asking the player for confirmation.
  - With another option to choose whether to display the battle results.

26-03-15 (BR)
- New options tool feature to call outside method without creating subclasses.
  - Implemented as test to existing options, and relocated them where they are used. (StarSystem and GalaxyMapPanel)
- New option to add a bit of transparency to the flight path.
- New Option to disable flight path to small transports.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
