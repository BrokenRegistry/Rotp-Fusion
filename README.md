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

26-02-27 (BR)
- Space Monsters: New option to change their speed and the distance to the next world.
  - Minimum distance to the next world. (default = 0, up to 25)
  - Maximum distance to the next world. (default = 8, up to 100)
  - Because of the distance extension, and option to increase their speed has been added. (Up to 3.0 ly/turn)
- The space monsters unleashed by technology will first target the empire that discovered this dangerous technology.

26-02-26 (BR)
- New option to allows for a gradual increase in range, depending on the overall level of propulsion technologies.
  - Formula: Range = Fuel * (1 + Lin * Tech + Quad * Tech^2)
    - Fuel = Range related to fuel cell. (Original range)
    - Tech = Overall level of propulsion technologies. (Displayed on the right of the Tech panel)
    - Lin  = Selected linear factor.
    - Quad = Selected quadratic factor.

26-02-25 (BR)
- Galaxy Map: Fixed a recurring issue where the right panel would continue to display the last fleet or system flown over, instead of returning to the selected fleet or system.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
