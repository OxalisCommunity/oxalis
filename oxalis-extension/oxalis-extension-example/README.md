# Example extension

This is an example extension providing a simple implementation of TransmissionVerifier to log each message (metadata only) to be verified.

This implementation consists of the following files:

* [LoggingTransmissionVerifier](src/main/java/network/oxalis/ext/example/LoggingTransmissionVerifier.java) - The implementation.
* [ExampleModule](src/main/java/network/oxalis/ext/example/ExampleModule.java) - Guice module to make the implementation known to Oxalis.
* [reference.conf](src/main/resources/reference.conf) - Configuration to register the Guice module during Oxalis startup.
* [pom.xml](pom.xml) - Maven configuration to build an extension using Maven.

Extensions are included in classpath as of Oxalis 4.0.0.
The best ways of deploying your Oxalis instance:

* WAR file - Build your own WAR file containing the Oxalis libraries and your extensions for deployment on your servers.
* Oxalis Server - Include extensions and additional libaries in the `ext` folder.
* Docker - Include extensions and additional libraries in `/oxalis/ext/`.

To enable the verifier implementation included in this extension must the following be included in the oxalis configuration file:

```properties
oxalis.transmission.verifier = logging
``` 