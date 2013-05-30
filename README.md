This repository contains the [PEPPOL](http://www.peppol.eu) Access Point which is being developed by [SendRegning](http://www.sendregning.no).
The project is codenamed [Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel).
The Oxalis solution is an enhancement of the PEPPOL Sample Implementation.

Binary distributions are no longer due to the fact that Github no longer supports downloading binary artifacts.

NOTE! The "head" revision on branch *master* is often in "flux" and should be considered a "nightly build". Please use the
official releases which may be downloaded by clicking on [Tags](https://github.com/difi/oxalis/tags) over at the right hand side.

Oxalis consists of 5 components:

* Oxalis START inbound (war): an access point implementation which runs in Tomcat. The access point receives inbound documents using the START protocol and stores them locally.
* Oxalis START outbound (jar): a component which is able to send PEPPOL business documents. The component may be incorporated into any system which requires to be able to send documents. Documents may be sent with or without SMP lookup to find the remote access point.
* Oxalis standalone (main): enables sending of business documents directly from the command line. Uses START outbound to send documents.

* Oxalis collector: restricted application, which collects statistics from a set of access points. This application is only usable
 for the PEPPOL Authority as it requires a private key to decrypt the responses from the access points.
* Oxalis statistics web: a simple web application allowing for the extraction of aggregated statistical data in CSV format

To install:

* make sure Maven is installed.
* make sure [Tomcat 7](http://tomcat.apache.org/download-70.cgi) and [Metro 2.2.1-1](https://metro.java.net/2.2.1-1/) are both installed
* make sure the Tomcat manager is available with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:443
* make sure that ''your'' keystore.jks is installed in a known directory (separate instructions for constructing the keystore)
* Create an "OXALIS_HOME" directory and edit the file `oxalis-global.properties`. `OXALIS_HOME` environment variable should reference this directory.
* Install MySQL
* Build Oxalis using maven.
* Deploy `oxalis.war` to your Tomcat `webapps` directory.
* Send a sample invoice; modify `example.sh` to your liking and execute it.
* See the [new detailed installation guide for V2.0](/doc/install/install-v2.md) for the gory details.
* If you need to modify any of the source code, you are advised to read the [Oxalis developer notes](/developer-readme.md)

To build from source (which is your only option just now):

* In the oxalis src root directory: `mvn clean install`
* Verify that you have everything configured: `mvn clean install -Dit-test` (runs the integration tests)
* At oxalis-start-inbound: `mvn package -Dmaven.test.skip cargo:deployer-undeploy cargo:deployer-deploy`. This will start the access point in Tomcat.

NOTE! The compiled artifacts can be found in `oxalis/oxalis-distribution/target/oxalis-distribution-<version.number>-distro/`

Miscellaneous notes:

* At `oxalis-standalone/src/main/bash` you will find an assortment of shell scripts:
	- fetch-metatdata.sh` is a freestanding SMP lookup
	- keystore.sh contains commands for constructing keystores and truststores
	- wsimport.sh is used to create the webservices for Oxalis

* While we have tried to improve the Sample Implementation as much as possible, some issues remain:
	- The signature on the SMP lookup reply is verified, however; the chain of trust for the SMP certificate is NOT checked.
	- the outbound code does no check the SSL credentials of the remote access point.
	- the authentication level of the SAML token is fixed for all senders. Probably should be made variable.

	
