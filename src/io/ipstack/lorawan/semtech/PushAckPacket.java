package io.ipstack.lorawan.semtech;



/** Semtech-LoRaWAN PUSH_ACK packet.
 */public class PushAckPacket extends SemtechPacket {
	
	
	/** Creates a new PUSH_ACK packet
	 * @param token the random token */
	public PushAckPacket(int token) {
		super(SemtechPacket.PUSH_ACK,token);
	}


	/** Creates a new PUSH_ACK packet
	 * @param data the buffer containing the packet */
	public PushAckPacket(byte[] data) {
		this(data,0);
	}


	/** Creates a new PUSH_ACK packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer */
	public PushAckPacket(byte[] buf, int off) {
		super(buf,off);
		if (type!=SemtechPacket.PUSH_ACK) throw new RuntimeException("It is not a PUSH_ACK packet: "+(0xff&buf[off+3]));
	}


	@Override
	public PushAckPacket clone() {
		return new PushAckPacket(token);
	}
	
}
