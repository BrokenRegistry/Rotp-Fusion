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

26-03-05 (BR)
- Mass transport panel: when the panel popup, stop the back ground animations when the refresh time is greater than 100ms.

26-03-05 (BR)
- Fixed possible Crash on Game Over panel.

26-03-04 (BR)
- Restoration of an original feature: The name of an empire is only randomized starting with the second of the same type.
- Galaxy map: new fixes for mouse responsiveness issues on maps containing thousands of stars.
  - (Continuation) Not all sub-panel and sub-sub-panels were fixed in the previous fixes.

26-03-03 (BR)
- Galaxy map: new fixes for mouse responsiveness issues on maps containing thousands of stars.

The hardest bugs to find are those that are multiple…

The mouse responsiveness issue mostly occurs with larger galaxies and gets worse as the number of fleets and transports increases.

Elements of the problem:

The analysis of the management of objects (Stars, fleets or transports) selected or hovered over by the mouse comes from three sources: A mouse action (Move or click), a keyboard action, or a secondary consequence of the two previous actions.
At some point in the analysis, it is necessary to know the current position of the mouse, and if it is outside the galaxy map the analysis ends. As the start of the analysis does not depend solely on a mouse action, a query is made to obtain the current mouse position, the result being valid only if the mouse is on an unobscured part of the galaxy map.

The problem:

When the cursor leaves an object (Star, fleet, or transport), a search is started to find out if another object is under the cursor: a loop will go through all the objects, to see if their position corresponds to that of the mouse. The bigger the galaxy, the more advanced the empires become, and the longer it takes.

At the end of the search, comes the time to ask for the mouse position again. If the mouse is then on the right panel, the analysis takes the wrong path, and the previously hovered object is not correctly deactivated. (This step is fast enough for galaxies of reasonable size to not pose a problem)

In the previous fix, I forced the deactivation of these objects, but not all the time, because the right panel is chosen from around ten panels, themselves made up of sub-panels… And not all of them had received the fix (far from it).

I now think I have covered all the sub-panels.

In fact some sub-panels did not capture the mouse, (notably the central part of the massive transport and fleet deployment) which had the consequence that the objects under the panel could be hovered over, redirecting the destination…

Another bug, under certain conditions the hovered object is stored in two places… The fix only deactivated one, then we found ourselves with a Schrödinger object, both hovered over and not hovered over… The consequence was that if this object was hovered again, it could no longer be correctly marked as hovered.

This new patch should address all of these issues. When the mouse leaves a star or a fleet, all the stars, all the fleets, all the transports will be looked up to check if the mover hovers them. This may takes times, and if

26-03-01 (BR)
- Intelligence Panel: When the mouse hovers over a technology, its description appears in a pop-up window.


### [Features Historic](FeaturesChanges.md)

### [Reverse Chronological Historic](DetailedChanges.md)


## [To-Do list](TodoList.md)

[How To](doc/HowTo.md)
