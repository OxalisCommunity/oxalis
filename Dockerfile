FROM maven:3.3.9-jdk-8 as mvn

ADD . $MAVEN_HOME

RUN cd $MAVEN_HOME \
 && mvn -B clean package -Pdist -Dgit.shallow=true \
 && mv $MAVEN_HOME/target/oxalis-server /oxalis \
 && find /oxalis -name .gitkeep -exec rm -rf '{}' \;



FROM java:8-jre-alpine

COPY --from=mvn /oxalis /oxalis

WORKDIR /oxalis

ENTRYPOINT ["sh", "bin/run.sh"]
