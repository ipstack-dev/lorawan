package io.ipstack.lorawan.server.json;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.semtech.json.RxPacketInfo;


public class RxMetadata {

	public String gwEUI;
	public RxPacketInfo rxPktInfo;
	
	protected RxMetadata() {}
	
	public RxMetadata(String gwEUI,	RxPacketInfo rxPktInfo) {
		this.gwEUI= gwEUI;
		this.rxPktInfo= rxPktInfo;
	}
	
	public EUI getGwEUI() {
		return new EUI(gwEUI);
	}


}
