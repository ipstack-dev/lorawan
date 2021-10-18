package it.unipr.netsec.ipstack.lorawan.device.service;


/** service that produces and/or consumes byte data.
 */
public interface Service {

	public byte[] getData();

	public void setData(byte[] data);
}
