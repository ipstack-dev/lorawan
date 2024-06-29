package test;

import java.io.IOException;

import org.zoolu.net.InetAddrUtils;
import org.zoolu.util.Bytes;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.lorawan.client.LorawanClient;
import io.ipstack.lorawan.device.DataDevice;
import io.ipstack.lorawan.device.service.Counter;
import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.mac.LorawanJoinRequestMacMessage;
import io.ipstack.lorawan.mac.UdpMacMessageExchanger;


/** LoRaWAN device.
 */
public class LorawanClientServerTest {
	
	static int SERVER_PORT= 7001;

	
	public static void server() {
		try {
			var appCtx= AppContext.fromFile("cfg/local-dev01.cfg");
			var homeNetId= Bytes.fromHex("000001");
			new LorawanMacMessageServer(homeNetId,appCtx,new UdpMacMessageExchanger(SERVER_PORT),new LorawanMacMessageServerListener() {
				@Override
				public void onJoinRequest(LorawanMacMessageServer server, LorawanJoinRequestMacMessage joinReq) {
					System.out.println(LorawanClientServerTest.class.getSimpleName()+": onJoinRequest(): devEUI: "+joinReq.getDevEui());
					server.accept(joinReq);
				}
				@Override
				public void onReceivedData(LorawanMacMessageServer server, byte[] data) {
					System.out.println(LorawanClientServerTest.class.getSimpleName()+": onReceivedData(): "+Bytes.toHex(data));
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void client() {
		try {
			var devEui= new EUI("feffff0000000001");
			var appEui=new EUI("0000000000000001");
			var appKey= "C9AF8714B512698B2CEE7282CA2682B8";
			int devNonce= 1;
			//byte[] devNonce= Random.nextBytes(2);
			String gwSoaddr= "127.0.0.1:"+SERVER_PORT;
			long devTime= 10;
			int fPort= 1;
			
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.INFO));
			LorawanMacMessageServer.VERBOSE= true;
			LorawanClient.VERBOSE= true;
			UdpMacMessageExchanger.VERBOSE= true;
			DataService service= new Counter();
			var appCtx= new AppContext(devEui,appEui,appKey,devNonce);
			var macMsgExchanger= new UdpMacMessageExchanger(InetAddrUtils.parseInetSocketAddress(gwSoaddr));
			new DataDevice(appCtx,fPort,macMsgExchanger,service,devTime*1000);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		new Thread(LorawanClientServerTest::server).start();
		client();
	}

}
