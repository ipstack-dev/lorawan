# Configuring the gateway and devices on TTN

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



# Running the gateway

For starting the gateway:
```
java -cp lorawan.jar test.LorawanGateway -f gw.cfg -v
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


# Running a device

For starting a device:
```
java -cp lorawan.jar test.LorawanDevice -f dev.cfg -v
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
