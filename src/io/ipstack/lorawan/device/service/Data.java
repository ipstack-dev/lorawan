package io.ipstack.lorawan.device.service;

import org.zoolu.util.Bytes;

/** Service that reads data from and writes data to RAM.
 * The data is passed to the constructor as byte array or as hexadecimal string;
 */
public class Data implements DataService {
	
	byte[] data;

	
	public Data(String[] args) {
		this(Bytes.fromFormattedHex(args[0]));
	}
	
	public Data(byte[] data) {
		this.data= data;
	}
	
	@Override
	public synchronized byte[] getData() {
		return data;
	}

	@Override
	public synchronized void setData(byte[] data) {
		this.data= data;
	}

}
