#!/bin/sh
#
# Author: Steinar O. Cook
#
# Sample program illustrating how a single file may be sent using the stand alone client.
# 
java -jar target/oxalis-standalone.jar \
-kf ~/appl/apache-tomcat-7.0.22/conf/keystore/keystore.jks \
-kp=peppol \
-f /Users/steinar/Dropbox/SendRegning/bussinessdevelopment/PEPPOL/EHF/ehf-test.xml \
-d "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0" \
-p "urn:www.cenbii.eu:profile:bii04:ver1.0" \
-c CH1 \
-r 9908:810017902 \
-s 9908:810017902 \
-u https://localhost:8443/oxalis/accessPointService

# Other usefull PPIDs:
# ESV = 0088:7314455667781
#  
