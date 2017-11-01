#!/bin/sh

cd $(dirname $(readlink -f $0))/..

java $JAVA_OPTS -classpath conf/*:ext/*:lib/* no.difi.oxalis.server.Main $@