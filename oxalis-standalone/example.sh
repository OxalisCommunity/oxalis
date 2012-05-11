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
-p PROCUREMENT \
-c CH1 \
-r 9908:976098897 \
-s 9908:976098897 \
-u https://localhost:8443/oxalis/accessPointService

# Other usefull PPIDs:
# ESV = 0088:7314455667781
#  
