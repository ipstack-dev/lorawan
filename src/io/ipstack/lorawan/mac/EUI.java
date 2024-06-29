package io.ipstack.lorawan.mac;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.zoolu.util.Bytes;


/**
 * 8-bytes Extended Unique Identifier.
 */
public class EUI {

	private String hex= null;
	private byte[] bytes= null;
	
	/**
	 * @param eui
	 */
	public EUI(byte[] eui) {
		if (eui==null) throw new RuntimeException("invalid EUI: null");
		if (eui.length!=8) throw new RuntimeException("invalid EUI length: "+eui.length);
		this.bytes= eui;
	}

	
	/**
	 * @param eui
	 */
	public EUI(String eui) {
		this(eui!=null? Bytes.fromHex(eui) : (byte[])null);
		this.hex= eui;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EUI)) return false;
		return Bytes.compare(bytes,((EUI)obj).bytes)==0;
	}

	
	@Override
	public int hashCode() {
		return Arrays.hashCode(bytes);
	}
	
	
	/**
	 * @return array of EUI bytes
	 */
	public byte[] bytes() {
		return bytes;
	}
	
	/**
	 * @return hexadecimal representation of EUI (same as toString())
	 */
	public String hex() {
		if (hex==null) hex= Bytes.toHex(bytes);
		return hex;
	}
	
	@Override
	public String toString() {
		return hex();
	}

}
