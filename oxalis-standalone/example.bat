@echo off
REM
REM Sends a sample invoice to Difi's test server.
REM Check the results at
REM https://test-aksesspunkt.difi.no/inbound/9908_810418052/
REM
@echo on
java -jar target/oxalis-standalone.jar -f src/main/resources/BII04_T10_PEPPOL-v2.0_invoice.xml^
    -r 9908:810418052 ^
    -s 9909:810418052
@echo off

REM Sends a sample invoice to your own local access point
@echo on
java -jar target/oxalis-standalone.jar -f src/main/resources/BII04_T10_PEPPOL-v2.0_invoice.xml^
      -r 9908:810418052^
      -s 9946:ESPAP^
      -u http://localhost:8080/oxalis/as2^
      -m as2^
      -i APP_1000000135
@echo off