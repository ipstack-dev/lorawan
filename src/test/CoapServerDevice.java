package test;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.thingsstack.coap.message.CoapRequest;
import it.unipr.netsec.thingsstack.coap.message.CoapResponseCode;
import it.unipr.netsec.thingsstack.coap.server.AbstractCoapServer;
import it.unipr.netsec.thingsstack.coap.server.CoapServer;
import it.unipr.netsec.thingsstack.lorawan.device.LorawanClient;
import it.unipr.netsec.thingsstack.lorawan.device.LorawanClientListener;


/** LoRaWAN device that includes a CoAP server. Data received by the CoAP server are sent as LoRaWAN payload.
 */
public class CoapServerDevice extends AbstractCoapServer {
	
	/** CoAP server */
	CoapServer coapServer;

	/** LoRaWAN client */
	LorawanClient client;
	

	
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
	public CoapServerDevice(byte[] devEUI, String appCtxFile, byte[] joinEUI, byte[] appKey, int fPort, SocketAddress gwSoaddr) throws IOException {
		super();
		client=new LorawanClient(devEUI,appCtxFile,joinEUI,appKey,fPort,gwSoaddr,new LorawanClientListener(){
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
		// TODO
	}

	
	private void processReceivedData(LorawanClient client, byte[] data) {
		// not implemented
	}

	
	@Override
	protected void handlePutRequest(CoapRequest req) {
		if (client!=null && client.isAssociated()) {
			byte[] data=req.getPayload();
			System.out.println("DEBUGGGRRRR: "+data);
			if (data!=null) System.out.println("DEBUGGGRRRR: "+Bytes.toHex(data));
			try {
				client.sendData(data);
				respond(req,CoapResponseCode._2_04_Changed);
			}
			catch (IOException | GeneralSecurityException e) {
				e.printStackTrace();
				respond(req,CoapResponseCode._5_00_Internal_Server_Error);
			}
		}
		else respond(req,CoapResponseCode._4_04_Not_Found);
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
			System.out.println(flags.toUsageString(CoapServerDevice.class));
			return;
		}
				
		new CoapServerDevice(Bytes.fromFormattedHex(devEui),null,Bytes.fromFormattedHex(appEui),Bytes.fromFormattedHex(appKey),fPort,new InetSocketAddress("127.0.0.1",devPort));
	}

}
