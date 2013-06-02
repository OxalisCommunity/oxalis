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

function usage() {
    cat <<EOT

    usage:

    Sends a PEPOL document to a reciever using the supplied URL.

    $0 [-k password] [-f file] [-d doc.type] [-p profile ] [-c channel] [-r receiver] [-s sender] [-u url] [-t]

    "file" denotes the xml document to be sent.
    Optional document type and profile indicates the PEPPOL document type identifier and profile id.
    Optional "channel" indicates the channel to use
    Default receiver is 9908:810017902 (SendRegning)
    Default sender is 9908:810017902 (SendRegning)

    -t trace option, default is off
EOT
}

while getopts k:f:d:p:c:r:s:u:t opt
do
    case $opt in
        t)
            TRACE="-t"
            ;;
		c) 
	    	CHANNEL="$OPTARG"
	    	;;
        *) echo "Sorry, unknown option $opt"
           usage
           exit 4
           ;;
    esac
done

java -jar target/oxalis-standalone.jar \
-kp=peppol \
-f /Users/steinar/Dropbox/SendRegning/bussinessdevelopment/PEPPOL/EHF/ehf-test.xml \
-d "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0" \
-p "urn:www.cenbii.eu:profile:bii04:ver1.0" \
-c "$CHANNEL" \
-r 9908:810017902 \
-s 9908:810017902 \
-u https://localhost:8443/oxalis/accessPointService \
$TRACE

# Other usefull PPIDs:
# ESV = 0088:7314455667781
#  
