package test;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.zoolu.net.InetAddrUtils;
import org.zoolu.util.Flags;
import org.zoolu.util.Random;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.lorawan.client.LorawanClient;
import io.ipstack.lorawan.device.DataDevice;
import io.ipstack.lorawan.device.service.Counter;
import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.gateway.LorawanGateway;
import io.ipstack.lorawan.gateway.LorawanGatewayConfig;
import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.mac.UdpMacMessageExchanger;
import io.ipstack.lorawan.semtech.SemtechClient;


/** Virtual LoRaWAN device and gateway connected to a remote network server (e.g. TTN).
 */
public class LorawanRemoteNetworkTest {
	
	static String networkServer;
	static ArrayList<EUI> gwEUIs= new ArrayList<>();
	static int fPort;
	static long devTime;
	static AppContext appCtx;
	
	
	static void device(Collection<Integer> gwPorts) {
		try {
			DataService service= new Counter();
			SocketAddress[] gwSoaddrs= new SocketAddress[gwPorts.size()];
			int i=0;
			for (var gwPort: gwPorts) {
				gwSoaddrs[i++]= InetAddrUtils.parseInetSocketAddress("127.0.0.1:"+gwPort.intValue());
			}
			var macMsgExchanger= new UdpMacMessageExchanger(gwSoaddrs);
			new DataDevice(appCtx,fPort,macMsgExchanger,service,devTime*1000);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	static int gateway(EUI gwEUI, int port) {
		try {
			var cfg= new LorawanGatewayConfig(gwEUI.hex(),44.764705773216F,10.308244228363F,networkServer);
			var macLayer= new UdpMacMessageExchanger(port);
			new LorawanGateway(cfg,macLayer);	
			return macLayer.getPort();
		}
		catch (IOException e) {
			e.printStackTrace(); 
		}
		return -1;
	}
	
	public static void main(String[] args) throws Exception {
		var flags= new Flags(args);
		fPort= flags.getInteger("-fp",1,"fPort","fPort value (default is 1)");
		var gwPort= flags.getInteger("-p",-1,"port","first gateway port (default they are dynamically allocated)");
		networkServer= flags.getString("-s",null,"nsAddr","address of the Network Server (e.g. 'eu1.cloud.thethings.network')");
		String gwEUI= flags.getString("-g",null,"gwEUI","Gateway EUI");
		String devEUI= flags.getString("-d",null,"devEUI","DeviceEUI");
		String joinEUI= flags.getString("-j",null,"joinEUI","JoinEUI");
		String appKey= flags.getString("-k",null,"appKey","AppKey in hexadecimal");
		devTime= flags.getInteger("-t",20,"time","device sending interval, in seconds");
		boolean help= flags.getBoolean("-h","prints this message");
		
		while (gwEUI!=null) {
			gwEUIs.add(new EUI(gwEUI));
			gwEUI= flags.getString("-g",null,null,null);
		}
		
		/*networkServer= "127.0.0.1";
		gwEUI= "feffff2002118001";
		devEUI= "feffff2002110001";
		joinEUI= "0000000000000001";
		appKey= "C9AF8714B512698B2CEE7282CA2682B8";
		fPort= 1;*/
		
		if (help || networkServer==null || gwEUIs.size()==0 || devEUI==null || joinEUI==null || appKey==null || fPort<0) {
			System.out.println(flags.toUsageString(LorawanRemoteNetworkTest.class));
			return;
		}
		
		appCtx= new AppContext(new EUI(devEUI),new EUI(joinEUI),appKey,Random.nextInt(65536));
		
		DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.INFO));
		LorawanClient.VERBOSE= true;
		SemtechClient.VERBOSE= true;
		LorawanGateway.VERBOSE= true;
		
		var gwPorts= new ArrayList<Integer>();
		for (EUI eui: gwEUIs) {
			if (gwPort>0) {
				// use pre-configured ports
				final var port= gwPort++;
				new Thread(()->{ gateway(eui,port); }).start();
				gwPorts.add(port);
			}
			else {
				// use dynamically allocated ports
				var port= new AtomicInteger();
				new Thread(()->{ port.set(gateway(eui,-1)); }).start();
				while(port.get()==0) SystemUtils.sleep(1000);
				if (port.get()>0) gwPorts.add(port.get());				
			}
		}
		device(gwPorts);
	}

}
