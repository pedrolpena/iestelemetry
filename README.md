# iestelemetry
# A platform independent program to download data from URI CPIES/PIES/IES via acoustic telemetry.
This program has been tested on Windows, Mac OSX and Linux.

Disclaimer
==========
This repository is a scientific product and is not official communication of the National Oceanic and
Atmospheric Administration, or the United States Department of Commerce. All NOAA GitHub project code is
provided on an ‘as is’ basis and the user assumes responsibility for its use. Any claims against the Department of
Commerce or Department of Commerce bureaus stemming from the use of this GitHub project will be governed
by all applicable Federal law. Any reference to specific commercial products, processes, or services by service
mark, trademark, manufacturer, or otherwise, does not constitute or imply their endorsement, recommendation or
favoring by the Department of Commerce. The Department of Commerce seal and logo, or the seal and logo of a
DOC bureau, shall not be used in any manner to imply endorsement of any commercial product or activity by
DOC or the United States Government.


Download executable and/or deb package
--------------------------------------
https://github.com/pedrolpena/iestelemetry/releases
<br>

------------------------
********Data Tab********
------------------------

![IES Telemetry App Data Tab](/images/iesTelemetryAppDataTab.png?raw=true "IES Telemetry App Data Tab")

---------------------------------
********Configuration Tab********
---------------------------------

![IES Telemetry App Configuration Tab](/images/iesTelemetryAppConfigurationTab.png?raw=true "IES Telemetry App Configuration Tab")

-------------------------
***RUNNING THE PROGRAM***
-------------------------

The steps to get this program running are

-Compile the program<br>
-Install a serial port<br>
-Give the user permission to access the serial port<br>
-Run the program<br>

*Instructions to visually edit the forms with Netbeans are included
 at the end

-------------------------
**COMPILING THE PROGRAM**
-------------------------
Compiling is easy if you have apache ant installed.
Just navigate to the iestelemetry folder and type ant.
The program should compile.
If you don't have ant follow the steps below.

To compile, make sure that a java sdk version of at least 1.6 is installed
and that the jar archive tool is installed.

You can check by opening a terminal or command window and typing 
```bash
javac -version
```
you should see something like 
```bash
javac 1.6.0_65
```

Type 
```bash
jar
```
and your screen should scroll with many jar options.

If the above fails, search for how to install the latest Java Development Kit or "JDK" 
for your operating system.

Oracle has binaries for all of the major platforms, however, linux users may
want to use OpenJDK.

When you list the contents of the directory, you should see the following.<br>

documents             - directory containing place holders for equipment manuals and a help file<br>
lib                   - directory containing libraries<br>
librxtxSerial.jnilib  - java serial library for OSX (here for convenience but may not work)<br>
LICENSE               - GNU GENERAL PUBLIC LICENSE version 3<br>
makeit.bat            - batch script to compile and archive the program<br>
makeit.sh             - bash  script to compile and archive the program<br> 
manifest.txt          - info that will be added to the manifest in the resulting jar file<br>
README.md             - description of the program<br>
README.txt            - this document<br>
rxtxSerial.dll        - java serial library for 32/64 bit Microsoft Windows (here for convenience but may not work)<br>
src                   - directory with source files.<br>


Under Windows
```batch
makeit.bat
```
Under linux/Mac OSX
```bash
make
sudo make install
```
When done compiling, the telemetry program will be placed in the dist directory.



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
"/dev/ttyUSB0" <br>

Under OSX the device should be listed in the dev folder as something like this
"/dev/cu.usbserial"<br>
"/dev/tty.usbserial"<br>

Under Windows, the serial port should be listed in device manager under Ports(COM & LPT)
as something like this<br>

"Communications Port (COM1)"<br>



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
or issue this command for the current user (easier)

```bash
sudo usermod -a -G dialout $USER
```


logout and log back in. 

To list groups the current user is part of, type

groups<br>

dialout should be in the list.<br>

-Fedora 22-<br>

sudo usermod -a -G dialout,lock aardvark (replace aardvark with the username)<br>

log out, log in. open a terminal and type<br>

groups<br>

the dialout and lock groups should be in the list.<br>


-----------------------
**RUNNING THE PROGRAM**
-----------------------

To run the program enter the dist directory and type.<br>
```bash
java -jar IESTelemetry.jar
```
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

This program was originally written using the Swing Aplication Framework(SAF) under Netbeans 7.01.<br>
Netbeans has since removed support for the SAF and you will not be able to modify the forms
with any stock version of Netbeans after version 7.01.<br>
To enable SAF modifications, you must install the "Swing Application Framework Support for Form Editor" plugin.
The file is included in this repo in the event it can't be downloaded anymore.<br>
The filename is "1341985500_org-netbeans-modules-swingapp.nbm"<br>

The plugin can be downoaded from the following location.<br>

http://plugins.netbeans.org/plugin/43853/swing-application-framework-support<br>

To install the plugin<br>
Click on "Tools"-->"plugins"<br>
Click on "Downloaded" tab<br>
Click on "Add Plugins..." button<br>
Click on "Istall"<br>

Once installed restart the IDE<br>


This plugin works with the Netbeans 8.2<br>

