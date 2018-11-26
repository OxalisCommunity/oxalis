@echo off
cd %0\..\..

java %JAVA_OPTS% -classpath conf/*;lib/*;ext/* eu.sendregning.oxalis.Main %*