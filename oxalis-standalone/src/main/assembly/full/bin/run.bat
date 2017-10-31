@echo off
cd %0\..\..

java -classpath conf/*;ext/*;lib/* eu.sendregning.oxalis.Main %*