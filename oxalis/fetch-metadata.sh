#!/bin/sh

dirname=`dirname $0`

if [ $# -eq 0 ]
then
	echo "Usage: $0 org_no"
	exit 1
fi

# orgno="9902:DK28158815"
orgno=$1

# Translates the org no to lower case as required by PEPPOL
hash=`echo "$orgno" |tr "[:upper:]" "[:lower:]"`
hash=`md5 -qs $hash`	# Creates the MD5 hash

# Request metadata about invoice document
params="iso6523-actorid-upis%3A%3A${orgno}/services/busdox-docid-qns%3A%3Aurn%3Aoasis%3Anames%3Aspecification%3Aubl%3Aschema%3Axsd%3AInvoice-2%3A%3AInvoice%23%23urn%3Awww.cenbii.eu%3Atransaction%3Abiicoretrdm010%3Aver1.0%3A%23urn%3Awww.peppol.eu%3Abis%3Apeppol4a%3Aver1.0%3A%3A2.0" 

# Computes the final URL
URL="http://b-${hash}.iso6523-actorid-upis.sml.peppolcentral.org/${params}"

echo $URL >&2

# Fetch the data
curl "$URL"
