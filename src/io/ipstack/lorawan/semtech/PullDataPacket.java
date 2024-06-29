package io.ipstack.lorawan.semtech;

import io.ipstack.lorawan.mac.EUI;

/** Semtech-LoRaWAN PULL_DATA packet.
 */
public class PullDataPacket extends SemtechGatewayPacket {
	
	
	/** Creates a new PUSH_DATA packet
	 * @param token the random token
	 * @param gw_addr gateway unique identifier */
	public PullDataPacket(int token, EUI gw_addr) {
		super(SemtechPacket.PULL_DATA,token,gw_addr);
		this.gw_addr= gw_addr;
	}


	/** Creates a new PUSH_DATA packet
	 * @param data the buffer containing the packet */
	public PullDataPacket(byte[] data) {
		this(data,0);
	}


	/** Creates a new PUSH_DATA packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet total length */
	public PullDataPacket(byte[] buf, int off) {
		super(buf,off);
		if (buf[off+3]!=SemtechPacket.PULL_DATA) throw new RuntimeException("It is not a PULL_DATA packet: "+(0xff&buf[off+3]));
	}


	@Override
	public PullDataPacket clone() {
		return new PullDataPacket(token,gw_addr);
	}
	
}
