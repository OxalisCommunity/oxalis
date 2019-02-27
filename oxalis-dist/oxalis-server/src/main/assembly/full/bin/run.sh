#!/bin/sh

cd $(dirname $(readlink -f $0))/..

java $JAVA_OPTS -classpath conf/*:lib/*:ext/* no.difi.oxalis.server.Main $@