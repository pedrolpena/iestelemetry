#!/bin/bash
javac -source 1.6 -target 1.6 -d ./ -cp ./lib/appframework-1.0.3.jar:./lib/RXTXcomm.jar:./lib/swing-worker-1.1.jar src/iestelemetry/*.java
cp -R src/iestelemetry/resources iestelemetry
jar cfm IESTelemetry.jar manifest.txt iestelemetry/*.class 
jar vfu IESTelemetry.jar iestelemetry/resources
if [ -d "dist" ]; then
    rm -r dist
fi
mkdir ./dist
rm -r ./iestelemetry
mv ./IESTelemetry.jar ./dist
cp -r ./lib ./dist


