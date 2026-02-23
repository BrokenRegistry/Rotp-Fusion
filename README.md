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

26-02-23 (BR)
- Fixed ShipSet displayed text on Species Setting Panel.
- Fixed Winning over rebels not limiting population size to the planet size.

26-02-22 (BR)
- Fixed Star systems displaying wrong Data.
  - When an empire invades a planet, other empires will not be aware of the new data until a spy informs them. The right panel correctly displayed the "???" while on the map, the star system displayed the former data of the invaded empire.

26-02-19 (BR)
- Restored possibility to change AI in Game. (was missing in the new interface)
- Fixed some possible crash in the invasion panel.

26-02-18 (BR)
- Fixed Transport path not being displayed while creating transport, with the option "Flight Path Display" being set to "Hide All"

26-02-17 (BR)
- Added "Display Mode" and "Java Version" to the error report.
- New auto-colonization tuning options:
  - Option to limit the maximum flight time: to prevent a colony from being sent to the other side of the galaxy instead of waiting for a closer planet to become accessible.
  - Option to send a second colony ship that will reach a planet before the one already en route.
  - Option to set a minimum time savings threshold that justifies sending a second colonial ship.
  - Option to adjust the priority of sending a second ship to a valuable planet versus sending it to a untargeted planet.
- A few minor changes to the auto-scout settings to make them similar to the new auto-colonization options.
  - Option to set a minimum time savings threshold that justifies sending a second scout ship.
  - Option to adjust the priority of sending a second scout to a nearby system rather than to a distant, untargeted System.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
