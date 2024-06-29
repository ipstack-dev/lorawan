# Virtual devices

The provided virtual devices are software LoRaWAN devices capable to exchange LoRaWAN MAC packets with a (virtual) gateway connected to a LoRaWAN backend system.

Software devices that acts as standard LoRaWAN devices starting at MAC layer and above. They are capable to connect to a remote LoRaWAN backend system through a virtual gateway.
Since the LoRaWAN physical layer is not present, the devices communicate with the virtual gateway by simply encapsulating LoRaWAN MAC packets within UDP datagrams.

These types of virtual devices are currently provided:
 * Counter - simple device with readable and writable integer value that is incremented at each reading; the integer is encoded as four bytes in big-endian;
 * CurrentTime - simple device with read-only data that is the current time returned as YYYY-MM-dd HH:mm:ss string;
 * Data - device with readable and writable data maintained in RAM; the data has to be passed as parameter (byte array as hexadecimal string);
 * FileData - device with readable and writable data stored in a file; the file name has to be passed as parameter;</li>
 * DraginoLHT65 - Dragino LHT65 with artificial temperature and humidity values;
 * DraginoLSE01 - Dragino LSE01 with artificial temperature and soil moisture values.

Other types of devices can be also used by simply adding their upper layer implementation (payload generation and/or consumption) and by running the generic device (*LorawanDevice*) using as type name the complete class name.
