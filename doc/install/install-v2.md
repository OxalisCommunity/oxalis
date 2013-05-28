# Oxalis installation

The purpose of this document is to document how to install Oxalis version 2.0 as simple as possbile.

## Prerequisites

* Java JDK 1.6 or later
* Maven 3.x or later. Make sure you have updated your environment with the `M2_HOME` variable.
* Apache Tomcat version 7.x. If you have a different JEE container, you need to figure out the differences on your own, sorry :-)
* MySQL version 5.1.x or later. This guide assumes a user named "oxalis" with a password of "Blomster2013".

Alle of these must be installed properly, i.e. make sure that the binaries are available from your command line.

## Installation steps


1. Download latest version from https://github.com/difi/oxalis. Unfortunately we are no longer able to provide binary downloadable versions due
to the fact that GitHub no longer supports it.

1. Determine a download directory ("Download directory") and download the Oxalis software distribution.

1. Change directory to the "Download directory" and verify that you see a bunch of directories with a prefix of "oxalis".
You should also see a file named `pom.xml`

1. Compile using maven: `mvn clean install -Dmaven.test.skip`. Assuming your compilation was ok,
    the binary artifacts are now located in
    `oxalis-distribution/target/oxalis-distribution-<your_version>-distro/`

1. Create the oxalis database for storing statistical data as required by the PEPPOL Authority:

        cd <oxalis_src_dir>/oxalis-distribution/target/oxalis-distribution-<your_version>-distro/sql
        > mysql -u oxalis -p Blomster2013
        mysql> create database oxalis;
        mysql> quit

1.
1. Verify that you have configured Tomcat for SSL. Do not use your PEPPOL certificate as the SSL certificate! You must obtain
a separate SSL certificate from one of the well known CAs.

1. Copy the file `oxalis.war` into your Tomcat deployment directory:

        copy oxalis\oxalis-master\oxalis-distribution\target\oxalis-distribution-1.18-SNAPSHOT-distro\jee\oxalis.war \u
        sers\steinar\opt\apache-tomcat-7.0.32\webapps

