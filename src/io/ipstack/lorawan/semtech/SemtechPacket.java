package io.ipstack.lorawan.semtech;


import org.zoolu.util.Bytes;

import io.ipstack.net.base.Address;
import io.ipstack.net.base.Packet;


/** Semtech-LoRaWAN packet.
 */
public abstract class SemtechPacket implements Packet<Address> {

	/** Protocol version */
	public static final int VERSION= 2;

	/** PUSH DATA identifier */
	public static final int PUSH_DATA= 0;

	/** PUSH ACK identifier */
	public static final int PUSH_ACK= 1;

	/** PULL DATA identifier */
	public static final int PULL_DATA= 2;

	/** PULL ACK identifier */
	public static final int PULL_ACK= 4;

	/** PULL RESP identifier */
	public static final int PULL_RESP= 3;

	/** TX ACK identifier */
	public static final int TX_ACK= 5;

	
	/** Type */
	protected int type;

	/** Token */
	protected int token;
	
	
	/** Creates a new Semtech packet.
	 * @param type packet type
	 * @param token the token */
	protected SemtechPacket(int type, int token) {
		this.type= type;
		this.token= token;
	}

	
	/** Creates a new ACK packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer */
	protected SemtechPacket(byte[] buf, int off) {
		this(0xff&buf[off+3],Bytes.toInt16(buf,off+1));
		if (buf[off]!=VERSION) throw new RuntimeException("Unsupported Semtech-LoRaWAN protocol version: "+(0xff&buf[off]));
	}

	/** Gets type.
	 * @return the packet type */
	public int getType() {
		return type;
	}
	
	/** Gets type.
	 * @return the packet type */
	public String getTypeString() {
		switch (type) {
			case PUSH_DATA : return "PUSH_DATA";
			case PUSH_ACK : return "PUSH_ACK";
			case PULL_DATA : return "PULL_DATA";
			case PULL_ACK : return "PULL_ACK";
			case PULL_RESP : return "PULL_RESP";
			case TX_ACK : return "TX_ACK";
			default : return String.valueOf(type);
		}
	}
	
	/** Gets the token.
	 * @return the token value */
	public int getToken() {
		return token;
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
		return 4;
	}

	@Override
	public int getBytes(byte[] buf, int off) {
		buf[off]= VERSION;
		Bytes.fromInt16(token,buf,off+1);
		buf[off+3]= (byte)type;
		return 4;
	}

	@Override
	public byte[] getBytes() {
		var data= new byte[getPacketLength()];
		getBytes(data,0);
		return data;
	}

	@Override
	public abstract SemtechPacket clone();

	/** Gets a Semtech-LoRaWAN packet from an array of bytes.
	 * @param data the array of byte
	 * @return the Semtech-LoRaWAN packet */
	public static SemtechPacket parseSemtechPacket(byte[] data) {
		return parseSemtechPacket(data,0,data.length);
	}

	/** Gets a Semtech-LoRaWAN packet from an array of bytes.
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len the packet total length
	 * @return the Semtech-LoRaWAN packet */
	public static SemtechPacket parseSemtechPacket(byte[] buf, int off, int len) {
		int type= 0xff&buf[off+3];
		switch (type) {
			case PUSH_DATA : return new PushDataPacket(buf,off,len);
			case PUSH_ACK : return new PushAckPacket(buf,off);
			case PULL_DATA : return new PullDataPacket(buf,off);
			case PULL_ACK : return new PullAckPacket(buf,off);
			case PULL_RESP : return new PullRespPacket(buf,off,len);
			case TX_ACK : return new TxAckPacket(buf,off,len);
			default : throw new RuntimeException("It is not a valid Semtech-LoRaWAN packet: "+Bytes.toHex(buf,off,len));
		}
	}
	
	@Override
	public String toString() {
		return getTypeString()+' '+Bytes.int16ToHex(getToken());
	}

}
