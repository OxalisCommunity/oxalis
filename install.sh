#!/bin/bash
#
# Various useful shell script commands.
#
# This script could serve as the *basis* for an installation program.
#
# Prerequisites:
#   JDK 6 or higher
#   Ant
#   Tomcat version 7 with SSL enabled
#   Your keystore, holding your private key and PEPPOL certificate resides in ~/keystore.jks

TOMCAT_HOME=~/appl/apache-tomcat-7.0.56
export TOMCAT_HOME

if [ ! -d "$TOMCAT_HOME" ]; then
    echo "Seems that TOMCAT_HOME=$TOMCAT_HOME does not reference a directory"
    exit 4
fi

# 1. Install Metro on Ubuntu, the documentation says -Dcatalina.home, which is wrong
sudo ant -Dtomcat.home=$TOMCAT_HOME -f metro-on-tomcat.xml install


# 1. Edit the configuration file
sudo -u tomcat vi $OXALIS_HOME/oxalis-global.properties


# 1. Add a global JNDI datasource by adding this XML snippet into $TOMCAT_HOME/conf/server.xml, inside
#    the <GlobalNamingResources>:
# This is only to be done if you are going to use JNDI (not recommended)
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

# 1. Configure JNDI DataSource in Tomcat, add this XML snippet into $TOMCAT_HOME/conf/context.xml
#
cat <<END2
    <ResourceLink name="jdbc/peppol-ap" global="jdbc/sr" type="javax.sql.DataSource"/>
END2

#
# NOTE! The global JNDI name needs to correspond to the name you declared in the GlobalNamingResources
