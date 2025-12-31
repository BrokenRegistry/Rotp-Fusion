[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

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


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
