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

26-01-30 (BR)
- Fixed asteroids no longer disappeared gradually.
- Updated JRE in the .exe file to Version 25.0.1_8

26-01-23 (BR)
- Changed the default mouse sensitivity to "High" to prevent annoyingly slow UI responsiveness.

26-01-15 (BR)
- Fixed numerous "getGraphics()" calls that did not properly call "dispose()".
  - Many of them were used intensively.
  - This should lighten the load for the garbage collector and, hopefully, prevent "insufficient memory" screens from appearing.

26-01-14 (BR)
- Fixed certain fields displaying the generic name of the species instead of the specific name of the Empire.

26-01-12 (BR)
- Fixed potential "Memory Leak" (added by the last fix)

26-01-10 (BR)
- Improved a security for customized Ship Names.
  - The original names are added as reserve in case the number of custom names is to small.

26-01-09 (BR)
- Added a security filter to remove space only customized text.

26-01-07 (BR)
- Added tooltips in the "Custom Species Naming" interface.
- New custom species option to add ship names.
- Fixed "Same as player" abilities.

26-01-06 (BR)
- New option to customize planet size bonus, their minimum and maximum sizes, as well as the rounding of values.

26-01-05 (BR)
- New option to restore MoO1 combat resolution: Unlike in MoO1, damage inflicted by weapons in RotP is not affected by the difference between attack and defense levels. (Only affects the probability of hitting.) In MoO1, a high differential between attack and defense increases the weapon's minimum damage. This gives kittens a boost.

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
