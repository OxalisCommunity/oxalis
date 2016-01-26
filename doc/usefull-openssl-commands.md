
## Base64 decoding an AS2 MDN

Given the AS2 MDN encoded in Base64 on a single line, extracted from the ETSI REM evidence:

    openssl enc -base64 -d -in receipt-b64.mdn -out receipt.mdn -A
    
The `-A` option is used when the entire base64 encoded MDN is held on a single line.    

To extract the certificate used to sign the MDN into file `certificate.pem`:

    openssl cms -verify -in receipt.mdn -noverify -signer certificate.pem

Inspect the contents of the certificate:

    openssl x509 -in certificate.pem -inform PEM -text
        