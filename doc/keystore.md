# Certificates, keys and key stores in Oxalis

The purpose of this document is to guide you in how to set up your PEPPOL certificates in order to make Oxalis "tick".

## What are certificates used for?

PEPPOL has defined a PKI structure which allows for prudent governance of the access points, the SMP's and so on.

Every low level message passed between access points and between the access point and the SMP, are signed with digital certificates.

The PKI structure comes in two releases:

* V1 which is the current (as of May 2013) scheme. It was initially launched as part of the PEPPOL project a couple of
years ago.
* V2 is the new PKI scheme to be implemented and activated during the summer and autumn of 2013.

The idea was to have a "test" and a "production" hierarchy of certificates. However; in the initial release only
 the test certificates were ever issued.

In V2, there will be a "test" and "production" hierarchy of certificates. The PEPPOL test root certificate are identicial
  for V1 and V2.

![Truststore structure](illustrations/truststore.png)

When your certificate is issued by PEPPOL, it will be signed with the *intermediate* AP certificate.

The long and short of this is: you have 3 trust stores in Oxalis holding the following chain of certificates:

1. V1 test certificates, which are also used in production today.
1. V2 test certificates, having the same "root" CA as the V1 certificates.
1. V2 Production certificates, which has an entirely different "root" CA.

## How are they used in Oxalis?

Oxalis comes with all of the three trust stores included.

You need only to supply with your key store, holding your private key and the corresponding PEPPOL certificate with your public key embedded.

This key store, which I refer to as the `oxalis-keystore.jks` should be placed in the `OXALIS_HOME`
directory and references in your `oxalis-global.properties`


## How do I obtain a PEPPOL certificate for my Access point?

1. Sign a Transport Infrastructure Agreement (TIA) with a PEPPOL authority. Once that is done, you will receive instructions on how
to submit a certificate signing request (CSR).
1. Create your own keystore `oxalis-keystore.jks` holding your private key and your self-signed certificate
1. Send a Certificate Signing Request (CSR) to PEPPOL.
1. You will receive a signed certificate with your public key.
1. Import the signed certificate into the key store (`oxalis-keystore.jks`)
1. Copy the `oxalis-keystore.jks` to your OXALIS_HOME directory.
1. Verify the configuration entry in `oxalis-global.properties`


## How do I manage the transition from PKI version 1 to version 2?

There are three properties in the `oxalis-global.properties` file, which controls which certificates are used when a)
signing and sending a message and b) receiving a message:

1. `oxalis.keystore` - references the certificate used when **signing** and **sending** a message. Should always reference your
local keystore holding your private key and your public key and PEPPOL certificate.
1. `oxalis.pki.version` - indicates what kind of inbound certificates will be accepted. Must be set to V1,T or V2.
1. `oxalis.operation.mode` - mode of operation. Must be set to either `TEST` or `PRODUCTION`

Between September 1, 2013 and October 31, 2013; all PEPPOL Access Points must accept certificates issued under the V1 and V2 regime.
Henceforth the `oxalis.pki.version` must be set to `T` to indicate "transitional" phase.

On November 1, 2013 and thereafter; `oxalis.pki.version` must be set to `V2`. As a consequence, the version 1 certificates will not be
accepted after this date.

During the transition phase the correct settings are:

    oxalis.pki.version=T
    oxalis.operation.mode=PRODUCTION


The table below shows the combinations of the properties `oxalis.pki.version` and `oxalis.operation.mode`.

