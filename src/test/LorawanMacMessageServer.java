package test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;
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


/** LoRaWAN server (Join Server and Application Server).
 */
public class LorawanMacMessageServer {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;
	
	/** Prints a message. */
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	private static long JOINING_TIMEOUT= 30*1000;

	private byte[] homeNetId; // b000 XXXXXXXXXXXXXX 0000001 (or 0)
	private AppContext appCtx;
	private SessionContext sessCtx;
	private byte[] devAddr= Bytes.fromHex("00000001");
	private byte[] joinNonce= Bytes.fromHex("000001"); // appNonce
	private boolean joined= false;
	
	private LorawanMacMessageExchanger phLayer;
	private LorawanMacMessageServerListener listener;
	
	
	public LorawanMacMessageServer(byte[] homeNetId, AppContext appCtx, LorawanMacMessageExchanger phLayer, LorawanMacMessageServerListener listener) throws IOException {
		this.homeNetId= homeNetId;
		this.appCtx= appCtx;
		this.phLayer= phLayer;
		this.listener= listener;
		phLayer.receive(this::processReceivedMacMessage);

	}
	
	
	/** Sends a data payload.
	 * @param data the data to send
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void sendData(int fPort, byte[] data) throws IOException, GeneralSecurityException {
		if (!joined) throw new IOException("Tried to send data while the device didn't joined yet");
		phLayer.send(getDataMacMessage(sessCtx,fPort,data));
	}

	
	/** When a new MAC message is received. 
	 * @throws IOException */
	private void processReceivedMacMessage(LorawanMacMessage macMsg) {
		if (VERBOSE) log("processReceivedDatagramPacket(): received LoRaWAN message: "+macMsg);
		var type= macMsg.getMType();
		
		if (type==LorawanMacMessage.TYPE_JOIN_REQUEST) {
			var joinReqMacMsg= (LorawanJoinRequestMacMessage)macMsg;
			if (VERBOSE) log("processReceivedDatagramPacket(): join request: "+joinReqMacMsg);
			if (listener!=null) listener.onJoinRequest(this,joinReqMacMsg);
		}
		else
		if (type==LorawanMacMessage.TYPE_UNCORFIRMED_DATA_UP) {
			var dataMessagePayload= new LorawanDataMacPayload(macMsg.getMacPayload(),true);
			if (VERBOSE) log("processReceivedDatagramPacket(): encrypted payload: "+dataMessagePayload.toString());
			//byte[] framePayload=dataMessagePayload.getFramePayload();
			if (dataMessagePayload.getFramePayload()!=null) {
				try {
					dataMessagePayload.decryptFramePayload(sessCtx.appSKey());
					var framePayload= dataMessagePayload.getFramePayload();
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
	
	public void accept(LorawanJoinRequestMacMessage joinReqMacMsg) {
		try {
			//joinReqMsg.getDevEui();
			//joinReqMsg.getJoinEui();
			var devNonce= joinReqMacMsg.getDevNonce();
			sessCtx= new SessionContext(devAddr,appCtx.appKey(),joinNonce,homeNetId,devNonce);
			if (VERBOSE) log("processReceivedDatagramPacket(): new session context: "+sessCtx.toString());
			var joinAcceptMacMsg= new LorawanJoinAcceptMacMessage(joinNonce,homeNetId,devAddr,0,0,null,appCtx.appKey());
			if (VERBOSE) log("processReceivedDatagramPacket(): sending Join Accept message: "+joinAcceptMacMsg);
			phLayer.send(joinAcceptMacMsg);
			joined= true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private LorawanMacMessage getDataMacMessage(SessionContext sessCtx, int fPort, byte[] data) throws GeneralSecurityException {
		if (VERBOSE) log("data: "+(data!=null?Bytes.toHex(data):null));		
		return new LorawanDataMacMessage(LorawanMacMessage.TYPE_UNCORFIRMED_DATA_DOWN,sessCtx.devAddr(),sessCtx.incFCntUp(),fPort,data,sessCtx.appSKey(),sessCtx.fNwkSIntKey());
	}

}
