package io.ipstack.lorawan.mac;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.util.Bytes;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;


/** LoRaWAN MAC message exchanger based on UDP.
  * MAC messages are simply encapsulated within UDP datagrams.
  * <p>
  * Two different communication models are supported:
  * <ul>
  * <li>If the message exchanger is created with one or more remote (static) endpoint addresses,
  * all outgoing messages are sent to that addresses; </li>
  * <li>If the message exchanger is created without a remote endpoint address,
  * remote addresses of incoming messages are dynamically collected and used as destination when a message is sent.</li>
  * </ul>
  * In the latter case, a remote address is considered active and used as possible destination
  * for a maximum of {@link ALIVE_TIME} seconds from the last received message from that endpoint.
  */ 
public class UdpMacMessageExchanger implements LorawanMacMessageExchanger {

	/** Verbose mode */
	public static boolean VERBOSE= false;
	
	/** Prints a message. */
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	/** Time that an endpoint is considered active without any message exchange */
	public static long ALIVE_TIME= 3600000; // one hour


	UdpProvider udpProvider;
	Consumer<LorawanMacMessage> receiver= null;

	/** Static addresses of remote endpoints */
	SocketAddress[] remoteSoaddrs= null;

	/** Time table, containing the socket addresses of active endpoints and last time (in milliseconds) */
	HashMap<SocketAddress,Long> activeTimes= null;

	
	/** Creates a new MAC message exchanger.
	 * @throws SocketException
	 */
	public UdpMacMessageExchanger() throws SocketException {
		this(-1,(SocketAddress[])null);
	}

	/** Creates a new MAC message exchanger.
	 * @param remoteSoaddr socket address of the remote UDP endpoint
	 * @throws SocketException
	 */
	public UdpMacMessageExchanger(SocketAddress remoteSoaddr) throws SocketException {
		this(-1,new SocketAddress[]{remoteSoaddr});
	}

	/** Creates a new MAC message exchanger.
	 * @param remoteSoaddrs socket addresses of the remote UDP endpoints
	 * @throws SocketException
	 */
	public UdpMacMessageExchanger(SocketAddress[] remoteSoaddrs) throws SocketException {
		this(-1,remoteSoaddrs);
	}

	/** Creates a new MAC message exchanger.
	 * @param port local UDP port. if 0 or less, port is dynamically assigned
	 * @throws SocketException
	 */
	public UdpMacMessageExchanger(int port) throws SocketException {
		this(port,(SocketAddress[])null);
	}

	/** Creates a new MAC message exchanger.
	 * @param port local UDP port. if 0 or less, port is dynamically assigned
	 * @param remoteSoaddr socket address of a given UDP endpoint
	 * @throws SocketException
	 */
	public UdpMacMessageExchanger(int port, SocketAddress remoteSoaddr) throws SocketException {
		this(port,new SocketAddress[]{remoteSoaddr});
	}

		/** Creates a new MAC message exchanger.
	 * @param port local UDP port. if 0 or less, port is dynamically assigned
	 * @param remoteSoaddrs socket addresses of a the remote UDP endpoints
	 * @throws SocketException
	 */
	public UdpMacMessageExchanger(int port, SocketAddress[] remoteSoaddrs) throws SocketException {
		if (remoteSoaddrs!=null) this.remoteSoaddrs= Arrays.copyOf(remoteSoaddrs,remoteSoaddrs.length);
		else activeTimes= new HashMap<>();
		udpProvider= new UdpProvider(port>0?new DatagramSocket(port):new DatagramSocket(),new UdpProviderListener() {
			@Override
			public void onReceivedPacket(UdpProvider udp, DatagramPacket packet) {
				processReceivedPacket(packet);
			}
			@Override
			public void onServiceTerminated(UdpProvider udp, Exception e) {
				e.printStackTrace();
			}});
	}
	
	/**
	 * @return UDP local port
	 */
	public int getPort() {
		return udpProvider.getSocket().getLocalPort();
	}

	private void processReceivedPacket(DatagramPacket packet) {
		var soaddr= packet.getSocketAddress();
		var data= new byte[packet.getLength()];
		System.arraycopy(packet.getData(),packet.getOffset(),data,0,data.length);
		if (VERBOSE) log("processReceivedPacket(): from "+soaddr+": "+Bytes.toHex(data));
		if (activeTimes!=null) {
			activeTimes.remove(soaddr);
			activeTimes.put(soaddr,Long.valueOf(System.currentTimeMillis()));			
		}
		var msg= LorawanMacMessage.parseMessage(data);
		if (receiver!=null) receiver.accept(msg);
	}
	
	@Override
	public void receive(Consumer<LorawanMacMessage> receiver) {
		this.receiver= receiver;
	}

	@Override
	public void send(LorawanMacMessage msg) throws IOException {
		if (VERBOSE) log("send(): msg: "+msg);
		var data= msg.getBytes();
		long now= System.currentTimeMillis();
		if (remoteSoaddrs!=null) for (SocketAddress soaddr: remoteSoaddrs) sendTo(data,soaddr);
		else {
			for (var it= activeTimes.keySet().iterator(); it.hasNext(); ) {
				var soaddr= it.next();
				//if (VERBOSE) log("send(): DEBUG: peer: "+soaddr);
				if ((now - activeTimes.get(soaddr)) > ALIVE_TIME) {
					//if (VERBOSE) log("send(): DEBUG: peer expired");
					activeTimes.remove(soaddr);
				}
				else {
					//if (VERBOSE) log("send(): DEBUG: sending to "+soaddr);
					sendTo(data,soaddr);		
				}
			}			
		}
	}

	private void sendTo(byte[] data, SocketAddress soaddr) throws IOException {
		if (VERBOSE) log("send(): to "+soaddr+": "+Bytes.toHex(data));
		DatagramPacket datagramPacket=new DatagramPacket(data,data.length,soaddr);
		udpProvider.send(datagramPacket);
	}

}
