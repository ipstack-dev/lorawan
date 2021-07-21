package it.unipr.netsec.ipstack.lorawan.device;


import org.zoolu.util.Bytes;


/** Simple device with readable and writable integer value that is incremented at each reading.
 * The integer is encoded as four bytes in big-endian.
 */
public class CounterDevice implements Device {
	
	long counter=0;

	
	public CounterDevice() {
	}
	
	@Override
	public synchronized byte[] getData() {
		return Bytes.fromInt32(counter++);
	}

	@Override
	public synchronized void setData(byte[] data) {
		counter=Bytes.toInt32(data);
	}

}
