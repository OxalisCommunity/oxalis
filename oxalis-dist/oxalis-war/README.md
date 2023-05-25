# Oxalis Web Archive (Oxalis WAR)

The traditional war distribution for application servers recreated to use Java Servlet 3.0 functionality.
This is the distribution made available as `oxalis.war` in `oxalis-distribution`.

It is recommended to create your own web archive (war) if you need to change anything inside the one provided by this project.
The following is a Maven configuration file (`pom.xml`) for your own project where you may customize Oxalis to your needs without having to change any existing artifacts.
This allow for the convenient deployment of a single web archive including your own code.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Replace the following three values according to your preferences. -->
    <groupId>com.example.peppol</groupId>
    <artifactId>oxalis</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!-- Create web archive. -->
    <packaging>war</packaging>

    <properties>
        <!-- Replace with 4.0.3 or newer. -->
        <oxalis.version>4.0.x</oxalis.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>network.oxalis</groupId>
            <artifactId>oxalis-war</artifactId>
            <version>${oxalis.version}</version>
            <classifier>classes</classifier>
        </dependency>
        <!-- Any other extensions or libraries to be included. -->
    </dependencies>

    <build>
        <!-- Resulting file as oxalis.war. -->
        <finalName>oxalis</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
                <configuration>
                    <!-- Allow building without web.xml. -->
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```
