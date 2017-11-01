#!/bin/sh

cd $(dirname $(readlink -f $0))/..

java $JAVA_OPTS -classpath conf/*:ext/*:lib/* eu.sendregning.oxalis.Main $@