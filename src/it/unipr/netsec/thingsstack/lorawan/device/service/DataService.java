package it.unipr.netsec.thingsstack.lorawan.device.service;


/** service that produces and/or consumes byte data.
 */
public interface DataService {

	public byte[] getData();

	public void setData(byte[] data);
}
