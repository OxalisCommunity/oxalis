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
# Removes the directory structure, which we extracted and no longer need
rm -rf WEB-INF

# 4) Edit the configuration file
sudo -u tomcat vi /opt/tomcat/lib/oxalis-web.properties
