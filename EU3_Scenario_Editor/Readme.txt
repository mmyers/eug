/=====================================\
|==   Clausewitz Scenario Editor    ==|
|==  Version 0.8.5 (Oct 12 2013)    ==|
|==           By MichaelM           ==|
\=====================================/


===================
=== What is it? ===
===================

This program loads data from either a saved game or history files and shows
the world map based on it. It lets you easily edit province and country
history files (or save game entries). The history editors even have syntax
highlighting and validation.

There are also a number of views to help you visualize the game data (or maybe
just find that last colony that's keeping you from getting 100% warscore on your
mortal enemy).


It is known to be compatible with EU3, Rome, Victoria 2, and HOI3. CK2 and EU4
probably work, but I don't guarantee it; I've only tried it on the demos, which
means no saved games.


===========================
=== System Requirements ===
===========================

Java 1.6 or later


====================
=== Instructions ===
====================

1. Unzip somewhere.
2. Double-click the .jar file (in Windows 7 or later) or scen-ed.bat.
3. Choose which game you want in the upper left dropdown, then browse to
   the location where that game is installed.
   Then select a mod (if desired) and choose a saved game (if desired).
4. Click OK to begin.

Click on a province, and its name should be shown at the bottom of the window.
Click "Show province history" or simply double-click a province to display
the province's history file.
If you make any changes, you will get a dialog asking whether to save the
changes. If you click "Yes", a backup of the old file will be saved (with a '~'
prepended to its name). Note that at most one backup file exists at a time; if
you already had a backup file, it will be automatically overwritten.



=== Looking at the world ===

In the menu titled "Views" are quite a few different ways to view the map. Play
around with them for a while to get a feel for what you can and can't do. If
the editor feels laggy, turning borders off may help, but then it can be hard
to pick out a specific province to edit.

In some views, if a province history file is not found, the province will
appear bright red on the country map, and the message "Province has no history
file" will appear next to the "Show country history" button. This is not
necessarily an indication of a bug in the mod; EU3 wasteland provinces, for
instance, often don't have history files.



The current date is shown in the toolbar. You can change the numbers to whatever
you want, and then click "Set date" to apply the changes. The date of course
only affects views which use history data. The editor also lets you easily
jump to any of the game's bookmarks (if applicable).

Use Ctrl +/- to zoom in and out. Note that zooming too far in may cause the
program to run out of memory; thus, I have capped the zoom at 5x.

If you do run out of memory, see the Troubleshooting section below.



=======================
=== Troubleshooting ===
=======================


If the program hangs, check the console window; there will probably be a stack
trace -- a lot of lines saying something like
	at javax.swing.JComponent.paint(Unknown Source)
-- in the window. Post it on the forum and I'll see what I can do.



If a popup appears indicating that the program is "Out of Memory" and your
machine has plenty of memory, then you'll need to use the .bat file to run
the program.
If you were already using the .bat file, then you'll need to edit it to:

java -Xmx1024m -jar EU3_Scenario_Editor.jar

where the 1024 is the maximum number of megabytes of memory that the program
is allowed to use. (Yes, it's silly, but that's how Java operates.) You can
set this higher than 1024 if you need; 1024 is usually enough, but the
games and their saves are getting more complex all the time.



If you get an error indicating that Java is not installed and you know it is,
it is possible that you are on a 64-bit computer with a 32-bit Java Runtime
Environment. If this is the case and for some reason you can't install a 64-bit
JRE, you can change the scen-ed.bat file to this:

----- start copying here ----

@setlocal
@if exist %SystemRoot%\SysWOW64\java.exe. (
@echo 64-bit detected
@set JavaCommand=%SystemRoot%\SysWOW64\java.exe
) else (
@set JavaCommand=java
)

%JavaCommand% -Xmx512m -jar EU3_Scenario_Editor.jar

pause

----- stop copying here -----


==================
=== Known Bugs ===
==================


* Some items in save games are time-dependent, like revolt_risk. The editor
  currently does not handle them specially and assumes that anything that has
  not been reset to 0 must still exist.


=== To do ===

    * Add a view mode for discoveries.
    * Improve saved game capabilites to allow viewing variables which are not
        in the history (e.g. country national ideas).
    * I don't know what else; what do you think? Post your suggestions on the
        thread!


===============
=== Credits ===
===============

EUG-file handling (lib/eugFile.jar) is based partly on Kinniken's
VictoriaEditor (http://www.kstudio.net/vedit/). All else is entirely my own
work.
Big thanks go to seboden. He has been most helpful in not just finding bugs,
but also suggesting fixes.
Thanks also to Kurper and jdrou for making suggestions for the Divine Wind
version.
Thanks to everyone who posted feedback and suggestions in the forum thread.

-- MichaelM

