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

26-07-05 (BR)
- New option to prevent governors to send transports to besieged colonies, even if the troops are able to sneak in. As sometime it is preferable to conserve its population to strengthen the defenses of the colonies close to the front.
- Improved some distance sorting by removing the square root.
- Improved some sorting by computing the sorted value only once.
- Empire Status panel:
  - Fixed display of lists not scrolling all the way to the bottom when there were a high number of empires.
  - Fixed the empire names of the top boxes dripping onto those of the bottom boxes when scrolling through the bottom boxes.
- Fixed misplaced scrollbar in the Manage Diplomats subpanel.

26-07-04 (BR)
- Fixed slow display of very large colony lists.

26-07-03 (BR)
- Fixed a typo preventing the correct display of the "Ultra-Rich" option in the selection of colonies to build a stargate.
- New governor options:
  - New option to allocate funds to colonies with artifacts. (Added to the original governor interface)
  - New option to select which colonies with artifacts or new colonies receive funds first.
  - New option to define the level of industry necessary for a colony to cease being a new colony.
  - The option "follow colony requests" is renamed to "Manageable Governor".
  - Reorganized the compact options panel related to original Governor options.
  - Reorganized the compact options panel related to advanced governor options.
  - More options, disabled by other options, are shaded when disabled.
- More informative empire reserves display:
  - The amount now includes excess spending, when redirected to the reserve.
  - The sprite to the left of the galaxy also displays the amount that will be added to the reserve.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
