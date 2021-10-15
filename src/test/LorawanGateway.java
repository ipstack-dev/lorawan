package test;


import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.util.Base64;
import org.zoolu.util.Flags;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.config.Configure;
import org.zoolu.util.json.JsonUtils;

import it.unipr.netsec.ipstack.lorawan.semtech.SemtechClient;
import it.unipr.netsec.ipstack.lorawan.semtech.json.RxPacketInfo;
import it.unipr.netsec.ipstack.lorawan.semtech.json.TxPacketMessage;


/** Virtual LoRaWAN gateway.
 */
public class LorawanGateway {
		
	/** Verbose mode */
	public static boolean VERBOSE=false;
	
	/** Prints a message. */
	private void log(String str) {
		SystemUtils.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	SemtechClient semtechClient;
	
	UdpProvider udpProvider;
	
	/** Device table, containing the device socket address and last time (in milliseconds) */
	HashMap<SocketAddress,Long> devices=new HashMap<>();

	
	/**
	 * @param strId
	 * @param lati
	 * @param longi
	 * @param deviceLocalPort
	 * @param localPort
	 * @param remoteHost
	 * @param remotePort
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public LorawanGateway(String strId, float lati, float longi, int deviceLocalPort, int localPort, String remoteServer) throws IOException {
		semtechClient=new SemtechClient(strId,lati,longi,localPort,remoteServer);
		semtechClient.setListener(this::processReceivedTxPacketMessage);
		DatagramSocket udpSocket=deviceLocalPort>0? new DatagramSocket(deviceLocalPort) : new DatagramSocket();
		udpProvider=new UdpProvider(udpSocket,new UdpProviderListener() {
			@Override
			public void onReceivedPacket(UdpProvider udp, DatagramPacket packet) {
				processReceivedDatagramPacket(packet);
			}
			@Override
			public void onServiceTerminated(UdpProvider udp, Exception error) {
				processDatagramServiceTerminated(error);
			}		
		});
	}

	
	private void processReceivedTxPacketMessage(TxPacketMessage pktMsg) {
		if (VERBOSE) log("processReceivedTxPacketMessage(): pktInfo: "+pktMsg);
		byte[] data=Base64.decode(pktMsg.getTxpk().getData());
		DatagramPacket pkt=new DatagramPacket(data,data.length);
		for (SocketAddress soaddr: devices.keySet()) {
			pkt.setSocketAddress(soaddr);
			try {
				udpProvider.send(pkt);
				if (VERBOSE) log("processReceivedTxPacketMessage(): sent to: "+soaddr.toString());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	private void processReceivedDatagramPacket(DatagramPacket packet) {
		byte[] data=new byte[packet.getLength()];
		System.arraycopy(packet.getData(),packet.getOffset(),data,0,data.length);
		/*if (LorawanMacMessage.parseMessage(data).getMType()==LorawanMacMessage.TYPE_JOIN_REQUEST) {
			LorawanJoinRequestMessage joinReq=new LorawanJoinRequestMessage(data);
			String devEui=Bytes.toHex(joinReq.getDevEui());
		}*/
		RxPacketInfo pktInfo=new RxPacketInfo(data);
		if (VERBOSE) log("processReceivedDatagramPacket(): pktInfo: "+pktInfo);
		semtechClient.send(pktInfo);
		// update device socket address
		SocketAddress soaddr=packet.getSocketAddress();
		devices.remove(soaddr);
		devices.put(soaddr,new Long(System.currentTimeMillis()));
	}


	private void processDatagramServiceTerminated(Exception error) {
		if (VERBOSE) log("processDatagramServiceTerminated(): "+error);
	}

	
	public static void main(String[] args) throws Exception {
		Flags flags=new Flags(args);

		String gwEui=flags.getString("-gweui",null,"EUI","gateway EUI (eg. XXXXXXfffeYYYYYY, where: XXXXXX=MAC[0-2] and YYYYYY=MAC[3-5] from 48bit MAC address)");
		String[] gwCoordinates=flags.getStringTuple("-gwpos",2,null,"lati longi","gateway latitude and longitude");
		float gwLatitude=gwCoordinates!=null? Float.parseFloat(gwCoordinates[0]) : 44.76492876F;
		float gwLongitude=gwCoordinates!=null? Float.parseFloat(gwCoordinates[1]) : 10.30846590F;
		int gwPort=flags.getInteger("-gwport",-1,"port","local UDP port");

		String networkServer=flags.getString("-netsrv","router.eu.thethings.network","address","address (and port, if not default 1700) of the network server (default is 'router.eu.thethings.network')");
		
		int devPort=flags.getInteger("-devport",4444,"port","local port for communicating with virtual devices");
				
		String configJsonFile=flags.getString("-j",null,"file","gateway configuration JSON file");
		String configFile=flags.getString("-f",null,"file","gateway configuration file");

		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this message");
		
		if (verbose) {
			SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.INFO));
			SemtechClient.VERBOSE=true;
			LorawanGateway.VERBOSE=true;
		}

		if (help) {
			System.out.println(flags.toUsageString(LorawanGateway.class));
			return;
		}
		
		if (configJsonFile!=null) {
			LorawanGatewayJson cfg=(LorawanGatewayJson)JsonUtils.fromJsonFile(new File(configJsonFile),LorawanGatewayJson.class);
			gwEui=cfg.gw.eui;
			gwLatitude=cfg.gw.latitude;
			gwLongitude=cfg.gw.longitude;
			gwPort=cfg.gw.port;
			networkServer=cfg.app.networkServer;
		}
		else
		if (configFile!=null) {
			LorawanGatewayConfig cfg=(LorawanGatewayConfig)Configure.fromFile(new File(configFile),LorawanGatewayConfig.class);
			gwEui=cfg.gwEui;
			gwLatitude=cfg.gwLatitude;
			gwLongitude=cfg.gwLongitude;
			gwPort=cfg.gwPort;
			networkServer=cfg.networkServer;
		}
		
		new LorawanGateway(gwEui,gwLatitude,gwLongitude,devPort,gwPort,networkServer);		
	}

}
