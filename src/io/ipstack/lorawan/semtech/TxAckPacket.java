package io.ipstack.lorawan.semtech;

import io.ipstack.lorawan.mac.EUI;

/** Semtech-LoRaWAN TX_ACK packet.
 */
public class TxAckPacket extends SemtechGatewayPacket implements SemtechJsonObjectPacket {
	
	/* JSON object */
	String json_object;
	
	
	/** Creates a new TX_ACK packet
	 * @param token the random token
	 * @param gw_addr gateway unique identifier
	 * @param json_object JSON object */
	public TxAckPacket(int token, EUI gw_addr, String json_object) {
		super(SemtechPacket.TX_ACK,token,gw_addr);
		this.json_object= json_object;
	}


	/** Creates a new TX_ACK packet
	 * @param data the buffer containing the packet */
	public TxAckPacket(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new TX_ACK packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet total length */
	public TxAckPacket(byte[] buf, int off, int len) {
		super(buf,off);
		if (buf[off+3]!=SemtechPacket.TX_ACK) throw new RuntimeException("It is not a TX_ACK packet: "+(0xff&buf[off+3]));
		json_object= new String(buf,off+12,len-12);
	}
	
	@Override
	public String getJsonObject() {
		return json_object;
	}
	
	@Override
	public int getPacketLength() {
		return 12+(json_object!=null?json_object.length():0);
	}

	@Override
	public int getBytes(byte[] buf, int off) {
		super.getBytes(buf,off);
		if (json_object!=null && json_object.length()>0) {
			var body= json_object.getBytes();
			System.arraycopy(body,0,buf,off+12,body.length);
			return 12+body.length;			
		}
		else return 12;
	}

	@Override
	public TxAckPacket clone() {
		return new TxAckPacket(token,gw_addr,json_object);
	}
	
}
