#!/bin/bash
javac -d ./ -cp ./lib/bsaf.jar:./lib/rxtx-api-2.2-stabilize-SNAPSHOT.jar:./lib/rxtxSerial-2.2-stabilize-SNAPSHOT.jar:./lib/swing-worker-1.1.jar src/iestelemetry/*.java
cp -R src/iestelemetry/resources iestelemetry
cp manifest_1.txt manifest.txt
jar cfm IESTelemetry.jar manifest.txt iestelemetry/*.class 
jar vfu IESTelemetry.jar iestelemetry/resources
if [ -d "dist" ]; then
    rm -r dist
fi
mkdir ./dist
rm -r ./iestelemetry
mv ./IESTelemetry.jar ./dist
cp -r ./lib ./dist


