package io.ipstack.lorawan.device.service;


/** service that produces and/or consumes byte data.
 */
public interface DataService {

	public byte[] getData();

	public void setData(byte[] data);
}
