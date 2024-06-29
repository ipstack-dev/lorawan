package io.ipstack.lorawan.device.service;


import java.util.Date;

import org.zoolu.util.DateFormat;


/** Read-only service that provides the current time returned as YYYY-MM-dd HH:mm:ss string.
 */
public class CurrentTime implements DataService {

	public CurrentTime() {
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
