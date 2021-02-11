#!/bin/sh

cd $(dirname $(readlink -f $0))/..

exec java $JAVA_OPTS -classpath conf/*:lib/*:ext/* network.oxalis.server.Main $@