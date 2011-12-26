#!/bin/bash
#
# Various useful shell script commands. See also nigel.bat :-)
#
# This script could serve as the basis for an installation program.
#
# Prerequisites:
#   JDK 6
#   Ant
#   Tomcat version 6 or 7 with SSL enabled
#   Your keystore, holding your private key and PEPPOL certificate resides in ~/keystore.jks

TOMCAT_HOME=~/appl/apache-tomcat-7.0.22
export TOMCAT_HOME

if [ ! -d "$TOMCAT_HOME" ]; then
    echo "Seems that TOMCAT_HOME=$TOMCAT_HOME does not reference a directory"
    exit 4
fi

# 1) Install Metro on Ubuntu, the documentation says -Dcatalina.home, which is wrong
sudo ant -Dtomcat.home=$TOMCAT_HOME -f metro-on-tomcat.xml install

# 2) Install your keystore on the machine may I suggest shoving it into the Tomcat installation
#    If you decide to leave it where it is, remember to specify the location in the configuration file
#    as mentioned below
sudo -u tomcat cp ~/keystore.jks $TOMCAT_HOME/conf/keystore

#
# 3) Installs the oxalis-web.properties file into $TOMCAT_HOME/lib
# Extracts the sample file
jar xvf oxalis-org.war WEB-INF/classes/sample-oxalis-web.properties
# Copies it to your tomcat installation, renaming it as we go along
sudo -u tomcat cp WEB-INF/classes/sample-oxalis-web.properties $TOMCAT_HOME/lib/oxalis-web.properties
# Removes the directory structure, which we extracted and eu longer need
rm -rf WEB-INF

#
# 3b) Installs the required jar-files
#
# Copies logback-classic and oxalis-api to $TOMCAT_HOME/shared_lib
cp ~/.m2/repository/ch/qos/logback/logback-classic/0.9.30/logback-classic-0.9.30.jar $TOMCAT_HOME/shared/lib/
cp ~/.m2/repository/org/slf4j/slf4j-api/1.6.2/slf4j-api-1.6.2.jar $TOMCAT_HOME/shared/lib/
cp ~/.m2/repository/ch/qos/logback/logback-core/0.9.30/logback-core-0.9.30.jar $TOMCAT_HOME/shared/lib/

#
# 3c) Modify the $TOMCAT_HOME/conf/catalina.properties file by editing the line starting with "shared.loader" :
cat <<CONF
shared.loader=${catalina.base}/shared/lib,${catalina.home}/shared/lib/*.jar, \
${catalina.base}/shared/lib/oxalis,${catalina.base}/shared/lib/oxalis/*.jar
CONF

# Creates the Oxalis shared directory
mkdir $TOMCAT_HOME/shared/lib/oxalis

# 4) Edit the configuration file
sudo -u tomcat vi $TOMCAT_HOME/lib/oxalis-web.properties


# 5) Add a global JNDI datasource by adding this XML snippet into $TOMCAT_HOME/conf/server.xml, inside
#    the <GlobalNamingResources>:
cat <<END1
	<Resource name="jdbc/sr"
		auth="Container"
		type="javax.sql.DataSource"
		maxActive="100"
		maxIdle="30"
		maxWait="10000"
		username="skrue"
		password="vable"
		driverClassName="com.mysql.jdbc.Driver"
		url="jdbc:mysql://localhost:3306/sendregning?autoReconnect=true"
		removeAbandoned="true"
		removeAbandonedTimeout="60"
		logAbandoned="true"
	/>
END1
#
#

# 6) Configure JNDI DataSource in Tomcat, add this XML snippet into $TOMCAT_HOME/conf/context.xml
#
cat <<END2
    <ResourceLink name="jdbc/peppol-ap" global="jdbc/sr" type="javax.sql.DataSource"/>
END2

#
# NOTE! The global JNDI name needs to correspond to the name you declared in the GlobalNamingResources
