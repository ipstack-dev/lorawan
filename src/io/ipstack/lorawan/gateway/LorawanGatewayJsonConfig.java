package io.ipstack.lorawan.gateway;

import io.ipstack.lorawan.mac.EUI;

/** JSON-compatible Gateway configuration.
 */
public class LorawanGatewayJsonConfig {

	public GwInfo gw;
	
	public AppInfo app;
	
	
	public static class GwInfo {
		public String eui;
		public float latitude= -1;
		public float longitude= -1;
		public EUI getEUI() { return new EUI(eui); }
	}
	
	public static class AppInfo {
		public String networkServer;
	}
	
	public LorawanGatewayConfig toConfig() {
		LorawanGatewayConfig cfg= new LorawanGatewayConfig();
		cfg.eui= gw.eui;
		cfg.latitude= gw.latitude;
		cfg.longitude= gw.longitude;
		cfg.networkServer= app.networkServer;
		return cfg;
	}
		
}
