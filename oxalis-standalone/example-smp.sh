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
if [ $# = 1 ]
then
	export RECEIVER="$1"
fi



java -jar $INSTALL_DIR/target/oxalis-standalone.jar \
-kf $KEYSTORE \
-kp=peppol \
-f $SAMPLE_FILE \
-p INVOICE_ONLY \
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
