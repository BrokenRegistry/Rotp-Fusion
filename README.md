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

25-10-20 (BR)
- When a player select a custom species, the name of the custom species will replace the name of the selected animation species. But if the custom species has the same name as another species, and close to the maximum number of opponents is selected, this species name will appears twice. To fix this, the default option to keep the MoO1 names will add them to the list instead of replacing the first element. And as the human keep their names, I've added for them a new denomination: Pangaean.

25-10-10 (BR)
- Opponents panel automation:
  - Reducing the number of opponents will no longer remove those who are out of bounds, so they will reappear when the number increases again.
  - If you clear an opponent (with mid-click) while holding down the Ctrl key, all the following opponent will be cleared too.
  - If you click on an opponent while holding down, all the following empty opponents will be set to the first available species.
- The 'Reworked' abilities will now randomly select one of the custom species that has the corresponding "Species for Animation".

25-10-09 (BR)
- Added field in custom Species: Bound AI and Species for animation.
- Fixed Galaxy shape options fields not showing guide before changing the shape at least once.
- Fixed Galaxy Setup Start Button being blank when "Shift" was down.
- Standardization and centralization of aptitudes display methodes in the opponents' control panel.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
