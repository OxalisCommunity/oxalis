@echo off
cd %0\..\..

java -classpath conf/*;ext/*;lib/* no.difi.oxalis.server.Main %*