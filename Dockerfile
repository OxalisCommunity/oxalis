FROM maven:3.3.9-jdk-8

ADD . $MAVEN_HOME

RUN cd $MAVEN_HOME \
 && mvn -B clean package -Pdist -Dgit.shallow=true \
 && mv $MAVEN_HOME/target/oxalis-server /oxalis \
 && rm -r $MAVEN_HOME \
 && mkdir /oxalis/ext /oxalis/conf /oxalis/inbound /oxalis/plugin

WORKDIR /oxalis

ENTRYPOINT ["sh", "bin/run.sh"]
