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

26-10-09 (Xilmi)
- Assign more weight to maneuverability
  - Under Moo1 ruleset maneuverability is quite a bit bigger and AI would often design ships with only 1 combat-speed

26-10-09 (BR)
- Updated Advisor for Ecology allocation and Defense Allocation...

26-10-08 (BR)
- Fixed possible crash on new colony.
- Fixed null pointer exception due to concurrent modification while computing the total production.
- New option to make transport ships awesome again.
- Fixed the Gauntlet event disrupting colony reserve management.
  - The enormous loss of income resulting from this event triggered a reaction from the governor who attempted a readjustment of allocations, intended in principle to keep the colony clean and deliver the promised ship... This readjustment was not planned for events of this origin, nor of such magnitude, nor so early in the progress of the turn...
- Fixed reference point for reporting population growth or loss. It's not normal that after the Gauntlet event, all the arrows are green!
- Partial deactivation of left widgets during turn processing: If the player loses a colony while a widget is interrogating it, this could crash the game...
- Minor improvements to the governor and advisor.

26-10-06 (BR)
- Using the "Transfer" button will now mark the budget as invalid, the budget will then be updated, based on the player's settings.
- Incoming transport will be taken into account for subsidies.
- Improved advisor prevision for ecology spending.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)


