echo off

javac -d .\ -cp .\lib\bsaf.jar;.\lib\nrjavaserial.jar;.\lib\swing-worker-1.1.jar src\iestelemetry\*.java
mkdir iestelemetry\resources
xcopy src\iestelemetry\resources iestelemetry\resources /E/Y
copy manifest_1.txt manifest.txt
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


