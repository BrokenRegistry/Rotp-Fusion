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

26-07-23 (BR)
- Options for adjusting ship space size are now dynamic, to help AI early in the game. The space never drops below 90% of the original space. Thus, the impact will mainly affect the middle and end of games.
- Fixed Restart with option to Swap with GUI species still keeping the old abilities.

26-07-22 (BR)
- Fixed an issue where the governor spending analysis could be started concurently by a screen refresh and a mouse click... Resulting in colony display glitch.
- Added options to set the limit of fund accumulation. (Absolute and relative)
  - When funds collected exceed funds spent, at what point does the governor stop collecting?.
  -The maximum of the absolute and relative value will be collected.
- Fixed the description of fund limits: it is always the maximum of the absolute and relative value.
  - Then you may choose your favorite method, and set the other to 0...
- Shipyard: If there is a building limit and the amount of BC is already enough to build them (due to new technology), you no longer need to spend a tick to get your ships.

26-07-21 (BR)
- Colony spending sliders: Press Alt to view expenses in BC.

26-07-20 (BR)
- Governor's Funding: New option to prioritize research funding into plague and supernova random events.
- Coin icon: added a separate level of transparency for each species.

26-07-19 (BR)
- In parallel with the permanent fund transfer, it is now possible to establish a budget and modify it. New tasks may be assigned to the governor to collect funds and distribute them.
- New Governor option for ho to allocate subsidies: (Gold coin on the side of the Governor button)
  - Metal color: Follow global instructions.
  - Gold color: Allocate subsidies.
  - Red arc (right click): Can draw from the player's reserve.
  - Uncircled: active until the player deactivates it.
  - Magenta circle: Active while an emergency is activated.
  - Uncircled: active until the player deactivates it.
  - Green circle: Active as long as the colony has not reached the development limit.
  - Blue circle: Active as long as a blue priority is present. (With the exception of research and shipsyard, as they do not deactivate automatically)
- New Budget tab in the Colony panel, to review the governor's choices and refine the budget for yourself.
- Right-clicking the Mandate button will open the Governor's Advanced Panel, providing access to the new funding options:
  - Activation and deactivation of the collection of funds
  - Amounts to be collected (absolute or relative)
  - Reserve for the player (absolute or relative)
  - Development limit of a colony for receiving subsidies.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
