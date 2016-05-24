# Oxalis installation

The purpose of this document is to document how to install Oxalis as simple as possible.

## Prerequisites

* Java JDK 1.8 (newer versions should also work)
* [Maven 3+](http://maven.apache.org/download.cgi) (if you plan to build Oxalis yourself)
* [Tomcat 7+](http://tomcat.apache.org/download-70.cgi) (if you have a different JEE container, you need to figure out the differences on your own, sorry :-)
* [MySQL 5.1+](http://www.mysql.com/downloads/mysql/) (the free version is named MySQL Community Server)
* Create `OXALIS_HOME` directory to hold configuration files, certificates etc
* Add `OXALIS_HOME` environment variable to reference that directory

Alle of these must be installed properly, i.e. make sure that the binaries are available from your command line.


## Checklist
When running the following commands you should expect output similar to the one shown

| Verify | Command | Expected output |
| ------ | ------- | --------------- |
| JDK 1.8 | `javac -version` | javac 1.8.0_45 |
| Maven 3 | `mvn -version` | Apache Maven 3.2.1 |
| MySQL 5.1+ | `mysql --version` | mysql  Ver 14.14 Distrib 5.1.71 |
| OXALIS_HOME | `echo $OXALIS_HOME` | /Users/thore/.oxalis |


## Installation steps

1. Install Tomcat and configure it for SSL on port 443 or make sure you terminate SSL in front of Tomcat on port 443 (using nginx or similar). Please, do not change this port. Most other access points need to communicate with you and their fascist department (operations) usually frowns upon opening non-standard ports. **Do not use your PEPPOL certificate as an SSL certificate!**

1. Make sure Tomcat starts and stops and manager is available with user manager/manager

1. Obtain the binary artifacts for Oxalis by either:
    1. Downloading the binary artifacts from [DIFI](http://vefa.difi.no/oxalis/) and unpack the distribution. 
    1. Building yourself from the source at [GitHub](https://github.com/difi/oxalis/)

1. Create the oxalis database for storing statistical data as required by the PEPPOL Authority:

1. Create MySQL user named "oxalis" with a password of "Blomster2013"

1. Create MySQL database Oxalis and run the script to create the `raw_stats-mysql.sql`

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
  
     Note! The value of the `oxalis.jdbc.class.path` should be a complete URL, not just a path name. I.e.
      
        oxalis.jdbc.class.path=file:///usr/share/tomcat7/.oxalis/mysql-connector-java-5.1.38-bin.jar

1. Copy and edit the sample logback configuration files, just like you did with `oxalis-global.properties`.

1. Copy the file `oxalis.war` into your Tomcat deployment directory, example :

        cp oxalis-distribution/target/oxalis-distribution-x.y.z/jee/oxalis.war /users/oxalis/apache-tomcat-7.0.56/webapps

1. Start Tomcat, check the logs for any errors and make sure the [oxalis status page](http://localhost/oxalis/status) seems right (the URL could be differet for your setup).
   Note! If you intend to terminate TLS in your Tomcat instance, the status pages resides at `https://localhost:443/oxalis/status`

1. Attempt to send a sample invoice using the file `example.sh` file located in `oxalis-standalone`.
   Do not forget to review the script first!
   
## Testing and verifying your installation  

Testing and verification of your installation presupposes that you have performed the actions
as listed above. 
  
 * You have obtained a PEPPOL test certificate.
 * Your configuration file indicates TEST mode.
  
   
### Sending a sample invoice to Difi's test access point

This is how you send a sample invoice to Difi's test access point using the test SML (SMK):
```
java -jar target/oxalis-standalone.jar \
     -f src/main/resources/BII04_T10_PEPPOL-v2.0_invoice.xml \
     -r 9908:810418052 \
     -s 9909:810418052
```

Verify that your sample invoice was received at
[Difi's test access point](https://test-aksesspunkt.difi.no/inbound/9908_810418052/)


### Sending a sample invoice to your own local access point

You need to override the use of the SML/SMP in order to send directly to your own access point.
This is done by specifying a) the URL, b) the protocol and the c) AS2 system identifier.

The AS2 system identifier can be found in your PEPPOL certificate. Executing this command:
```
keytool -list -v -keystore path\to\your\oxalis-keystore.jks
```

Will result in something like this:
```
.... lots of output removed
Alias name: difi_ap
Creation date: 25.jan.2016
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=APP_1000000135, O=DIFI (Oxalis renewal test), C=NO
Issuer: CN=PEPPOL ACCESS POINT TEST CA, OU=FOR TEST PURPOSES ONLY, O=NATIONAL IT AND TELECOM AGENCY, C=DK
Serial number: 682d674303d3171f339eb0a51ac0958
Valid from: Tue Oct 06 02:00:00 CEST 2015 until: Fri Oct 06 01:59:59 CEST 2017
Certificate fingerprints:
.... rest of output removed ....
```
       
What you want is the value of the "CN" attribute, i.e. "APP_1000000135" in the sample.
       
Here is how to send a sample invoice in PEPPOOL Bis 4A profile to your own local access point:
  
````
java -jar target/oxalis-standalone.jar \
     -f src/main/resources/BII04_T10_PEPPOL-v2.0_invoice.xml \
     -r 9908:810418052 \
     -s 9946:ESPAP \
     -u http://localhost:8080/oxalis/as2 \
     -m as2 \
     -i APP_1000000135
````


   
   
Good luck!
