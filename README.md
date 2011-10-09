This repository contains the [PEPPOL](http://www.peppol.eu) Access Point which is being developed by [SendRegning](http://sendregning.no). The project is codenamed [Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel).

Oxalis runs in Tomcat. The initial version simply runs a self-test when it starts. The test verifies that it is possible to send an EHF invoice to itself.  

To install:

* make sure that Tomcat 7 and Metro 2.1.1 are installed
* make sure that the Tomcat manager is available on port 8080 with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:8443
* make sure that truststore.jks and keystore.jks are installed in a known directory (separate instructions for constructing these)
* change oxalis-busdox-commons/src/main/filters/nnn.properties to reflect your local preferences
* cd to oxalis
* mvn install
* cd to oxalis/oxalis-start-server
* mvn package -Dmaven.test.skip cargo:deployer-undeploy cargo:deployer-deploy
* monitor the log file that you specified in the properties file and verify that it eventually reaches the line "Test message successfully dispatched"

