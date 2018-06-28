JC = javac
JRT = java
JAR = jar
JCFLAGS = -source 1.6 -target 1.6 -d ./
JFLAGS = -jar
SOURCE = src/iestelemetry
PACKAGE = iestelemetry
DEST = IESTelemetry.jar
INSTALLDIR = /usr/local
STARTDIR = /usr/local/sbin
CP = .:./lib/appframework-1.0.3.jar:./lib/RXTXcomm.jar:./lib/swing-worker-1.1.jar


all: build copyres archive dist

build:
	$(JC) $(JCFLAGS) -cp $(CP) $(JDP) $(SOURCE)/*.java

copyres:
	cp -R $(SOURCE)/resources $(PACKAGE)

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
