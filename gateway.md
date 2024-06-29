# Virtual gateway

Software LoRaWAN gateway that can be connected to a standard remote LoRaWAN backend system (network server, join server and application server) and relays data from/to some virtual devices.
The gateway uses the Semtech protocol for the communication with the LoRaWAN network server.

The virtual gateway can be configured via a configuration file, e.g.:
```
java -cp lorawan.jar test.LorawanGateway -f gw.cfg
```

or by using command line options, e.g.:
```
java -cp lorawan.jar test.LorawanGateway -gweui FEFFFFabcdef0000 -netsrv router.eu.thethings.network
```

Replaces the gateway EUI and network server address with the proper values.

Regarding the gateway EUI, since the gateway is virtual, it doesn't have a manufacturer provided EUI and you probably need to generate it.

In order to avoid or minimize the probability of conflict with other valid EUIs, a suggestion could be to use one of the two options:

* create the EUI-64 identifier using an assigned OUI or, if you not have one, using [FE-FF-FF as OUI](https://lora-developers.semtech.com/library/tech-papers-and-guides/the-book/deveui/); for example: [FE FF FF ab cd ef 00 01]; or
* starting from a 48 bit IEEE MAC address it can be expanded to EUI-64 using the method specified by IEEE as ["Guidelines for Use of Extended Unique Identifier (EUI)"]( https://standards.ieee.org/content/dam/ieee-standards/standards/web/documents/tutorials/eui.pdf) (although it is now formally considered deprecated); starting from a 48 bit address [X1 X2 X3 X4 X5 X6] the mapped EUI-64 is obtained by adding in the middle two bytes 0xFF and 0xFE, i.e. [X1 X2 X3 FF FE X4 X5 X6].

Note that if no network server is specified, TTN 'router.eu.thethings.network' is used by default.

The gateway communicates with virtual devices using UDP protocol. The default gateway UDP port is 1700. 
