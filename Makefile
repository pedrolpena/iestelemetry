JC = javac
JRT = java
JAR = jar
JCFLAGS = -source 1.6 -target 1.6 -d ./
JFLAGS = -jar
RMDIR = rmdir
CP = .:./lib/appframework-1.0.3.jar:./lib/RXTXcomm.jar:./lib/swing-worker-1.1.jar:./src/iestelemetry/resources

all: build copyres archive dist

build:
	$(JC) $(JCFLAGS) -cp $(CP) src/iestelemetry/*.java

copyres:
	cp -R src/iestelemetry/resources iestelemetry

archive:
	$(JAR) cfm IESTelemetry.jar manifest.txt iestelemetry/*.class 
	$(JAR) vfu IESTelemetry.jar iestelemetry/resources

dist:
	mkdir -p dist/lib
	mv IESTelemetry.jar dist
	cp ./lib/* ./dist/lib
run:
	$(JRT) $(JFLAGS) dist/IESTelemetry.jar

clean:
	$(RM) -r ./iestelemetry
	$(RM) -r ./dist
