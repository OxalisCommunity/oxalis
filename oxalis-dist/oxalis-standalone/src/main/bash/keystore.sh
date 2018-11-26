#!/bin/sh

#
# This script was used to construct the keystore and truststore for SendRegning's aksesspunkt
#
# Note: the script will have to be adapted to generate keystores for other participants
#


home=~/Filer/mazeppa/SendRegning/live-certificates
build=build
final_location=$1

our_certificate=unit4-accesspoint.cer
our_private_key=PEPPOL_private.key.encrypted
peppol_certificates=PEPPOL-CA-certificates

keystore_file=$final_location/keystore.jks
truststore_file=$final_location/truststore.jks
keystore_and_key_password=$2

private_key_unencrypted_file=$build/private-key.txt
tmp2=$build/temp2.p12
temporary_password=mypass
alias=1

cd $home


# --------------------------------------------------------------------------
# keystore
# --------------------------------------------------------------------------


if [ ! -f $private_key_unencrypted_file ];
then
	# Decrypts our private key
	openssl des3 -d -salt -in $our_private_key -out $private_key_unencrypted_file
fi

# Reads our certificate together with our private key and exports both of them into a PKCS12 file
openssl pkcs12 -export -in $our_certificate -inkey $private_key_unencrypted_file -out $tmp2 -passout pass:$temporary_password -name $alias

# Imports our private key and certificate from the PKCS12 formatted file into Java keystore
rm $keystore_file
keytool -importkeystore -srckeystore $tmp2 -srcstoretype PKCS12 -srcstorepass $temporary_password -alias $alias -destkeystore $keystore_file -deststorepass $keystore_and_key_password -destkeypass $keystore_and_key_password

rm $tmp2


# --------------------------------------------------------------------------
# truststore - holding the PEPPOL Root and intermediate certificates
# this step is only used by the maintainers of Oxalis
# --------------------------------------------------------------------------

if [ ! -f $truststore_file ];
then
	keytool -import -keystore $truststore_file -storepass $keystore_and_key_password -alias root -file "$peppol_certificates/PEPPOL Root Test CA.cer"
	keytool -import -keystore $truststore_file -storepass $keystore_and_key_password -alias ap -file "$peppol_certificates/PEPPOL Access Point Test CA.cer"
fi

cd $final_location
ls -l
