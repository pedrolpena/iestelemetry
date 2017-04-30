************************************************************************
*                                                                      *
* IESTelemetry, a URI CPIES/PIES/IES telemetry program.                *
* Copyright (C) 2015  Pedro Pena                                       *
*                                                                      *
* This program is free software: you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation, either version 3 of the License, or    *
* any later version.                                                   *
*                                                                      * 
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program.  If not, see <http://www.gnu.org/licenses/>.*
*                                                                      *                                         
************************************************************************


The steps to get this program running are

-Compile the program
-Install the rxtx serial library
-Install a serial port
-Give the user permission to access the serial port
-Run the program

*Instructions to visually edit the forms with Netbeans are included
 at the end

-------------------------
**COMPILING THE PROGRAM**
-------------------------

To compile, make sure that a java sdk version of at least 1.6 is installed
and that the jar archive tool is installed.

You can check by opening a terminal or command window and typing "javac -version"
you should see something like "javac 1.6.0_65"

Type "jar" and your screen should scroll with many jar options.

If the above fails, search for how to install the latest Java Development Kit or "JDK" 
for your operating system.

Oracle has binaries for all of the major platforms, however, linux users may
want to use OpenJDK.

When you list the contents of the directory, you should see the following.

documents             - directory containing place holders for equipment manuals and a help file
lib                   - directory containing libraries  
librxtxSerial.jnilib  - java serial library for OSX (here for convenience but may not work)
LICENSE               - GNU GENERAL PUBLIC LICENSE version 3
makeit.bat            - batch script to compile and archive the program 
makeit.sh             - bash  script to compile and archive the program 
manifest.txt          - info that will be added to the manifest in the resulting jar file
README.md             - description of the program
README.txt            - this document
rxtxSerial.dll        - java serial library for 32/64 bit Microsoft Windows (here for convenience but may not work)
src                   - directory with source files.


In Windows run makeit.bat in linux/unix run makeit.sh .
In linux/unix you will have to make makeit.sh executable.
To make it executable type

"chmod +x makeit.sh"

When done compiling, the ftp program will be placed in the dist directory.


--------------------------------------
**INSTALLING THE RXTX SERIAL LIBRARY**
--------------------------------------
RXTX is a Java library, using a native implementation (via JNI), providing 
serial and parallel communication for the Java Development Toolkit (JDK). 
http://rxtx.qbang.org/
Without this library, the program will not be able to communicate with
the serial port.


---------------------------------
**INSTALLING RXTX UNDER WINDOWS**
---------------------------------

Make sure that the "rxtxSerial.dll" file is in the classpath.
This file can be placed where your version of java keeps its libraries
or "C:\Windows\System32"


-------------------------------
**INSTALLING RXTX UNDER LINUX**
-------------------------------

-Ubuntu 14.04-

The distribution you are using should have this available via one of its repositories.
under ubuntu 14.04 open a terminal and type

sudo apt-get update
sudo apt-get install librxtx-java


-Fedora 22-

The following instructions worked under fedora 22 and should work
for redhat & centos(may have to add the epel repository first)

sudo yum install rxtx (yum has been deprecated and replaced by dnf)
sudo dnf install rxtx



The serial library needs to be "/usr/lib" in order for the program
to work. For whatever reason, fedora places the library in "/usr/lib64/rxtx" or "/usr/lib/rxtx"
To fix this, create symlink "/usr/lib/librxtxSerial.so" that points to 
the nstalled library.

Under 64 bit Fedora create a sym link to the rxtx serial library.
Note that the version number of the library can change so look for a file
in the form of "librxtxSerial-VERSION.so"

sudo ln -s /usr/lib64/rxtx/librxtxSerial-2.2pre1.so /usr/lib/librxtxSerial.so


Under 32 bit Fedora create a sym link to the rxtx serial library.
Note that the version number of the library can change so look for a file
in the form of "librxtxSerial-VERSION.so"

sudo ln -s /usr/lib/rxtx/librxtxSerial-2.2pre1.so /usr/lib/librxtxSerial.so



-----------------------------
**INSTALLING RXTX UNDER OSX**
-----------------------------

copy the "librxtxSerial.jnilib" file to 
"/Library/Java/Extensions" directory
Note, you may have to hunt around for a version of this library
compiiled for the version of OSX you are using.
You could also compile rxtx from source.



