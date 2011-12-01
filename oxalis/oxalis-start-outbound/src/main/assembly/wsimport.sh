#!/bin/sh
#
# Executes the wsimport utility
#
# Invoked by maven-exec-plugin in start-inbound and start-outbound.
# This utility will be phased out, once the jaxws-maven-plugin works as expected.
#
# Deprecated since Dec 1, 2011, jaxws-maven-plugin works beautifully :-)
#
if [ ! -d $1 ];
then
    mkdir -p $1
    mkdir -p $2
    echo wsimport -s $1 -d $2 -p $3 $4 -extension
    wsimport -s $1 -d $2 -p $3 $4 -extension
else
    echo "Wsdl sources already generated"
fi

