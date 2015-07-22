LanDiscovery is a plugin that broadcasts a server over lan.

Useful resources
----------------
- https://github.com/SpoutDev/Vanilla/blob/master/src/main/java/org/spout/vanilla/protocol/LANThread.java Implementation of server broadcast

Format is `[MOTD]server mothd[/MOTD][AD]port#[/AD]` -- server ip is the same as the broadcasting host


File locations
--------------
- `src/site/markdown`: Markdown files that will be included in the generated site
- `src/main/java`: Main plugin source files
- `src/test/java`: Test source files

Building
--------
`mvn clean install`. The built jar is located in `target/`
