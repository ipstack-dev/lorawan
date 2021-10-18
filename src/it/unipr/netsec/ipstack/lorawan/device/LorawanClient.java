package it.unipr.netsec.ipstack.lorawan.device;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.util.Bytes;
import org.zoolu.util.Clock;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.Random;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.Timer;

import it.unipr.netsec.ipstack.lorawan.ApplicationContext;
import it.unipr.netsec.ipstack.lorawan.LorawanDataMessage;
import it.unipr.netsec.ipstack.lorawan.LorawanJoinAcceptMessage;
import it.unipr.netsec.ipstack.lorawan.LorawanJoinRequestMessage;
import it.unipr.netsec.ipstack.lorawan.LorawanMacMessage;
import it.unipr.netsec.ipstack.lorawan.SessionContext;
import it.unipr.netsec.ipstack.lorawan.device.service.Service;


/** LoRaWAN client.
 */
public class LorawanClient {
	
	/** Verbose mode */
	public static boolean VERBOSE=false;
	
	/** Prints a message. */
	private void log(String str) {
		SystemUtils.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	static long JOINING_TIMEOUT=30*1000;

	String appCtxFile;
	
	ApplicationContext appCtx;
	
	byte[] joinEUI;

	byte[] appKey;

	byte[] devEUI;
	
	byte[] devNonce;

	SessionContext sessCtx;
	
	int fPort;
	
	boolean joined=false;
	
	UdpProvider udpProvider;
	SocketAddress gwSoaddr;
	
	LorawanClientListener listener;
	
	
	/** Creates a new client. 
	 * @param devEUI the device EUI
	 * @param appCtxFile application context file
	 * @param joinEUI join EUI
	 * @param appKey AppKey
	 * @param fPort fport field
	 * @param gwSoaddr socket address of LoRaWAN gateway
	 * @param event listener
	 * @throws IOException
	 */
	public LorawanClient(byte[] devEUI, String appCtxFile, byte[] joinEUI, byte[] appKey, int fPort, SocketAddress gwSoaddr, LorawanClientListener listener) throws IOException {
		this.listener=listener;
		this.appCtxFile=appCtxFile;
		this.appCtx=appCtxFile!=null? ApplicationContext.fromFile(appCtxFile) : null;
		this.joinEUI=joinEUI!=null? joinEUI : appCtx.getJoinEUI();
		this.appKey=appKey!=null? appKey : appCtx.getAppKey();
		this.devEUI=devEUI!=null? devEUI : appCtx.getDevEUI();
		this.devNonce=appCtx!=null? appCtx.getDevNonce() : Random.nextBytes(2);
		this.fPort=fPort;
		this.gwSoaddr=gwSoaddr;
		
		udpProvider=new UdpProvider(new DatagramSocket(),new UdpProviderListener() {
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
	
	
	/** Whether it is associated.
	 * @return true if join request has been accepted
	 */
	public boolean isAssociated() {
		return joined;
	}
	
	
	/** Starts a join procedure.
	 */
	public void join() {
		try {
			LorawanJoinRequestMessage joinReq=getJoinMessage();
			if (VERBOSE) log("join(): sending Join request message: "+joinReq);
			sendMacMessage(joinReq);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Clock.getDefaultClock().newTimer(JOINING_TIMEOUT,this::processjoiningTimeout).start();
	}
	
	
	private void processjoiningTimeout(Timer t) {
		if (!joined) join();
	}
	
	
	/** Sends a data payload.
	 * @param data the data to send
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void sendData(byte[] data) throws IOException, GeneralSecurityException {
		sendMacMessage(getDataMessage(data));
	}

	
	/** Sends a LoRaWAN MAC packet.
	 * @param macMsg the LoRaWAN MAC packet
	 * @throws IOException
	 */
	private void sendMacMessage(LorawanMacMessage macMsg) throws IOException {
		if (VERBOSE) log("sendPacket(): sending MAC message: "+macMsg);
		byte[] data=macMsg.getBytes();
		DatagramPacket datagramPacket=new DatagramPacket(data,data.length,gwSoaddr);
		udpProvider.send(datagramPacket);
	}
	
	
	/** When a new UDP datagram is received. 
	 * @throws IOException */
	private void processReceivedDatagramPacket(DatagramPacket packet) {
		byte[] data=new byte[packet.getLength()];
		System.arraycopy(packet.getData(),packet.getOffset(),data,0,data.length);
		LorawanMacMessage macMsg=LorawanMacMessage.parseMessage(data);
		if (VERBOSE) log("processReceivedDatagramPacket(): received LoraWAN message: "+macMsg);
		int type=macMsg.getMType();
		if (type==LorawanMacMessage.TYPE_JOIN_ACCEPT) {
			joined=true;
			if (VERBOSE) log("processReceivedDatagramPacket(): associated");
			LorawanJoinAcceptMessage joinAcceptMsg=(LorawanJoinAcceptMessage)macMsg;
			try {
				//joinAcceptMsg.decrypt(appCtx.getNwkKey());
				//sessCtx=new SessionContext(joinAcceptMsg.getDevAddr(),appCtx.getNwkKey(),joinAcceptMsg.getJoinNonce(),joinAcceptMsg.getHomeNetID(),appCtx.getDevNonce());
				joinAcceptMsg.decrypt(appKey);
				sessCtx=new SessionContext(joinAcceptMsg.getDevAddr(),appKey,joinAcceptMsg.getJoinNonce(),joinAcceptMsg.getHomeNetID(),devNonce);
				if (VERBOSE) log("processReceivedDatagramPacket(): new session context: "+sessCtx.toString());
				if (listener!=null) listener.onJoinAccept(this);
			}
			catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
		// if incoming data
		// TODO
	}

	/** When the UDP provider terminates. */
	private void processDatagramServiceTerminated(Exception error) {
		if (VERBOSE) log("processDatagramServiceTerminated(): "+error);
	}

	
	private LorawanJoinRequestMessage getJoinMessage() throws GeneralSecurityException, IOException {
		if (appCtx!=null) {
			appCtx.incDevNonce();
			appCtx.toFile(appCtxFile);
			devNonce=appCtx.getDevNonce();
		}
		else Bytes.inc(devNonce);
		//return new LorawanJoinRequestMessage(joinEUI,appCtx.getDevEUI(),appCtx.getDevNonce(),appCtx.getNwkKey());
		return new LorawanJoinRequestMessage(joinEUI,devEUI,devNonce,appKey);
	}

	
	private LorawanMacMessage getDataMessage(byte[] data) throws GeneralSecurityException {
		if (VERBOSE) log("data: "+(data!=null?Bytes.toHex(data):null));		
		return new LorawanDataMessage(LorawanMacMessage.TYPE_UNCORFIRMED_DATA_UP,sessCtx.getDevAddr(),sessCtx.incFCntUp(),fPort,data,sessCtx.getAppSKey(),sessCtx.getFNwkSIntKey());
	}

}
