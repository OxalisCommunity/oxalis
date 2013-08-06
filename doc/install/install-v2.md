# Oxalis installation

The purpose of this document is to document how to install Oxalis version 2.0 as simple as possbile.

## Prerequisites

* Java JDK 1.6 or later.
* Apache Ant 1.8 or later is needed for the installation of Metro.
* Maven 3.x or later. Make sure you have updated your environment with the `M2_HOME` variable.
* Apache Tomcat version 7.x. If you have a different JEE container, you need to figure out the differences on your own, sorry :-)
* MySQL version 5.1.x or later. This guide assumes a user named "oxalis" with a password of "Blomster2013".

Alle of these must be installed properly, i.e. make sure that the binaries are available from your command line.

## Installation steps


1. Install Tomcat and configure it for SSL on port 443. Please, do not change this port. Most other access points need to communicate with you and their
fascist department (operations) usually frowns upon opening non-standard ports. **Do not use your PEPPOL certificate as an SSL certificate!**

1. Download latest version from https://github.com/difi/oxalis. Unfortunately we are no longer able to provide binary downloadable versions due
to the fact that GitHub no longer supports it.

1. Download and install [Metro 2.2.1-1](https://metro.java.net/2.2.1-1/) You need Apache ant for this. This guide assumes you have made "ant" available
in your execution path. Metro is the SOAP stack being used by Oxalis and is very much required :-)
    1. On a Linux/Unix/Mac: `sudo ant -Dtomcat.home=$TOMCAT_HOME -f metro-on-tomcat.xml install`
    1. On Windows do this: `ant -Dtomcat.home="%TOMCAT_HOME%" -f metro-on-tomcat.xml install`

1. Determine a download directory ("Download directory") and download the Oxalis software distribution.

1. Change directory to the "Download directory" and verify that you see a bunch of directories with a prefix of "oxalis".
You should also see a file named `pom.xml`

1. Compile using maven: `mvn clean install -Dmaven.test.skip`. Assuming your compilation was ok,
    the binary artifacts are now located in
    `oxalis-distribution/target/oxalis-distribution-<your_version>-distro/`

1. Create the oxalis database for storing statistical data as required by the PEPPOL Authority:

        > mysql -u root -p
        Enter password:
        mysql> create database oxalis;
        mysql> grant all on oxalis.* to oxalis@localhost identified by 'Blomster2013';
        mysql> quit
        > cd <oxalis_src_dir>/oxalis-distribution/target/oxalis-distribution-<your_version>-distro/sql
        > mysql -u oxalis -pBlomster2013 oxalis < raw_stats-mysql.sql

1. Create a OXALIS_HOME diretory in which you place files that do not change between new releases of Oxalis.

1. Copy your Oxalis keystore into `OXALIS_HOME`. I personally name this file `oxalis-keystore.jks`.
See the [Oxalis keystore guide](/doc/keystore.md) for further details.

1. Copy and edit the file `oxalis-global.properties` from `oxalis-distribution/target/oxalis-distribution-<your_version>-distro/etc` to
 you `OXALIS_HOME` directory.

1. Copy your keystore holding your private key together with your PEPPOL certificate into `OXALIS_HOME` directory.

1. Copy and edit the sample logback configuration files, just like you did with `oxalis-global.properties`.

1. The value of the property `oxalis.ap.identifier` can be found in the file `access-points.csv`

1. Copy the file `oxalis.war` into your Tomcat deployment directory:

        cp oxalis-distribution/target/oxalis-distribution-2.0.2-distro/oxalis.war /users/steinar/opt/apache-tomcat-7.0.32/webapps

1. Deploy `oxalis.war` to Tomcat

1. Attempt to send a sample invoice using the file `example.sh` file located in `oxalis-standalone`.
 Do not forget to edit the script first!
