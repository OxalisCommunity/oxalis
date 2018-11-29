# Certificates, keys and key stores in Oxalis

The purpose of this document is to guide you in how to set up your PEPPOL certificates in order to make Oxalis "tick".


## What are certificates used for?

PEPPOL has defined a PKI structure which allows for prudent governance of the access points, the SMP's and so on.

Every low level message passed between access points and between the access point and the SMP, are signed with digital certificates.

There is a "test" and "production" hierarchy of certificates.

When your certificate is issued by PEPPOL, it will be signed with the *intermediate* AP certificate.


## How are they used in Oxalis?

Oxalis validates your certificate as part of startup, and configures your installation accordingly.

You need only to supply your own key store, holding the private key and the corresponding PEPPOL certificate with your public key embedded.


## How do I obtain a PEPPOL certificate for my Access point?

1. Request PKI certificate in the [OpenPEPPOL Service Desk](https://openpeppol.atlassian.net/servicedesk/customer/portal/1) (OpenPEPPOL members only).

1. Follow instruction on the [PKI issuing information page](https://openpeppol.atlassian.net/wiki/spaces/OPMA/pages/193069072/Introduction+to+the+revised+PKI+Certificate+infrastructure+and+issuing+process) or updated link provided by OpenPEPPOL Support Team.

1. Copy the generated JKS or PKCS#12 keystore to your ```$OXALIS_HOME``` directory.

1. Update the configuration entry in [`oxalis.conf`](/doc/configuration.adoc) (**Keystore** part).

1. Start Oxalis.

