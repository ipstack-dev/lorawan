package io.ipstack.lorawan.mac;


import org.zoolu.util.Bytes;


/** Join-Request frame that is the Payload of a Join-Request MAC message.
 */
public class LorawanJoinRequest {

	/** JoinEUI (8 bytes) */
	EUI joinEui;
	
	/** DevEUI (8 bytes) */
	EUI devEui;
	
	/** DevNonce (2 bytes) */
	byte[] devNonce;

	
	/** Creates a new payload.
	 * @param joinEui JoinEUI
	 * @param devEui DevEUI
	 * @param devNonce DevNonce */
	public LorawanJoinRequest(EUI joinEui, EUI devEui, byte[] devNonce) {
		this.joinEui= joinEui;
		this.devEui= devEui;
		this.devNonce= devNonce;
	}

	/** Creates a new payload.
	 * @param data the payload */
	public LorawanJoinRequest(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new payload.
	 * @param buf buffer containing the payload
	 * @param off offset within the buffer
	 * @param len payload length */
	public LorawanJoinRequest(byte[] buf, int off, int len) {
		joinEui= new EUI(Bytes.reverseOrderCopy(buf,off,8));
		devEui= new EUI(Bytes.reverseOrderCopy(buf,off+8,8));
		devNonce= Bytes.reverseOrderCopy(buf,off+16,2);
	}
	
	/** Gets payload length.
	 * @return the length */
	public int getLength() {
		return 18;
	}

	/** Gets payload bytes.
	 * @return the bytes */
	public byte[] getBytes() {
		var data= new byte[18];
		getBytes(data,0);
		return data;
	}

	/** Gets payload bytes.
	 * @param buf buffer where payload has to be written
	 * @param off offset within the buffer
	 * @return the payload length */
	public int getBytes(byte[] buf, int off) {
		Bytes.reverseOrderCopy(joinEui.bytes(),0,buf,off,8);
		Bytes.reverseOrderCopy(devEui.bytes(),0,buf,off+8,8);
		Bytes.reverseOrderCopy(devNonce,0,buf,off+16,2);
		return 18;
	}

	/**
	 * @return the joinEui */
	public EUI getJoinEui() {
		return joinEui;
	}
	
	/**
	 * @return the devEui */
	public EUI getDevEui() {
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
		var sb= new StringBuffer();
		sb.append("AppEUI: ").append(joinEui.hex());
		sb.append(delim).append("DevEUI: ").append(devEui.hex());
		sb.append(delim).append("DevNonce: ").append(Bytes.toHex(devNonce));
		return sb.toString();
	}

}
