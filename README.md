# ipstack Lorawan

Java implementation of the LoRaWAN® protocol stack together with the reference implementation of the main LoRaWAN network elements and some utilities.
In particular it includes the implementation of:
* the complete LoRaWAN protocol stack (LoRaWAN MAC layer and above), as defined by the standard LoRaWAN® specifications;
* Semtech protocol, that can be used between a LoRaWAN gateway and the network server;
* a LoRaWAN server, integratiting a Network Server, a Join Server, and an Application Server;
* a LoRaWAN virtual gateway, that is a LoRaWAN gateway where the LoRa PHY layer is replaced by a virtual PHY layer on top of UDP/IP;
* some virtual devices, that are software devices that can communicate with one or more virtual gateways by means of a virtual PHY layer (on top of UDP/IP).
* other LoRaWAN utilities for capturing and/or analyzing LoRaWAN traffic.



## Dependencies

The source code uses the following libraries from some our other projects:
* [zutil](https://github.com/zoolu-org/zutil) - some utilities used for managing command line options, byte arrays, JSON strings, etc. 
* [ipstack](https://github.com/ipstack-dev/ipstack) - network libraries used for packet handling; 
* [mjcoap](https://github.com/thingsstack/mjcoap) - CoAP implementation used in some examples of virtual devices.

In the [lib](https://github.com/ipstack-dev/lorawan/tree/main/lib) folder the corresponding jar files are provided.

For simplicity, the all-in-one jar file [lorawan-all.jar](https://github.com/ipstack-dev/lorawan/blob/main/lorawan-all.jar) containing binary code from all libraries is also provided.

## Reference applications

More information regarding LoRaWAN server, gateway, and device reference implementation can be found here:

- [server](https://github.com/ipstack-dev/lorawan/blob/main/doc/server.md)
- [gateway](https://github.com/ipstack-dev/lorawan/blob/main/doc/gateway.md)
- [device](https://github.com/ipstack-dev/lorawan/blob/main/doc/device.md)


