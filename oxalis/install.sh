#!/bin/sh
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

# 1) Install Metro on Ubuntu, the documentation says -Dcatalina.home, which is wrong
sudo ant -Dtomcat.home=/opt/tomcat -f metro-on-tomcat.xml install

# 2) Install your keystore on the machine may I suggest shoving it into the Tomcat installation
#    If you decide to leave it where it is, remember to specify the location in the configuration file
#    as mentioned below
sudo -u tomcat cp ~/keystore.jks /opt/tomcat/conf/keystore

#
# 3) Install the oxalis-web.properties file into $TOMCAT_HOME/lib

# Extracts the sample file
jar xvf oxalis-org.war WEB-INF/classes/sample-oxalis-web.properties
# Copies it to your tomcat installation, renaming it as we go along
sudo -u tomcat cp WEB-INF/classes/sample-oxalis-web.properties /opt/tomcat/lib/oxalis-web.properties
# Removes the directory structure, which we extracted and eu longer need
rm -rf WEB-INF

# 4) Edit the configuration file
sudo -u tomcat vi /opt/tomcat/lib/oxalis-web.properties


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


