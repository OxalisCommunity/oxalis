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

Oxalis comes with alle of the three trust stores included.

You need only to supply with your key store, holding your private key and the corresponding PEPPOL certificate with your public key embedded.

This key store, which I refer to as the `oxalis-keystore.jks` should be placed in the OXALIS_HOME directory and references in your `oxalis-global.properties

## How do I obtain a PEPPOL certificate for my Access point?

1. Sign a Transport Infrastructure Agreement (TIA) with a PEPPOL authority. Once that is done, you will receive instructions on how
to submit a certificate signing request (CSR).
1. Create your own keystore `oxalis-keystore.jks` holding your private key and your self-signed certificate
1. Send a Certificate Signing Request (CSR) to PEPPOL.
1. You will receive a signed certificate with your public key.
1. Import the signed certificate into the key store (`oxalis-keystore.jks`)
1. Copy the `oxalis-keystore.jks` to your OXALIS_HOME directory.
1. Verify the configuration entry in `oxalis-global.properties`

## How do I create such a keystore?

Sorry, that is outside the scope of this document.

  However; if you have a look at the file `keystore.sh`, which is part of Oxalis, you should get the idea.

  There are many ways to skin a cat; some pepole prefer *openssl* others tools like *portecle*, while others use native tools supplied
  by their operating system.

  The first methods that spring to my mind are:

  * Use Java *keytool* and the *portecle* utility, which may be downloaded from the [Portecle project page at Sourceforge](http://sourceforge.net/projects/portecle/)
  * Use *openssl* togehter with Java *keytool* command
  * Java *keytool* only.
  Import the PEPPOL and intermediate certificates into your keystore, **before** you import the signed certificate returned from PEPPOL.
  * Find some other tool more to your liking

### Creating a keystore using keytool and portecle

  1. Generate the RSA 2048bit keypair:

     ```
     $ keytool -genkey -alias ap-prod -keyalg RSA -keystore oxalis-production-keystore.jks -keysize 2048
     Enter keystore password:
     Re-enter new password:
     What is your first and last name?
       [Unknown]: Donald Duck
     What is the name of your organizational unit?
       [Unknown]:  Ducktown
     What is the name of your organization?
       [Unknown]:  Acme Inc.
     What is the name of your City or Locality?
       [Unknown]:  Oslo
     What is the name of your State or Province?
       [Unknown]:  Akershus
     What is the two-letter country code for this unit?
       [Unknown]:  NO
     Is CN=Donald Duck, OU=Ducktown, O=Acme Inc., L=Oslo, ST=Akershus, C=NO correct?
       [no]:  yes

     Enter key password for <ap-pilot>
             (RETURN if same as keystore password):
     ```
     **When promptet for a key password** - hit enter!

     This will generate your keystore with a single entry holding your private key and self signed certificate with the corresponding public key.
     The alias will be *ap-prod*

  1. Generate the Certificate Signing Request (CSR):

     ```
     keytool -certreq -alias ap-pilot -keystore oxalis-production-keystore.jks -file oxalis-prod.csr
     ```

     The generated file `oxalis-prod.csr` is the one you upload on the PEPPOL Certificate enrollment web site

 1. Save the returned, signed certificate into a file; `oxalis-prod.cer`. The contents of the file should look something
    like this:

    ```
    -----BEGIN CERTIFICATE-----
    MIIEajCCA1KgAwIBAgIQWsel5HqrbFlnuo9C1S9dlTANBgkqhkiG9w0BAQsFADB9
    MQswCQYDVQQGEwJESzEnMCUGA1UEChMeTkFUSU9OQUwgSVQgQU5EIFRFTEVDT00g
    .... several lines deleted as this is mean as a sample only ....
    NTV4wJdAlu6S+fVVxNp70xDsP6uEDcVXCi4syVwgyj1l0T8OZOSjVvqfPLfhRnOQ
    B+Ti/Pn+CkxsG/koptXPvfrTFARQ4qs2KpxWxI1cGXMgxQw5L0Q6oDYI8W6ulhcS
    lV7UHiGnnX1PlGO2Ehz+8dj9mhHOOx854SEpZMQN
    -----END CERTIFICATE-----
    ```

 1. Download Portecle from their [project web site](http://sourceforge.net/projects/portecle/) at SourceForge.

 1. Run Portecle:

    ```
    java -jar portecle.jar
    ```
 1. **Make a backup copy of your keystore**. If you make a mistake you can simply take another copy from the backup
    and give it another try :-)

 1. Replace your current self-signed certificate with the signed certificate from PEPPOL:

    1. Open your keystore file

    1. Select the certificate "ap-prod"

    1. Select Tools -> Import Trusted Certificate (CTRL-T)

    1. You will be warned that a trust path could not be established. Press `OK`.

    1. Inspect the certificate details and press `OK`

    1. Press `Yes` to accepts the certificate as trusted.

    1. You are prompted for the "Trusted Certificate Entry Alias". Make sure you enter the name of the alias you
       chose when you initially generated the key pair, in this case `ap-prod`

    1. You will be warned that the keystore contains an entry with this alias. Press `Yes` to overwrite it.

    1. Press `OK` and save your keystore.

 1. Copy the keystore `oxalis-production-keystore.jks` to your `OXALIS_HOME` directory and verify that your
    `oxalis-global.properties` references this keystore with a full path.


 A final note: you may use *portecle* for the whole operation. This miniguide simply shows you how to use the two in
 combination. Henceforth; ensuring that the file format of the keystore will be supported by Oxalis' Java implementation
 of a keystore.


### Using openssl together with keytool

When using *openssl(1)*, all the files are generated using *openssl* after which they are imported into a Java
keystore (JKS) using the Java *keytool* utility.

 1. Create the private key and the Certificate Signing Request as described
       [Certificate Signing Request (CSR) Generation Instructions for Apache SSL](https://knowledge.verisign.com/support/ssl-certificates-support/index?page=content&actp=CROSSLINK&id=AR198)

 1. Read our certificate together with our private key and export both of them into a PKCS12 file:

    ```
    openssl pkcs12 -export -in $our_certificate -inkey ${private_key_unencrypted_file} -out ${tmp2} -passout pass:${password} -name ${aliasname}
    ```

 1. Import our private key and certificate from the PKCS12 formatted file into Java keystore:

    ```
    keytool -importkeystore -srckeystore ${tmp2} -srcstoretype PKCS12 -srcstorepass ${password} -alias ${aliasname} -destkeystore $keystore_file -deststorepass peppol
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
 $ keytool -keystore oxalis-production-keystore.jks -list
 Enter keystore password:

 Keystore type: JKS
 Keystore provider: SUN

 Your keystore contains 1 entry  <<<< Only a single entry

 ap-prod, 29.mai.2013, trustedCertEntry,
 Certificate fingerprint (SHA1): D7:6D:C0:C9:87:F2:21:32:8D:2C:4B:E8:11:89:32:BA:68:BE:AA:C4
 ```

 * There is only a single entry in the keystore
 * The password of the keystore corresponds to the contents in your `oxalis-global.properties`
 * Make sure the entry does not have a certificate chain length.

   ```
   $ keytool -keystore oxalis-production-keystore.jks -list -v
   ```
   I.e. if you see something like `Certificate chain length: ....` in the output. You got it wrong.


## Where can I find more information about the PEPPOL PKI structure?

<to be done>


