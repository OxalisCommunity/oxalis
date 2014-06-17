#!/bin/bash
#
# Author: Steinar O. Cook
#
# Sample program illustrating how a single file may be sent using the stand alone client.
#
# The -t option switches the trace facility on
#
#
TRACE=""
CHANNEL="CH1"

# The default is to send the sample document to our own access point running on our own machine.
URL="https://localhost:8443/oxalis/accessPointService"

# The URL and the METHOD must be synchronized
METHOD="start"

# The AS2 destination system identifier has to be specified when using AS2 (X.509 common name of receiver)
AS2SID=""

FILE="./src/main/resources/BII04_T10_EHF-v1.5_invoice.xml"
DOC_TYPE_OPTION=""
RECEIVER="9908:810017902"
SENDER="9908:810017902"
PROFILE="urn:www.cenbii.eu:profile:bii04:ver1.0"

# Location of the executable program
EXECUTABLE="target/oxalis-standalone.jar"

function usage() {
    cat <<EOT

    usage:

    Sends a PEPOL document to a reciever using the supplied URL.

    $0 [-k password] [-f file] [-d doc.type] [-p profile ] [-c channel] [-m start|as2] [-i as2-identifer] [-r receiver] [-s sender] [-u url|-u 'smp'] [-t]

    -d doc.type optional, overrides the PEPPOL document type as can be found in the payload.

    -f "file"   denotes the xml document to be sent.

    -r receiver optional PEPPOL Participan ID of receiver, default receiver is $RECEIVER (SendRegning)

    -s sender optional PEPPOL Participan ID of sender, default is $SENDER (SendRegning)

    -m method of transmission, either 'start' or 'as2'. Required if you specify a url different from 'smp'

    -i as2 destination system identifier (X.509 common name of receiver when using as2 protocol)

    -u url indicates the URL of the access point. Specifying 'smp' causes the URL of the end point to be looked up
       in the SMP. Default URL is our own local host: $URL

    -t trace option, default is off
EOT

}

while getopts k:f:d:p:c:m:r:s:u:i:t opt
do
    case $opt in
        d)  DOC_TYPE_OPTION="-d $OPTARG"
            ;;
        t)  TRACE="-t"
            ;;
        f)  FILE="$OPTARG"
            ;;
        m)  METHOD="$OPTARG"
            if [[ "$METHOD" != "as2" && "$METHOD" != "start" ]]; then
                echo "Only 'as2' or 'start' are valid protocols"
                exit 4
            fi
            ;;
	    r)  RECEIVER="$OPTARG"
	        ;;
        s)  SENDER="$OPTARG"
            ;;
	    u)  URL="$OPTARG"
			if [[ "$URL" == "" ]]; then
			    echo "Must specify URL if you use -u option"
			    exit 4
            fi
			;;
	    i)  AS2SID="$OPTARG"
	        ;;
        *)  echo "Sorry, unknown option $opt"
            usage
            exit 4
            ;;
    esac
done

# Verifies that we can read the file holding the XML message to be sent
if [ ! -r "$FILE" ]; then
    echo "Can not read $FILE"
    exit 4;
fi

# Verifies that the .jar file is available to us
if [ ! -r "$EXECUTABLE" ]; then
    echo "Unable to locate the executable .jar file in $EXECUTABLE"
    echo "This script is expected to run from the root of the oxalis-standalone source dir"
    exit 4
fi

# If the user specified a url of 'smp', we simply omit the -u option thus allowing the Java program to perform a
# SMP lookup in order to find the URL of the destination access point
if [ "$URL" == "smp" ]; then
    URL_OPTION=""
    METHOD_OPTION=""    # Uses the default from the SMP
else
    # Uses either the default values at the top of the script or whatever has been supplied on the command line
    URL_OPTION="-u $URL" # Uses either the URL specified by the user or the default one
    METHOD_OPTION="-m $METHOD"
fi

# make sure we decode the AS2 System Identifier
if [ -n "$AS2SID" ]; then
    AS2SID_OPTION="-id $AS2SID"
else
    AS2SID_OPTION=""
fi


cat <<EOT
================================================================================
    Sending...
    File $FILE
    Sender: $SENDER
    Reciever: $RECEIVER
    Destination: $URL
    Method (protocol): $METHOD
================================================================================
EOT

echo "Executing ...."
echo java -jar "$EXECUTABLE" \
    -f "$FILE" \
    -r "$RECEIVER" \
    -s "$SENDER" \
    $DOC_TYPE_OPTION \
    $URL_OPTION \
    $METHOD_OPTION \
    $AS2SID_OPTION \
    $TRACE

# Executes the Oxalis outbound standalone Java program
java -jar "$EXECUTABLE" \
    -f "$FILE" \
    -r "$RECEIVER" \
    -s "$SENDER" \
    $DOC_TYPE_OPTION \
    $URL_OPTION \
    $METHOD_OPTION \
    $AS2SID_OPTION \
    $TRACE

# Other usefull PPIDs:
# ESV = 0088:7314455667781
#  
