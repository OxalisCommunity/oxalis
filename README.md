This repository contains the [PEPPOL](http://www.peppol.eu) Access Point which is being developed by [SendRegning](http://www.sendregning.no). The project is codenamed [Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel). The Oxalis solution is an enhancement of the PEPPOL Sample Implementation.

The binary distribution is available under "Downloads".

Oxalis consists of 3 components:

* Oxalis START inbound (war): an access point implementation which runs in Tomcat. The access point receives inbound documents using the START protocol and stores them locally.
* Oxalis START outbound (jar): a component which is able to send PEPPOL business documents. The component may be incorporated into any system which requires to be able to send documents. Documents may be sent with or without SMP lookup to find the remote access point.
* Oxalis standalone (main): enables sending of business documents directly from the command line. Uses START outbound to send documents.

To install:

* make sure that Maven is installed.
* make sure that Tomcat 7 and Metro 2.1.1 are installed
* make sure that the Tomcat manager is available on port 8080 with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:8443
* make sure that keystore.jks is installed in a known directory (separate instructions for constructing the keystore)
* change oxalis-commons/src/main/filters/soc.properties to reflect your local preferences

To build:

* At oxalis: mvn clean install
* At oxalis-start-inbound: mvn package -Dmaven.test.skip cargo:deployer-undeploy cargo:deployer-deploy. This will start the access point in Tomcat.
* At oxalis-standalone: mvn -Dmaven.test.skip assembly:assembly. This builds the command line component.
* At oxalis-standalone/target: java -jar oxalis.jar. This gives an overview over the command line options.

Miscellaneous notes:

* At oxalis-start-outbound/src/main/assembly you will find an assortment of shell scripts:
	- fetch-metatdata.sh is a freestanding SMP lookup
	- keystore.sh contains commands for constructing keystores and truststores
	- wsimport.sh is used to create the webservices for Oxalis

* While we have tried to improve the Sample Implementation as much as possible, some issues remain:
	- The signature on the SMP lookup reply is verified, however; the chain of trust for the SMP certificate is NOT checked.
	- the outbound code does no check of the SSL credentials of the remote access point.
	- the authentication level of the SAML token is fixed for all senders. Probably should be made variable.
