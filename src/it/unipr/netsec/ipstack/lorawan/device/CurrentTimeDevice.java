package it.unipr.netsec.ipstack.lorawan.device;


import java.util.Date;

import org.zoolu.util.DateFormat;


/** Simple device with read-only data that is the current time returned as YYYY-MM-dd HH:mm:ss string.
 */
public class CurrentTimeDevice implements Device {

	public CurrentTimeDevice() {
	}

	@Override
	public byte[] getData() {
		return DateFormat.formatYyyyMMddHHmmssSSS(new Date()).substring(0,19).getBytes();
	}

	@Override
	public void setData(byte[] data) {
		// do nothing	
	}

}
