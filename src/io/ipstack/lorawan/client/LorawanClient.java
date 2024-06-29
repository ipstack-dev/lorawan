package io.ipstack.lorawan.client;

import java.io.IOException;

import java.net.SocketAddress;
import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;
import org.zoolu.util.Clock;
import org.zoolu.util.Timer;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.LorawanDataMacMessage;
import io.ipstack.lorawan.mac.LorawanDataMacPayload;
import io.ipstack.lorawan.mac.LorawanJoinAcceptMacMessage;
import io.ipstack.lorawan.mac.LorawanJoinRequestMacMessage;
import io.ipstack.lorawan.mac.LorawanMacMessage;
import io.ipstack.lorawan.mac.LorawanMacMessageExchanger;
import io.ipstack.lorawan.mac.SessionContext;


/** LoRaWAN client.
 */
public class LorawanClient {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;
	
	/** Prints a message. */
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	static long JOINING_TIMEOUT= 30*1000;

	AppContext appCtx;
	SessionContext sessCtx;
	int fPort;
	boolean joined= false;
	
	LorawanMacMessageExchanger macMsgExchanger;
	SocketAddress gwSoaddr;
	
	LorawanClientListener listener;
	
	
	/** Creates a new client. 
	 * @param appCtx application context
	 * @param fPort fport field
	 * @param macMsgExchanger MAC message exchanger
	 * @param listener client listener
	 * @throws IOException
	 */
	public LorawanClient(AppContext appCtx, int fPort, LorawanMacMessageExchanger macMsgExchanger, LorawanClientListener listener) throws IOException {
		this.listener= listener;
		this.appCtx= appCtx;
		this.fPort= fPort;
		this.macMsgExchanger= macMsgExchanger;
		macMsgExchanger.receive(this::processReceivedMacMessage);
		if (VERBOSE) log("LorawanClient(): appCtx: "+appCtx.toString());
		if (VERBOSE) log("LorawanClient(): DEBUG: devEUI: "+appCtx.devEUI());
	}
	
	
	/** Whether it is associated.
	 * @return true if join request has been accepted
	 */
	public boolean isAssociated() {
		return joined;
	}
	
	
	/** Starts join procedure.
	 */
	public void join() {
		joined= false;
		try {
			var joinReq= getJoinMacMessage();
			if (VERBOSE) log("join(): sending Join request message: "+joinReq);
			macMsgExchanger.send(joinReq);
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
		if (VERBOSE) log("sendData(): data: "+(data!=null?Bytes.toHex(data):null));
		if (!joined) throw new IOException("Tried to send data while the device didn't joined yet");
		macMsgExchanger.send(createDataMacMessage(data));
	}

	
	/** When a new MAC message is received. 
	 * @throws IOException */
	private void processReceivedMacMessage(LorawanMacMessage macMsg) {
		if (VERBOSE) log("processReceivedDatagramPacket(): received LoRaWAN message: "+macMsg);
		int type= macMsg.getMType();
		
		if (type==LorawanMacMessage.TYPE_JOIN_ACCEPT) {
			joined= true;
			if (VERBOSE) log("processReceivedDatagramPacket(): associated");
			var joinAcceptMacMsg=(LorawanJoinAcceptMacMessage)macMsg;
			try {
				//joinAcceptMacMsg.decrypt(appCtx.nwkKey());
				//sessCtx=new SessionContext(joinAcceptMsg.getDevAddr(),appCtx.nwkKey(),joinAcceptMsg.getJoinNonce(),joinAcceptMsg.getHomeNetID(),appCtx.devNonce());
				joinAcceptMacMsg.decrypt(appCtx.appKey());
				sessCtx= new SessionContext(joinAcceptMacMsg.getDevAddr(),appCtx.appKey(),joinAcceptMacMsg.getJoinNonce(),joinAcceptMacMsg.getHomeNetID(),appCtx.devNonce());
				if (VERBOSE) log("processReceivedDatagramPacket(): new session context: "+sessCtx.toString());
				if (listener!=null) listener.onJoinAccept(this);
			}
			catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
		else
		if (type==LorawanMacMessage.TYPE_UNCORFIRMED_DATA_DOWN) {
			var dataMacPayload= new LorawanDataMacPayload(macMsg.getMacPayload(),false);
			if (VERBOSE) log("processReceivedDatagramPacket(): encrypted payload: "+dataMacPayload.toString());
			//var framePayload=dataMessagePayload.getFramePayload();
			if (dataMacPayload.getFramePayload()!=null) {
				try {
					dataMacPayload.decryptFramePayload(sessCtx.appSKey());
					var framePayload= dataMacPayload.getFramePayload();
					if (VERBOSE) log("processReceivedDatagramPacket(): decrypted frame payload: "+Bytes.toHex(framePayload));
					if (listener!=null) listener.onReceivedData(this,framePayload);
				}
				catch (GeneralSecurityException e) {
					e.printStackTrace();
				}				
			}
		}
		// process other message types
		// TODO
	}

	private LorawanJoinRequestMacMessage getJoinMacMessage() throws GeneralSecurityException, IOException {
		appCtx.incDevNonce();
		//return new LorawanJoinRequestMessage(joinEUI,appCtx.getDevEUI(),appCtx.getDevNonce(),appCtx.getNwkKey());
		return new LorawanJoinRequestMacMessage(appCtx.joinEUI(),appCtx.devEUI(),appCtx.devNonce(),appCtx.appKey());
	}

	
	private LorawanMacMessage createDataMacMessage(byte[] data) throws GeneralSecurityException {
		//if (VERBOSE) log("getDataMacMessage(): data: "+(data!=null?Bytes.toHex(data):null));		
		return new LorawanDataMacMessage(LorawanMacMessage.TYPE_UNCORFIRMED_DATA_UP,sessCtx.devAddr(),sessCtx.incFCntUp(),fPort,data,sessCtx.appSKey(),sessCtx.fNwkSIntKey());
	}

}
