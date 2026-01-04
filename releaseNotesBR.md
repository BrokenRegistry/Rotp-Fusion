[Official website](https://www.remnantsoftheprecursors.org) <br/>

New Java requirement: minimum JRE-17, recommended JRE-23.

[Installation instructions](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/installation.md)


<b><ins>Very last changes:</ins></b>

26-01-04 (BR)
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


#### [Features Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/FeaturesChanges.md)

#### [Reverse  Chronological Historic](https://github.com/BrokenRegistry/Rotp-Fusion/blob/main/DetailedChanges.md)
