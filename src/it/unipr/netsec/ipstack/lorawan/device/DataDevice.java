package it.unipr.netsec.ipstack.lorawan.device;

import org.zoolu.util.Bytes;

/** Device with readable and writable data maintained in RAM.
 * The data is passed to the constructor as byte array or as hexadecimal string;
 */
public class DataDevice implements Device {
	
	byte[] data;

	
	public DataDevice(String[] args) {
		this(Bytes.fromFormattedHex(args[0]));
	}
	
	public DataDevice(byte[] data) {
		this.data=data;
	}
	
	@Override
	public synchronized byte[] getData() {
		return data;
	}

	@Override
	public synchronized void setData(byte[] data) {
		this.data=data;
	}

}
