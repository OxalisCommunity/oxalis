
----------------------------------------------------------------------------
Normal cycle
----------------------------------------------------------------------------

rm /usr/local/apache-tomcat-7.0.21/logs/oxalis.log
cd /Users/nigel/Filer/mazeppa/SendRegning/oxalis
mvn -Pnmp install
cd /Users/nigel/Filer/mazeppa/SendRegning/oxalis/oxalis-start-server
mvn -Pnmp package -Dmaven.test.skip=true cargo:deployer-undeploy cargo:deployer-deploy
cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 oxalis.log

----------------------------------------------------------------------------
Clean cycle
----------------------------------------------------------------------------

rm /usr/local/apache-tomcat-7.0.21/logs/oxalis.log
cd /Users/nigel/Filer/mazeppa/SendRegning/oxalis
mvn -Pnmp clean install
cd /Users/nigel/Filer/mazeppa/SendRegning/oxalis/oxalis-start-server
mvn -Pnmp package -Dmaven.test.skip=true cargo:deployer-undeploy cargo:deployer-deploy
cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 oxalis.log


cd /Users/nigel/Filer/mazeppa/SendRegning/oxalis/oxalis-start-server
mvn test




cd /Users/nigel/Filer/mazeppa/SendRegning/oxalis
find . -name .svn
find . -name .svn |xargs rm -rf

cd /usr/local/apache-tomcat-7.0.21/bin
/usr/local/apache-tomcat-7.0.21/bin/shutdown.sh
cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 catalina.out

rm /usr/local/apache-tomcat-7.0.21/logs/catalina.out
cd /usr/local/apache-tomcat-7.0.21/bin
/usr/local/apache-tomcat-7.0.21/bin/startup.sh
cd /usr/local/apache-tomcat-7.0.21/logs
tail -f -n 300 catalina.out


cd /Users/nigel/Filer/mazeppa/SendRegning/oxalis
mvn dependency:tree


