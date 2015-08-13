#!/bin/sh
#
# Simple utility for looking up an organisation in the PEPPOL SML after which a lookup in the SMP of the
# organisation is performed.
#
# Author: Steinar Overbeck Cook
# Copyright (c) SendRegning AS 2011
#

# The first and only argument should be the PEPPOL Participant ID
dirname=`dirname $0`

if [ $# -eq 0 ]
then
    cat <<-EOF

	Usage: $0 org_no

	Where org_no represents the 4 digit PEPPOL scheme id followed by ':' and the organisation identification number

	For example: $0 9908:983974724

	EOF
	exit 1
fi

#
# orgno="9902:DK28158815" # Alfa1Lab
#

orgno=$1

# Translates the org eu to lower case as required by PEPPOL
hash=`echo "$orgno" |tr "[:upper:]" "[:lower:]"`
hash=`md5 -qs $hash`	# Creates the MD5 hash

# Request metadata about invoice document in a URL encoded paramter list
params="iso6523-actorid-upis%3A%3A${orgno}/services/busdox-docid-qns%3A%3Aurn%3Aoasis%3Anames%3Aspecification%3Aubl%3Aschema%3Axsd%3AInvoice-2%3A%3AInvoice%23%23urn%3Awww.cenbii.eu%3Atransaction%3Abiicoretrdm010%3Aver1.0%3A%23urn%3Awww.peppol.eu%3Abis%3Apeppol4a%3Aver1.0%3A%3A2.0"

# Computes the hostname to be looked up
HOSTNAME=b-${hash}.iso6523-actorid-upis.edelivery.tech.ec.europa.eu

echo "nslookup $HOSTNAME ----------------------------------------"
# Performs a name server lookup first
# Unfortunately nslookup(1) will have an exit code of 0 eu matter what the result of the lookup is.
# Thus success is determined by not finding the word "can't" in the result
lookup_result=`nslookup $HOSTNAME`
if echo $lookup_result | grep -v "can't"   ; then
    echo "======================================================================"
else
    echo "nslookup failed, perhaps $orgno is not registered in any SMP?"
    exit 4
fi


# Computes the final URL
URL="http://${HOSTNAME}/${params}"

echo $URL >&2

# Fetch the data and format the output
curl "$URL" | xmllint --format -

exit 0
