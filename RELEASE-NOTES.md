# Oxalis release notes
Release notes were first introduced as of version 3.0-Alpha, newest on top.

## 4.0
A major rewrite.

* Support for START and SOAP has been removed. No need to install Metro
* Java 8 required
* New pre-award features like message digest of original payload implemented.
* Unit tests can be executed without  

## 3.2.0


## 3.1.0 (2014-12-02)
Support for other UBL/BIS formats, supports more databases bugfixes.

* Fixed "poodle" / "SSLv3" issues when communicating with TLS only servers (#197, #187 and #196)
* Fixed issue which allowed sender to override document values in production when sending (#191)
* Fixed ServiceLoader which enables 3rd party raw-statistics implementations (#165)
* Added support for OIOUBL and NESUBL (#184)
* Added raw-statistics support for Microsoft MS-SQL and Oracle databases (#177 and #195)
* Improved TransmissionResponse returns more details, like endpoint and protocol used (#132)
* Changed Maven grouping to no.difi.oxalis, allows for Maven Central later (#189)

## 3.0.2 (2014-11-06)
Mostly a bugfix and a few improvements.

* Added proxy handling (#172)
* Requesting TLS context when doing outbound https (#187)
* Added support for user properties in global properties file (#188)
* Fixed filename normalization (#192)
* Added support for BASE64 encoded MDN
* Integration tests moved to latest Apache Tomcat v7.0.56


## 3.0.1 (2014-08-22)
Mostly a bugfix correctly identifying sender / receiver from various document types.

* Rewritten meta data extraction from BIS/EHF documents
* Correctly identifying sender / receiver for BIS/EHF document types
* Fixed bugs (including blocking GitHub issue #116)
* Added more test files, covering most document types, improved logging and exceptions
* Integration tests moved to latest Apache Tomcat v7.0.55


## 3.0.0 (2014-06-04)
Oxalis was released with the following changes since 2.x.

* Support for both START and AS2 transport protocols
* Support for new EHF and BIS formats based on UBL 2.1
* Inbound persistence defaults to store transport metadata as JSON file
* Inbound persistence defaults to store full payload as XML file (full SBDH for AS2)
* Outbound `TransmissionRequestBuilder` simplifies sending when using Oxalis as API
* Request and response debugging controllable with `TransmissionRequestBuilder` (AS2)


## 3.0-Beta (2014-05-26)
The `MessageRepository` interface has been changes since 2.x, so those who have made their own persistence
module for storing inbound messages in a database, in a workflow engine, to a queue system etc will have
to adjust their code.  See `SimpleMessageRepository.java` for example example.

With proper tuning the interoperability tests have shown that Oxalis handles files of 50MB and 100MB both
inbound and outbound using AS2.


## 3.0-Alpha (2013-12-10)
Oxalis version 3.x and upwards is not backwards compatible with version 2.x in terms of the API.

Interoperability between 2.x and 3.x using the START protocol should work fine.

However; due to the introduction of the new AS2 protocol, various interfaces had to be moved in order to prevent chaos.

All modules named `oxalis-start-xxxxx` have been renamed to `oxalis-xxxxx`.

The components used by PEPPOL authorities to collect statistics, have been refactored out of this project as it is of no concern to an Access Point.

As usual, have a look at `oxalis-standalone` to get an idea of how to transmit outbound messages.

Inbound messages are handled by `oxalis-inbound` as before.

Switching between START and AS2 is performed "automagically". However; as a programmer you may override the selection of protocol at your discretion.

Have fun!
