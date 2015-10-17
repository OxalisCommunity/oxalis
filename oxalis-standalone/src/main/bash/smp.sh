#!/bin/bash

#
# Performs SMP lookup for a given participant id
#
#
# syntax:
#
#   smp -p <participant id> [-g | -d <document type id]
#
# TODO: implement support for -d
#
# Author: Steinar O. Cook
#

declare -r GET_SERVICE_GROUP="GROUP"
declare -r GET_DOC_TYPE_INFO="DOC_TYPE_INFO"

declare -r PRODUCTION_SML=".iso6523-actorid-upis.edelivery.tech.ec.europa.eu"
declare -r TEST_SML=".iso6523-actorid-upis.acc.edelivery.tech.ec.europa.eu"

# Difi test participant id
declare -r DIFI_TEST_PPID="9908:810418052"

# Default behaviour uses the production SML, may be changed with the -t option
declare SML=${PRODUCTION_SML}

# Holds the current option letter
declare opt=""
declare PEPPOL_ID=""

function usage {
    cat <<-EOH

    Usage: $0 -p participant_id [-g] [-d doc_type] [-t]

    -p <peppol participant id> Participant ID (required)
    -g Fetch list of service groups
    -d <document_type_id>  Fetch service meta data for given document type, which may be specified as a regexp
    -t Uses the test SML rather than the production SML

    Sample Participant ID (Difi Test): $DIFI_TEST_PPID

    Test SML (SMK): $TEST_SML

    Example: Does Difi support reception of electronic Tenders in TEST mode?

    $0 -p $DIFI_TEST_PPID -d Tender -t

EOH
}

function hostnameForPEPPOL_ID() {

    hash=`echo "$PEPPOL_ID" |tr "[:upper:]" "[:lower:]"`
    hash=`md5 -qs $hash`	# Creates the MD5 hash

    echo $hash
}

#
# Retrieves a complete list of all the capabilities supported by the supplied PPID.
# The list simply contains all the URLs referencing the complete data set for
# each document type wrapped in XML
#
# Returns an XML formated list
#
function serviceGroups() {
    hash_id=$1

    # Computes the hostname to be looked up
    URL="http://b-${hash_id}${SML}/iso6523-actorid-upis%3A%3A${PEPPOL_ID}"
    echo "URL is $URL" >&2

    service_group_data=`curl "$URL" 2>/dev/null`

    echo $service_group_data | xmllint --format -
}

#
# Retrieves the complete list of all capabilities supported and extracts the URL only
#
# Returns a list of URLs
#
function service_metadata_url_list() {
        service_groups=`serviceGroups "$HASH"`
        href_list=$(echo $service_groups | xpath "//ns2:ServiceMetadataReference/@href" 2>/dev/null |\
        sed -e 's/href="/\
        /g' -e 's/"//g' )

        echo ${href_list}
}

# TODO: make this wretched function actually work :-)
function lookup_doctype() {
    hash=$1
    doctype=$2
    ppid_encoded=`urlencode $PEPPOL_ID`
    URL="http://b-${hash}${SML}/iso6523-actorid-upis%3A%3A${ppid_encoded}/services/busdox-docid-qns"
    echo "Looking up .."
    echo $hash
    d=`urlencode $doctype`
    URL="$URL%3A%3A${d}"
    echo $URL

}


function urldecode() {
    arg="$1"
    i="0"
    while [ "$i" -lt ${#arg} ]; do
        c0=${arg:$i:1}
        if [ "x$c0" = "x%" ]; then
            c1=${arg:$((i+1)):1}
            c2=${arg:$((i+2)):1}
            printf "\x$c1$c2"
            i=$((i+3))
        else
            echo -n "$c0"
            i=$((i+1))
        fi
    done
}

function urlencode() {
  local string="${1}"
  local strlen=${#string}
  local encoded=""

  for (( pos=0 ; pos<strlen ; pos++ )); do
     c=${string:$pos:1}
     case "$c" in
        [-_.~a-zA-Z0-9] ) o="${c}" ;;
        * )               printf -v o '%%%02x' "'$c"
     esac
     encoded+="${o}"
  done
  echo "${encoded}"    # You can either set a return variable (FASTER)
  REPLY="${encoded}"   #+or echo the result (EASIER)... or both... :p
}


#
#     M A I N
#
while getopts "p:gd:t" opt
do
    case "$opt" in
        p)
            echo "PEPPOL_ID is $OPTARG" >&2
            PEPPOL_ID="$OPTARG"
            ;;
        g)
            MODE=$GET_SERVICE_GROUP
            ;;

        d)  MODE=$GET_DOC_TYPE_INFO
            DOC_TYPE="$OPTARG"
            ;;
        t)  SML=${TEST_SML}
            ;;
        :)
            echo "Option -$OPTARG requires an argument" >&2
            exit 4
            ;;
        \?)
            usage
            exit 1
            ;;
        esac
done

# Calculates the hostname by hashing the PEPPOL Participant Id
HASH=`hostnameForPEPPOL_ID "$PEPPOL_ID"`

if [ "$SML" == "$PRODUCTION_SML" ]; then
    echo "Using production SML: $PRODUCTION_SML"
else
    echo "Using test SML: $TEST_SML"
fi

case "$MODE" in

    #
    # Dumps all the DocumentIdentifer strings and the associated ProcessIdentifier
    # to stdout.
    #
    $GET_SERVICE_GROUP)
        url_list=$(service_metadata_url_list)

        for url in $url_list; do
            echo "-------------------------------------------------"
            echo $url
            curl $url 2>/dev/null | xmllint --format - |egrep "(DocumentIdentifier)|(ProcessIdentifier)"
        done | sed -e 's/^.*<.*>urn/urn/g' -e 's/<\/.*>//g'
      ;;

    $GET_DOC_TYPE_INFO)
        # Retrieves list of all supported documents in all processes with all the protocols supported
        url_list=$(service_metadata_url_list)
        found=0
        for url in $url_list; do
            if [[ "$url" =~ $DOC_TYPE ]]; then
                echo $url
                curl $url 2>/dev/null | xmllint --format -
                found=1
            fi
        done
        if [[ "$found" == "0" ]]; then
            echo "No matching entries found for $DOC_TYPE"
            exit 4
        fi
    ;;
esac