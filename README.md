# LoRaWAN virtual gateway

LoRaWAN virtual gateway and LoRaWAN virtual devices. The virtual gateway that can be used to connect to a remote LoRaWAN network server using the Semtech protocol, relaying data from the given virtual devices to the network server.

# Configuration

The LoRaWAN virtual gateway can be configured via a configuration file, e.g.:
```
java -cp lorawan.jar test.LorawanGw -f gw.cfg
```

or by using command line options, e.g.:
```
java -cp lorawan.jar test.LorawanGw -gweui FEFFFF0123450000 -appServer router.eu.thethings.network -deveui FEFFFF0123450001 -t 40 -appeui 0000000000000000 -appkey 00000000000000000000000000000000 -v
```

Note that, if no network server is specified, TTN is used by default.

# virtual devices

Different types of virtual devices are available:
 * CountDevice - simple device with readable and writable integer value that is incremented at each reading; the integer is encoded as four bytes in big-endian;
 * CurrentTimeDevice - simple device with read-only data that is the current time returned as YYYY-MM-dd HH:mm:ss string;
 * DataDevice - device with readable and writable data maintained in RAM; the data has to be passed as parameter (byte array as hexadecimal string);
 * FileDevice - device with readable and writable data stored in a file; the file name has to be passed as parameter;</li>
 * DraginoLHT65 - Dragino LHT65 with artificial temperature and humidity values;
 * DraginoLSE01 - Dragino LSE01 with artificial temperature and soil moisture values.

 
Other types virtual devices can be used by specifying the complete class name; for example: "it.unipr.netsec.ipstack.lorawan.device.CurrentTimeDevice".

