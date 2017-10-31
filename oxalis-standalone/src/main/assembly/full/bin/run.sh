#!/bin/sh

cd $(dirname $(readlink -f $0))/..

java -classpath conf/*:ext/*:lib/* eu.sendregning.oxalis.Main $@