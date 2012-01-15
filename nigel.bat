This file contains the notes of Nigel Parker, made while he
was working on the Oxalis project together with Steinar O. Cook

----------------------------------------------------------------------------
Clean cycle
----------------------------------------------------------------------------

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis
mvn clean install


----------------------------------------------------------------------------
Normal cycle
----------------------------------------------------------------------------

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis
mvn -Dmaven.test.skip install
cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis/oxalis-start-inbound
mvn package -Dmaven.test.skip cargo:deployer-undeploy cargo:deployer-deploy
cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis/oxalis-standalone
mvn -Dmaven.test.skip assembly:assembly
cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis/oxalis-standalone/target
java -jar oxalis-standalone.jar -d ORDER -p ORDER_ONLY --kf /usr/local/apache-tomcat-7.0.21/conf/keystore/keystore.jks --kp=peppol -f /Users/nigel/Filer/mazeppa/SendRegning/doc/EHF-faktura.xml -r 9908:976098897 -s 9908:976098897 -u https://localhost:8443/oxalis/accesspointService

java -jar oxalis-standalone.jar

cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 catalina.out


----------------------------------------------------------------------------
Logs
----------------------------------------------------------------------------

cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 oxalis.log

rm /usr/local/apache-tomcat-7.0.21/logs/oxalis.log

----------------------------------------------------------------------------
Git
----------------------------------------------------------------------------

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis
git status
git log
git mv oxalis-start-server oxalis-start-inbound
git commit -a -m 'Rename modules'
git push origin master

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol
git blame README.md
git clone https://github.com/SendRegning/sr-peppol-binary.git


----------------------------------------------------------------------------
Testing
----------------------------------------------------------------------------

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis/oxalis-start-outbound
mvn -Dtest=StressTest test
mvn -Dtest=JaxbContextCacheTest test
mvn test

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis/oxalis-start-inbound
mvn test

<argLine>-Dfile.encoding=ISO-8859-1 -Xms256m -Xmx512m -javaagent:${user.home}/.m2/repository/no/nmp/profil/1.0/profil-1.0.jar=eu</argLine>

----------------------------------------------------------------------------
Tomcat
----------------------------------------------------------------------------

cd /usr/local/apache-tomcat-7.0.21/bin
/usr/local/apache-tomcat-7.0.21/bin/shutdown.sh
cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 catalina.out

rm /usr/local/apache-tomcat-7.0.21/logs/catalina.out
cd /usr/local/apache-tomcat-7.0.21/bin
/usr/local/apache-tomcat-7.0.21/bin/startup.sh
cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 catalina.out


----------------------------------------------------------------------------
Diverse
----------------------------------------------------------------------------

./fetch-metadata.sh 9908:983974724
./fetch-metadata.sh 9902:DK28158815

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis
mvn dependency:tree

cd /Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis
find . -name .svn
find . -name .svn |xargs rm -rf

