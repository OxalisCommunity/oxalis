This repository contains the [PEPPOL](http://www.peppol.eu/) Access Point, named [Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel),
which was developed by [SendRegning](http://www.sendregning.no/).

The Oxalis solution is an enhancement of the PEPPOL Sample Implementation and can be used used as 
a complete standalone PEPPOL solution or as an API from your own code.

Out of the box it persists raw transfer statistics to a database and inbound messages to a filesystem.
Persistence have been modularized so you can provide your own implementation if you need to
send inbound messages to a message queue, a workflow engine, a document archive or others.

It comes with a basic command line tool for sending messages, outbound raw statistics are also persisted to the database.

Binary distributions are available at [Difi](http://vefa.difi.no/oxalis/).


# New features in Oxalis 3

* Support for both START and AS2 transport protocols
* Support for new EHF and BIS formats based on UBL 2.1
* Inbound persistence stores transport metadata as JSON file
* Inbound persistence stores full payload as XML file (whole SBDH for AS2)
* Outbound TransmissionRequestBuilder simplifies sending when using Oxalis as API
 

# Oxalis components

| Component | Type | Description |
| --------- | ---- | ----------- |
| oxalis-inbound    | war  | inbound access point implementation which runs on Tomcat (1) |
| oxalis-outbound   | jar  | outbound component for sending PEPPOL business documents (2) |
| oxalis-standalone | main | command line application for sending PEPPOL business documents (3) |

(1) Receives messages using AS2 or START protocol and stores them in the filesystem as default.

(2) Can be incorporated into any system which needs to send PEPPOL documents.

(3) Serves as example code on how to send a business documents using the oxalis-outbound component.


# Installation

* make sure [Tomcat 7](http://tomcat.apache.org/download-70.cgi) is installed
* make sure [Metro 2.2.1-1](https://metro.java.net/2.2.1-1/) is installed (in Tomcat for inbound and webservices-api.jar as endorsed lib for standalone/outbound)
* make sure [MySQL 5.6](http://www.mysql.com/downloads/mysql/) is installed (the free version is named MySQL Community Server)
* make sure that Tomcat is up and running and that manager is available with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:443 (unless you terminate SSL in front of Tomcat)
* make sure that ''your'' keystore.jks is installed in a known directory (separate instructions for constructing the keystore)
* Create an `OXALIS_HOME` directory and edit the file `oxalis-global.properties`
* Add `OXALIS_HOME` environment variable to reference that directory
* Build Oxalis yourself (see below) or [download the binary artifacts provided by Difi](http://vefa.difi.no/oxalis/)
* Deploy `oxalis.war` to your Tomcat `webapps` directory
* Send a sample invoice; modify `example.sh` to your liking and execute it.
* See the [installation guide](/doc/install/installation.md) for more additional details.
* If you need to modify any of the source code, you are advised to read the [Oxalis developer notes](/developer-readme.md)


# Troubleshooting

* `Provider net.sf.saxon.TransformerFactoryImpl not found` might be an XSLT implementation conflice between Oxalis and the [VEFA validator](https://github.com/difi/vefa-validator-app).  VEFA needs XSLT 2.0 and explicitly set Saxon 9 as the transformer engine to the JVM.  Since Saxon 9 is not used and included with Oxalis you'll end up with that error on the Oxalis side.  To get rid of the error make sure you run Oxalis and VEFA in separate Tomcats/JVM processes.


# Build from source

Note that the Oxalis "head" revision on *master* branch is often in "flux" and should be considered a "nightly build".
The official releases are tagged and may be downloaded by clicking on [Tags](https://github.com/difi/oxalis/tags).

* make sure [Maven 3](http://maven.apache.org/) is installed
* make sure [JDK 6](http://www.oracle.com/technetwork/java/javase/) is installed (the version we have tested with)
* pull the version of interest from [GitHub](https://github.com/difi/oxalis).
* from `oxalis` root directory run : `mvn clean install`
* verify that everything is configured : `mvn clean install -Dit-test` (runs the integration tests)
* locate assembled artifacts in `oxalis-distribution/target/oxalis-distribution-<version.number>-distro/` (after integration tests)

# Miscellaneous notes:

* At `oxalis-standalone/src/main/bash` you will find some shell scripts :
    - `fetch-metatdata.sh` is a freestanding SML + SMP lookup utility (example usage `./fetch-metadata.sh 9908:810017902`)
    - `keystore.sh` contains example commands for constructing keystores and truststores
    - `smp.sh` simple SMP lookup for a given participant id (example usage `./smp.sh -p 9908:810017902 -g`)

# Securing Oxalis

By default Oxalis publish 4 web addresss listed in the table below.  The table describes their use and give some hints on how to secure those addresses.  A pretty standard scenario is to use some kind load balancer and SSL offloader in front of the appserver running Oxalis.  This could be open / free software like [Nginx](http://nginx.org/) and Apache or commercial software like NetScaler and BigIP.  All such front end software should be able to enforce security like the one suggested below.

| URL | Function | Transport | Security |
| --- | -------- | --------- | -------- |
| oxalis/accessPointService | START protocol endpoint | HTTPS with proper certificates | Enable inbound access from Internet |
| oxalis/as2 | AS2 protocol endpoint | HTTPS with proper certificates | Enable inbound access from Internet |
| oxalis/status | Status information, for internal use and debugging | HTTP/HTTPS | Internet access can be blocked |
| oxalis/statistics | RAW statistics for DIFI | HTTPS with proper certificates | Not is active use, can be blocked |
