# LoRaWAN virtual gateway

It includes a LoRaWAN virtual gateway and different LoRaWAN virtual devices (plus some LoRaWAN related utilities). The virtual gateway can be used to connect to a remote LoRaWAN network server using the Semtech protocol, and to relay data from some virtual devices to the network server.

# Running the gateway

The LoRaWAN virtual gateway can be configured via a configuration file, e.g.:
```
java -cp lorawan.jar test.LorawanGw -f gw.cfg
```

or by using command line options, e.g.:
```
java -cp lorawan.jar test.LorawanGw -gweui FEFFFFabcdef0000 -appServer router.eu.thethings.network -deveui FEFFFFabcdef00001 -t 40 -appeui 0000000000000000 -appkey 00000000000000000000000000000000 -v
```

Replaces the EUIs and key with the proper values.

In particular, regarding GW and device EUIs, since the two nodes are virtual, the corresponding EUIs are not provided by a manufacturer and we need to define them.

In order to avoid or minimize the probability of conflict with other valid EUIs, a suggestion could be to use one of the two options:

* create the EUI-64 identifier using an assigned OUI or, if you not have one, using [FE-FF-FF as OUI](https://lora-developers.semtech.com/library/tech-papers-and-guides/the-book/deveui/); for example: [FE FF FF ab cd ef 00 01]; or
* starting from a 48 bit IEEE MAC address it can be expanded to EUI-64 using the method specified by IEEE as ["Guidelines for Use of Extended Unique Identifier (EUI)"]( https://standards.ieee.org/content/dam/ieee-standards/standards/web/documents/tutorials/eui.pdf) (although it is now formally considered deprecated); starting from a 48 bit address [X1 X2 X3 X4 X5 X6] the mapped EUI-64 is obtained by adding in the middle two bytes 0xFF and 0xFE, i.e. [X1 X2 X3 FF FE X4 X5 X6].

If no network server is specified, TTN is used by default.



# Virtual devices

There are some types of virtual devices already available:
 * CountDevice - simple device with readable and writable integer value that is incremented at each reading; the integer is encoded as four bytes in big-endian;
 * CurrentTimeDevice - simple device with read-only data that is the current time returned as YYYY-MM-dd HH:mm:ss string;
 * DataDevice - device with readable and writable data maintained in RAM; the data has to be passed as parameter (byte array as hexadecimal string);
 * FileDevice - device with readable and writable data stored in a file; the file name has to be passed as parameter;</li>
 * DraginoLHT65 - Dragino LHT65 with artificial temperature and humidity values;
 * DraginoLSE01 - Dragino LSE01 with artificial temperature and soil moisture values.

 
Self-made virtual devices can be also used by simply specifying the complete class name; for example:
```java
it.unipr.netsec.ipstack.lorawan.device.CurrentTimeDevice
```


# Attaching the GW to a remote LoRaWAN platform

As an example of using this virtual gateway we are going to attach it to TTN (The Things Networks) platform.

The first step is to create and configure a gateway profile on the TTN platform. For this purpose we have to log-in to TTN. If you don't have a account in TTN you have to create it ("Sign Up").

Once we are in, we go to "Console" and select a region (for example "Europe 1"). Then we do "Register a gateway", and specify the requested information. In particular you have to indicate:
* a Gateway ID,
* the selected Gateway EUI,
* Frequency plan.

From this configuration we copy the provided "Gateway server address" that is the address of the network server where the gateway has to connect to.
Optionally we can download from TTN the "Global configuration" as JSON file.

The second step is to create a new TTN application and add one or more devices. We go to "Applications", we do "Add application" and specify the requested information (at lest just an Application ID).
Then we do "Add end device" and do "manual device registration" with the following information:
* Activation mode: OTAA
* LoRaWAN version: MAC V1.0.4 could be ok

We press "Start" and continue with the basic settings:
* End device ID: select a distinguish name
* JoinEUI: also called AppEUI; all zeros could be ok
* DevEUI: specify the EUI selected for device

Then go "Network layer settings":
* Frequency plan
* Regional Parameters version: PHY V1.0 could be ok

Then go "Join settings" and fill Root keys:
* AppKey: specify the same key selected for device

Finish by pressing "Add end node". Now we are ready to start the virtual gateway and device:
```
java -cp lorawan.jar test.LorawanGw -f gw.cfg
```

where the configuration file "gw.cfg" includes the same values (gwEui, appEui aka JoinEUI, appServer aka Gateway server address, devEui) specified in the TTN console.