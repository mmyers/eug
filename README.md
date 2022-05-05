### Europa Universalis Editor
[![Download Europa Universalis Game Tools](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/eug/files/latest/download) [![Download Europa Universalis Game Tools](https://img.shields.io/sourceforge/dt/eug.svg)](https://sourceforge.net/projects/eug/files/latest/download) [Chat on Discord!](https://discord.gg/bkeaCZxd)

A game editor which has as many mapmodes as the game does itself. Includes a syntax-checking text editor at no extra cost.

Compatible with:
- Europa Universalis IV ([see EU4 discusson thread on Paradox forums](https://forum.paradoxplaza.com/forum/index.php?threads/announcing-a-scenario-editor-and-map-viewer.707474/))
- Europa Universalis III ([EU3 discussion thread](https://forum.paradoxplaza.com/forum/index.php?threads/clausewitz-save-game-and-scenario-editor-viewer.527308/))

Compatible with older versions of:
- Crusader Kings II (CK2)
- Hearts of Iron III (HOI3)
- Victoria II
- Europa Universalis: Rome

The editor might be compatible with the current versions of all of the above, but I own none except Victoria II, which I do not have the expansions for. I welcome test reports for any game.

### Screenshots

![Map view](/screenshot.png)
*Europe map in 1444. Click for larger image*

![Religions view](/screenshot-religions.png)
*Religious map at the Thirty Years War bookmark. As in game, diagonal stripes indicate the province religion is different from the country religion.*

![Editor](/screenshot-editor.png)
*Syntax-highlighting text editor. If a brace is unmatched, the editor will highlight the line to make it easy to find.*

![Trade nodes view](/screenshot-tradenodes.png)
*Trade nodes and trade flow*

![Map modes](/screenshot-menu.png)
*Menu showing all current mapmodes available when working on EU4. Other games may have different mapmodes available.*

#### Subprojects

- **EU3_Scenario_Editor** - This is the main scenario editor project.
- **eugFile** - The library that makes the rest of this possible. It handles parsing and loading Paradox game files. The "eug" refers to EU2 save games, which used a .eug extension.
- **EugFile_specific** - Abstracts engine-specific details, mostly Clausewitz vs. Clausewitz 2 games. Also has some code for old Europa games.
- **EugSyntax** - A plug-and-play syntax highlighting editor kit for a javax.swing.JEditorPane.
- **PositionsEditor** - Editor for sprite positions on the game map. Useful for map modders only. Mostly irrelevant for newer games which include the "nudge" editor.
