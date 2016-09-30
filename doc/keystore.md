# Certificates, keys and key stores in Oxalis

The purpose of this document is to guide you in how to set up your PEPPOL certificates in order to make Oxalis "tick".


## What are certificates used for?

PEPPOL has defined a PKI structure which allows for prudent governance of the access points, the SMP's and so on.

Every low level message passed between access points and between the access point and the SMP, are signed with digital certificates.

The PKI structure comes in two releases:

* V2 is the current PKI scheme (as of autumn 2013)
* V1 is the old "PILOT" scheme and should no longer be needed (will be removed in later releases)

There is a "test" and "production" hierarchy of certificates.
The PEPPOL test root certificate were identicial for V1 and V2.

![Truststore structure](illustrations/truststore.png)

When your certificate is issued by PEPPOL, it will be signed with the *intermediate* AP certificate.

The long and short of this is: you have 3 trust stores in Oxalis holding the following chain of certificates:

1. V2 Production certificates, which has a production "root" CA.
1. V2 Test certificates, having a test "root" CA
1. V1 Pilot/Test certificates (sharing the test "root" CA as V2)


## How are they used in Oxalis?

Oxalis comes with all of the three trust stores included.

You need only to supply with your own key store, holding the private key and the corresponding PEPPOL certificate with your public key embedded.

This key store, which I refer to as the `oxalis-keystore.jks` should be placed in the `OXALIS_HOME` directory and references in your `oxalis-global.properties`


## How do I obtain a PEPPOL certificate for my Access point?

1. Sign a Transport Infrastructure Agreement (TIA) with a PEPPOL authority. Once that is done, you will receive instructions on how to submit a certificate signing request (CSR).

1. Generate a pair of keys (a private and a public key) together with a certificate signing request (CSR) using openssl:
    ```     
    openssl req -out my-certificate.csr -new -newkey rsa:2048 -nodes -keyout my-private.key
    ```    
   You will be prompted for some details, which by the way are **ignored** 
   (this fact is also mentioned in the instructions you receive from PEPPOL):
    ```       
       Generating a 2048 bit RSA private key
       .+++
       ...................................................................................................................+++
       writing new private key to 'difi-private.key'
       -----
       You are about to be asked to enter information that will be incorporated
       into your certificate request.
       What you are about to enter is what is called a Distinguished Name or a DN.
       There are quite a few fields but you can leave some blank
       For some fields there will be a default value,
       If you enter '.', the field will be left blank.
       -----
       Country Name (2 letter code) [AU]:NO  <<< Ignored, but you must supply something
       State or Province Name (full name) [Some-State]:Oslo <<< Ignored, but you must supply something
       Locality Name (eg, city) []:Oslo <<< Ignored, but you must supply something
       Organization Name (eg, company) [Internet Widgits Pty Ltd]:Difi <<< Ignored, but you must supply something
       Organizational Unit Name (eg, section) []:ANS/STS <<< Ignored, but you must supply something
       Common Name (e.g. server FQDN or YOUR name) []:ap.difi.no <<< Ignored, but you must supply something
       Email Address []:soc@difi.no <<< Ignored, but you must supply something
       
       Please enter the following 'extra' attributes
       to be sent with your certificate request
       A challenge password []:
       An optional company name []:
    ```
       
1. Upload the Certificate Signing Request (CSR), which is now held in ```my-certificate.csr``` 
   in accordance with the instructions. Make sure you select the correct
   type of certificate, i.e. click on the correct link.
   
1. You will receive a signed certificate with your public key. Copy the certificate into a file 
    named ```my_certificate.cer```-file. 
   
1. Create a PKCS12 file holding your private key and the certificate you have received:
    ```
    openssl pkcs12 -export -in my_certificate.cer -inkey my-private.key \
        -out oxalis-keystore.p12 -passout pass:${password} -name ${aliasname}
    ```
     
1. Import the signed certificate into the key store (`oxalis-keystore.jks`)
    ```
    keytool -importkeystore -srckeystore oxalis-keystore.p12 -srcstoretype PKCS12 -srcstorepass ${password} \
        -alias ${aliasname} -destkeystore oxalis-keystore.jks -deststorepass peppol
    ```
    
    Do not specify a password for the entry itself, only for the keystore.

1. Copy the `oxalis-keystore.jks` to your ```$OXALIS_HOME`` directory.

1. Verify the configuration entry in `oxalis-global.properties`


## How do I specify PRODUCTION or TEST certificates?

You should only be running with version 2 certificates for test and production.

This is a snippet of the `oxalis-global.properties` that enables PRODUCTION use :

    # Location of keystore holding our private key AND the certificate with the public key
    oxalis.keystore=/Users/thore/.oxalis/oxalis-production-v2.jks

    # Which version of the PKI system are we using, should be V2 (which is also the default)
    oxalis.pki.version=V2

    # Mode of operation? Specify TEST for pilot/test certificate or PRODUCTION for production (defaults to TEST)
    oxalis.operation.mode=TEST


The `oxalis.keystore` property references the certificate used for **signing** and **sending** a message or **returning a receipt**.  It should always reference your local keystore holding the private key, your public key and PEPPOL certificate.



## Verify the contents of your keystore

 You should verify the following aspects of your keystore using the keytool command :
 
    ```
    $ keytool -list -v -keystore oxalis-keystore.jks 
    Enter keystore password:  

    Keystore type: JKS
    Keystore provider: SUN
    
    Your keystore contains 1 entry
    
    Alias name: difi_ap
    Creation date: 06.okt.2015
    Entry type: PrivateKeyEntry
    Certificate chain length: 1
    Certificate[1]:
    Owner: CN=APP_1000000135, O=DIFI (Oxalis renewal test), C=NO
    Issuer: CN=PEPPOL ACCESS POINT TEST CA, OU=FOR TEST PURPOSES ONLY, O=NATIONAL IT AND TELECOM AGENCY, C=DK
    Serial number: 682d674303d3171f339eb0a51ac0958
    Valid from: Tue Oct 06 02:00:00 CEST 2015 until: Fri Oct 06 01:59:59 CEST 2017
    Certificate fingerprints:
         MD5:  2B:AD:9C:65:A6:E1:D1:0F:7A:6B:9A:A9:23:31:99:D8
         SHA1: 4A:FA:28:38:FA:54:4A:54:8A:E2:B4:6A:D1:AB:A2:7D:07:95:E9:B6
         SHA256: A4:95:A8:DC:24:F5:B7:05:E3:C8:DE:1F:13:23:04:EA:11:12:0C:F7:D0:5C:4C:46:26:F8:A9:62:51:AC:12:83
         Signature algorithm name: SHA256withRSA
         Version: 3
    ```
    
 * There is only a single entry in the keystore with a type of **PrivateKeyEntry**
 * The password of the keystore corresponds to the contents in your `oxalis-global.properties`
