package test.old;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.zoolu.util.Bytes;
import org.zoolu.util.Clock;
import org.zoolu.util.Flags;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.Timer;

import it.unipr.netsec.thingsstack.lorawan.device.LorawanClient;
import it.unipr.netsec.thingsstack.lorawan.device.LorawanClientListener;
import it.unipr.netsec.thingsstack.lorawan.device.service.DataService;
import it.unipr.netsec.thingsstack.lorawan.device.service.SyntheticTemperature;


/** LoRaWAN device periodically changes the Join key.
 */
public class DynamicKeyDevice {
	
	private static void log(String str) {
		System.out.println("OUT: "+str);
	}

	
	/** LoRaWAN client */
	DynamicKeyLorawanClient client;
		
	/** Data service */
	DataService service;

	/** Data timeout */
	long dataTimeout;

	/** Key timeout */
	long keyTimeout=3*60*1000; // 3 min

	
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
	public DynamicKeyDevice(byte[] devEUI, String appCtxFile, byte[] joinEUI, byte[] appKey, int fPort, SocketAddress gwSoaddr, DataService service, long dataTimeout) throws IOException {
		super();
		this.service=service;
		this.dataTimeout=dataTimeout;
		client=new DynamicKeyLorawanClient(devEUI,appCtxFile,joinEUI,appKey,fPort,gwSoaddr,new LorawanClientListener(){
			@Override
			public void onJoinAccept(LorawanClient client) {
				processJoinAccept(client);
			}
			@Override
			public void onReceivedData(LorawanClient client, byte[] data) {
				processReceivedData(client,data);
		}});
		client.join();
	}
	

	private void processJoinAccept(LorawanClient client) {
		Clock.getDefaultClock().newTimer(keyTimeout,this::processKeyTimeout).start();
		// start sending data
		processDataTimeout(null);
	}

	
	private void processKeyTimeout(Timer t) {
		byte[] key=client.getAppKey();
		int counter=Bytes.toInt16(key,key.length-2);
		counter+=1;
		Bytes.fromInt16(counter,key,key.length-2);
		client.setAppKey(key);
		client.join();
	}

	
	private void processDataTimeout(Timer t) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] data=service.getData();
					if (data!=null) client.sendData(data);
					else log("No data to send");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		Clock.getDefaultClock().newTimer(dataTimeout,this::processDataTimeout).start();
	}

	
	private void processReceivedData(LorawanClient client, byte[] data) {
		// not implemented
	}
	
	
	public static void main(String[] args) throws Exception {
		Flags flags=new Flags(args);

		String appEui=flags.getString("-appeui",null,"EUI","join/application EUI");
		String appKey=flags.getString("-appkey",null,"key","application key");
		
		int devPort=flags.getInteger("-devPort",4444,"port","local port for communicating with virtual devices");		
		String devEui=flags.getString("-deveui",null,"EUI","device EUI");	
		int fPort=flags.getInteger("-fport",1,"port","value of FPort field in the LoRaWAN DATA messages (default is 1)");

		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this message");
		
		if (verbose) {
			SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.INFO));
			LorawanClient.VERBOSE=true;
		}

		if (help) {
			System.out.println(flags.toUsageString(DynamicKeyDevice.class));
			return;
		}
				
		new DynamicKeyDevice(Bytes.fromFormattedHex(devEui),null,Bytes.fromFormattedHex(appEui),Bytes.fromFormattedHex(appKey),fPort,new InetSocketAddress("127.0.0.1",devPort),new SyntheticTemperature(),1*60*1000);
	}

}
