# Oxalis installation

The purpose of this document is to document how to install Oxalis as simple as possible.

## Prerequisites

* Java JDK 1.6 (newer versions should also work)
* [Maven 3+](http://maven.apache.org/download.cgi) (if you plan to build Oxalis yourself)
* [Tomcat 7+](http://tomcat.apache.org/download-70.cgi) (if you have a different JEE container, you need to figure out the differences on your own, sorry :-)
* [MySQL 5.1+](http://www.mysql.com/downloads/mysql/) (the free version is named MySQL Community Server)
* [Ant 1.8+](http://ant.apache.org/bindownload.cgi) (only needed for the Metro installation script)
* [Metro 2.2.1-1](https://metro.java.net/2.2.1-1/) (install in Tomcat for inbound and webservices-api.jar as endorsed lib for standalone/outbound)
* Create `OXALIS_HOME` directory to hold configuration files, certificates etc
* Add `OXALIS_HOME` environment variable to reference that directory

Alle of these must be installed properly, i.e. make sure that the binaries are available from your command line.


## Checklist
When running the following commands you should expect output similar to the one shown

| Verify | Command | Expected output |
| ------ | ------- | --------------- |
| JDK 1.6 | `javac -version` | javac 1.6.0_65 |
| Maven 3 | `mvn -version` | Apache Maven 3.2.1 |
| MySQL 5.1+ | `mysql --version` | mysql  Ver 14.14 Distrib 5.1.71 |
| ANT 1.8+ | `ant -version` | Apache Ant(TM) version 1.9.4 |
| OXALIS_HOME | `echo $OXALIS_HOME` | /Users/thore/.oxalis |


## Installation steps

1. Install Tomcat and configure it for SSL on port 443 or make sure you terminate SSL in front of Tomcat on port 443 (using nginx or similar). Please, do not change this port. Most other access points need to communicate with you and their fascist department (operations) usually frowns upon opening non-standard ports. **Do not use your PEPPOL certificate as an SSL certificate!**

1. Make sure Tomcat starts and stops and manager is available with user manager/manager

1. Install [Metro 2.2.1-1](https://metro.java.net/2.2.1-1/) You need Apache ant for this. This guide assumes you have made "ant" available in your execution path. Metro is the SOAP stack being used by Oxalis and is very much required :-)
    1. On a Linux/Unix/Mac: `sudo ant -Dtomcat.home=$TOMCAT_HOME -f metro-on-tomcat.xml install`
    1. On Windows do this: `ant -Dtomcat.home="%TOMCAT_HOME%" -f metro-on-tomcat.xml install`

1. Obtain the binary artifacts for Oxalis by either:
    1. Downloading the binary artifacts from [DIFI](http://vefa.difi.no/oxalis/) and unpack the distribution. 
    1. Building yourself from the source at [GitHub](https://github.com/difi/oxalis/)

1. Create the oxalis database for storing statistical data as required by the PEPPOL Authority:

1. Create MySQL user named "oxalis" with a password of "Blomster2013"

1. Create MySQL database Oxalis and run the script to create the `raw_stats-mysql.sql

        > mysql -u root -p
        Enter password:
        mysql> create database oxalis;
        mysql> grant all on oxalis.* to oxalis@localhost identified by 'Blomster2013';
        mysql> quit
        > cd <oxalis_src_dir>/oxalis-distribution/target/oxalis-distribution-<your_version>-distro/sql
        > mysql -u oxalis -pBlomster2013 oxalis < raw_stats-mysql.sql

1. Create a OXALIS_HOME diretory in which you place files that do not change between new releases of Oxalis.
   We recommend that you create `.oxalis` in what is considered the home directory of the user running Oxalis. If you
   are using Tomcat, it should be the home directory of the tomcat user.

1. Copy your Oxalis keystore holding your private key together with your PEPPOL certificate into `OXALIS_HOME`. I personally name this file `oxalis-production.jks`.  See the [Oxalis keystore guide](/doc/keystore.md) for further details.

1. Copy and edit the file `oxalis-global.properties` from `oxalis-distribution/target/oxalis-distribution-<your_version>-distro/etc` to your `OXALIS_HOME` directory.

1. Copy and edit the sample logback configuration files, just like you did with `oxalis-global.properties`.

1. Copy the file `oxalis.war` into your Tomcat deployment directory, example :

        cp oxalis-distribution/target/oxalis-distribution-x.y.z/jee/oxalis.war /users/oxalis/apache-tomcat-7.0.56/webapps

1. Start Tomcat, check the logs for any errors and make sure the [oxalis status page](https://localhost:443/oxalis/status) seems right (the URL could be differet for your setup).

1. Attempt to send a sample invoice using the file `example.sh` file located in `oxalis-standalone`.
 Do not forget to edit the script first!

Good luck!
