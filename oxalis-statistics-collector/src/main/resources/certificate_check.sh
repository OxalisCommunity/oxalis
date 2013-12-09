#!/bin/sh

# Inspects the certificates for a set of known access points
for url in \
    start-ap.alfa1lab.com:443 \
    aksesspunkt.amesto.no:443 \
    ap.kpmg.no:443 \
    peppol.basware.com:443 \
    aksesspunkt.compello.com:443 \
    aksesspunkt.compello.com:443 \
    peppap01.daldata.no:8443 \
    ap.ergogroup.no:443 \
    ediex.no:8443 \
    edi.hatteland.com:443 \
    peppolap.ibxplatform.net:8443 \
    start-ap-2.inexchange.com:443 \
    test.isyshub.no:8443 \
    peppol.itella.net:443 \
    213.52.35.18:8443 \
    peppol.logiq.no:8443 \
    peppol.maventa.com:443 \
    ap.mirroraccounting.com:8443 \
    ehf.netclient.no:443 \
    peppol-test2.nets.no:443 \
    peppol.nets.no:443 \
    aksesspunkt.byggtjeneste.no:8443 \
    officient.com:8443 \
    ap-peppol.pageroonline.com:443 \
    efaktura.quick.no:9443 \
    aksesspunkt.sendregning.no:443 \
    stralfors-ap.galaxygw.com:10181 \
    153.110.250.4:8443 \
    tripletex.no:443 \
    aksesspunkt.unimicro.no:8443 \
    ap.visma.com:8443 \
    peppol.xledger.net:8443 \
    ehf.xsoffice.no:8443 \
    ap.zirius.no:8443
do
    echo |\
     openssl s_client -connect ${url} 2>/dev/null |\
     sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' |\
     openssl x509 -noout -subject -dates 2>/dev/null
done
