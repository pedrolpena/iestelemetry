echo off

javac -source 1.6 -target 1.6 -d .\ -cp .\lib\appframework-1.0.3.jar;.\lib\RXTXcomm.jar;.\lib\swing-worker-1.1.jar src\iestelemetry\*.java
mkdir iestelemetry\resources
xcopy src\iestelemetry\resources iestelemetry\resources /E/Y
jar cfm IESTelemetry.jar manifest.txt iestelemetry\*.class 
jar vfu IESTelemetry.jar iestelemetry\resources

IF EXIST .\dist goto deletedist

:deletedist
del /q /s .\dist  > nul
rmdir /q /s .\dist  > nul
:exit

mkdir .\dist
mkdir .\dist\lib
move /y IESTelemetry.jar .\dist > nul
copy /y .\lib .\dist\lib > nul
del /s /q .\iestelemetry  > nul
rmdir /s /q .\iestelemetry  > nul


