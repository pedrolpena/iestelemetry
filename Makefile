JC          = javac
JRT         = java
JAR         = jar
JCFLAGS     = -source 1.6 -target 1.6 -d ./
JFLAGS      = -jar
PACKAGE     = iestelemetry
SOURCEDIR   = src/$(PACKAGE)
DEST        = IESTelemetry.jar
INSTALLDIR  = /usr/local
STARTDIR    = /usr/local/sbin
CP          = .:./lib/appframework-1.0.3.jar:./lib/RXTXcomm.jar:./lib/swing-worker-1.1.jar

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



all: build copyres archive dist

build:
	$(JC) $(JCFLAGS) -cp $(CP) $(JDP) $(SOURCEFILES)

copyres:
	cp -R $(SOURCEDIR)/resources $(PACKAGE)

archive:
	$(JAR) cfm $(DEST) manifest.txt $(PACKAGE)/*.class 
	$(JAR) vfu  $(DEST) $(PACKAGE)/resources

dist:
	mkdir -p dist/lib
	mv  $(DEST) dist
	cp ./lib/* ./dist/lib
run:
	$(JRT) $(JFLAGS) dist/$(DEST)

install:
	mkdir -p $(INSTALLDIR)/$(PACKAGE)/lib
	cp dist/$(DEST) $(INSTALLDIR)/$(PACKAGE)
	cp dist/lib/* $(INSTALLDIR)/$(PACKAGE)/lib
	echo "#!/bin/bash" > $(STARTDIR)/$(PACKAGE)
	echo "java -jar $(INSTALLDIR)/$(PACKAGE)/$(DEST)" >> $(STARTDIR)/$(PACKAGE)
	chmod +x $(STARTDIR)/$(PACKAGE)
	
uninstall:
	$(RM) $(STARTDIR)/$(PACKAGE)
	$(RM) $(INSTALLDIR)/$(PACKAGE)/lib/*
	rmdir $(INSTALLDIR)/$(PACKAGE)/lib
	$(RM) $(INSTALLDIR)/$(PACKAGE)/$(DEST)
	rmdir $(INSTALLDIR)/$(PACKAGE)
	
clean:
	$(RM) -r ./$(PACKAGE)
	$(RM) -r ./dist
