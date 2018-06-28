JC          = javac
JRT         = java
JAR         = jar
JCFLAGS     = -source 1.6 -target 1.6 -d ./
JFLAGS      = -jar
CP          = .:./lib/appframework-1.0.3.jar:./lib/RXTXcomm.jar:./lib/swing-worker-1.1.jar
PACKAGE     = iestelemetry
SOURCEDIR   = src/$(PACKAGE)
FILENAME    = IESTelemetry.jar
INSTALLDIR  = /usr/local
STARTDIR    = /usr/local/sbin
DESKTOPDIR  = $(HOME)/.local/share/applications

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
	mkdir -p dist/lib
	mv  $(FILENAME) dist
	cp ./lib/* ./dist/lib

desktop:
	echo "[Desktop Entry]" > $(PACKAGE).desktop
	echo "Comment=IES Telemetry Application" >> $(PACKAGE).desktop
	echo "Terminal=false" >> $(PACKAGE).desktop
	echo "Name=IES Telemetry Application" >> $(PACKAGE).desktop
	echo "Exec=$(STARTDIR)/$(PACKAGE)" >> $(PACKAGE).desktop
	echo "Type=Application" >> $(PACKAGE).desktop
	echo "Icon=$(INSTALLDIR)/$(PACKAGE)/icon.png" >> $(PACKAGE).desktop
	echo "NoDisplay=false" >> $(PACKAGE).desktop
	echo "Categories=science" >> $(PACKAGE).desktop
run:
	$(JRT) $(JFLAGS) dist/$(FILENAME)

install:
	mkdir -p $(INSTALLDIR)/$(PACKAGE)/lib
	cp dist/$(FILENAME) $(INSTALLDIR)/$(PACKAGE)
	cp dist/lib/* $(INSTALLDIR)/$(PACKAGE)/lib
	echo "#!/bin/bash" > $(STARTDIR)/$(PACKAGE)
	echo "java -jar $(INSTALLDIR)/$(PACKAGE)/$(FILENAME)" >> $(STARTDIR)/$(PACKAGE)
	chmod +x $(STARTDIR)/$(PACKAGE)
	cp icon.png $(INSTALLDIR)/$(PACKAGE)
	cp $(PACKAGE).desktop $(DESKTOPDIR)
	chown $(SUDO_USER):$(SUDO_USER) $(DESKTOPDIR)/$(PACKAGE).desktop
	
uninstall:
	$(RM) $(STARTDIR)/$(PACKAGE)
	$(RM) $(INSTALLDIR)/$(PACKAGE)/lib/*
	rmdir $(INSTALLDIR)/$(PACKAGE)/lib
	$(RM) $(INSTALLDIR)/$(PACKAGE)/$(FILENAME)
	$(RM) $(INSTALLDIR)/$(PACKAGE)/icon.png
	rmdir $(INSTALLDIR)/$(PACKAGE)
	
clean:
	$(RM) -r ./$(PACKAGE)
	$(RM) -r ./dist
	$(RM) $(PACKAGE).desktop
