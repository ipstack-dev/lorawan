# LoRaWAN virtual gateway

It includes a LoRaWAN virtual gateway and different LoRaWAN virtual devices (plus some LoRaWAN related utilities). The virtual gateway can be used to connect to a remote LoRaWAN network server using the Semtech protocol, and to relay data from some virtual devices to the network server.

# Configuring the gateway

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



# Attaching the gateway to a LoRaWAN backend (network server, join server and application server)

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

Finish by pressing "Add end node".



# Running the gateway

Now we are ready to start the virtual gateway and device:
```
java -cp lorawan.jar test.LorawanGw -f gw.cfg
```

where the configuration file "gw.cfg" includes the same values (gwEui, appEui aka JoinEUI, appServer aka Gateway server address, devEui) specified in the TTN console.

Here is an example of fragment of the console log that includes the device association and three data messages:  
  
03:44:50.388: SemtechClient[7000]: sending: PULL_DATA c0c8 feffff0123450000
03:44:50.445: DeviceClient: device: CounterDevice
03:44:50.495: SemtechClient[7000]: received: PULL_ACK c0c8
03:44:50.695: SemtechClient[7000]: sending: PUSH_DATA 41c6 feffff0123450000 {"rxpk":[{"time":"2021-02-01T03:44:50.692Z","tmst":0,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":23,"data":"AP2SA9B+1bNwAQBFIwH///4CiSUpMj4="}]}
03:44:50.815: SemtechClient[7000]: received: PUSH_ACK 41c6
03:44:55.056: SemtechClient[7000]: received: PULL_RESP 0001 {"txpk":{"imme":false,"tmst":5000000,"freq":868.1,"rfch":0,"powe":14,"modu":"LORA","datr":"SF7BW125","codr":"4/5","ipol":true,"size":33,"ncrc":true,"data":"II69kW1jc2U6c8JDJGJxN9K3FV3DbsV8cIMAFBPfH6B9"}}
03:44:55.056: SemtechClient[7000]: sending: TX_ACK 0001 feffff0123450000
03:44:55.062: DeviceClient: received LoraWAN message: pktInfo: {"txpk":{"imme":false,"tmms":0,"freq":868.1,"rfch":0,"powe":14,"modu":"LORA","datr":"SF7BW125","codr":"4/5","ipol":true,"size":33,"ncrc":true,"data":"II69kW1jc2U6c8JDJGJxN9K3FV3DbsV8cIMAFBPfH6B9"}}
03:44:55.062: DeviceClient: received LoraWAN message: MType: Join Accept, MacPayload: 8ebd916d6373653a73c24324627137d2b7155dc36ec57c7083001413, MIC: df1fa07d
03:44:55.063: DeviceClient: associated
03:44:55.064: DeviceClient: new session context: {"fNwkSIntKey":"b47b4dd19b8f66067269e0d8d6c2e8ba","sNwkSIntKey":"b47b4dd19b8f66067269e0d8d6c2e8ba","nwkSEncKey":"b47b4dd19b8f66067269e0d8d6c2e8ba","fCntUp":0,"fCntDown":0,"nFCntDwn":0,"devAddr":"2601468a","appSKey":"51b4868353692c8982a5ff6ccd77c5bc","aFCntDown":0}
03:44:55.065: DeviceClient: data: 00000000
03:44:55.068: SemtechClient[7000]: sending: PUSH_DATA 82df feffff0123450000 {"rxpk":[{"time":"2021-02-01T03:44:55.067Z","tmst":4375,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QIpGASYAAAABADDq0cW1s0Q="}]}
03:44:55.135: SemtechClient[7000]: received: PUSH_ACK 82df
03:45:00.447: SemtechClient[7000]: sending: PUSH_DATA dd80 feffff0123450000 {"status":{"time":"2021-02-01 03:45:00 GMT","lati":0.0,"long":0.0,"alti":0,"rxnb":0,"rxok":0,"rxfw":0,"ackr":0.0,"dwnb":0,"txnb":0}}
03:45:00.576: SemtechClient[7000]: received: PUSH_ACK dd80
03:45:10.445: SemtechClient[7000]: sending: PULL_DATA 5197 feffff0123450000
03:45:10.576: SemtechClient[7000]: received: PULL_ACK 5197
03:45:30.445: SemtechClient[7000]: sending: PULL_DATA 6000 feffff0123450000
03:45:30.576: SemtechClient[7000]: received: PULL_ACK 6000
03:45:50.446: SemtechClient[7000]: sending: PULL_DATA 9fd6 feffff0123450000
03:45:50.576: SemtechClient[7000]: received: PULL_ACK 9fd6
03:45:55.068: DeviceClient: data: 00000001
03:45:55.069: SemtechClient[7000]: sending: PUSH_DATA 7b64 feffff0123450000 {"rxpk":[{"time":"2021-02-01T03:45:55.069Z","tmst":64377,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QIpGASYAAQAB/r3gWefY6Xg="}]}
03:45:55.215: SemtechClient[7000]: received: PUSH_ACK 7b64
03:46:00.448: SemtechClient[7000]: sending: PUSH_DATA 916e feffff0123450000 {"status":{"time":"2021-02-01 03:46:00 GMT","lati":0.0,"long":0.0,"alti":0,"rxnb":0,"rxok":0,"rxfw":0,"ackr":0.0,"dwnb":0,"txnb":0}}
03:46:00.575: SemtechClient[7000]: received: PUSH_ACK 916e
03:46:10.446: SemtechClient[7000]: sending: PULL_DATA 755d feffff0123450000
03:46:10.574: SemtechClient[7000]: received: PULL_ACK 755d
03:46:30.446: SemtechClient[7000]: sending: PULL_DATA 876b feffff0123450000
03:46:30.575: SemtechClient[7000]: received: PULL_ACK 876b
03:46:50.446: SemtechClient[7000]: sending: PULL_DATA 64c2 feffff0123450000
03:46:50.575: SemtechClient[7000]: received: PULL_ACK 64c2
03:46:55.069: DeviceClient: data: 00000002
03:46:55.070: SemtechClient[7000]: sending: PUSH_DATA 0e3f feffff0123450000 {"rxpk":[{"time":"2021-02-01T03:46:55.069Z","tmst":124378,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QIpGASYAAgABsCja2LXSvec="}]}
03:46:55.215: SemtechClient[7000]: received: PUSH_ACK 0e3f


At the beginning of the log we can see the device type ('CounterDevice'):

03:44:50.445: DeviceClient: device: CounterDevice


Then we can see when the device is associated:

03:44:55.063: DeviceClient: associated
03:44:55.064: DeviceClient: new session context: {"fNwkSIntKey":"b47b4dd19b8f66067269e0d8d6c2e8ba","sNwkSIntKey":"b47b4dd19b8f66067269e0d8d6c2e8ba","nwkSEncKey":"b47b4dd19b8f66067269e0d8d6c2e8ba","fCntUp":0,"fCntDown":0,"nFCntDwn":0,"devAddr":"2601468a","appSKey":"51b4868353692c8982a5ff6ccd77c5bc","aFCntDown":0}


And then, the data payload that is sent to the application server. For example:

03:44:55.065: DeviceClient: data: 00000000
03:44:55.068: SemtechClient[7000]: sending: PUSH_DATA 82df feffff0123450000 {"rxpk":[{"time":"2021-02-01T03:44:55.067Z","tmst":4375,"freq":868.1,"chan":0,"rfch":1,"stat":1,"modu":"LORA","datr":"SF7BW125","codr":"4/5","rssi":-65,"lsnr":7.8,"size":17,"data":"QIpGASYAAAABADDq0cW1s0Q="}]}


In particular the first line shows the data in cleartext ('00000000'), while the second line shows the Semtech packet. It is a PUSH_DATA packet and the following information is shown: the Semtech packet type, the two-byte token, the gateway EUI, and the enclosed JSON object containing some metadata and the actual LoraWAN MAC message.
The LoraWAN MAC message is included in base64 format. It can be decode as follows:

  java -cp lorawan.jar test.LorawanParser -B QIpGASYAAAABADDq0cW1s0Q=

        MACMessage: 408a460126000000010030ead1c5b5b344
        MType: Unconfirmed Data Up
        MacPayload: 8a460126000000010030ead1
        MIC: c5b5b344
        Data message payload:
                DevAddr: 2601468a
                FCtrl: {"adr":false,"adrAckReq":false,"ack":false,"classB":false,"fOptsLen":0}
                FCnt: 0
                FPort: 1
                EncryptedFRMPayload: 0030ead1
				
The device data is encrypted. The data can be decrypted using the 'appSKey' of the session context created above.
In this example the appSKey is '51b4868353692c8982a5ff6ccd77c5bc', and the data can decrypted by doing:

  java -cp lorawan.jar test.LorawanParser -B QIpGASYAAAABADDq0cW1s0Q= -appskey 51b4868353692c8982a5ff6ccd77c5bc

        MACMessage: 408a460126000000010030ead1c5b5b344
        MType: Unconfirmed Data Up
        MacPayload: 8a460126000000010030ead1
        MIC: c5b5b344
        Data message payload:
                DevAddr: 2601468a
                FCtrl: {"adr":false,"adrAckReq":false,"ack":false,"classB":false,"fOptsLen":0}
                FCnt: 0
                FPort: 1
                FRMPayload: 00000000
