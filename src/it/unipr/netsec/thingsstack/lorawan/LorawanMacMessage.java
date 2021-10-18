package it.unipr.netsec.thingsstack.lorawan;


import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;

import it.unipr.netsec.ipstack.net.Address;
import it.unipr.netsec.ipstack.net.Packet;


/** LoraWAN MAC message, that is the PHY payload (PHYPayload) at PHY layer.
 * <p>
 * It is formed by a MAC header (MHDR), a MAC payload (MACPayload), and a MIC trailer.
 * <p>
 * The MHDR and MIC are 1 and 4 bytes long, respectively. The MACPayload length is greater than or equal to 7 bytes.
 */
public abstract class LorawanMacMessage implements Packet<Address> {

	/** Major version (LoRaWAN R1) */
	protected static final int MAJOR_VERSION=0;

	/** Join Request type */
	public static final int TYPE_JOIN_REQUEST=0;

	/** Join Accept type */
	public static final int TYPE_JOIN_ACCEPT=1;

	/** Unconfirmed Data Up type */
	public static final int TYPE_UNCORFIRMED_DATA_UP=2;

	/** Unconfirmed Data Down type */
	public static final int TYPE_UNCORFIRMED_DATA_DOWN=3;

	/** Confirmed Data Up type */
	public static final int TYPE_CORFIRMED_DATA_UP=4;

	/** Confirmed Data Down type */
	public static final int TYPE_CORFIRMED_DATA_DOWN=5;

	/** Rejoin-request type */
	public static final int TYPE_REJOIN_REQUEST=6;

	/** Proprietary type */
	public static final int TYPE_PROPRIETARY=7;

	
	/** Message type (MType) */
	protected int type;

	/** Major version bits */
	protected int major_bits=MAJOR_VERSION;
	
	/** MacPayload */
	protected byte[] payload;

	/** Message Integrity Code (MIC) */
	protected byte[] mic;
	
	
	/** Creates a new message. 
	 * @throws GeneralSecurityException */
	/*public LorawanMacMessage(int type, byte[] payload, byte[] mic) {
		this.type=type;
		this.payload=payload;
		major_bits=MAJOR_VERSION;
		this.mic=mic;
	}*/

	/** Creates a new message.
	 * @param type Message type (MType) */
	protected LorawanMacMessage(int type) {
		this.type=type;
	}

