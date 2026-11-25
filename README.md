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

26-11-25 (BR)
- Fix for a potential error related to concurrent access to the transport list during landing.

26-11-24 (BR)
- New galaxy option to prevent an empire from becoming overly isolated from others due to inadequate fuel cells, one can ensure the existence of a path of colonizable star systems with spacing compatible with the fuel cells easily available. The maximum distance can be defined using the "Maximum connection distance" option.
  - Connect empires only.
  - Connects empires and Orion.
  - Connect all star systems, but asteroids may still limit access to less significant stars.
  - This feature is disabled by default.
- New option to select the maximum distance between two stellar systems that creates a path, allowing two empires to be connected.
- New option to minimize the number of added stars when connecting empires.
  - The connection procedure involves linking the nearest stars until all required connections have been established. Not all of the new stellar systems generated in this process will be used to form the final, required links. You may therefore choose to remove these "unnecessary" additions. (It is generally better to keep them, so that the paths are not too obvious.)
- New option to preview the warranted path in yellow. (In the galaxy preview of the setup panel only)
- Fixed an issue that prevented the generation of the "Bulls Eye - Ring" galaxy when empire spreading exceeded 700%.
- Standardization of the star location  in the GalaxyShape class to allow empire connection.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)


