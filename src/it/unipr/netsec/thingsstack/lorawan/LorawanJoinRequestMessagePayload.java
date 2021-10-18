package it.unipr.netsec.thingsstack.lorawan;


import org.zoolu.util.Bytes;


public class LorawanJoinRequestMessagePayload {

	/** JoinEUI (8 bytes) */
	byte[] joinEui;
	
	/** DevEUI (8 bytes) */
	byte[] devEui;
	
	/** DevNonce (2 bytes) */
	byte[] devNonce;

	
	/** Creates a new payload.
	 * @param joinEui JoinEUI
	 * @param devEui DevEUI
	 * @param devNonce DevNonce */
	public LorawanJoinRequestMessagePayload(byte[] joinEui, byte[] devEui, byte[] devNonce) {
		this.joinEui=joinEui;
		this.devEui=devEui;
		this.devNonce=devNonce;
	}

	/** Creates a new payload.
	 * @param data the payload */
	public LorawanJoinRequestMessagePayload(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new payload.
	 * @param buf buffer containing the payload
	 * @param off offset within the buffer
	 * @param len payload length */
	public LorawanJoinRequestMessagePayload(byte[] buf, int off, int len) {
		joinEui=Bytes.reverseOrderCopy(buf,off,8);
		devEui=Bytes.reverseOrderCopy(buf,off+8,8);
		devNonce=Bytes.reverseOrderCopy(buf,off+16,2);
	}
	
	/** Gets payload length.
	 * @return the length */
	public int getLength() {
		return 18;
	}

	/** Gets payload bytes.
	 * @return the bytes */
	public byte[] getBytes() {
		byte[] data=new byte[18];
		getBytes(data,0);
		return data;
	}

	/** Gets payload bytes.
	 * @param buf buffer where payload has to be written
	 * @param off offset within the buffer
	 * @return the payload length */
	public int getBytes(byte[] buf, int off) {
		Bytes.reverseOrderCopy(joinEui,0,buf,off,8);
		Bytes.reverseOrderCopy(devEui,0,buf,off+8,8);
		Bytes.reverseOrderCopy(devNonce,0,buf,off+16,2);
		return 18;
	}

	/**
	 * @return the joinEui */
	public byte[] getJoinEui() {
		return joinEui;
	}
	
	/**
	 * @return the devEui */
	public byte[] getDevEui() {
		return devEui;
	}
	
	/**
	 * @return the devNonce */
	public byte[] getDevNonce() {
		return devNonce;
	}
	
	@Override
	public String toString() {
		return toString(", ");
	}

	/** Gets a string representation of this object.
	 * Different field descriptions are separated by the given delimiter.
	 * @param delim the field delimiter
	 * @return the string representation */
	public String toString(String delim) {
		StringBuffer sb=new StringBuffer();
		sb.append("AppEUI: ").append(Bytes.toHex(getJoinEui()));
		sb.append(delim).append("DevEUI: ").append(Bytes.toHex(getDevEui()));
		sb.append(delim).append("DevNonce: ").append(Bytes.toHex(getDevNonce()));
		return sb.toString();
	}

}
