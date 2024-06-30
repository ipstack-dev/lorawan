# ipstack Lorawan

Java implementation of the LoRaWAN® protocol stack together with some reference implementations of the main LoRaWAN network elements, and utilities.
In particular it includes the implementation of:
* complete LoRaWAN protocol stack (LoRaWAN MAC layer and above), as defined by the standard LoRaWAN® specifications;
* Semtech protocol, that can be used between a LoRaWAN gateway and the network server;
* LoRaWAN server, integratiting a Network Server, a Join Server, and an Application Server;
* LoRaWAN virtual gateway, that is a LoRaWAN gateway where the LoRa PHY layer is replaced by a virtual PHY layer on top of UDP/IP;
* some virtual devices, that are software devices that can communicate with one or more virtual gateways by means of a virtual PHY layer (on top of UDP/IP).
* other LoRaWAN utilities for capturing and/or analyzing LoRaWAN traffic.



## Dependencies

The following two small libraries are required:
* [zutil](https://github.com/zoolu-org/zutil) - it is a collection of some utilities; in particular ipstack Lorawan uses zutil for managing command line options, for handling byte arrays, as JSON parser, and for handling logs. 
* [ipstack](https://github.com/ipstack-dev/ipstack) - it is mainly a TCP/IP library; however it provides also generic support for handling packets and a protocol analyzer used by ipstack Lorawan.

You can download these libraries from the corresponding repositories or you can find them, as jar files, directly in the [`lib`](https://github.com/ipstack-dev/lorawan/tree/main/lib) folder.


In case you want to use CoAP-based virtual devices or you want to interact with the Lorawan Server using the CoAP REST interface, the following library is also required:

* [mjcoap](https://github.com/thingsstack/mjcoap) - CoAP implementation.


For simplicity, the all-in-one jar file [lorawan-all.jar](https://github.com/ipstack-dev/lorawan/blob/main/lorawan-all.jar) containing binary code from all libraries is also provided.


In the following subsections different components included in the library are described.



## Lorawan Server

Reference implementation of a LoRaWAN server platform (Network Server, Join Server and App Server). More details, libraries, and instructions for running the ipstack Lorawan Server can be found [here](https://github.com/ipstack-dev/lorawan-server).



## Virtual Gateway

Software LoRaWAN gateway that can be connected to a standard remote LoRaWAN backend system (network server, join server and application server) and relays data from/to virtual devices.
The gateway uses the Semtech protocol for the communication with the LoRaWAN network server.

The virtual gateway can be configured by means of command-line parameters and/or via a configuration file.

To run the virtual gateway using command-line parameters you can simply execute the following command:
```
java -cp "lib/*" run.gateway.VirtualGateway -g <gwEUI> -s <networkServer> -v
```

where `<gwEUI>` is the EUI assigned to the gateway, while `<networkServer>` is the address (IP address or fully-qualified domain name) of the newtork server, possibly including the UDP port (appending `:<port>`) if the network server uses a UDP port that differs from the standard 1700.

Use option '-h' to watch all command-line options.


Regarding the gateway EUI, since the gateway is virtual, it doesn't have a manufacturer-provided EUI and you probably need to generate it.

In order to avoid or minimize the probability of conflict with other valid EUIs, a suggestion could be to use one of the following two options:

* create the EUI-64 identifier using an assigned OUI or, if you not have one, using [FE-FF-FF as OUI](https://lora-developers.semtech.com/library/tech-papers-and-guides/the-book/deveui/); for example: [FE FF FF ab cd ef 00 01]; or

* starting from a 48 bit IEEE MAC address it can be expanded to EUI-64 using the method specified by IEEE as ["Guidelines for Use of Extended Unique Identifier (EUI)"]( https://standards.ieee.org/content/dam/ieee-standards/standards/web/documents/tutorials/eui.pdf) (although it is now formally considered deprecated); starting from a 48 bit address [X1 X2 X3 X4 X5 X6] the mapped EUI-64 is obtained by adding in the middle two bytes 0xFF and 0xFE, i.e. [X1 X2 X3 FF FE X4 X5 X6].


As simple test, you can locally run a LoRaWAN server and virtual gaeway by executing the following two commands:

```
java -cp "lib/*" run.server.Server -f cfg/server.cfg -v
```

```
java -cp "lib/*" run.gateway.VirtualGateway -g feffff0000ffffff -s 127.0.0.1 -v
```


## Virtual devices

Virtual devices are software LoRaWAN devices included in the ipstack Lorawan library and capable to exchange LoRaWAN MAC packets with a virtual gateway connected to a LoRaWAN backend system.

Software devices that acts as standard LoRaWAN devices at MAC layer and above. They are capable to connect to a remote LoRaWAN backend system through a virtual gateway.
Since the LoRaWAN physical layer is not present, the devices communicate with the virtual gateway by simply encapsulating LoRaWAN MAC packets within UDP datagrams.

The following types of virtual devices are currently supported:
 * Counter - simple device with readable and writable integer value that is incremented at each reading; the integer is encoded as four bytes in big-endian;
 * CurrentTime - simple device with read-only data that is the current time returned as YYYY-MM-dd HH:mm:ss string;
 * Data - device with readable and writable data maintained in RAM; the data has to be passed as parameter (byte array as hexadecimal string);
 * FileData - device with readable and writable data stored in a file; the file name has to be passed as parameter;</li>
 * DraginoLHT65 - Dragino LHT65 with artificial temperature and humidity values;
 * DraginoLSE01 - Dragino LSE01 with artificial temperature and soil moisture values.

Other types can be also used by simply adding their upper layer implementation (payload generation and/or consumption) and by running the generic device (*LorawanDevice*) using as type name the complete class name.

Hereafter a simple test that locally runs a LoRaWAN server platform, a virtual gateway, and a virtual device:

```
java -cp "lib/*" run.server.Server -f cfg/server.cfg -v
```

```
java -cp "lib/*" run.gateway.VirtualGateway -g ffffff0000ffffff -s 127.0.0.1 -p 7001 -v
```

```
java -cp "lib/*" run.device.VirtualDevice -d ffffff0000000001 -j 0000000000000001 -k aaaaaaaabbbbbbbbccccccccdddddddd -p 1 -gw 127.0.0.1:7001 -v
```


## Test a remote LoRaWAN platform

If you want to test a remote LoRaWAN platform you can either:

1. run one or more virtual devices and a virtual gateway and connect the gatewat to the remote network server as describe above;

2. use the proper ipstack Lorawan `LorawanRemoteNetworkTest` tool.


According to the latter solution you can uses the `LorawanRemoteNetworkTest` application that runs a virtual gateway, connect it to the lorawan server, and run a virtual device.

For example let's suppose that you already run locally a lorawan server (e.g. a [ipstack Lorawan Server](https://github.com/ipstack-dev/lorawan-server)). Then you can test the server platform by simply executing:

```
java -cp lib/* test.LorawanRemoteNetworkTest -g ffffff0000ffffff -d ffffff0000000001 -j 0000000000000001 -k aaaaaaaabbbbbbbbccccccccdddddddd -p 1 -s 127.0.0.1
```

where:
- '-g ffffff0000ffffff' specifies the EUI assigned to the gateway (usually you have to register this EUI also in the server platform);
- '-d ffffff0000000001' specifies the EUI of the devices (DevEUI);
- '-j 0000000000000001' specifies the AppEUI;
- '-k aaaaaaaabbbbbbbbccccccccdddddddd' specifies the AppKey
- '-p 1' the used fPort value;
- '-s 127.0.0.1' the address (and port) of the server; if no port is specified the default port 1700 is used (it is the same as '-s 127.0.0.1:1700'). 



## Using virtual gateway and devices with TTN

Hereafter there are some guidelines for connecting a virtual gateway and virtual device to TTN (The Things Network). Similar procedure can be followed to connect them to a different LoRaWAN platform.


### Configuring the gateway and devices on TTN

In order to run the gateway we have to attach it to a LoRaWAN backend platform including a network server, a join server and an application server.

As an example of backend platform we are going to use the TTN (The Things Networks).

The first step is to create and configure a gateway profile on the TTN platform. For this purpose we have to log-in to TTN. If you don't have a account in TTN you have to create it ("Sign Up").

Once we are in, we go to "Console" and select a region (for example "Europe 1"). Then we do "Register a gateway", and specify the requested information. In particular you have to indicate:
* A Gateway ID,
* The selected Gateway EUI,
* The frequency plan.

From this configuration we copy the provided "Gateway server address" that is the address of the network server where the gateway has to connect to.
Optionally we can download from TTN the "Global configuration" as JSON file.

The second step is to create a new TTN application and add one or more devices. We go to "Applications", we do "Add application" and specify the requested information (at lest just an Application ID).
Then we do "Add end device" and do "manual device registration" with the following information:
* Activation mode: OTAA
* LoRaWAN version: MAC V1.0.2 could be ok

We press "Start" and continue with the basic settings:
* End device ID: select a distinguish name
* JoinEUI: also called AppEUI; all zeros could be ok
* DevEUI: specify the EUI selected for device

Then go "Network layer settings":
* Frequency plan
* Regional Parameters version: PHY V1.0 could be ok

Then go "Join settings" and fill Root keys:
* AppKey: specify the same key selected for device

Finish by pressing "Add end node".

Now we are ready to start the virtual gateway and devices.



### Running the gateway

For starting the gateway:
```
java -cp lorawan.jar run.gateway.LorawanGateway -f gw.cfg -v
```

where the configuration file "gw.cfg" includes the same gateway values (gwEui and networkServer) configured in the TTN console.

Here is an example of fragment of the gateway console log that includes the association of a device and the first device data message:  
```
16:46:05.250: SemtechClient[7000]: sending: PULL_DATA 78f2 feffff0123450000
16:46:05.372: SemtechClient[7000]: received: PULL_ACK 78f2
16:46:15.334: SemtechClient[7000]: sending: PUSH_DATA 00f7 feffff0123450000 {"status":{"time":"2021-10-15 16:46:15 GMT","lati":0.0,"long":0.0,"alti":0,"rxnb":0,"rxok":0,"rxfw":0,"ackr":0.0,"dwnb":0,"txnb":0}}
16:46:15.399: SemtechClient[7000]: received: PUSH_ACK 00f7
16:46:25.331: SemtechClient[7000]: sending: PULL_DATA 687d feffff0123450000
16:46:25.399: SemtechClient[7000]: received: PULL_ACK 687d
16:46:45.332: SemtechClient[7000]: sending: PULL_DATA f7b4 feffff0123450000
16:46:45.398: SemtechClient[7000]: received: PULL_ACK f7b4
16:46:48.998: LorawanGateway: processReceivedDatagramPacket(): pktInfo: {"time":"2021-10-15T16:46:48.996Z","tmst":0,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":23,"data":"AAEAAAAAAAAAAQARAiD///5ekfjnZGw="}
16:46:48.999: SemtechClient[7000]: sending: PUSH_DATA 3bb8 feffff0123450000 {"rxpk":[{"time":"2021-10-15T16:46:48.996Z","tmst":0,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":23,"data":"AAEAAAAAAAAAAQARAiD///5ekfjnZGw="}]}
16:46:49.075: SemtechClient[7000]: received: PUSH_ACK 3bb8
16:46:53.277: SemtechClient[7000]: received: PULL_RESP 0001 {"txpk":{"imme":false,"tmst":5000000,"freq":868.1,"rfch":0,"powe":14,"modu":"LORA","datr":"SF7BW125","codr":"4/5","ipol":true,"size":33,"ncrc":true,"data":"ILwFnAdShjHpTliPUd7Emk8yWWs1eGB7X34xmq0PxbX3"}}
16:46:53.278: SemtechClient[7000]: sending: TX_ACK 0001 feffff0123450000
16:46:53.291: LorawanGateway: processReceivedTxPacketMessage(): pktInfo: {"txpk":{"imme":false,"tmms":0,"freq":868.1,"rfch":0,"powe":14,"modu":"LORA","datr":"SF7BW125","codr":"4/5","ipol":true,"size":33,"ncrc":true,"data":"ILwFnAdShjHpTliPUd7Emk8yWWs1eGB7X34xmq0PxbX3"}}
16:46:53.293: LorawanGateway: processReceivedTxPacketMessage(): sent to: /127.0.0.1:61004
16:46:53.316: LorawanGateway: processReceivedDatagramPacket(): pktInfo: {"time":"2021-10-15T16:46:53.316Z","tmst":4320,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4
/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QL70CyYAAAABgrZYaGIJdkM="}
16:46:53.321: SemtechClient[7000]: sending: PUSH_DATA 2eac feffff0123450000 {"rxpk":[{"time":"2021-10-15T16:46:53.316Z","tmst":4320,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QL70CyYAAAABgrZYaGIJdkM="}]}
16:46:53.395: SemtechClient[7000]: received: PUSH_ACK 2eac
16:46:53.807: SemtechClient[7000]: received: PULL_RESP 0002 {"txpk":{"imme":false,"tmst":6004320,"freq":869.525,"rfch":0,"powe":27,"modu":"LORA","datr":"SF9BW125","codr":"4/5","ipol":true,"size":13,"ncrc":true,"data":"YL70CyaBAAAG83GiiQ=="}}
16:46:53.808: SemtechClient[7000]: sending: TX_ACK 0002 feffff0123450000
16:46:53.812: LorawanGateway: processReceivedTxPacketMessage(): pktInfo: {"txpk":{"imme":false,"tmms":0,"freq":869.525,"rfch":0,"powe":27,"modu":"LORA","datr":"SF9BW125","codr":"4/5","ipol":true,"size":13,"ncrc":true,"data":"YL70CyaBAAAG83GiiQ=="}}
16:46:53.813: LorawanGateway: processReceivedTxPacketMessage(): sent to: /127.0.0.1:61004
```


### Running a device

For starting a device:
```
java -cp lorawan.jar run.gateway.VirtualDevice -f dev.cfg -v
```

where the configuration file "dev.cfg" includes the same device values (devEui, appEui, etc.) configured in the TTN console.

The device type should be one of the supported types (Counter, CurrentTime, Data, FileData, DraginoLHT65, DraginoLSE01), or a new implemented device type. In the latter case the complete class name must be used as device type. For example:
```java
com.example.lorawan.device.MyNewDevice
```



Here is an example of fragment of the device console log that includes the device association and three data messages:  
```
16:46:48.664: LorawanDevice: device: Counter
16:46:48.995: LorawanDevice: processjoiningTimeout(): sending Join request message: MType: Join Request, MacPayload: 01000000000000000100110220fffffe5e91, MIC: f8e7646c
16:46:53.294: LorawanDevice: processReceivedDatagramPacket(): received LoraWAN message: MType: JoinAccept, MacPayload: bc059c07528631e94e588f51dec49a4f32596b3578607b5f7e319aad, MIC: 0fc5b5f7
16:46:53.295: LorawanDevice: processReceivedDatagramPacket(): associated
16:46:53.309: LorawanDevice: processReceivedDatagramPacket(): new session context: {"fNwkSIntKey":"c06ae35b383584adb39df05c9ac9878e","sNwkSIntKey":"c06ae35b383584adb39df05c9ac9878e","nwkSEncKey":"c06ae35b383584adb39df05c9ac9878e","fCntUp":0,"fCntDown":0,"nFCntDwn":0,"devAddr":"260bf4be","appSKey":"2ab6ddbb0ae7032de57ceb3d9faa5aa1","aFCntDown":0}
16:46:53.311: LorawanDevice: data: 00000000
16:46:53.315: LorawanDevice: processDataTimeout(): sending Data message: MType: Unconfirmed Data Up, MacPayload: bef40b260000000182b65868, MIC: 62097643
16:46:53.814: LorawanDevice: processReceivedDatagramPacket(): received LoraWAN message: MType: Unconfirmed Data Down, MacPayload: bef40b2681000006, MIC: f371a289
16:47:53.316: LorawanDevice: data: 00000001
16:47:53.318: LorawanDevice: processDataTimeout(): sending Data message: MType: Unconfirmed Data Up, MacPayload: bef40b26000100014af601a3, MIC: ec1f3b80
16:48:53.321: LorawanDevice: data: 00000002
16:48:53.323: LorawanDevice: processDataTimeout(): sending Data message: MType: Unconfirmed Data Up, MacPayload: bef40b26000200011edb724a, MIC: ffac232f
16:48:53.825: LorawanDevice: processReceivedDatagramPacket(): received LoraWAN message: MType: Unconfirmed Data Down, MacPayload: bef40b2681010006, MIC: 4af3821c
```


At the beginning of the log we can see the device type ('Counter'):
```
16:46:48.664: DeviceClient: device: Counter
```

Then we can see when the device is associated:
```
16:46:53.295: LorawanDevice: processReceivedDatagramPacket(): associated
16:46:53.309: LorawanDevice: processReceivedDatagramPacket(): new session context: {"fNwkSIntKey":"c06ae35b383584adb39df05c9ac9878e","sNwkSIntKey":"c06ae35b383584adb39df05c9ac9878e","nwkSEncKey":"c06ae35b383584adb39df05c9ac9878e","fCntUp":0,"fCntDown":0,"nFCntDwn":0,"devAddr":"260bf4be","appSKey":"2ab6ddbb0ae7032de57ceb3d9faa5aa1","aFCntDown":0}
```

And then, the data payload that is sent to the gateway:
```
16:46:53.311: LorawanDevice: data: 00000000
16:46:53.315: LorawanDevice: processDataTimeout(): sending Data message: MType: Unconfirmed Data Up, MacPayload: bef40b260000000182b65868, MIC: 62097643
```

The first line shows the payload data in cleartext ('00000000'), while the latter indicates that the data is sent to the gateway.

At the same time, on the gateway console we have:
```
16:46:53.316: LorawanGateway: processReceivedDatagramPacket(): pktInfo: {"time":"2021-10-15T16:46:53.316Z","tmst":4320,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4
/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QL70CyYAAAABgrZYaGIJdkM="}
16:46:53.321: SemtechClient[7000]: sending: PUSH_DATA 2eac feffff0123450000 {"rxpk":[{"time":"2021-10-15T16:46:53.316Z","tmst":4320,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QL70CyYAAAABgrZYaGIJdkM="}]}
16:46:53.395: SemtechClient[7000]: received: PUSH_ACK 2eac
```

That shows that a MAC packet is received from the device and it is sent to the network server using a Semtech PUSH_DATA packet. In particular the following information is reported: the Semtech packet type, the two-byte token, the gateway EUI, and the enclosed JSON object containing some metadata and the actual LoraWAN MAC message.
The LoraWAN MAC message is included in base64 format. It can be decode as follows:
```
  java -cp lorawan.jar test.LorawanParser -B QL70CyYAAAABgrZYaGIJdkM=

        MACMessage: 40bef40b260000000182b6586862097643
        MType: Unconfirmed Data Up
        MacPayload: bef40b260000000182b65868
        MIC: 62097643
        Data message payload:
                DevAddr: 260bf4be
                FCtrl: {"adr":false,"adrAckReq":false,"ack":false,"classB":false,"fOptsLen":0}
                FCnt: 0
                FPort: 1
                EncryptedFRMPayload: 82b65868				
```
				
The device data is encrypted. The data can be decrypted using the 'appSKey' of the session context created above.
In this example the appSKey is '2ab6ddbb0ae7032de57ceb3d9faa5aa1', and the data can decrypted by doing:
```
  java -cp lorawan.jar test.LorawanParser -B QL70CyYAAAABgrZYaGIJdkM= -appskey 2ab6ddbb0ae7032de57ceb3d9faa5aa1

        MACMessage: 40bef40b260000000182b6586862097643
        MType: Unconfirmed Data Up
        MacPayload: bef40b260000000182b65868
        MIC: 62097643
        Data message payload:
                DevAddr: 260bf4be
                FCtrl: {"adr":false,"adrAckReq":false,"ack":false,"classB":false,"fOptsLen":0}
                FCnt: 0
                FPort: 1
                FRMPayload: 00000000 
```

