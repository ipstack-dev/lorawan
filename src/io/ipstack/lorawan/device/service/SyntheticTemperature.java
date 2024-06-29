package io.ipstack.lorawan.device.service;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import jacob.CborEncoder;


/** Read-only service that provides synthetic temperature values.
 */
public class SyntheticTemperature implements DataService {
	
	public static enum Format { CBOR, JSON }
	
	public static String NAME= "temperature";

	Format format;
	
	
	public SyntheticTemperature() {
		this.format= Format.CBOR;
	}

	public SyntheticTemperature(Format format) {
		this.format= format;
	}

	@Override
	public byte[] getData() {
		try {
			double value=DataUtils.getTemperature(new Date());
			if (format==Format.CBOR) {
				var baos= new ByteArrayOutputStream();
				var enc= new CborEncoder(baos);
				enc.writeMapStart(1);
				enc.writeTextString(NAME);
				enc.writeDouble(value);
				return baos.toByteArray();				
			}
			else
			if (format==Format.JSON) {
				return DataUtils.getJson(NAME,value).getBytes();
			}				
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setData(byte[] data) {
		// do nothing	
	}

}
