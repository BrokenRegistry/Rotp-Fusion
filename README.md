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

26-09-26 (BR)
- Fixed "Randomize" issue with "Tech Discovery" and "Tech Research"
- Improved advisor content in defense allocation
- Tech UI: "Alt-Num" will equalize the tech allocation up to the limit of: num * 10% chance of discovery.
  - example: "Alt-6" ==> no more tick will be given once the chance of discovery exceeds 60%.  

26-09-24 (BR)
- Fixed and updated the galaxy size description (Thanks to @Corbeau)
- New content for the advisor: in defense, shipyard and ecology.

26-09-22 (BR)
- Fixed the crossed coin icon not appearing in the colonies columns.
- Improved a bit the coin image quality (Some scaling issues)
- New content for the advisor: the list of events expected in the development of the industry.
- Fixed a shortcut key overlap on the colonies panel.
- Fixed a potential null pointer exception when sending transports.
- Fixed ROI analysis when stolen factories are available, the analysis will now be progressive and use the available ones before switching to population growth when factories become too expensive.
- Improvement of the advisor content, on continuous shipbuilding.

26-09-21 (BR)
- New settings: development limits for each planet type.
- The Advisor has entered the Design panel.
- Fixed some advisor display issues (pop-up remaining too long)


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
