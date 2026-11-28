[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

$${\color{red}Warning}$$
To minimize the risk of problems, JAR and EXE files must be placed in their own directories.
When updating, you can reuse the same folder.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-11-28 (BR)
- The maximum number of star systems is now capped at 50,000, because even with sufficient memory, the time required for AI decision-making becomes unbearable in the late game... It is therefore pointless to offer false hope by suggesting even larger galaxies.
- Connect Empires option:
  - The product of the number of star systems and the spread factor is now capped at 50,000.
  - The connection of all star systems, their number is now capped 5,000.
- The galaxy size option does not cycle back to the beginning once the end of the list is reached (and vice versa), because transitioning from the minimum size to the maximum size could result in a significant generation time.

26-11-26 (BR)
- Galaxy setup: Cover over the start button when "Advanced Galaxy Configuration" is open.
  - Clicking "start" without getting any result can be frustrating...

26-11-25 (BR)
- In addition to the "Connect the Empires" option, you can now set the maximum hostility level for the star systems that connect the empires.
  - The random generation of a planet is determined by the parameters set in other options (galaxy age, planet quality, etc.). However, if the result does not meet the requirements of this option, new planets will be generated until a valid one is obtained.
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


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
