package test.old;


import java.io.IOException;
import java.security.GeneralSecurityException;

import org.zoolu.util.Base64;
import org.zoolu.util.Bytes;
import org.zoolu.util.Clock;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.Random;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.Timer;

import it.unipr.netsec.thingsstack.lorawan.device.service.DataService;
import it.unipr.netsec.thingsstack.lorawan.mac.ApplicationContext;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanDataMessage;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanJoinAcceptMessage;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanJoinRequestMessage;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanMacMessage;
import it.unipr.netsec.thingsstack.lorawan.mac.SessionContext;
import it.unipr.netsec.thingsstack.lorawan.semtech.SemtechClient;
import it.unipr.netsec.thingsstack.lorawan.semtech.json.RxPacketInfo;
import it.unipr.netsec.thingsstack.lorawan.semtech.json.TxPacketMessage;


/** Device endpoint within a LoRaWAN gateway.
 * <p>
 * It uses the LoRaWAN protocol to join a LoRaWAN network server and to exchange data with an application server.
 * The LoRaWAN messages are exchanges with the server endpoits using the Semtech protocol over UDP/IP.
 */
public class DeviceClient {
	
	/** Verbose mode */
	public static boolean VERBOSE=false;
	
	/** Prints a message. */
	private void log(String str) {
		SystemUtils.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	public static long DEFAULT_DATA_TIMEOUT=20*60*1000;
	
	static long JOINING_TIMEOUT=30*1000;

	DataService device;
	
	String appCtxFile;
	
	ApplicationContext appCtx;
	
	byte[] joinEUI;

	byte[] appKey;

	byte[] devEUI;
	
	byte[] devNonce;

	SessionContext sessCtx;
	
	int fPort;
	
	SemtechClient semtechClient;
	
	boolean joined=false;
	
	long dataTimeout;

	
	public DeviceClient(DataService device, byte[] devEUI, String appCtxFile, byte[] joinEUI, byte[] appKey, int fPort, SemtechClient semtechClient) throws IOException {
		this(device,devEUI,appCtxFile,joinEUI,appKey,fPort,semtechClient,DEFAULT_DATA_TIMEOUT);
	}
	
	public DeviceClient(DataService device, byte[] devEUI, String appCtxFile, byte[] joinEUI, byte[] appKey, int fPort, SemtechClient semtechClient, long timeout) throws IOException {
		if (VERBOSE) log("device: "+device.getClass().getSimpleName());
		this.device=device;
		this.appCtxFile=appCtxFile;
		this.appCtx=appCtxFile!=null? ApplicationContext.fromFile(appCtxFile) : null;
		this.joinEUI=joinEUI!=null? joinEUI : appCtx.getJoinEUI();
		this.appKey=appKey!=null? appKey : appCtx.getAppKey();
		this.devEUI=devEUI!=null? devEUI : appCtx.getDevEUI();
		this.devNonce=appCtx!=null? appCtx.getDevNonce() : Random.nextBytes(2);
		this.fPort=fPort;
		this.semtechClient=semtechClient;
		this.dataTimeout=timeout;
		semtechClient.setListener(this::processReceivedLorawanMacMessage);
		// start joining procedure
		processjoiningTimeout(null);
	}
	
	private void processjoiningTimeout(Timer t) {
		if (!joined) {
			try {
				sendLorawanMacMessage(getJoinMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			Clock.getDefaultClock().newTimer(dataTimeout,this::processjoiningTimeout).start();			
		}
	}
	
	private void processReceivedLorawanMacMessage(TxPacketMessage pktMsg) {
		if (VERBOSE) log("received LoRaWAN message: pktInfo: "+pktMsg);
		LorawanMacMessage macMsg=LorawanMacMessage.parseMessage(Base64.decode(pktMsg.getTxpk().getData()));
		if (VERBOSE) log("received LoRaWAN message: "+macMsg);
		int type=macMsg.getMType();
		if (type==LorawanMacMessage.TYPE_JOIN_ACCEPT) {
			joined=true;
			if (VERBOSE) log("associated");
			LorawanJoinAcceptMessage joinAcceptMsg=(LorawanJoinAcceptMessage)macMsg;
			try {
				//joinAcceptMsg.decrypt(appCtx.getNwkKey());
				//sessCtx=new SessionContext(joinAcceptMsg.getDevAddr(),appCtx.getNwkKey(),joinAcceptMsg.getJoinNonce(),joinAcceptMsg.getHomeNetID(),appCtx.getDevNonce());
				joinAcceptMsg.decrypt(appKey);
				sessCtx=new SessionContext(joinAcceptMsg.getDevAddr(),appKey,joinAcceptMsg.getJoinNonce(),joinAcceptMsg.getHomeNetID(),devNonce);
				if (VERBOSE) log("new session context: "+sessCtx.toString());
				// start sending data
				processDataTimeout(null);
			}
			catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void processDataTimeout(Timer t) {
		try {
			sendLorawanMacMessage(getDataMessage());
		}
		catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		Clock.getDefaultClock().newTimer(dataTimeout,this::processDataTimeout).start();
	}

	
	private void sendLorawanMacMessage(LorawanMacMessage macMsg) {
		RxPacketInfo pktInfo=new RxPacketInfo(macMsg.getBytes());
		semtechClient.send(pktInfo);
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

	private LorawanMacMessage getDataMessage() throws GeneralSecurityException {
		byte[] data=device.getData();
		if (VERBOSE) log("data: "+(data!=null?Bytes.toHex(data):null));		
		return new LorawanDataMessage(LorawanMacMessage.TYPE_UNCORFIRMED_DATA_UP,sessCtx.getDevAddr(),sessCtx.incFCntUp(),fPort,data,sessCtx.getAppSKey(),sessCtx.getFNwkSIntKey());
	}
	
}
