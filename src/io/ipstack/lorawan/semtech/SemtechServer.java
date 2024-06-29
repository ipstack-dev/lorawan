package io.ipstack.lorawan.semtech;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.util.Random;
import org.zoolu.util.json.JsonParser;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.semtech.json.RxPacketInfo;
import io.ipstack.lorawan.semtech.json.RxPacketMessage;
import io.ipstack.lorawan.semtech.json.StatusInfo;
import io.ipstack.lorawan.semtech.json.StatusMessage;
import io.ipstack.lorawan.semtech.json.TxPacketInfo;
import io.ipstack.lorawan.semtech.json.TxPacketMessage;
import io.ipstack.net.analyzer.LibpcapWriter;
import io.ipstack.net.udp.UdpPacket;
import io.ipstack.net.util.IpAddressUtils;

import java.io.IOException;


/** Responds to client PULL_DATA packets maintaining corresponding associations.
 * <p>
 * On demand, it sends PULL_RESP requests containing {@link TxPacketInfo} messages.
 */
public class SemtechServer {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;
	
	/** Prints a message. */
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,toString()+": "+str);
	}

	
	/** If not null, the pcap file where all traffic is recorded */
	public static LibpcapWriter PCAP_WRITER= null;
	
	
	private UdpProvider udpProvider;
	
	private HashMap<EUI, SocketAddress> gwAddrs= new HashMap<>(); // EUI -> socket address
	
	private SemtechServerListener listener;
	
	private long txCount= 0;
	private long rxCount= 0;

	

	public SemtechServer(int serverPort, SemtechServerListener listener) throws SocketException, UnknownHostException {
		this.listener= listener;
		var udpSocket= new DatagramSocket(serverPort);
		udpProvider=new UdpProvider(udpSocket,new UdpProviderListener() {
			@Override
			public void onReceivedPacket(UdpProvider udp, DatagramPacket packet) {
				if (PCAP_WRITER!=null) pcapWriteReceivedPacket(packet);
				processReceivedPacket(packet);
			}
			@Override
			public void onServiceTerminated(UdpProvider udp, Exception error) {
				processServiceTerminated(error);
			}		
		});
	}
	

	/** Sends an TX packet to a given gateway.
	 * @param gwEUI gateway EUI
	 * @param txpk txpk JSON object
	 */
	public void send(EUI gwEUI, TxPacketInfo txpk) {
		var gwAddr= gwAddrs.get(gwEUI);
		if (gwAddr!=null) send(gwAddr, new PullRespPacket(Random.nextInt(0x10000), new TxPacketMessage(txpk).toString()));
		else if (VERBOSE) log("cannot send to unknown gateway "+gwAddr);
	}

	
	/** Sends a Semtech packet to a given socket address. */
	protected void send(SocketAddress destSoaddr, SemtechPacket semPkt) {
		try {
			if (VERBOSE) log("sending to "+destSoaddr+": "+semPkt);
			var data= semPkt.getBytes();
			var udpPkt= new DatagramPacket(data,data.length);
			udpPkt.setSocketAddress(destSoaddr);
			if (PCAP_WRITER!=null) pcapWriteSentPacket(udpPkt);
			udpProvider.send(udpPkt);
			txCount++;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public long getTxCount() {
		return txCount;
	}

	
	public long getRxCount() {
		return rxCount;
	}
	
	
	/** When a new UDP datagram is received. 
	 * @throws IOException */
	private void processReceivedPacket(DatagramPacket udpPkt) {
		rxCount++;
		var buf= udpPkt.getData();
		int off= udpPkt.getOffset();
		int len= udpPkt.getLength();
		processReceivedPacket(udpPkt.getSocketAddress(),SemtechPacket.parseSemtechPacket(buf,off,len));
	}
	
	
	/** Processes an incoming Semtech packet.
	 *  @param pkt the packet
	 * @throws IOException */
	protected void processReceivedPacket(SocketAddress remoteSoAddr, SemtechPacket pkt) {
		if (VERBOSE) log("received from "+remoteSoAddr+": "+pkt);
		if (pkt.getType()==SemtechPacket.PULL_DATA) {
			var pullDataPkt=(PullDataPacket)pkt;
			send(remoteSoAddr,new PullAckPacket(pullDataPkt.getToken()));
			var gwEUI= pullDataPkt.getGateway();
			gwAddrs.put(gwEUI,remoteSoAddr);
		}
		else
		if (pkt.getType()==SemtechPacket.PUSH_DATA) {
			PushDataPacket pushDataPkt=(PushDataPacket)pkt;
			send(remoteSoAddr,new PushAckPacket(pushDataPkt.getToken()));
			if (listener!=null) {
				var gwEUI= pushDataPkt.getGateway();
				var jsonstr= pushDataPkt.getJsonObject();
				var par= new JsonParser(jsonstr);
				try {
					var jobj= par.parseObject();
					var type= jobj.getMembers().get(0).getName();
					if (type.equals("rxpk")) {
						RxPacketInfo[] rxInfoArray= Json.fromJSON(pushDataPkt.getJsonObject(),RxPacketMessage.class).getRxpk();
						for (RxPacketInfo rxPktInfo : rxInfoArray) listener.onRxPacket(gwEUI,rxPktInfo);
					}
					else
					if (type.equals("stat")) {
						StatusInfo status= Json.fromJSON(pushDataPkt.getJsonObject(),StatusMessage.class).getStatus();
						if (VERBOSE) log("processReceivedPacket(): stat message from "+gwEUI);
						listener.onStatus(gwEUI,status);
					}
					else if (VERBOSE) log("processReceivedPacket(): message '"+type+"' is not processed");
				}
				catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}
	}

	
	/** When the UDP provider terminates. */
	private void processServiceTerminated(Exception error) {
		log("processServiceTerminated(): "+error);
	}

	
	private void pcapWriteReceivedPacket(DatagramPacket pkt) {
		UdpPacket udpPkt= new UdpPacket(IpAddressUtils.toIpAddress(pkt.getAddress()),pkt.getPort(),IpAddressUtils.toIpAddress(udpProvider.getSocket().getLocalAddress()),udpProvider.getSocket().getLocalPort(),pkt.getData(),pkt.getOffset(),pkt.getLength());
		PCAP_WRITER.write(udpPkt.toIp4Packet());
	}

	
	private void pcapWriteSentPacket(DatagramPacket pkt) {
		UdpPacket udpPkt= new UdpPacket(IpAddressUtils.toIpAddress(udpProvider.getSocket().getLocalAddress()),udpProvider.getSocket().getLocalPort(),IpAddressUtils.toIpAddress(pkt.getAddress()),pkt.getPort(),pkt.getData(),pkt.getOffset(),pkt.getLength());
		PCAP_WRITER.write(udpPkt.toIp4Packet());
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+udpProvider.getSocket().getLocalPort()+']';
	}

}
