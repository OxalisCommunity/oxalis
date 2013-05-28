# Certificates, keys and key stores in Oxalis

The purpose of this document is to guide you in how to set up your PEPPOL certificates in order to make Oxalis "tick".

## What are certificates used for?

PEPPOL has defined a PKI structure which allows for prudent governance of the access points, the SMP's and so on.

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

## How are tehy used in Oxalis?

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



