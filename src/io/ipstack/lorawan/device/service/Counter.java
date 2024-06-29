package io.ipstack.lorawan.device.service;


import org.zoolu.util.Bytes;


/** Service that provides an integer value that is incremented at each reading.
 * The integer is encoded as four bytes in big-endian.
 */
public class Counter implements DataService {
	
	long counter= 0;

	
	public Counter() {
	}
	
	@Override
	public synchronized byte[] getData() {
		return Bytes.fromInt32(counter++);
	}

	@Override
	public synchronized void setData(byte[] data) {
		counter= Bytes.toInt32(data);
	}

}
