package io.ipstack.lorawan.server;


/** LoRaWAN Server configuration.
 * It can be easily imported/exported in JSON format.
 */
public class LorawanServerConfiguration {
	
	public byte[] homeNetId; // network ID, 3 hexadecimal bytes
	
	public int port= LorawanServer.DEFAULT_NETWORK_SERVER_PORT;

	public String deviceFile; // where app contexts are stored
	
	public String stateFile; // where the current server state is saved
	
	public String backendNetworkServer;
	
	public String[] allowedGateways;
	
}