By this point the program should run even though you may not have a properly installed 
serial port yet.

----------------------------
**INSTALLING A SERIAL PORT**
----------------------------

This program communicates with the deck boxes via an RS232 serial interface.
You must first make sure that a serial port is available and functioning 
under your operating system before attempting to communicate with a deck box.

Serial ports are rarely included with computers these days but one can
purchase inexpensive USB to RS232 serial adapters for this.
Under linux most of these adapters work out of the box without the need to install drivers.

Under Windows and OSX, drivers must be installed.


Once installed you can verify that the operating system has mounted the usb to serial converter.

Under linux the device should be listed in the dev folder as something like this
"/dev/ttyUSB0" 

Under OSX the device should be listed in the dev folder as something like this
"/dev/cu.usbserial"
"/dev/tty.usbserial"

Under Windows, the serial port should be listed in device manager under Ports(COM & LPT)
as something like this

"Communications Port (COM1)"



-----------------------------------------
**PERMISSIONS TO ACCESS THE SERIAL PORT**
-----------------------------------------

-----------------
**UNDER WINDOWS**
-----------------
The serial ports should already be available for use by the user.

---------------
**UNDER LINUX**
---------------

-Ubuntu 14.04-
On ubuntu and other distributions, the user running the program
will not have access to serial ports unless the user is part of the
"dialout" group.

For example, to add user aardvark to the dialout group, open a terminal and type
sudo usermod -a -G dialout aardvark (replace aardvark with the username)

logout and log back in. 

To list groups the current user is part of, type

groups

dialout should be in the list.

-Fedora 22-

sudo usermod -a -G dialout,lock aardvark (replace aardvark with the username)

log out, log in. open a terminal and type

groups

the dialout and lock groups should be in the list.



-------------
**UNDER OSX**
-------------

Access to the serial port is a little more complicated under OSX as it changes across
several different versions. The user needs to be part of the uucp group

These instructions worked under OSX Leopard 10.5

sudo mkdir /var/lock 
sudo chmod 775 /var/lock
sudo dscl . -append /groups/_uucp GroupMembership aardvark (replace aardvark with the username)


These instructions worked under a later version of OSX
sudo mkdir /var/lock 
sudo chmod 777 /var/lock
sudo dseditgroup -o edit -a aardvark (replace aardvark with the username) -t user uucp

Reboot the computer

rxtx uses the /var/lock directory to create lock files for a serial port when in use.
This prevents other programs from trying to use the serial port when in use.
You will likely have trouble at this point. If you have trouble and you figure it out,
please include what you did in this document.

peruse "http://rxtx.qbang.org/wiki/index.php/Trouble_shooting#Mac_OS_X_users" for hints.

If you can't figure it out, you can run the program as the super user by preceding the command
by "sudo"

"sudo java -jar IESTelemetry.jar"

This will work but all the files created by the program will belong to root and you will
have to change the permissions of the created files to access them.

-----------------------
**RUNNING THE PROGRAM**
-----------------------

To run the program enter the dist directory and type.

"java -jar IESTelemetry.jar"

If your Operating system assosciates the jar files with the java virtual machine,
you may be able to just double click on "IESTelemetry.jar" to execute it.


Keep the IESTelemetry.jar file and the lib folder together.
You can create a directory like "dist" and copy IESTelemetry.jar and the lib folder into it.

------------------------------------
**Modifying the code with Netbeans**
------------------------------------
You may encounter an error when first opening the project.
The IDE complains about not having swing application framework support 
and will ask if you want to download and install it. You must
install this to be able to edit the program. This is different than
the Swing Aplication Framework form editor which is discussed below.

This program was originally written using the Swing Aplication Framework(SAF) under Netbeans 7.01
Netbeans has since removed support for the SAF and you will not be able to modify the forms
with any stock version of Netbeans after version 7.01
To enable SAF modifications, you must install the "Swing Application Framework Support for Form Editor" plugin.
The file is included in this repo in the event it can't be downloaded anymore.
The filename is "1341985500_org-netbeans-modules-swingapp.nbm"

The plugin can be downoaded from the following location.

http://plugins.netbeans.org/plugin/43853/swing-application-framework-support

To install the plugin
Click on "Tools"-->"plugins"
Click on "Downloaded" tab
Click on "Add Plugins..." button
Click on "Istall"

Once installed restart the IDE


This plugin works with the Netbeans 8.2





