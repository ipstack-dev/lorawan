package it.unipr.netsec.ipstack.lorawan.device;


import java.io.IOException;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

import org.zoolu.util.Clock;
import org.zoolu.util.Timer;

import it.unipr.netsec.ipstack.lorawan.device.service.Service;


/** LoRaWAN device that periodically sends updated data obtained by a given data service.
 */
public class DataDevice {

	/** LoRaWAN client */
	LorawanClient client;
	
	/** Data service */
	Service service;	

	/** Data timeout */
	long timeout;

	
	/** Creates a new client. 
	 * @param devEUI the device EUI
	 * @param appCtxFile application context file
	 * @param joinEUI join EUI
	 * @param appKey AppKey
	 * @param fPort fport field
	 * @param gwSoaddr socket address of LoRaWAN gateway
	 * @param dataService data service
	 * @param timeout data timeout
	 * @throws IOException
	 */
	public DataDevice(byte[] devEUI, String appCtxFile, byte[] joinEUI, byte[] appKey, int fPort, SocketAddress gwSoaddr, Service service, long timeout) throws IOException {
		client=new LorawanClient(devEUI,appCtxFile,joinEUI,appKey,fPort,gwSoaddr,new LorawanClientListener(){
			@Override
			public void onJoinAccept(LorawanClient client) {
				processJoinAccept(client);
			}
			@Override
			public void onReceivedData(LorawanClient client, byte[] data) {
				processReceivedData(client,data);
		}});
		this.service=service;
		this.timeout=timeout;
		client.join();
	}
	

	private void processJoinAccept(LorawanClient client) {
		// start sending data
		processDataTimeout(null);
	}
	
	
	private void processDataTimeout(Timer t) {
		try {
			client.sendData(service.getData());
		}
		catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}
		Clock.getDefaultClock().newTimer(timeout,this::processDataTimeout).start();
	}

	
	private void processReceivedData(LorawanClient client, byte[] data) {
		// not implemented
	}
	
}
