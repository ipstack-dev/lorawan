package test;

import java.io.IOException;

import org.zoolu.net.InetAddrUtils;
import org.zoolu.util.Bytes;
import org.zoolu.util.Random;
import org.zoolu.util.Timer;
import org.zoolu.util.TimerListener;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.lorawan.device.DataDevice;
import io.ipstack.lorawan.device.service.Counter;
import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.gateway.LorawanGateway;
import io.ipstack.lorawan.gateway.LorawanGatewayConfig;
import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.mac.UdpMacMessageExchanger;
import io.ipstack.lorawan.semtech.SemtechServer;
import io.ipstack.lorawan.server.LorawanServer;
import io.ipstack.lorawan.server.LorawanServerConfiguration;
import io.ipstack.lorawan.server.LorawanServerListener;
import io.ipstack.lorawan.server.json.RxMsgInfo;
import io.ipstack.net.analyzer.LibpcapHeader;
import io.ipstack.net.analyzer.LibpcapWriter;


/** Virtual LoRaWAN device and gateway connected to local network server.
 */
public class LorawanLocalNetworkTest {
	
	static int GATEWAY_PORT= 7001; // port used for the UDP interface between the LorawanGateway (UDP sever) and the LoRaWAN virtual device (UDP client)
	static int LORAWAN_SERVER_PORT=7002; // internal port used for the UDP interface between the LorawanServer (UDP sever) and the SemtechServer (UDP client)
	
	static String networkServer= "127.0.0.1";
	static EUI gwEUI= new EUI("feffff0000008001");	
	static String gwSoaddr= "127.0.0.1:"+GATEWAY_PORT;
	static long devTime= 10;
	static int fPort= 1;
	//static AppContext appCtx= AppContext.fromFile("cfg/local-dev01.cfg");
	static AppContext appCtx= new AppContext(new EUI("feffff0000000001"),new EUI("0000000000000001"),"C9AF8714B512698B2CEE7282CA2682B8",Random.nextInt(65536));
	static long downlinkDataTime= 5000; // sends downlink data after this time (in milliseconds)
	static byte[] downlinkData= Bytes.fromHex("00001000"); // downlink data
	
	static void device() {
		try {
			DataService service= new Counter();
			var macMsgExchanger= new UdpMacMessageExchanger(InetAddrUtils.parseInetSocketAddress(gwSoaddr));
			new DataDevice(appCtx,fPort,macMsgExchanger,service,devTime*1000);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	static void gateway() {
		try {
			var cfg= new LorawanGatewayConfig(gwEUI.toString(),44.764705773216F,10.308244228363F,networkServer);
			new LorawanGateway(cfg,new UdpMacMessageExchanger(GATEWAY_PORT));				
		}
		catch (IOException e) {
			e.printStackTrace(); 
		}
	}
	
	
	static void server() {
		try { 
			var homeNetId= Bytes.fromHex("000001");
			var config= new LorawanServerConfiguration();
			config.homeNetId= homeNetId;
			config.port= LorawanServer.DEFAULT_NETWORK_SERVER_PORT;
			//var config= (LorawanServerConfig)Json.fromJSONFile(new File("../lorawan/cfg/server.cfg"),LorawanServerConfig.class);
			
			var lorawanServer= new LorawanServer(config,new LorawanServerListener() {
				@Override
				public void onJoinRequest(LorawanServer server, RxMsgInfo rxMsgInfo) {
					System.out.println(LorawanServer.class.getSimpleName()+": onJoinRequest(): devEUI: "+rxMsgInfo.devEUI);
					server.accept(rxMsgInfo);
					new Timer(downlinkDataTime,new TimerListener() {
						@Override
						public void onTimeout(Timer t) {
							try {
								server.sendData(rxMsgInfo.getDevEUI(),fPort,downlinkData);
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
				@Override
				public void onReceivedData(LorawanServer server, RxMsgInfo rxMsgInfo) {
					System.out.println(LorawanServer.class.getSimpleName()+": onReceivedData(): "+rxMsgInfo.devEUI+"/"+rxMsgInfo.fport+"/"+Bytes.toHex(rxMsgInfo.payload));
				}
				@Override
				public void onConnectedGateway(LorawanServer server, EUI gwEUI) {
					System.out.println(LorawanServer.class.getSimpleName()+": onConnectedGateway(): "+gwEUI);
					
				}
			});
			lorawanServer.addDevice(appCtx);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) throws Exception {
		
		DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.INFO));
		//UdpMacMessageExchanger.VERBOSE= true;
		//LorawanClient.VERBOSE= true;
		//SemtechClient.VERBOSE= true;
		//LorawanGateway.VERBOSE= true;
		LorawanServer.VERBOSE= true;
		SemtechServer.VERBOSE= true;
		
		SemtechServer.PCAP_WRITER= new LibpcapWriter(LibpcapHeader.LINKTYPE_IPV4,"test.pcap");
				
		new Thread(LorawanLocalNetworkTest::server).start();
		new Thread(LorawanLocalNetworkTest::gateway).start();
		device();
	}

}
