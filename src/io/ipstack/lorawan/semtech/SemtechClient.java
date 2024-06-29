package io.ipstack.lorawan.semtech;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Consumer;

import org.zoolu.net.InetAddrUtils;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.util.Clock;
import org.zoolu.util.Random;
import org.zoolu.util.Timer;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.semtech.json.RxPacketInfo;
import io.ipstack.lorawan.semtech.json.RxPacketMessage;
import io.ipstack.lorawan.semtech.json.StatusInfo;
import io.ipstack.lorawan.semtech.json.StatusMessage;
import io.ipstack.lorawan.semtech.json.TxPacketMessage;

import java.io.IOException;


/** Establishes an association with a server by sending PULL_DATA packets.
 * <p>
 * It also periodically sends PUSH_DATA packets containing gateway statistics.
 * <p>
 * On demand, it sends PUSH_DATA requests containing {@link RxPacketInfo} messages.
 */
public class SemtechClient {
	
	/** Verbose mode */
	public static boolean VERBOSE=false;
	
	/** Prints a message. */
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,toString()+": "+str);
	}

	
	private static int DEFAULT_SERVER_PORT= 1700; 
	private static long PULLDATA_TIMEOUT= 20000;
	private static long STATUS_TIMEOUT= 60000;
	
	private EUI id;
	private float lati;
	private float longi;
	private UdpProvider udpProvider;
	private InetAddress remoteIAddr; // remote server IP address
	private int remotePort; // remote server port
	private int pushDataToken; // last PUSH_DATA token
	private int pullDataToken; // last PULL_DATA token
	private boolean running= false;
	private Consumer<TxPacketMessage> listener;
	
	private long txCount= 0;
	private long rxCount= 0;
	
	

	public SemtechClient(EUI id, float lati, float longi, int localPort, String remoteServer) throws IOException {
		this(id,lati,longi,localPort,remoteServer,null);
	}

	
	public SemtechClient(EUI id, float lati, float longi, int localPort, String remoteServer, Consumer<TxPacketMessage> listener) throws IOException {
		this.id= id;
		this.lati= lati;
		this.longi= longi;
		this.listener= listener;
		this.remoteIAddr= InetAddrUtils.parseSocketInetAddress(remoteServer);
		this.remotePort= InetAddrUtils.parseSocketPort(remoteServer,DEFAULT_SERVER_PORT);
		var udpSocket=localPort>0? new DatagramSocket(localPort) : new DatagramSocket();
		udpProvider= new UdpProvider(udpSocket,new UdpProviderListener() {
			@Override
			public void onReceivedPacket(UdpProvider udp, DatagramPacket packet) {
				processReceivedPacket(packet);
			}
			@Override
			public void onServiceTerminated(UdpProvider udp, Exception error) {
				processServiceTerminated(error);
			}		
		});
		running= true;
		// start sending PUSH_DATA STATUS
		processStatusTimeout(null);
		// start sending PULL_DATA
		processPullDataTimeout(null);
	}
	
	
	/*public String getRemoteServer() {
		return new InetSocketAddress(remoteIAddr,remotePort).toString();
	}*/
	
	
	/**
	 * @param lati latitude
	 * @param longitude
	 */
	public void setPosition(float lati, float longi) {
		this.lati= lati;
		this.longi= longi;
	}

	
	public int getPort() {
		return udpProvider.getSocket().getLocalPort();
	}

	
	public long getTxCount() {
		return txCount;
	}

	
	public long getRxCount() {
		return rxCount;
	}

	
	private void processPullDataTimeout(Timer t) {
		if (running) {
			pullDataToken= Random.nextInt(0x10000);
			send(new PullDataPacket(pullDataToken,id));
			Clock.getDefaultClock().newTimer(PULLDATA_TIMEOUT,this::processPullDataTimeout).start();			
		}
	}


	private void processStatusTimeout(Timer t) {
		if (running) {
			pushDataToken=Random.nextInt(0x10000);
			send(new PushDataPacket(pushDataToken,id,new StatusMessage(new StatusInfo(lati,longi)).toString()));
			Clock.getDefaultClock().newTimer(STATUS_TIMEOUT,this::processStatusTimeout).start();			
		}
	}


	/** Sets client listener.
	 * @param listener the listener to set */
	public void setListener(Consumer<TxPacketMessage> listener) {
		this.listener= listener;
	}


	/** Sends an RX packet. */
	public void send(RxPacketInfo rxpk) {
		send(new RxPacketMessage(rxpk));
	}

	
	/** Sends an RX packet. */
	public void send(RxPacketInfo[] rxpk) {
		send(new RxPacketMessage(rxpk));
	}

	
	/** Sends an RX packet. */
	public void send(RxPacketMessage msg) {
		pushDataToken= Random.nextInt(0x10000);
		send(new PushDataPacket(pushDataToken,id,msg.toString()));
	}
	
	
	/**
	 * Stops the client.
	 */
	public void halt() {
		if (udpProvider!=null && udpProvider.isRunning()) udpProvider.halt();
		running= false;
	}

	
	/** Sends a Semtech packet to the remote server. */
	protected void send(SemtechPacket pkt) {
		try {
			if (VERBOSE) log("sending: "+pkt);
			var data= pkt.getBytes();
			var datagramPacket= new DatagramPacket(data,data.length);
			datagramPacket.setAddress(remoteIAddr);
			datagramPacket.setPort(remotePort);
			udpProvider.send(datagramPacket);
			txCount++;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/** When a new UDP datagram is received. 
	 * @throws IOException */
	private void processReceivedPacket(DatagramPacket packet) {
		rxCount++;
		var buf= packet.getData();
		int off= packet.getOffset();
		int len= packet.getLength();
		processReceivedPacket(SemtechPacket.parseSemtechPacket(buf,off,len));
	}
	
	
	/** Processes an incoming Semtech packet.
	 *  @param pkt the packet
	 * @throws IOException */
	protected void processReceivedPacket(SemtechPacket pkt) {
		if (VERBOSE) log("received: "+pkt);
		if (pkt.getType()==SemtechPacket.PULL_RESP/*&& pkt.getToken()==pushDataToken*/) {
			var pullRespPkt= (PullRespPacket)pkt;
			send(new TxAckPacket(pullRespPkt.getToken(),id,null));
			TxPacketMessage txPktMsg= Json.fromJSON(pullRespPkt.getJsonObject(),TxPacketMessage.class);
			// PATCH
			/*String jsonBody= pullRespPkt.getJsonObject();
			int index= jsonBody.indexOf("\"data\":");
			if (index>0) {
				String data64= jsonBody.substring(index+7).split("\"")[1];
				txPktInfo= new TxPacketInfo(Base64.decode(data64));
			}*/
			if (listener!=null) listener.accept(txPktMsg);
		}
	}

	
	/** When the UDP provider terminates. */
	private void processServiceTerminated(Exception error) {
		if (VERBOSE) log("processServiceTerminated(): "+error);
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName()+'['+udpProvider.getSocket().getLocalPort()+']';
	}

}
