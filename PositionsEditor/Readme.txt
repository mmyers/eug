/=================================\
|== Clausewitz Positions Editor ==|
|==         Beta version        ==|
|==          By MichaelM        ==|
\=================================/


===================
=== What is it? ===
===================

This program gives you an easy way of viewing and editing the icon positions
found in positions.txt.


===========================
=== System Requirements ===
===========================

Java 1.5 or later (tested on Java 1.6)


====================
=== Instructions ===
====================

1. Unzip it.
2. Run it (double-click on pos-ed.bat).
3. Select the game type and the .map file you want to edit.

     ***  MAKE A BACKUP OF THE .MAP FILE FIRST! ***

4. Click OK to load the map. The program will remember the last map you loaded
   in each game version.


=== A Brief Tutorial ===

The first thing is to open up a map file. Note that this program does not
handle backing up the old files; you'll have to do that yourself if you want
to. In other words, DO NOT OPEN UP THE VANILLA DEFAULT.MAP FILE if you're
going to change things.

Double-click any province to edit it. Single-click any province to view its
positions.txt entry in the lower pane (it is not editable here). The list in
the left-hand panel works the same way.

The editing dialog shows the positions that are defined. The dots are the
actual locations; the text (except for the province name) is just to label the
dots. You can't move things around by dragging them; you have either edit the
text in the form or else click on one of the edit buttons. To assist you,
the status bar displays the coordinates of any point the mouse is over.

If you've made changes, you will be prompted to save them when you close the
dialog. This does not alter the actual file yet. You will be prompted to save
the positions.txt file when you exit. If you agree, it will be written to the
filename shown on the title bar. Make sure that this is what you want before
you accept.


=======================
=== Troubleshooting ===
=======================


Q. I added a position entry for a province that didn't have one, and now I
   can't find it in the saved file. Where is it?

A. It's at the bottom of the file. I considered sorting the entries by ID
   number before saving, but some mods have painstakingly sorted them by region
   and I would hate to mess it up. Besides, it isn't really that hard to move
   them to the right spot, is it?

   If enough people (roughly two) say it really is, then maybe I'll add an
   option to sort them before saving.


Q. I just saved my changes, but I just realized that I saved over the vanilla
   file. What can I do?

A. Next time, read the tutorial....
   Seriously, you'll just have to get the file from somewhere else. Sorry about
   that, but do remember this is a beta version.


Q. I think I found a bug. What do I do?

A. Check the thread; if it hasn't been reported, please do it! I can't fix bugs
   that I don't know about.


=== Known Bugs ===

* Only province names are placed when there is no definition. Since the game
  also places cities, units, manufactories, and trade centers, the province
  text shown may not be in exactly the same position it is in game.


=== To Do ===

    * Make a better way to edit the text and port rotations.
    * Allow the positions to be moved by dragging them.
    * Show the province names on the main map.
    * Sort the province entries before saving the file (maybe).
    * Allow more flexibility in choosing the formatting of the output file.


===============
=== Credits ===
===============

As usual, EUG-file handling (lib/eugFile.jar) is based partly on Kinniken's
VictoriaEditor (http://www.kstudio.net/vedit/). All else is my own work.

-- MichaelM

