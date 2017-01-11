# Work in progress.

FROM maven:3.3.9-jdk-8

ADD . $MAVEN_HOME

RUN cd $MAVEN_HOME \
 && mvn -B clean package \
 && mv $MAVEN_HOME/target /oxalis \
 && rm -r $MAVEN_HOME