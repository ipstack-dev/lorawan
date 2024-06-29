package io.ipstack.lorawan.semtech;

import org.zoolu.util.Bytes;

import io.ipstack.lorawan.mac.EUI;

/** Semtech-LoRaWAN PUSH_DATA packet.
 */
public class PushDataPacket extends SemtechGatewayPacket implements SemtechJsonObjectPacket {
	
	/* JSON object */
	String json_object;
	
	
	/** Creates a new PUSH_DATA packet
	 * @param token the random token
	 * @param gw_addr gateway unique identifier
	 * @param json_object JSON object */
	public PushDataPacket(int token, EUI gw_addr, String json_object) {
		super(SemtechPacket.PUSH_DATA,token,gw_addr);
		this.json_object= json_object;
	}


	/** Creates a new PUSH_DATA packet
	 * @param data the buffer containing the packet */
	public PushDataPacket(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new PUSH_DATA packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet total length */
	public PushDataPacket(byte[] buf, int off, int len) {
		super(buf,off);
		if (buf[off+3]!=SemtechPacket.PUSH_DATA) throw new RuntimeException("It is not a PUSH_DATA packet: "+(0xff&buf[off+3]));
		json_object= new String(buf,off+12,len-12);
	}

	@Override
	public String getJsonObject() {
		return json_object;
	}
	
	@Override
	public int getPacketLength() {
		return 12+json_object.length();
	}

	@Override
	public int getBytes(byte[] buf, int off) {
		super.getBytes(buf,off);
		byte[] body= json_object.getBytes();
		System.arraycopy(body,0,buf,off+12,body.length);
		return 12+body.length;
	}

	@Override
	public PushDataPacket clone() {
		return new PushDataPacket(token,gw_addr,json_object);
	}
	
	@Override
	public String toString() {
		return super.toString()+' '+getJsonObject();
	}

	
}
