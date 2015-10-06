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
declare -r GET_SERVICE_META="SERVICE"

declare -r SML=".iso6523-actorid-upis.edelivery.tech.ec.europa.eu"

# Holds the current option letter
declare opt=""
declare PEPPOL_ID=""

function usage {
    cat <<-EOH
    -p <peppol participant id> Participant ID (required)
    -g                     Fetch list of service groups
    -d <document_type_id>  Fetch service meta data for given document type (TODO)
EOH
}

function hostnameForPEPPOL_ID() {

    hash=`echo "$PEPPOL_ID" |tr "[:upper:]" "[:lower:]"`
    hash=`md5 -qs $hash`	# Creates the MD5 hash

    echo $hash
}

function serviceGroups() {
    hash_id=$1

    # Computes the hostname to be looked up
    URL="http://b-${hash_id}${SML}/iso6523-actorid-upis%3A%3A${PEPPOL_ID}"
    echo "URL is $URL" >&2

    service_group_data=`curl "$URL" 2>/dev/null`

    echo $service_group_data | xmllint --format -
}

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


urldecode() {
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

urlencode() {
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
while getopts "p:gd:" opt
do
    case "$opt" in
        p)
            echo "PEPPOL_ID is $OPTARG" >&2
            PEPPOL_ID="$OPTARG"
            ;;
        g)
            MODE=$GET_SERVICE_GROUP
            ;;

        d)  MODE=$GET_SERVICE_META
            DOC_TYPE="$OPTARG"
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


case "$MODE" in

    #
    # Dumps all the DocumentIdentifer strings and the associated ProcessIdentifier
    # to stdout.
    #
    $GET_SERVICE_GROUP)
        service_groups=`serviceGroups "$HASH"`
        url_list=$(echo $service_groups | xpath "//ns2:ServiceMetadataReference/@href" 2>/dev/null |\
        sed -e 's/href="/\
        /g' -e 's/"//g' )

        for url in $url_list; do
            curl $url 2>/dev/null | xmllint --format - |egrep "(DocumentIdentifier)|(ProcessIdentifier)"
       done | sed -e 's/^.*<.*>urn/urn/g' -e 's/<\/.*>//g'
      ;;

    $GET_SERVICE_META)
        lookup_doctype  "$HASH" "$DOC_TYPE"
    ;;
esac