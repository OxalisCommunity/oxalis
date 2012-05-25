#!/bin/sh
#
# Author: Steinar O. Cook
#
# Sample program illustrating how a single file may be sent using the stand alone client.
# 

# Set up default receiver, which is SendRegning in this case
export RECEIVER="9908:976098897"

# Location of keystore
export KEYSTORE="/Users/steinar/appl/apache-tomcat-7.0.22/conf/keystore/keystore.jks" 

# Figure out our installation directory relative to invoication path
export INSTALL_DIR=`dirname $0`

# Determines the path of our sample file
export SAMPLE_FILE="$INSTALL_DIR/src/main/resources/BII04_T10_EHF-v1.5_invoice.xml"
if [ ! -r "$SAMPLE_FILE" ]
then
	echo "ERROR: unable to locate $SAMPLE_FILE"
	exit 4
fi

# Verifies that keystore can be read
if [ ! -r "$KEYSTORE" ]; then
	echo "Unable to read $KEYSTORE"
	exit 4
fi

# If another receiver was supplied on the command line, use it.
if [ $# -ge 1 ]
then
	export RECEIVER="$1"
fi

# If another file was specified on the command line, use it.
if [ $# -ge 2 ]
then
    export SAMPLE_FILE=$2
    if [ ! -r "$SAMPLE_FILE" ]; then
        echo "$SAMPLE_FILE can not be read"
        exit 4
    fi
fi

#
# Sends the file using a Document type identificator indicating a Peppol bis4a document (generic european)
# and a process type identificator indicating CEN/BII "Invoice only"
# This is the simplest of them all.
#
java -jar $INSTALL_DIR/target/oxalis-standalone.jar \
-kf $KEYSTORE \
-kp=peppol \
-f $SAMPLE_FILE \
-d "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0" \
-p "urn:www.cenbii.eu:profile:bii04:ver1.0" \
-c CH1 \
-r $RECEIVER \
-s 9908:976098897

# Other usefull PPIDs:
# SendRegning
# -r 9908:976098897 \
#
# ESV = 0088:7314455667781
# EdiGard 9908:994241257
#
