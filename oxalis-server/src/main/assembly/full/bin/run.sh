#!/bin/sh

cd $(dirname $(readlink -f $0))/..

java -classpath conf/*:ext/*:lib/* no.difi.oxalis.server.Main $@