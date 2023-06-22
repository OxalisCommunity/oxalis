[![Oxalis Master Build](https://github.com/OxalisCommunity/oxalis/workflows/Oxalis%20Master%20Build/badge.svg?branch=master)](https://github.com/OxalisCommunity/oxalis/actions?query=workflow%3A%22Oxalis%20Master%20Build%22)
[![Maven Central](https://img.shields.io/maven-central/v/network.oxalis/oxalis.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22network.oxalis%22%20AND%20a%3A%22oxalis%22)

---
# Oxalis
[Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel) is the leading open-source software implementation of OpenPeppol eDelivery Access Point (AS4) specifications.
This repository was originally developed by Steinar Overbeck Cook(SendRegning) and later looked after by the Norwegian agency for Public Management and eGovernment (Difi) until March 2020.

Starting November 2020, Oxalis is being maintained by [NorStella Oxalis Community](https://www.oxalis.network/).

## Oxalis Community
Oxalis Community is a not-for-profit organization organized under NorStella Foundation based in Norway, dedicated to the continued support and development of Oxalis, to secure [Peppol](https://peppol.org/about/) compliance and value for its users.

Oxalis Community is facilitated by the foundation NorStella. It is organized according to democratic non-for-profit principles and established as an independent and autonomous part of the NorStella association with independent budgets.

The goals of Oxalis Community:
- Secure sustainability and managed development of the Oxalis software
- Encourage continued implementation of eProcurement using Peppol specifications.
- Support innovative Peppol-based services that promotes the goal of harmonized and interoperable processes.

Oxalis can be used either as a complete standalone PEPPOL solution or as an API component from your own code. Standalone component (```oxalis-standalone```) comes with a basic command line tool for sending messages.
It persists inbound messages to the filesystem Out of the box. 
Persistence have been modularized so you can provide your own implementation if you need to send inbound messages to a message queue, a workflow engine, a document archive or others.

Binary distributions is available both at [Maven Central](https://repo1.maven.org/maven2/network/oxalis/) and [GitHub](https://github.com/OxalisCommunity/oxalis/releases). 

## The Latest version is Oxalis 6.0.0

Java 11 is minimum supported Java version since Oxalis 6.0.0. For detailed documentation, refer: [main.adoc](/doc/main.adoc)


# Technical Information
The Latest technical news is available at : https://www.oxalis.network/technical-information

---
# Are you Contributor?
We are actively looking for contributors who can contribute to Oxalis and associated Git repositories. You can start fixing issues by selecting any existing issue or you can add new feature. Please refer [Pull request Checklist](/pull_request_template.md) while generating new pull request. Team will review your code, if it will meet desired goal, and will be according to standards and guidelines then it will be merged to master.

---
## Oxalis components

| Component | Type | Description |
| --------- | ---- | ----------- |
| oxalis-inbound    | war  | Inbound access point implementation which runs on Tomcat (1) |
| oxalis-outbound   | jar  | Outbound component for sending PEPPOL business documents (2) |
| oxalis-standalone | main | Command line application for sending PEPPOL business documents (3) |

(1) Receives messages using AS2 protocol and stores them in the filesystem as default.

(2) Can be incorporated into any system which needs to send PEPPOL documents.

(3) Serves as example code on how to send a business documents using the oxalis-outbound component.


## Installation

* make sure the latest version of Tomcat is installed. See [installation guide](/doc/installation.md) for additional details.
* make sure that Tomcat is up and running and that manager is available with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:443 (unless you terminate SSL in front of Tomcat)
* make sure that ''your'' keystore is installed in a known directory (separate instructions for constructing the keystore)
* Create an `OXALIS_HOME` directory and edit the file `oxalis.conf`
* Add `OXALIS_HOME` environment variable to reference that directory
* Build Oxalis yourself (see below) or download the binary artifacts provided by Norstella from [Maven Central](https://search.maven.org)
  Search for "oxalis" and download the latest version of `oxalis-distribution`.
* Deploy `oxalis.war` to your Tomcat `webapps` directory
* Send a sample invoice; modify `example.sh` to your liking and execute it.
* See the [installation guide](/doc/installation.md) for more additional details.
* To install or replace the PEPPOL certificate, see the [keystore document](/doc/keystore.adoc).
* Oxalis is meant to be extended rather than changing the Oxalis source code.


## Troubleshooting

* `Sending failed ... Received fatal alert: handshake_failure` happens when Oxalis cannot establish HTTPS connection with the remote server.  Usually because destination AccessPoint has "poodle patched" their HTTPS server.  Oxalis v3.1.0 contains fixes for this, so you need to upgrade.  See the https://github.com/OxalisCommunity/oxalis/issues/197 for more info.

* `Provider net.sf.saxon.TransformerFactoryImpl not found` might be an XSLT implementation conflice between Oxalis and the [VEFA validator](https://github.com/difi/vefa-validator-app).  VEFA needs XSLT 2.0 and explicitly set Saxon 9 as the transformer engine to the JVM.  Since Saxon 9 is not used and included with Oxalis you'll end up with that error on the Oxalis side.  To get rid of the error make sure you run Oxalis and VEFA in separate Tomcats/JVM processes.

* `ValidatorException: PKIX path building failed` is probably because the receivers SSL certificate does not contain the correct certificate chain.  The AS2 implementation needs to validate the SSL certificate chain and any intermediate certificates needs to be present.  See the https://github.com/OxalisCommunity/oxalis/issues/173 for more info.

* `Internal error occured: null` when receiving might be due to a bug in some
   Apache Tomcat versions.  The full error message logged is `ERROR [network.oxalis.as2.inbound.As2Servlet] [] Internal error occured: null` followed by a stack trace with `java.lang.NullPointerException: null`.  To resolve this upgrade Tomcat to a newer version, take a look at https://github.com/OxalisCommunity/oxalis/issues/179 for more details.


## Build from source

Note that the Oxalis "head" revision on *master* branch is often in "flux" and should be considered a "nightly build".
The official releases are tagged and may be downloaded by clicking on [Tags](https://github.com/OxalisCommunity/oxalis/tags).

* make sure [Maven 3+](http://maven.apache.org/) is installed
* make sure [JDK 11](http://www.oracle.com/technetwork/java/javase/) is installed (the version we have tested with)
* pull the version of interest from [GitHub](https://github.com/OxalisCommunity/oxalis).
* from `oxalis` root directory run : `mvn clean install -Pdist`
* locate assembled artifacts in `oxalis-dist/oxalis-distribution/target/oxalis-distribution-<version.number>-distro/`


## Securing Oxalis

By default Oxalis publish the web addresss listed in the table below.  
The table describes their use and give some hints on how to secure those addresses.  
A pretty standard scenario is to use some kind of load balancer and SSL offloader in front of the appserver running Oxalis.  
This could be free/open software like [Nginx](http://nginx.org/) and Apache or commercial software like NetScaler and BigIP.  
All such front end software should be able to enforce security like the one suggested below.

| URL | Function | Transport | Security |
| --- | -------- | --------- | -------- |
| oxalis/as2 | AS2 protocol endpoint | HTTPS with proper certificates | Enable inbound access from Internet |
| oxalis/status | Status information, for internal use and debugging | HTTP/HTTPS | Internet access can be blocked |
| oxalis/statistics | RAW statistics for DIFI | HTTPS with proper certificates | Used by DIFI to collect statistics |
