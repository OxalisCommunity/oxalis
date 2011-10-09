#!/bin/sh

if [ ! -d $1 ];
then
    mkdir -p $1
    mkdir -p $2
    wsimport -s $1 -d $2 -p $3 $4 -extension
else
    echo "Wsdl sources already generated"
fi

