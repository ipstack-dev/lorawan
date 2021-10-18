package it.unipr.netsec.thingsstack.lorawan.semtech;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.function.Consumer;

import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.json.JsonUtils;

import it.unipr.netsec.thingsstack.lorawan.semtech.json.RxPacketInfo;

import java.io.IOException;


/** Responds to client PULL_DATA packets maintaining corresponding associations.
 * <p>
 * On demand, it sends PULL_RESP requests containing {@link TxPacketInfo} messages.
 */
public class SemtechServer {
	
	/** Verbose mode */
	public static boolean VERBOSE=true;
	
	/** Prints a message. */
	private void log(String str) {
		SystemUtils.log(LoggerLevel.INFO,toString()+": "+str);
	}

	
	static long REGISTRATION_TO=5*60*1000; // 5 min
	
	UdpProvider udpProvider;
	
	boolean running=false;
	
	HashMap<String,SocketAddress> gatewaySoAddrs=new HashMap<>();
	
	Consumer<RxPacketInfo> listener;
	

	public SemtechServer(int localPort, Consumer<RxPacketInfo> listener) throws SocketException, UnknownHostException {
		this.listener=listener;
		DatagramSocket udpSocket=localPort>0? new DatagramSocket(localPort) : new DatagramSocket();
		udpProvider=new UdpProvider(udpSocket,new UdpProviderListener() {
			@Override
			public void onReceivedPacket(UdpProvider udp, DatagramPacket packet) {
				processReceivedPacket(packet);
			}
			@Override
			public void onServiceTerminated(UdpProvider udp, Exception error) {
				processServiceTerminated(error);
			}		
		});
	}
	

	/** Sends a UDP datagram. */
	protected void send(SemtechPacket pkt, String gatewayEUI) {
		if (!gatewaySoAddrs.containsKey(gatewayEUI)) {
			log("sending: gateway EUI unknown:"+gatewayEUI);
		}
		else {
			send(pkt,gatewaySoAddrs.get(gatewayEUI));
		}
	}

	
	/** Sends a UDP datagram. */
	protected void send(SemtechPacket pkt, SocketAddress destSoaddr) {
		try {
			log("sending: "+pkt);
			byte[] data=pkt.getBytes();
			DatagramPacket datagramPkt=new DatagramPacket(data,data.length);
			datagramPkt.setSocketAddress(destSoaddr);
			udpProvider.send(datagramPkt);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/** When a new UDP datagram is received. 
	 * @throws IOException */
	private void processReceivedPacket(DatagramPacket packet) {
		byte[] buf=packet.getData();
		int off=packet.getOffset();
		int len=packet.getLength();
		processReceivedPacket(SemtechPacket.parseSemtechPacket(buf,off,len),packet.getSocketAddress());
	}
	
	
	/** Processes an incoming Semtech packet.
	 *  @param pkt the packet
	 * @throws IOException */
	protected void processReceivedPacket(SemtechPacket pkt, SocketAddress remoteSoAddr) {
		log("received: "+pkt);
		if (pkt.getType()==SemtechPacket.PULL_DATA) {
			PullDataPacket pullDataPkt=(PullDataPacket)pkt;
			String gatewayEui=Bytes.toHex(pullDataPkt.getGateway());
			if (!gatewaySoAddrs.containsKey(gatewayEui)) {
				gatewaySoAddrs.put(gatewayEui,remoteSoAddr);
				log("registered new gateway: "+gatewayEui);
			}
			send(new PullAckPacket(pullDataPkt.getToken()),remoteSoAddr);	
		}
		else
		if (pkt.getType()==SemtechPacket.PUSH_DATA) {
			PushDataPacket pushDataPkt=(PushDataPacket)pkt;
			send(new PushAckPacket(pushDataPkt.getToken()),remoteSoAddr);
			if (listener!=null) {
				listener.accept(JsonUtils.fromJson(pushDataPkt.getJsonObject(),RxPacketInfo.class));
			}
		}
	}

	
	/** When the UDP provider terminates. */
	private void processServiceTerminated(Exception error) {
		log("processServiceTerminated(): "+error);
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+udpProvider.getSocket().getLocalPort()+']';
	}

	
	/** Runs a client. */
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		int local_port=flags.getInteger("-p",-1,"port","local port");
		boolean help=flags.getBoolean("-h","prints this message");
		
		if (help) {
			System.out.println(flags.toUsageString(SemtechServer.class));
			return;
		}
		
		if (VERBOSE) SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.INFO));
		
		SemtechServer server=new SemtechServer(local_port,null);
	}

}
