package io.ipstack.lorawan.server.json;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.mac.SessionContext;


public class DeviceState {

	public byte[] devEUI;
	
	public SessionContext sessCtx;
	
	//public String gateway= null; // last data

	
	protected DeviceState() {
	}
	
	public DeviceState(EUI devEUI, SessionContext sessCtx) {
		this.devEUI= devEUI.bytes();
		this.sessCtx= sessCtx;
	}
	
	public EUI getDevEUI() {
		return new EUI(devEUI);
	}

}
