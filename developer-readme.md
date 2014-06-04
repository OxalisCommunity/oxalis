# Oxalis developer notes

The purpose of this document is to document how to develop and maintain the Oxalis code base

## The Oxalis home directory and configuration

The concept of an "Oxalis home area", was introduced in version 2.0 in order to provide the users of Oxalis with these benefits:

* Maintaining your configuration when installing new releases.
* Several versions of Oxalis may be installed concurrently

The Oxalis home directory is located in the following order:

1. Using the environment variable `OXALIS_HOME`
1. The directory `.oxalis`, located relative to the users home directory. The users home directory is determined by
  inspecting the Java system property `user.home`

### Sample configuration file

There is a sample configuration file located in `oxalis-commons/src/main/resources/oxalis-global.properties`.
You should copy this file to your `OXALIS_HOME` directory and modify it to your likings.

## Unit testing, integration testing etc.

Testing is performed using the TestNG framework.

In order to ensure that Oxalis will compile on machines which have not been prepared with JDBC drivers, configuration files
etc., the various tests involving databases, internet connections etc. (integration tests), have been marked as part of the
test group `integration`:

    @Test(groups = {"integration"})

Furthermore, all such integration tests are excluded from the default maven test execution, which is performed with the
surefire plugin:

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
            <excludedGroups>integration</excludedGroups> <!-- Excludes integration tests -->
        </configuration>
    </plugin>

As a consequence, only the fast running unit tests will run when Oxalis is built using this command:
`mvn clean install`

### Executing integration tests

In order to run the integration tests from the command line:
`mvn -P it-test clean install`

The integration tests expects a completely installed system, with `OXALIS_HOME` directory containing `oxalis-global.properties`, which
references *your keystore*. In addition the MySQL database must be installed with the schema etc.

In the `pom.xml` files, this is achieved by including the following declaration in a profile named *it-test*:

            <profile>
                <!-- Used for integration testing -->
                <id>it-test</id>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-surefire-plugin</artifactId>
                        </plugin>
                    </plugins>
                </build>
            </profile>

Have a look at `oxalis-collector/pom.xml` to see further details.

### Functional tests with a running server (oxalis-integration-test)

As of November 2013, we have started moving these functional tests requiring a running server, to the Maven module `oxalis-integration-test`.

All tests are executed using the [failsafe](http://maven.apache.org/surefire/maven-failsafe-plugin/) Maven plugin,
which causes tests named with a suffix of `IT` to be executed in the
integration-test build phase. Furthermore the web container is started and the artifacts are deployed using the Maven Cargo plugin.
See [Maven Cargo2 plugin](http://cargo.codehaus.org/Maven2+plugin) for further reference.

The basic steps of this module are:

1. Builds the module and runs any applicable unit tests.
1. During the "pre-integration-test" phase; starts up the web container with the use of the "cargo" plugin and deploys
   our ".war" artifacts.
1. The "failsafe" plugin executes all tests with a suffix of `IT` during the "integration-test" phase.
1. Shuts down the web container during the "post-integration-test" phase.
1. Verifies the results during the "verify" phase.

#### Pre-requisites

Your `OXALIS_HOME` directory must exist and should contain a complete configuration, which is specified in `oxalis-global.properties`.
Please consult the installation instructions for further details. Looking at the `oxalis-global.properties` should give you
some clues as to what you need to do :-)

If you are going to run your integration tests with a "Jenkins", you must not forget to install the Metro (JAX-WS) libraries into your
JDK.

If you have a mixed installation with a JDK and a JRE, you should consult the contents of the property `java.endorsed.dirs` to figure out
into which directory you should install the `webservices-api.jar`.

This little program will give you the answer:

    public class P {

    	public static void main(String[] args) {

    		System.out.println(System.getProperty("java.endorsed.dirs"));
    	}
    }

*Note!* The installation of Metro into you JDK/JRE only applies if you are running Java version 1.6 or below.

Here is another trick, which should give you some information as to your build environment:

    mvn help:system


## DataSource and RawStatisticsRepository

All operations related to persistence of statistics are performed by an implementation of `RawStatisticsRepository`. There is only
a single implementation supplied with Oxalis, namely `RawStatisticsRepositoryJdbcImpl`. If you wish to use a different type of repository,
you should roll your own implementation and make it available using the *META-INF/services* idiom

However; since this class is used in standalone and JEE environments, it must be initialized with a `javax.sql.DataSource`,
which may be obtained either via JNDI or manual creation. Oxalis comes with two DataSource implementations:

1. `oxalis-jdbc-dbcp` - will instantiate a JDBC-driver according to the properties in `OXALIS_HOME/oxalis-global.properties` and
wrap that DataSource with Apache DBCP.
1. `oxalis-jdbc-jndi` - which will simply attempt to obtain a datasource from `java:/comp/env/+<whatever_you_defined_in_oxalis-global.properties>`
1. `oxalis-sql` contains the classes which simply expect a DataSource to be available. In addition the SQL-scripts are located here.

In order to make this totally transparent to the calling application, the following pattern should be used:

    // Locates an implementation of RawStatisticsRepositoryFactory using META-INF/services pattern
    RawStatisticsRepostiory repository = RawStatisticsRepostioryFactoryProvider.getInstance().getInstance();

I.e. when packaing your application, assuming you are using the supplied SQL based repository,
simply choose either `oxalis-jdbc-dbcp` or `oxalis-jdbc-jndi` and everything
should work fine. As of version 2.0 we no longer use JNDI at all. The `oxalis-jdbc-jndi` component is simply included for those requiring an implementation
which will obtain a DataSource from JNDI.


## Sending outbound messages

In order to handle multiple protocols, the API for sending documents have changed. This is how you do it:

    // Creates and wires up an Oxalis outbound module (Guice)
    OxalisOutboundModule oxalisOutboundModule = new OxalisOutboundModule();

    // Get the transmission request builder
    TransmissionRequestBuilder builder = oxalisOutboundModule.getTransmissionRequestBuilder();
    
    // construct the transmission request
    builder.payLoad(new FileInputStream("/some/file/to/send/sbdh.xml"));

    // Builds our transmission request
    TransmissionRequest transmissionRequest = builder.build();

    // Gets a transmitter, which will be used to execute our transmission request
    Transmitter transmitter = oxalisOutboundModule.getTransmitter();

    // Transmits our transmission request
    TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);
