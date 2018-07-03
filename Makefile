JC          = javac
JRT         = java
JAR         = jar
JCFLAGS     = -source 1.6 -target 1.6 -d ./
JFLAGS      = -jar
CP          = .:/usr/share/java/bsaf.jar:/usr/share/java/RXTXcomm.jar
PACKAGE     = iestelemetry
MAIN        = IESTelemetryApp
SOURCEDIR   = src/$(PACKAGE)
FILENAME    = IESTelemetry.jar
DESTDIR     =
DESTDIRI    = $(DESTDIR)
DESTDIR_B4  = $(DESTDIR)/..
PREFIX      = $(DESTDIR)/usr/lib
PREFIXI     = $(PREFIX)
STARTDIR    = $(DESTDIR)/usr/bin
STARTDIRI   = $(STARTDIR)
ICONDIR     = $(DESTDIR)/usr/share/doc/$(PACKAGE)
ICONDIRI    = $(ICONDIR)
DESKTOPDIR  = $(DESTDIR)/usr/share/applications
MANDIR      = $(DESTDIR)/usr/share/man/man7
MAKEDEB     = 0

SOURCEFILES = $(SOURCEDIR)/ClearUDB9000DataLogger.java \
              $(SOURCEDIR)/configureDeckBox_DS7000.java \
	          $(SOURCEDIR)/configureDeckBox.java \
	          $(SOURCEDIR)/configureDeckBox_UDB9000_DS7000_Mode.java \
	          $(SOURCEDIR)/configureDeckBox_UDB9000.java \
	          $(SOURCEDIR)/ConvertIncommingFreq2Data.java \
	          $(SOURCEDIR)/DeckBox.java \
	          $(SOURCEDIR)/FileChooserJFrame.java \
	          $(SOURCEDIR)/IESTelemetryAboutBox.java \
	          $(SOURCEDIR)/IESTelemetryApp.java \
	          $(SOURCEDIR)/IESTelemetryView.java \
	          $(SOURCEDIR)/LaunchDesktopDocument.java \
	          $(SOURCEDIR)/ReadSerialPort.java \
	          $(SOURCEDIR)/SendSinglePing.java \
	          $(SOURCEDIR)/SendURICommand.java \
	          $(SOURCEDIR)/SetChannelReceiveSensitivity.java \
	          $(SOURCEDIR)/SetRXThreshold.java



all: build copyres archive dist desktop

build:
	$(JC) $(JCFLAGS) -cp $(CP) $(JDP) $(SOURCEFILES)

copyres:
	cp -R $(SOURCEDIR)/resources $(PACKAGE)

