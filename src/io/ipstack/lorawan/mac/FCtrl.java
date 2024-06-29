package io.ipstack.lorawan.mac;


import org.zoolu.util.json.Json;


/** frame control octet (FCtrl) for uplink. */
public class FCtrl {

	boolean adr; // ADR
	boolean adrAckReq; // ADRACKReq (for uplink) or RFU (for downlink)
	boolean ack; // ACK
	boolean classB; // ClassB (for uplink) or fPending (for downlink)
	int fOptsLen; // FOptsLen

	
	/** Creates an uplink FCtrl field.
	 * @param adr
	 * @param adrAckReq
	 * @param ack
	 * @param classB
	 * @param fOptsLen */
	public FCtrl(boolean adr, boolean adrAckReq, boolean ack, boolean classB, int fOptsLen) {
		this.adr= adr;
		this.adrAckReq= adrAckReq;
		this.ack= ack;
		this.classB= classB;
		this.fOptsLen= fOptsLen;
	}

	/** Creates a downlink FCtrl field.
	 * @param adr
	 * @param adrAckReq
	 * @param ack
	 * @param classB
	 * @param fOptsLen */
	public FCtrl(boolean adr, boolean ack, boolean fPending, int fOptsLen) {
		this.adr= adr;
		this.adrAckReq= false;
		this.ack= ack;
		this.classB= fPending;
		this.fOptsLen= fOptsLen;
	}
	
	
	public FCtrl(int val) {
		adr= itob(val,7);
		adrAckReq= itob(val,6);
		ack= itob(val,5);
		classB= itob(val,4);
		fOptsLen= val&0xf;
	}
	
	public byte getVal() {
		return (byte)(btoi(adr,7)|btoi(adrAckReq,6)|btoi(ack,5)|btoi(classB,4)|(fOptsLen&0xf));
	}
	
	/*public byte[] getBytes() {
		return new byte[]{(byte)getVal()};
	}
	
	public int getBytes(byte[] buf, int off) {
		buf[off]= (byte)getVal();
		return 1;
	}*/
	
	/** Gets an integer with one bit set to a given value.
	 * @param b the bit value
	 * @param pos the bit position
	 * @return the bit value */
	private int btoi(boolean b, int pos) {
		return b? 1<<pos : 0;
	}
	
	/** Gets a given bit of an integer.
	 * @param i the integer
	 * @param pos the bit position
	 * @return the bit value */
	private boolean itob(int i, int pos) {
		return ((i>>pos)&1)==1;
	}

	/**
	 * @return the ADR
	 */
	public boolean isAdr() {
		return adr;
	}
	
	/**
	 * @return the ADRACKReq
	 */
	public boolean isAdrAckReq() {
		return adrAckReq;
	}
	
	/**
	 * @return the ACK
	 */
	public boolean isAck() {
		return ack;
	}
	
	/**
	 * @return the ClassB
	 */
	public boolean isClassB() {
		return classB;
	}

	/**
	 * @return the FPending
	 */
	public boolean isFPending() {
		return classB;
	}

	/**
	 * @return the Opts length (FOptsLen)
	 */
	public int getfOptsLen() {
		return fOptsLen;
	}
	
	@Override
	public String toString() {
		return Json.toJSON(this);
	}

}
