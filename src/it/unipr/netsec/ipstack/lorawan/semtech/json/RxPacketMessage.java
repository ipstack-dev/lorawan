package it.unipr.netsec.ipstack.lorawan.semtech.json;


import org.zoolu.util.json.JsonUtils;


public class RxPacketMessage {
	
	RxPacketInfo[] rxpk;

	/** Creates a new empty message. */
	public RxPacketMessage() {
	}
		
	/** Creates a new message. */
	public RxPacketMessage(RxPacketInfo rxpk) {
		this.rxpk=new RxPacketInfo[] {rxpk};
	}
	
	/** Creates a new message. */
	public RxPacketMessage(RxPacketInfo[] rxpk) {
		this.rxpk=rxpk;
	}
	
	/**
	 * @return the rxpk
	 */
	public RxPacketInfo[] getRxpk() {
		return rxpk;
	}

	@Override
	public String toString() {
		return JsonUtils.toJson(this);
	}

}
