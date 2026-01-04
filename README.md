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

26-01-04 (BR)
- Custom images can be put in an "images" folder located in the same folder as the jar file.
  - The folder structure inside the images folder must be the same as the original one, and the file type must be the same as specified in the txt files.
- Fixed useless creation of empty race files.
- When there is more than 31 empires, Replay will now list all of tem in smaller font size.
- Fixed some missing French dialog. (By default there was no pacific contact dialog for species that are never pacifist... Except when customized!)
- Fixed custom player dialog display.
- Fixed language change for custom species.
- Custom species are now built from their animation species.
- Fixed certain texts that extended beyond the boxes.

25-12-31 (BR)
- Complete overhaul of species management.
  - The "Race" class now consists of skills and animations.
    - However, the Race factory remains unchanged.
  - Custom species can choose their animations and assign them names, leaders, and home world.
    - This for Multiple civilizations per species.
    - This in multiple languages.
    - A new GUI is now available for this purpose.
  - The distribution of skills, animations, names, and civilizations is managed by a new "Species" class and includes all "Race" methods that were previously in the Empire class.
    - Then the Empire Class now extends the Species class to get back all these methods.
  - A "species factory" is now responsible for assembling species according to player requests.
  - A new option allows empires to be assigned from custom species that have selected an animation with dedicated names.

- Current limitations:
  - The number of civilizations per species is currently limited by the interface, which only allows for one row of tabs.
  - This also limits the number of languages.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