<table>
    <tr>
        <th></th><th>TEST</th><th>PRODUCTION</th>
    </tr>

    <tr>
        <td rowspan="3">V1</td><td style="background-color: green">v1-test == OK</td style="background-color: green"><td style="background-color: green">v1-test == OK</td>
    </tr>
    <tr>
        <td style="background-color: green">v2-test == OK</td><td style="background-color: green">v2-test == OK</td>
    </tr>
    <tr>
        <td style="background-color: red">v2-prod != OK</td><td style="background-color: red">v2-prod != OK</td>
    </tr>


    <tr>
        <td rowspan="3">T</td><td style="background-color: green">v1-test == OK</td><td style="background-color: green">v1-test == OK</td>
    </tr>
    <tr>
        <td style="background-color: green">v2-test == OK</td><td style="background-color: green">v2-test == OK</td>
    </tr>
    <tr>
        <td style="background-color: red">v2-prod != OK</td><td style="background-color: green">v2-prod == OK</td>
    </tr>

    <tr>
        <td rowspan="3">V2</td><td style="background-color: green">v1-test == OK</td><td style="background-color: red">v1-test == OK</td>
    </tr>
    <tr>
        <td style="background-color: green">v2-test == OK</td><td style="background-color: red">v2-test != OK</td>
    </tr>
    <tr>
        <td style="background-color: red">v2-prod != OK</td><td style="background-color: green">v2-prod == OK</td>
    </tr>

</table>

There are two things you need to do:

1. Upgrade to Oxalis 2.x no later than September 1, 2013 in order to accept incoming messages signed with V2 certificates.
1. Install a version 2 certificate into your Oxalis version and reference this certificate with property `oxalis.keystore`


## How do I create such a keystore?

Sorry, that is outside the scope of this document.

  However; if you have a look at the file `keystore.sh`, which is part of Oxalis, you should get the idea.

  There are many ways to skin a cat; some pepole prefer *openssl* others tools like *portecle*, while others use native tools supplied
  by their operating system.

  The first methods that spring to my mind are:

  * Use *openssl* togehter with Java *keytool* command
  * Java *keytool* only.
  Import the PEPPOL and intermediate certificates into your keystore, **before** you import the signed certificate returned from PEPPOL.
  * Find some other tool more to your liking, like for instance Keystore Explorer ( http://www.lazgosoftware.com/kse/index.html )


### Using openssl together with keytool

When using *openssl(1)*, all the files are generated using *openssl* after which they are imported into a Java
keystore (JKS) using the Java *keytool* utility.

 1. Create the private key and the Certificate Signing Request as described
       [Certificate Signing Request (CSR) Generation Instructions for Apache SSL](https://knowledge.verisign.com/support/ssl-certificates-support/index?page=content&actp=CROSSLINK&id=AR198)

 1. Read our certificate together with our private key and export both of them into a PKCS12 file:

    ```
    openssl pkcs12 -export -in $our_certificate -inkey ${private_key_unencrypted_file} \
        -out ${tmp2} -passout pass:${password} -name ${aliasname}
    ```

 1. Import our private key and certificate from the PKCS12 formatted file into Java keystore:

    ```
    keytool -importkeystore -srckeystore ${tmp2} -srcstoretype PKCS12 -srcstorepass ${password} \
        -alias ${aliasname} -destkeystore $keystore_file -deststorepass peppol
    ```

    Do not specify a password for the entry itself, only for the keystore.


### Using the Java keytool only

This method requires is for masochists only, so I shall give no detailed instructions.

  1. You must create the keystore and the CSR as described earlier.

  1. Import the PEPPOL root certificate and the intermediate certificates into the keystore.

  1. Import the PEPPOL signed certificate into the keystore.

  1. Best of luck!


## Verify the contents of your keystore

 You should verify the following aspects of your keystore using the keytool command:

 ```
 $ keytool -list -v -keystore keystore.jks 
Enter keystore password:  

Keystore type: JKS
Keystore provider: SUN

Your keystore contains 1 entry

Alias name: 1
Creation date: Oct 6, 2011
Entry type: PrivateKeyEntry       <<<<<< NOTE!!!
Certificate chain length: 1
Certificate[1]:
Owner: CN=APP_1000000021, O=SendRegning AS, C=NO
Issuer: CN=PEPPOL ACCESS POINT TEST CA, OU=FOR TEST PURPOSES ONLY, O=NATIONAL IT AND TELECOM AGENCY, C=DK
Serial number: 22c5c46bd8e3a697a971dd4c6771c78c
Valid from: Fri Sep 23 02:00:00 CEST 2011 until: Mon Sep 23 01:59:59 CEST 2013
Certificate fingerprints:
```

 * There is only a single entry in the keystore with a type of **PrivateKeyEntry**
 * The password of the keystore corresponds to the contents in your `oxalis-global.properties`
