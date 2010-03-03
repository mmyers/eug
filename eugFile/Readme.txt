To enable auto-generation of the parsers in NetBeans:

1. Download JFlex from http://jflex.de/download.html
2. Place JFlex.jar into Ant's library directory. In the default installation,
   this is $NETBEANS_HOME$\java3\ant\lib.
3. Clean and build EugFile before attempting to build any derivative project.

If you are not planning to modify the parsers, these steps may be omitted.
The .flex files will then be ignored by the compiler (I think).