archive:
	$(JAR) cfm $(FILENAME) manifest.txt $(PACKAGE)/*.class 
	$(JAR) vfu  $(FILENAME) $(PACKAGE)/resources

dist:
	mkdir dist
	cp $(FILENAME) dist
	rm $(FILENAME)
desktop:
	echo "[Desktop Entry]" > $(PACKAGE).desktop
	echo "Comment=IES Telemetry Application" >> $(PACKAGE).desktop
	echo "Terminal=false" >> $(PACKAGE).desktop
	echo "Name=IES Telemetry Application" >> $(PACKAGE).desktop
	echo "Exec=$(STARTDIRI)/$(PACKAGE)" >> $(PACKAGE).desktop
	echo "Type=Application" >> $(PACKAGE).desktop
	echo "Icon=$(ICONDIRI)/icon.png" >> $(PACKAGE).desktop
	echo "NoDisplay=false" >> $(PACKAGE).desktop
	echo "Categories=Application;Science;Education" >> $(PACKAGE).desktop
run:
	$(JRT) -cp $(CP):dist/$(FILENAME) $(PACKAGE).$(MAIN)

install:
	mkdir -p $(PREFIX)/$(PACKAGE)
	mkdir -p $(ICONDIR)
	mkdir -p $(STARTDIR)
	mkdir -p $(DESKTOPDIR)
	mkdir -p $(MANDIR)
	
	cp dist/$(FILENAME) $(PREFIX)/$(PACKAGE)
	chmod +x $(PREFIX)/$(PACKAGE)/$(FILENAME)
	echo "#!/bin/bash" > $(STARTDIR)/$(PACKAGE)
	echo "java -cp $(CP):$(PREFIXI)/$(PACKAGE)/$(FILENAME) $(PACKAGE).$(MAIN)" >> $(STARTDIR)/$(PACKAGE)
	chmod +x $(STARTDIR)/$(PACKAGE)
	cp icon.png $(ICONDIR)
	cp copyright $(ICONDIR)
	cp $(PACKAGE).desktop $(DESKTOPDIR)
	gzip -9 --no-name -c changelog > $(ICONDIR)/changelog.gz
	gzip -9 --no-name -c $(PACKAGE).7 > $(MANDIR)/$(PACKAGE).7.gz

ifeq ($(MAKEDEB),1)
	mkdir -p $(DESTDIR)/DEBIAN
	echo "#!/bin/bash" > $(DESTDIR)/DEBIAN/postinst
	echo "set -e" >> $(DESTDIR)/DEBIAN/postinst
	echo 'LIBPATH="/usr/lib/jni"' >> $(DESTDIR)/DEBIAN/postinst
	echo 'LIB="librxtxSerial.so"' >> $(DESTDIR)/DEBIAN/postinst
	echo 'if ! [ -f "/lib/$$LIB"  ] && [ "$$LIBPATH"/"$$LIB" ]; then' >> $(DESTDIR)/DEBIAN/postinst
	echo '    ln -s "$$LIBPATH"/"$$LIB" /lib/$$LIB'>> $(DESTDIR)/DEBIAN/postinst
	echo "fi" >> $(DESTDIR)/DEBIAN/postinst
	chmod +x $(DESTDIR)/DEBIAN/postinst
else

	if [ -f "/usr/lib/jni/librxtxSerial.so" ] && [ ! -f "/lib/librxtxSerial.so" ];then \
	ln -s /usr/lib/jni/librxtxSerial.so /lib/librxtxSerial.so;fi
	
endif
	
uninstall:
	$(RM) $(STARTDIR)/$(PACKAGE)
	$(RM) $(PREFIX)/$(PACKAGE)/$(FILENAME)
	$(RM) $(ICONDIR)/icon.png
	$(RM) $(ICONDIR)/copyright
	$(RM) $(ICONDIR)/changelog.gz
	$(RM) $(MANDIR)/$(PACKAGE).7.gz
	rmdir $(ICONDIR)
	rmdir $(PREFIX)/$(PACKAGE)
	
deb:
	mkdir -p $(DESTDIR)/usr/lib
	mkdir -p $(DESTDIR)/usr/bin
	mkdir -p $(DESTDIR)/usr/share/applications
	mkdir -p $(DESTDIR)/DEBIAN
	
	
	echo "Source: $(PACKAGE)" > $(DESTDIR_B4)/control
	echo "Section: x11" >> $(DESTDIR_B4)/control
	echo "Priority: optional" >> $(DESTDIR_B4)/control
	echo "Maintainer: Pedro Pena <pedro.pena@noaa.gov>" >> $(DESTDIR_B4)/control
	echo "Standards-Version: 3.9.7" >> $(DESTDIR_B4)/control
	echo "Build-Depends: gzip (>=1.5), debhelper (>=9), default-jre | java7-runtime , librxtx-java (>= 2.2pre2-3)," >> $(DESTDIR_B4)/control
	echo "	libbetter-appframework-java" >> $(DESTDIR_B4)/control
	echo "" >> $(DESTDIR_B4)/control
	echo "Package: iestelemetry" >> $(DESTDIR_B4)/control
	echo "Description: Telemeter data from URI IES's" >> $(DESTDIR_B4)/control
	echo " A platform independent program to download data from" >> $(DESTDIR_B4)/control
	echo " URI CPIES/PIES/IES via acoustic telemetry." >> $(DESTDIR_B4)/control
	echo "Architecture: all" >> $(DESTDIR_B4)/control
	echo "Homepage: https://github.com/pedrolpena/iestelemetry" >> $(DESTDIR_B4)/control
	echo "Depends: \$${misc:Depends}, jarwrapper, default-jre | java7-runtime , librxtx-java (>= 2.2pre2-3)," >> $(DESTDIR_B4)/control
	echo " libbetter-appframework-java" >> $(DESTDIR_B4)/control
	
	echo "#!/usr/bin/make -f"  > $(DESTDIR_B4)/rules
	echo "%:" >> $(DESTDIR_B4)/rules
	echo "	dh \$$@"  >> $(DESTDIR_B4)/rules
	echo ""  >> $(DESTDIR_B4)/rules
	echo "binary:"  >> $(DESTDIR_B4)/rules
	echo "	make install DESTDIR=$(DESTDIR) DESTDIRI=/usr PREFIXI=/usr/lib STARTDIRI=/usr/bin MAKEDEB=1"  >> $(DESTDIR_B4)/rules
	echo "	dh_gencontrol"  >> $(DESTDIR_B4)/rules
	echo "	dh_builddeb"  >> $(DESTDIR_B4)/rules
	chmod +x $(DESTDIR_B4)/rules
	
	echo "9" > $(DESTDIR_B4)/compat

	
	cp changelog $(DESTDIR_B4)/changelog
	cp copyright $(DESTDIR_B4)/copyright
	cp LICENSE $(DESTDIR_B4)/LICENSE
	cp license.txt $(DESTDIR_B4)
	cp $(DESTDIR_B4)/postinst $(DESTDIR)/DEBIAN

	
	
clean:
	$(RM) -r ./$(PACKAGE)
	$(RM) -r ./dist
	$(RM) $(PACKAGE).desktop
