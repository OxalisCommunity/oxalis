The contents of this repository is currently in the process of switching ownership to [NorStella Oxalis Community](https://www.oxalis.network/). You as a user of Oxalis may find it interesting to [join the community](https://www.oxalis.network/join) for access to support, roadmap, early access and more. The founding meeting is held online, Thursday November 19, 2020, at 08:30–10:30 CET. Deadline for registration of participation is Thursday November 13, 2020.

---
# Important Information about U-NAPTR regular expression change
| Update from CEF-EDELIVERY-SUPPORT |
| --------------------------------- | 
| Special warning:                  |
| At release time, the U-NAPTR records will stay compliant with Oasis BDXL examples '$.*^'. The transition to the regular expression '.*' as defined in RFC 4848, will be executed after the 15th of September 2021. In case you are using U-NAPTR records in the Dynamic Discovery process, please validate/test the dynamic discovery function with U-NAPTR records in the acceptance environment before the 15th of September 2021. In the SML acceptance environment SMK, the U-NAPTR values are already compliant with RFC 4848. | 

There is change in the way NAPTR records handled in SMK/SML now. This information based on email from OpenPeppol to the eDec mailing list with subject ”eDEC-Dev Digest, Vol 86, Issue 2”. This change deployed in SMK on 19th January 2021 and will be deployed in SML on 16th February 2021

Technical Change by CEF: Replaced NAPTR regular expression part from ^.*$ to .* to fulfil the requirements of RFC 4848 as specified in the OASIS BDXL specification.

Above change already affected Peppol outbound traffic in Test environment (SMK) for all Oxalis users unless below property set in "oxalis.conf" configuration file:
`lookup.locator.class=no.difi.vefa.peppol.lookup.locator.BusdoxLocator`

The issue reported in Oxalis github repo with possible solution: https://github.com/OxalisCommunity/oxalis/issues/498

Found that starting with Oxalis version [Oxalis 4.1.0](https://github.com/OxalisCommunity/oxalis/releases/tag/oxalis-4.1.0) (which is internally using vefa-peppol library version [1.1.3](https://github.com/OxalisCommunity/vefa-peppol/releases/tag/1.1.3)) by default started using NAPTR based participant lookup by taking into account possible future migration from CNAME based lookup to NAPTR based lookup. NAPTR based lookup is Not according to current Peppol SML specification which only mandates the usage of the CNAME lookup.

Oxalis community is working on new release where it is changing default behavior of Oxalis to CNAME based lookup.

Taking into consideration the CEF schedule deployment of NAPTR change in SML on 16th February 2021, it may not be possible to quality test and release version of Oxalis containing this bug fix after Norstella took over the ownership.

 
So recommendation is to add below property in "oxalis.conf" configuration file:
`lookup.locator.class=no.difi.vefa.peppol.lookup.locator.BusdoxLocator`

 
We will notify you by various channels once we will fix this bug in Oxalis. 

Looking forward for active support and quick action from your part for above mentioned property change in "oxalis.conf" configuration file in Test environment right now and in Production environment before 16th February 2021 for continued smooth operation.

---

# Oxalis

[![Build Status](https://travis-ci.org/difi/oxalis.svg?branch=release4)](https://travis-ci.org/difi/oxalis)

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

* `Sending failed ... Received fatal alert: handshake_failure` happens when Oxalis cannot establish HTTPS connection with the remote server.  Usually because destination AccessPoint has "poodle patched" their HTTPS server.  Oxalis v3.1.0 contains fixes for this, so you need to upgrade.  See the https://github.com/difi/oxalis/issues/197 for more info.

* `Provider net.sf.saxon.TransformerFactoryImpl not found` might be an XSLT implementation conflice between Oxalis and the [VEFA validator](https://github.com/difi/vefa-validator-app).  VEFA needs XSLT 2.0 and explicitly set Saxon 9 as the transformer engine to the JVM.  Since Saxon 9 is not used and included with Oxalis you'll end up with that error on the Oxalis side.  To get rid of the error make sure you run Oxalis and VEFA in separate Tomcats/JVM processes.

* `ValidatorException: PKIX path building failed` is probably because the receivers SSL certificate does not contain the correct certificate chain.  The AS2 implementation needs to validate the SSL certificate chain and any intermediate certificates needs to be present.  See the https://github.com/difi/oxalis/issues/173 for more info.

* `Internal error occured: null` when receiving might be due to a bug in some
   Apache Tomcat versions.  The full error message logged is `ERROR [no.difi.oxalis.as2.inbound.As2Servlet] [] Internal error occured: null` followed by a stack trace with `java.lang.NullPointerException: null`.  To resolve this upgrade Tomcat to a newer version, take a look at https://github.com/difi/oxalis/issues/179 for more details.


## Build from source

Note that the Oxalis "head" revision on *master* branch is often in "flux" and should be considered a "nightly build".
The official releases are tagged and may be downloaded by clicking on [Tags](https://github.com/difi/oxalis/tags).

* make sure [Maven 3](http://maven.apache.org/) is installed
* make sure [JDK 8](http://www.oracle.com/technetwork/java/javase/) is installed (the version we have tested with)
* pull the version of interest from [GitHub](https://github.com/difi/oxalis).
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