	/** Creates a new message.
	 * @param data the buffer containing the packet */
	protected LorawanMacMessage(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new message.
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet length */
	protected LorawanMacMessage(byte[] buf, int off, int len) {
		int mhdr=buf[off];
		type=(mhdr&0xe0)>>5;
		major_bits=mhdr&0x03;
		payload=Bytes.copy(buf,off+1,len-5);
		mic=Bytes.copy(buf,off+len-4,4);
	}
	
	/** Creates a new message.
	 * @param data the buffer containing the packet */
	public static LorawanMacMessage parseMessage(byte[] data) {
		return parseMessage(data,0,data.length);
	}
	
	/** Creates a new message.
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet length */
	public static LorawanMacMessage parseMessage(byte[] buf, int off, int len) {
		int mhdr=buf[off];
		int type=(mhdr&0xe0)>>5;
		switch (type) {
			case TYPE_JOIN_REQUEST : return new LorawanJoinRequestMessage(buf,off,len);
			case TYPE_JOIN_ACCEPT : return new LorawanJoinAcceptMessage(buf,off,len);
			case TYPE_UNCORFIRMED_DATA_UP : return new LorawanDataMessage(buf,off,len);
			case TYPE_UNCORFIRMED_DATA_DOWN : return new LorawanDataMessage(buf,off,len);
			case TYPE_CORFIRMED_DATA_UP : return new LorawanDataMessage(buf,off,len);
			case TYPE_CORFIRMED_DATA_DOWN : return new LorawanDataMessage(buf,off,len);
			case TYPE_REJOIN_REQUEST : return new LorawanDataMessage(buf,off,len);
			case TYPE_PROPRIETARY : return new LorawanProprietaryMessage(buf,off,len);
		}
		throw new RuntimeException("Unknown message type ("+type+")");
	}

	/** Gets message type (MType).
	 * @return the type */
	public int getMType() {
		return type;
	}
	
	/** Gets type.
	 * @return the packet type */
	public String getMTypeString() {
		switch (type) {
			case TYPE_JOIN_REQUEST : return "Join Request";
			case TYPE_JOIN_ACCEPT : return "Join Accept";
			case TYPE_UNCORFIRMED_DATA_UP : return "Unconfirmed Data Up";
			case TYPE_UNCORFIRMED_DATA_DOWN : return "Unconfirmed Data Down";
			case TYPE_CORFIRMED_DATA_UP : return "Confirmed Data Up";
			case TYPE_CORFIRMED_DATA_DOWN : return "Confirmed Data Down";
			case TYPE_REJOIN_REQUEST : return "Rejoin-request";
			case TYPE_PROPRIETARY : return "Proprietary";
			default : return String.valueOf(type);
		}
	}
	
	/** Gets MAC header.
	 * @return the header value */
	public static byte[] getMHdr(int type) {
		return getMHdr(type,MAJOR_VERSION);
	}
	
	/** Gets MAC header.
	 * @return the header value */
	public static byte[] getMHdr(int type, int major_bits) {
		return new byte[] {(byte)(((type&0x03)<<5) | (major_bits&0x03))};
	}
	
	/** Gets MAC header. */
	protected int getMHdr() {
		return ((type&0x03)<<5) | (major_bits&0x03);
	}
	
	/** Gets MAC payload.
	 * @return the payload */
	public byte[] getMacPayload() {
		return payload;
	}
		
	/** Gets Message Integrity Code (MIC).
	 * @return the MIC */
	public byte[] getMIC() {
		return mic;
	}
		
	@Override
	public Address getSourceAddress() {
		return null;
	}

	@Override
	public Address getDestAddress() {
		return null;
	}
	
	@Override
	public int getPacketLength() {
		return payload.length+5;
	}

	@Override
	public byte[] getBytes() {
		byte[] data=new byte[payload.length+5];
		getBytes(data,0);
		return data;
	}

	@Override
	public int getBytes(byte[] buf, int off) {
		//int mhdr=(type&0x03)<<5;
		//mhdr|=major_bits&0x03;
		//buf[off]=(byte)mhdr;
		buf[off]=(byte)getMHdr();
		System.arraycopy(payload,0,buf,off+1,payload.length);
		System.arraycopy(mic,0,buf,off+1+payload.length,4);
		return payload.length+5;
	}

	@Override
	public LorawanMacMessage clone() {
		// TODO
		return this;
	}
	
	@Override
	public String toString() {
		return toString(", ");
	}

	/** Gets a string representation of this message.
	 * Different field descriptions are separated by the given delimiter.
	 * @param delim the field delimiter
	 * @return the string representation */
	public String toString(String delim) {
		StringBuffer sb=new StringBuffer();
		sb.append("MType: ").append(getMTypeString());
		sb.append(delim).append("MacPayload: ").append(Bytes.toHex(getMacPayload()));
		sb.append(delim).append("MIC: ").append(Bytes.toHex(getMIC()));
		return sb.toString();
	}
	
	/** Decrypts payload and MIC.
	 * @param key secret key 
	 * @throws GeneralSecurityException */
	public void decrypt(byte[] key) throws GeneralSecurityException {
		byte[] plaintext=AesCipher.getEncryptionInstance(key).doFinal(Bytes.concat(payload,mic));
		payload=Bytes.copy(plaintext,0,plaintext.length-4);
		mic=Bytes.copy(plaintext,plaintext.length-4,4);
	}

}
