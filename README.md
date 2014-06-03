This repository contains the [PEPPOL](http://www.peppol.eu/) Access Point, named [Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel),
which was developed by [SendRegning](http://www.sendregning.no/).

The Oxalis solution is an enhancement of the PEPPOL Sample Implementation and can be used used as 
a complete standalone PEPPOL solution or as an API from your own code.

Out of the box it persists raw transfer statistics to a database and inbound messages to a filesystem.
Persistence have been modularized so you can provide your own implementation if you need to
persist inbound messages to a message queue, workflow engine, archive or others instead.

Binary distributions are available at [Difi](http://vefa.difi.no/oxalis/).

# New features in Oxalis v3:

* Support for both START and AS2 transport protocols
* Support for new EHF and BIS formats based on UBL 2.1
* Default inbound persistence store transport metadata as JSON file
* Default inbound persistence store full payload as XML file (the full SBDH for AS2)
* Outbound TransmissionRequestBuilder simplifies sending when using Oxalis as API
 
**NOTE!** The "head" revision on branch *master* is often in "flux" and should be considered a "nightly build".
The official releases are tagged and may be downloaded by clicking on [Tags](https://github.com/difi/oxalis/tags).

# Oxalis consists of 5 components:

* Oxalis START inbound (war): an access point implementation which runs in Tomcat.
    The access point receives inbound documents using the START protocol and stores them locally.
* Oxalis START outbound (jar): a component which is able to send outbound PEPPOL business documents.
    The component may be incorporated into any system which requires to be able to send documents. Documents may be sent with or without SMP lookup to find the remote access point.
* Oxalis standalone (main): enables sending of business documents directly from the command line.
    Uses START outbound to send documents.
* Oxalis collector: restricted application, which collects statistics from a set of access points. This application is only usable
 for the PEPPOL Authority as it requires a private key to decrypt the responses from the access points.
* Oxalis statistics web: a simple web application allowing for the extraction of aggregated statistical data in CSV format.

# Installation

* make sure [Maven 3](http://maven.apache.org/) is installed (if you plan to build Oxalis yourself)
* make sure [Tomcat 7](http://tomcat.apache.org/download-70.cgi) is installed
* make sure [Metro 2.2.1-1](https://metro.java.net/2.2.1-1/) is installed (in Tomcat for inbound and webservices-api.jar as endorsed lib for standalone/outbound)
* make sure [MySQL](www.mysql.com/downloads/mysql/) is installed (the free version is named MySQL Community Server)
* make sure that Tomcat is up and running and that manager is available with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:443 (unless you terminate SSL in front of Tomcat)
* make sure that ''your'' keystore.jks is installed in a known directory (separate instructions for constructing the keystore)
* Create an "OXALIS_HOME" directory and edit the file `oxalis-global.properties`
* Add `OXALIS_HOME` environment variable to reference that directory
* Build Oxalis using Maven or [download the binary artifacts provided by Difi](http://vefa.difi.no/oxalis/)
* Deploy `oxalis.war` to your Tomcat `webapps` directory
* Send a sample invoice; modify `example.sh` to your liking and execute it.
* See the [installation guide](/doc/install/installation.md) for the gory details.
* If you need to modify any of the source code, you are advised to read the [Oxalis developer notes](/developer-readme.md)

# Build from source

* In the oxalis src root directory: `mvn clean install`
* Verify that you have everything configured: `mvn clean install -Dit-test` (runs the integration tests)
* At oxalis-inbound: `mvn package -Dmaven.test.skip cargo:deployer-undeploy cargo:deployer-deploy`. This will start the access point in Tomcat.

NOTE! The compiled artifacts can be found in `oxalis/oxalis-distribution/target/oxalis-distribution-<version.number>-distro/`

Miscellaneous notes:

* At `oxalis-standalone/src/main/bash` you will find an assortment of shell scripts:
	- fetch-metatdata.sh` is a freestanding SMP lookup
	- keystore.sh contains commands for constructing keystores and truststores
	- wsimport.sh is used to create the webservices for Oxalis

* While we have tried to improve the Sample Implementation as much as possible, some issues remain:
	- The signature on the SMP lookup reply is verified, however; the chain of trust for the SMP certificate is NOT checked.
	- The outbound code does no check the SSL credentials of the remote access point.
	- The authentication level of the SAML token is fixed for all senders. Probably should be made variable.

	
