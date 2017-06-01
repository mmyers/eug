### Europa Universalis Editor

[Download it on SourceForge!](https://sourceforge.net/projects/eug/files/latest/download)

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

#### Subprojects

- **EU3_Scenario_Editor** - This is the main scenario editor project.
- **eugFile** - The library that makes the rest of this possible. It handles parsing and loading Paradox game files. The "eug" refers to EU2 save games, which used a .eug extension.
- **EugFile_specific** - Abstracts engine-specific details, mostly Clausewitz vs. Clausewitz 2 games. Also has some code for old Europa games.
- **EugSyntax** - A plug-and-play syntax highlighting editor kit for a javax.swing.JEditorPane.
- **PositionsEditor** - Editor for sprite positions on the game map. Useful for map modders only. Mostly irrelevant for newer games which include the "nudge" editor.
