*************************************************************************
*                                                                       *
* IESTelemetry, a URI CPIES/PIES/IES telemetry program.                 *
* Copyright (C) 2014  Pedro Pena                                        *
*                                                                       *
* This program is free software: you can redistribute it and/or modify  *
* it under the terms of the GNU General Public License as published by  *
* the Free Software Foundation, either version 3 of the License, or     *
* any later version.                                                    *
*                                                                       * 
* This program is distributed in the hope that it will be useful,       *
* but WITHOUT ANY WARRANTY; without even the implied warranty of        *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
* GNU General Public License for more details.                          *
*                                                                       *
* You should have received a copy of the GNU General Public License     *
* along with this program.  If not, see <http://www.gnu.org/licenses/>. *
*                                                                       *                                         
*************************************************************************




----------------------------------------------------
**COMPILING INSTALLING AND RUNNING THE FTP PROGRAM**
----------------------------------------------------

To compile, make sure that a java sdk version of at least 1.6 is installed
and that the jar archive tool is installed.

You can check by typing "javac -version"
you should see something like "javac 1.6.0_65"

Type "jar" and your screen should scroll with many jar options.

When you list the contents of the directory, you should see the following.

LICENSE         - GNU GENERAL PUBLIC LICENSE version 3
lib             - directory containing libraries  
makeit.bat      - batch script to compile and archive the program 
makeit.sh       - bash  script to compile and archive the program 
manifest.txt    - info that will be added to the manifest in the resulting jar file
README.md       - description of the program
README.txt      - this document
rxtxSerial.dll  - java serial library for 32/64 bit Microsoft Windows (http://rxtx.qbang.org/)
src             - directory with source files.


In Windows run makeit.bat in linux/unix run makeit.sh .
In linux/unix you will have to make makeit.sh executable.
To make it executable type

"chmod +x makeit.sh"

When done compiling, the ftp program will be placed in the dist directory.

----------------------------
**INSTALLING UNDER WINDOWS**
----------------------------

Make sure that the "rxtxSerial.dll" file is in the classpath.
This file can be placed where your version of java keeps its libraries
or "C:\Windows\System32"


--------------------------
**INSTALLING UNDER LINUX**
--------------------------

The distribution you are using should have this in its repositories.
under ubuntu 14.04 open a terminal and type

sudo apt-get update
sudo apt-get install librxtx-java

This will install the rxtx library. things should be similar under other
distributions. For RHEL 6/7 a specific repository has to be added and a link
to the library has to be made in /lib


To run the program enter the dist directory and type.

"java -jar IESTelemetry.jar "


Keep the IESTelemetry.jar file and the lib folder together.
You can create a directory like "dist" and copy IESTelemetry.jar and the lib folder into it.








