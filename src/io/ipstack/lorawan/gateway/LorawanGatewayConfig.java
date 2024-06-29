package io.ipstack.lorawan.gateway;

import io.ipstack.lorawan.mac.EUI;


/** Gateway configuration.
 */
public class LorawanGatewayConfig {
	
	public String eui;
	public float latitude= -1;
	public float longitude= -1;
	public String networkServer;
	
	
	protected LorawanGatewayConfig() {
	}

	public LorawanGatewayConfig(String eui, float latitude, float longitude, String networkServer) {
		this.eui= eui;
		this.latitude= latitude;
		this.longitude= longitude;
		this.networkServer= networkServer;
	}

	public EUI getEUI() {
		return new EUI(eui);
	}
	
}
