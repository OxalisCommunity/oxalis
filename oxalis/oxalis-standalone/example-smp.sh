#!/bin/sh
#
# Author: Steinar O. Cook
#
# Sample program illustrating how a single file may be sent using the stand alone client.
# 
java -jar target/oxalis.jar \
-k ~/appl/apache-tomcat-7.0.22/conf/keystore/keystore.jks \
-p=peppol \
-d ~/Desktop/EHF/ehf-test.xml \
-c CH1 \
-r 9908:976098897 \
-s 9908:976098897
# -u https://localhost:8443/oxalis/accesspointService

# Other usefull PPIDs:
# ESV = 0088:7314455667781
#  
