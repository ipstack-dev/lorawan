package it.unipr.netsec.thingsstack.lorawan.semtech;


import org.zoolu.util.Bytes;


/** Semtech-LoRaWAN generic packet with 'gateway' field.
 */
public abstract class SemtechGatewayPacket extends SemtechPacket {

	/* Gateway unique identifier */
	byte[] gw_addr;
	
	
	/** Creates a new DATA packet
	 * @param type packet type
	 * @param token the token
	 * @param gw_addr gateway unique identifier */
	protected SemtechGatewayPacket(int type, int token, byte[] gw_addr) {
		super(type,token);
		this.gw_addr=gw_addr;
	}


	/** Creates a new PUSH_DATA packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer */
	protected SemtechGatewayPacket(byte[] buf, int off) {
		super(0xff&buf[off+3],Bytes.toInt16(buf,off+1));
		if (buf[off]!=VERSION) throw new RuntimeException("Unsupported Semtech-LoRaWAN protocol version: "+(0xff&buf[off]));
		gw_addr=Bytes.copy(buf,off+4,8);
	}
	
	/** Gets gateway unique identifier.
	 * @return the gateway identifier */
	public byte[] getGateway() {
		return gw_addr;
	}

	@Override
	public int getPacketLength() {
		return 12;
	}

	@Override
	public int getBytes(byte[] buf, int off) {
		buf[off]=VERSION;
		Bytes.fromInt16(token,buf,off+1);
		buf[off+3]=(byte)type;
		System.arraycopy(gw_addr,0,buf,off+4,8);
		return 12;
	}

	@Override
	public byte[] getBytes() {
		byte[] data=new byte[getPacketLength()];
		getBytes(data,0);
		return data;
	}
	
	@Override
	public String toString() {
		return super.toString()+' '+Bytes.toHex(getGateway());
	}


}
