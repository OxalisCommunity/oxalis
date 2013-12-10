# Oxalis release notes

Release notes were introduced as of version 3.0-Alpha

## 3.0-Alpha

Oxalis version 3.x and upwards is not backwards compatible with version 2.x in terms of the API.

Interoperability between 2.x and 3.x using the START protocol should work fine.

However; due to the introduction of the new AS2 protocol, various interfaces had to be moved in order to prevent chaos.

All modules named `oxalis-start-xxxxx` have been renamed to `oxalis-xxxxx`.

The components used by PEPPOL authorities to collect statistics, have been refactored out of this project as it is of no concern to
an Access Point.

As usual, have a look at `oxalis-standalone` to get an idea of how to transmit outbound messages.

Inbound messages are handled by `oxalis-inbound` as before.

Switching between START and AS2 is performed "automagically". However; as a programmer you may override the selection of protocol at your discretion.

Have fun!
