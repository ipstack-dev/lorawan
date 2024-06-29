package io.ipstack.lorawan.semtech;

import org.zoolu.util.Bytes;

/** Semtech-LoRaWAN PULL_RESP packet.
 */
public class PullRespPacket extends SemtechPacket implements SemtechJsonObjectPacket {
	
	/* JSON object */
	String json_object;
	
	
	/** Creates a new PULL_RESP packet
	 * @param token the random token
	 * @param json_object the JSON object */
	public PullRespPacket(int token, String json_object) {
		super(SemtechPacket.PULL_RESP,token);
		this.json_object= json_object;
	}


	/** Creates a new PULL_RESP packet
	 * @param data the byte array containing the packet */
	public PullRespPacket(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new PULL_RESP packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet total length */
	public PullRespPacket(byte[] buf, int off, int len) {
		super(buf,off);
		if (type!=SemtechPacket.PULL_RESP) throw new RuntimeException("It is not a PULL_RESP packet: "+(0xff&buf[off+3]));
		json_object= new String(buf,off+4,len-4);
	}

	@Override
	public String getJsonObject() {
		return json_object;
	}
	
	@Override
	public int getPacketLength() {
		return 4+json_object.length();
	}

	@Override
	public int getBytes(byte[] buf, int off) {
		super.getBytes(buf,off);
		byte[] body= json_object.getBytes();
		System.arraycopy(body,0,buf,off+4,body.length);
		return 4+body.length;
	}

	@Override
	public PullRespPacket clone() {
		return new PullRespPacket(token,json_object);
	}
	
	@Override
	public String toString() {
		return super.toString()+' '+getJsonObject();
	}

}
