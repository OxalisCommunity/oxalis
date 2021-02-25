[![Oxalis Master Build](https://github.com/OxalisCommunity/oxalis/workflows/Oxalis%20Master%20Build/badge.svg?branch=master)](https://github.com/OxalisCommunity/oxalis/actions?query=workflow%3A%22Oxalis%20Master%20Build%22)
[![Build Status](https://travis-ci.org/difi/oxalis.svg?branch=release4)](https://travis-ci.org/difi/oxalis)
[![Maven Central](https://img.shields.io/maven-central/v/network.oxalis/oxalis.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22network.oxalis%22%20AND%20a%3A%22oxalis%22)


The contents of this repository is currently in the process of switching ownership to [NorStella Oxalis Community](https://www.oxalis.network/). You as a user of Oxalis may find it interesting to [join the community](https://www.oxalis.network/join) for access to support, roadmap, early access and more. The founding meeting held online, Thursday November 19, 2020, at 08:30–10:30 CET. 

The Oxalis Community annual meeting scheduled to be held on 25th of March 2021. Should you wish to sign up as a member of the Oxalis Community, please use the registration form available from this site: [link](https://www.oxalis.network/join)  

---
# Technical Information
Latest technical news is available at : https://www.oxalis.network/technical-information

---
# Oxalis

This repository contains the [PEPPOL](http://www.peppol.eu/) Access Point, named [Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel),
which was originally developed by Steinar Overbeck Cook, [SendRegning](http://www.sendregning.no/)
and now looked after by the Norwegian agency for Public Management and eGovernment (Difi).

The Oxalis system is an enhancement of the PEPPOL Sample Implementation and can be used used as
a complete standalone PEPPOL solution or as an API component from your own code.

Out of the box it persists inbound messages to the filesystem.
Persistence have been modularized so you can provide your own implementation if you need to
send inbound messages to a message queue, a workflow engine, a document archive or others.

It comes with a basic command line tool for sending messages (```oxalis-standalone```), which has been improved and
is now capable of sending multiple files.

Binary distributions are available at Maven Central.

As of version 4.x Oxalis no longer has any dependency on SQL databases.


## Newest version is Oxalis 4.x

* Inbound persistence stores full payload as XML file (whole SBDH for AS2)
* Fixed potential issues communicating with "POODLE" patched servers
* Support for START and all the horrible SOAP libraries has been removed.
* Supports the latest PEPPOL Security features.
* Uses OASIS BDXL by default and is ready to handle migration from PEPPOL SMP to OASIS BDX SMP.
* Much faster and efficient than Oxalis 3.x.
* Instrumented with Zipkin (Brave library).


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
* Build Oxalis yourself (see below) or download the binary artifacts provided by Difi from [Maven Central](https://search.maven.org)
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

* make sure [Maven 3](http://maven.apache.org/) is installed
* make sure [JDK 8](http://www.oracle.com/technetwork/java/javase/) is installed (the version we have tested with)
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
