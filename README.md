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

26-03-24 (BR)
- Each space monster now has its own level.
- The space monster that prevented you from exploring a system will appear in the star panel.

26-03-23 (Frank Zago)
- French Translation improvement.

26-03-22 (BR)
- New fearsome guardian Monsters: Space Crystal and Space Amoeba can now be selected to guard valuable planets.
- New Picky Roaming Space Monsters.
  - Space Crystals will be drawn to any populated ancient world, ANYWHERE on the map. When they can't find a populated ancient world, they will leave.
  - Space Amoebas will be drawn to populated GAIA planets, ANYWHERE on the map. When they can't find a populated Gaia, they will leave.
  - Space Pirates will be drawn to populated Ultra Rich planets, ANYWHERE on the map. When they can't find a populated ultra-rich, they will argue over the next target and destroy themselves.
  - Note: Monsters will also leave if they reach their attack limit.

26-03-20 (BR)
- New Option for Custom Species:
  - They now have Attack Confidence and Defense confidence.
    - This value will multiply the Confidence of the AI (or the Player) in the rules setting.
  - Prefixes and suffixes are no longer available, as they are redundant under new custom species naming system.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
