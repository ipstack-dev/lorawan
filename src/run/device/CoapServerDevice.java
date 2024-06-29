package run.device;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;
import org.zoolu.util.Random;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.coap.message.CoapRequest;
import io.ipstack.coap.message.CoapResponseCode;
import io.ipstack.coap.server.AbstractCoapServer;
import io.ipstack.lorawan.client.LorawanClient;
import io.ipstack.lorawan.client.LorawanClientListener;
import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.mac.UdpMacMessageExchanger;


/** LoRaWAN device that includes a CoAP server. Data received by the CoAP server are sent as LoRaWAN payload.
 */
public class CoapServerDevice extends AbstractCoapServer {
	
	/** LoRaWAN client */
	LorawanClient lorawanClient;

	
	/** Creates a new client. 
	 * @param appCtx application context
	 * @param fPort fport field
	 * @param gwSoaddr socket address of a virtual LoRaWAN gateway
	 * @param dataService data service
	 * @param timeout data timeout
	 * @throws IOException
	 */
	public CoapServerDevice(AppContext appCtx, int fPort, SocketAddress gwSoaddr) throws IOException {
		super();
		lorawanClient= new LorawanClient(appCtx,fPort,new UdpMacMessageExchanger(gwSoaddr),new LorawanClientListener(){
			@Override
			public void onJoinAccept(LorawanClient client) {
				processJoinAccept(client);
			}
			@Override
			public void onReceivedData(LorawanClient client, byte[] data) {
				processReceivedData(client,data);
		}});
		lorawanClient.join();
	}
	

	private void processJoinAccept(LorawanClient client) {
		// TODO
	}

	
	private void processReceivedData(LorawanClient client, byte[] data) {
		// not implemented
	}

	
	@Override
	protected void handlePutRequest(CoapRequest req) {
		if (lorawanClient!=null && lorawanClient.isAssociated()) {
			byte[] data=req.getPayload();
			System.out.println("DEBUGGGRRRR: "+data);
			if (data!=null) System.out.println("DEBUGGGRRRR: "+Bytes.toHex(data));
			try {
				lorawanClient.sendData(data);
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
		var flags= new Flags(args);

		String appEui= flags.getString("-appeui",null,"EUI","join/application EUI");
		String appKey= flags.getString("-appkey",null,"key","application key");
		
		int devPort= flags.getInteger("-devPort",4444,"port","local port for communicating with virtual devices");		
		String devEui= flags.getString("-deveui",null,"EUI","device EUI");	
		int fPort= flags.getInteger("-fport",1,"port","value of FPort field in the LoRaWAN DATA messages (default is 1)");

		boolean verbose= flags.getBoolean("-v","verbose mode");
		boolean help= flags.getBoolean("-h","prints this message");
		
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.INFO));
			LorawanClient.VERBOSE=true;
		}

		if (help) {
			System.out.println(flags.toUsageString(CoapServerDevice.class));
			return;
		}
		
		var appCtx= new AppContext(new EUI(devEui),new EUI(appEui),appKey,Random.nextInt(65536));	
		new CoapServerDevice(appCtx,fPort,new InetSocketAddress("127.0.0.1",devPort));
	}

}
