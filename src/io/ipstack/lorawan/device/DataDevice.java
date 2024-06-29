package io.ipstack.lorawan.device;

import java.io.IOException;

import org.zoolu.util.Clock;
import org.zoolu.util.Timer;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.lorawan.client.LorawanClient;
import io.ipstack.lorawan.client.LorawanClientListener;
import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.UdpMacMessageExchanger;


/** LoRaWAN device that periodically sends updated data obtained by a given data service.
 */
public class DataDevice {

	/** Prints a message. */
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	/** LoRaWAN client */
	LorawanClient client;
	
	/** Data service */
	DataService service;

	/** Data timeout */
	long timeout;

	
	/** Creates a new client. 
	 * @param appCtx application context
	 * @param fPort fport field
	 * @param macMsgExchanger MAC message exchanger
	 * @param dataService data service
	 * @param timeout data timeout
	 * @throws IOException
	 */
	public DataDevice(AppContext appCtx, int fPort, UdpMacMessageExchanger macMsgExchanger, DataService service, long timeout) throws IOException {
		client= new LorawanClient(appCtx,fPort,macMsgExchanger,new LorawanClientListener(){
			@Override
			public void onJoinAccept(LorawanClient client) {
				processJoinAccept(client);
			}
			@Override
			public void onReceivedData(LorawanClient client, byte[] data) {
				processReceivedData(client,data);
		}});
		this.service= service;
		this.timeout= timeout;
		client.join();
	}


	private void processJoinAccept(LorawanClient client) {
		// start sending data
		processDataTimeout(null);
	}
	
	
	private void processDataTimeout(Timer t) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] data= service.getData();
					if (data!=null) client.sendData(data);
					else log("No data to send");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		Clock.getDefaultClock().newTimer(timeout,this::processDataTimeout).start();
	}

	
	private void processReceivedData(LorawanClient client, byte[] data) {
		service.setData(data);
	}
	
}
