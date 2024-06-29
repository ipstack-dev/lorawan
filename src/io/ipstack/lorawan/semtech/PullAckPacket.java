package io.ipstack.lorawan.semtech;



/** Semtech-LoRaWAN PULL_ACK packet.
 */public class PullAckPacket extends SemtechPacket {
	
	
	/** Creates a new PULL_ACK packet
	 * @param token the random token */
	public PullAckPacket(int token) {
		super(SemtechPacket.PULL_ACK,token);
	}


	/** Creates a new PUSH_DATA packet
	 * @param data the PULL_ACK containing the packet */
	public PullAckPacket(byte[] data) {
		this(data,0);
	}


	/** Creates a new PULL_ACK packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer */
	public PullAckPacket(byte[] buf, int off) {
		super(buf,off);
		if (type!=SemtechPacket.PULL_ACK) throw new RuntimeException("It is not a PULL_ACK packet: "+(0xff&buf[off+3]));
	}


	@Override
	public PullAckPacket clone() {
		return new PullAckPacket(token);
	}
	
}
