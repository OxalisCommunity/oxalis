@echo off
cd %0\..\..

java %JAVA_OPTS% -classpath conf/*;ext/*;lib/* eu.sendregning.oxalis.Main %*