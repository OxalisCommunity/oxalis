FROM maven:3.3.9-jdk-8 AS mvn

ADD . $MAVEN_HOME

RUN cd $MAVEN_HOME \
 && mvn -B clean package -Pdist -Dgit.shallow=true \
 && mv $MAVEN_HOME/target/oxalis-server /oxalis-server \
 && mv $MAVEN_HOME/target/oxalis-standalone /oxalis-standalone \
 && mkdir -p /oxalis/lib \
 && for f in $(ls /oxalis-server/lib); do \
    if [ -e /oxalis-standalone/lib/$f ]; then \
        mv /oxalis-server/lib/$f /oxalis/lib/; \
        rm /oxalis-standalone/lib/$f; \
    fi; \
 done \
 && mv /oxalis-server/bin /oxalis/bin-server \
 && mv /oxalis-server/lib /oxalis/lib-server \
 && mv /oxalis-standalone/bin /oxalis/bin-standalone \
 && mv /oxalis-standalone/lib /oxalis/lib-standalone \
 && cat /oxalis/bin-server/run.sh | sed "s|lib/\*|lib-server/*:lib/*|" > /oxalis/bin-server/run-docker.sh \
 && chmod 755 /oxalis/bin-server/run-docker.sh \
 && cat /oxalis/bin-standalone/run.sh | sed "s|lib/\*|lib-standalone/*:lib/*|" > /oxalis/bin-standalone/run-docker.sh \
 && chmod 755 /oxalis/bin-standalone/run-docker.sh \
 && mkdir /oxalis/bin /oxalis/conf /oxalis/ext /oxalis/inbound /oxalis/outbound /oxalis/plugin \
 && echo "#!/bin/sh\n\nexec /oxalis/bin-\$MODE/run-docker.sh \$@" > /oxalis/bin/run-docker.sh \
 && find /oxalis -name .gitkeep -exec rm -rf '{}' \;


FROM openjdk:8u191-jre-alpine3.9 as oxalis-base

COPY --from=mvn /oxalis /oxalis

ENV MODE server

FROM oxalis-base as oxalis

VOLUME /oxalis/conf /oxalis/ext /oxalis/inbound /oxalis/outbound /oxalis/plugin

EXPOSE 8080

WORKDIR /oxalis

ENTRYPOINT ["sh", "bin/run-docker.sh"]
