package io.ipstack.lorawan.semtech;


import org.zoolu.util.Bytes;

import io.ipstack.lorawan.mac.EUI;


/** Semtech-LoRaWAN generic packet with 'gateway' field.
 */
public abstract class SemtechGatewayPacket extends SemtechPacket {

	/* Gateway unique identifier */
	EUI gw_addr;
	
	
	/** Creates a new DATA packet
	 * @param type packet type
	 * @param token the token
	 * @param gw_addr gateway unique identifier */
	protected SemtechGatewayPacket(int type, int token, EUI gw_addr) {
		super(type,token);
		this.gw_addr= gw_addr;
	}


	/** Creates a new PUSH_DATA packet
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer */
	protected SemtechGatewayPacket(byte[] buf, int off) {
		super(0xff&buf[off+3],Bytes.toInt16(buf,off+1));
		if (buf[off]!=VERSION) throw new RuntimeException("Unsupported Semtech-LoRaWAN protocol version: "+(0xff&buf[off]));
		gw_addr= new EUI(Bytes.copy(buf,off+4,8));
	}
	
	/** Gets gateway EUI.
	 * @return the gateway EUI */
	public EUI getGateway() {
		return gw_addr;
	}

	@Override
	public int getPacketLength() {
		return 12;
	}

	@Override
	public int getBytes(byte[] buf, int off) {
		buf[off]= VERSION;
		Bytes.fromInt16(token,buf,off+1);
		buf[off+3]= (byte)type;
		System.arraycopy(gw_addr.bytes(),0,buf,off+4,8);
		return 12;
	}

	@Override
	public byte[] getBytes() {
		var data= new byte[getPacketLength()];
		getBytes(data,0);
		return data;
	}
	
	@Override
	public String toString() {
		return super.toString()+' '+getGateway();
	}


}
