package io.ipstack.lorawan.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.zoolu.util.ArrayUtils;
import org.zoolu.util.Base64;
import org.zoolu.util.Bytes;
import org.zoolu.util.Timer;
import org.zoolu.util.json.Json;
import org.zoolu.util.json.JsonObject;
import org.zoolu.util.log.DefaultLogger;

import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.mac.LorawanDataMacMessage;
import io.ipstack.lorawan.mac.LorawanDataMacPayload;
import io.ipstack.lorawan.mac.LorawanJoinAcceptMacMessage;
import io.ipstack.lorawan.mac.LorawanJoinRequestMacMessage;
import io.ipstack.lorawan.mac.LorawanMacMessage;
import io.ipstack.lorawan.mac.SessionContext;
import io.ipstack.lorawan.semtech.SemtechClient;
import io.ipstack.lorawan.semtech.SemtechServer;
import io.ipstack.lorawan.semtech.SemtechServerListener;
import io.ipstack.lorawan.semtech.json.RxPacketInfo;
import io.ipstack.lorawan.semtech.json.StatusInfo;
import io.ipstack.lorawan.semtech.json.TxPacketInfo;
import io.ipstack.lorawan.semtech.json.TxPacketMessage;
import io.ipstack.lorawan.server.json.DeviceState;
import io.ipstack.lorawan.server.json.RxMsgInfo;
import io.ipstack.lorawan.server.json.RxMetadata;
import io.ipstack.lorawan.server.json.ServerState;


/** LoRaWAN server (Network Server, Join Server, and Application Server).
 */
public class LorawanServer {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;

	/** Very verbose mode */
	public static boolean VERY_VERBOSE= false;

	/** Prints a log message. */
	private void log(String str) {
		DefaultLogger.info(this.getClass(),str);
	}
	
	
	public static int DEFAULT_NETWORK_SERVER_PORT= 1700; 

	// Specified in LoRaWAN Regional Parameters (RP002-1.0.4):
	public static long RECEIVE_DELAY1= 1000; // 1sec (in milliseconds)
	public static long RECEIVE_DELAY2= 2000; // 2sec (RECEIVE_DELAY1 + 1s)
	public static long JOIN_ACCEPT_DELAY1= 5000; // 5sec
	public static long JOIN_ACCEPT_DELAY2= 6000; // 6sec
	public static int ADR_ACK_LIMIT= 64;
	public static long ADR_ACK_DELAY= 32000; // 32sec
	public static long RETRANSMIT_TIMEOUT= 2000; // 2s +/- 1s (random delay between 1 and 3 seconds)
	public static long DownlinkDwellTime= 0; // (No downlink dwell time enforced; impacts data rate offset calculations)
	public static long UplinkDwellTime; // Uplink dwell time is country-specific and it is the responsibility of the end-device to comply with such regulations
	public static long PING_SLOT_PERIODICITY; // 7 (2^7 = 128s)
	public static long PING_SLOT_DATARATE; // The value of the BEACON data rate (DR) defined for each regional band
	public static int PING_SLOT_CHANNEL; // Defined in each regional band CLASS_B_RESP_TIMEOUT 8s
	public static long CLASS_C_RESP_TIMEOUT; // 8s
	
	public static String DEFALUT_STATE_FILE= "server-state.json"; 

	public static long PACKET_DUPLICATION_INTERVAL= 2000; // in milliseconds

	
	private static long JOINING_TIMEOUT= 30*1000;
	private static long GATEWAY_TIMEOUT= 5*60000; // in milliseconds (5 minutes) 

	//private static byte[] DEFAULT_JOIN_NONCE= Bytes.fromHex("000001"); // appNonce
	private static byte[] DEFAULT_JOIN_NONCE= Bytes.fromHex("000062"); // DEBUG: from TTN test
	
	//private static byte[] DEFAULT_CFList= null;
	private static byte[] DEFAULT_CFList= Bytes.fromHex("184f84e85684b85e84886684586e8400"); // DEBUG: from TTN test

	private SemtechServer semtechServer;

	private byte[] homeNetId; // b000 XXXXXXXXXXXXXX 0000001 (or 0)
	
	private long devAddrCounter= 1; // counter for generating DevAddrs
	//private long devAddrCounter= Random.nextInt(1<<30); // counter for generating DevAddrs
	
	private HashMap<String,EUI> devEuiMap= new HashMap<>(); // maps DevAddr -> DevEUI

	private HashMap<EUI,AppContext> appCtxMap= new HashMap<>(); // maps DevEUI -> AppContext

	private HashMap<EUI,SessionContext> sessCtxMap= new HashMap<>(); // maps DevEUI -> SesssionContext
	
	private HashMap<EUI,HashSet<EUI>> joinDevEuiMap= new HashMap<>(); // maps JoinEUI -> DevEUIs

	private HashMap<EUI,RxMsgInfo> rxMsgMap= new HashMap<>(); // maps DevEUI -> metadata of last received packet

	private HashSet<EUI> allowedGateways= null; // EUIs of allowed gateways
	
	private HashMap<EUI,Long> associatedGateways= new HashMap<>(); // maps associated gwEUI -> last active time [millisecs]
	
	private HashMap<EUI,ArrayList<LorawanMacMessage>> downlinkQueues= new HashMap<>(); // maps DevEUI -> dowlink output queue

	private String backendNetworkServer= null;
	private HashMap<EUI,SemtechClient> semtechClients= null; // maps gwEUI -> SemtechClient
	
	private LorawanServerListener listener;
	
	private String deviceFile;

	private String stateFile;
			
	
	/** Creates a new server.
	 * @param config server configuration
	 * @param listener server listener
	 * @throws IOException
	 */
	public LorawanServer(LorawanServerConfiguration config, LorawanServerListener listener) throws IOException {
		this.listener= listener;	
		this.homeNetId= config.homeNetId;
		deviceFile= config.deviceFile;
		if (config.allowedGateways!=null) {
			allowedGateways= new HashSet<>();
			for (var gweui: config.allowedGateways) allowedGateways.add(new EUI(gweui));
		}
		loadAppContexts();
		stateFile= config.stateFile;
		loadState();
		if (config.port<=0) config.port=DEFAULT_NETWORK_SERVER_PORT;
		semtechServer= new SemtechServer(config.port,new SemtechServerListener() {
			@Override
			public void onRxPacket(EUI gwEUI, RxPacketInfo rxPktInfo) {
				processReceivedRxPacketInfo(gwEUI,rxPktInfo);
			}
			@Override
			public void onStatus(EUI gwEUI, StatusInfo statusInfo) {
				processReceivedStatusInfo(gwEUI,statusInfo);
			}
		});
		if (VERBOSE) log("LorawanServer(): running on port "+config.port);
		if (config.backendNetworkServer!=null) {
			backendNetworkServer= config.backendNetworkServer;
			if (VERBOSE) log("LorawanServer(): backend ns: "+backendNetworkServer);
			semtechClients= new HashMap<>();
		}
		new Timer((long)(GATEWAY_TIMEOUT*1.2),this::processGatewayActivityTimout).start();
	}
	
		
	/**
	 * Loads app contexts.
	 */
	private void loadAppContexts() {
		if (deviceFile==null) {
			if (VERBOSE) log("loadAppContexts(): no app context file has been configured");
			return;
		}
		var appContexts= (AppContext[])Json.fromJSONArrayFile(new File(deviceFile),AppContext.class);
		if (VERBOSE) log("loadAppContexts(): "+Json.toJSON(appContexts));
		for (AppContext appCtx: appContexts) addDevice(appCtx);
	}

	
	/**
	 * Saves app contexts.
	 */
	private void saveAppContexts() {
		if (deviceFile==null) {
			if (VERBOSE) log("saveAppContexts(): no app context file has been configured");
			return;
		}
		var appContexts= appCtxMap.values().toArray(new AppContext[0]);
		saveObjectToJsonFile(appContexts,deviceFile);
	}


	/**
	 * Loads the last active gateways and session contexts.
	 */
	private void loadState() {
		if (stateFile==null) {
			if (VERBOSE) log("loadState(): no state file has been configured");
			return;
		}
		File file= new File(stateFile);
		if (file.exists()) {
			var state= (ServerState)Json.fromJSONFile(new File(stateFile),ServerState.class);
			if (VERBOSE) log("loadState(): "+Json.toJSON(state));
			if (state.gateways!=null) {
				associatedGateways= new HashMap<>();
				long time= System.currentTimeMillis();
				for (var gweui: state.gateways) associatedGateways.put(new EUI(gweui),time);
			}
			if (state.devices!=null) {
				for (var devState: state.devices) {
					var devEui= devState.getDevEUI();
					var devAddr= Bytes.toHex(devState.sessCtx.devAddr());
					devEuiMap.put(devAddr,devEui);
					sessCtxMap.put(devEui,devState.sessCtx);
				}
			}
			if (state.devAddrCounter>0) devAddrCounter= state.devAddrCounter;
			else saveState();
		}
		else saveState();
	}

	
	/**
	 * Saves the current active gateways and session contexts.
	 */
	private void saveState() {
		if (stateFile==null) {
			if (VERBOSE) log("saveState(): no state file");
			return;
		}
		var state= new ServerState();
		state.devAddrCounter= devAddrCounter;
		if (associatedGateways!=null) state.gateways= ArrayUtils.toStringArray(associatedGateways.keySet());
		if (sessCtxMap.size()>0) {
			var devices= new ArrayList<DeviceState>();
			for (var devEui: sessCtxMap.keySet()) {
				var sessCtx= sessCtxMap.get(devEui);
				devices.add(new DeviceState(devEui,sessCtx));
			}
			state.devices= devices.toArray(new DeviceState[0]);
		}
		saveObjectToJsonFile(state,stateFile);
	}
	
	
	private void saveObjectToJsonFile(Object obj, String fileName) {
		try {
			var newFile= Paths.get(fileName);
			var tempFile= Paths.get(fileName+".temp");
			var backFile= Paths.get(fileName+".back");
			Files.deleteIfExists(tempFile);
			Json.toJSONFile(obj,tempFile.toFile());
			if (Files.exists(newFile)) {
				Files.deleteIfExists(backFile);
				Files.move(newFile,backFile);
			}
			Files.move(tempFile,newFile);			
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	private synchronized void processGatewayActivityTimout(Timer t) {
		long expireTime= System.currentTimeMillis()-GATEWAY_TIMEOUT;
		boolean changed= false;
		for (var gweui: associatedGateways.keySet().toArray()) {
			if (associatedGateways.get(gweui)<expireTime) {
				associatedGateways.remove(gweui);
				var client= semtechClients.get(gweui);
				if (client!=null) client.halt();
				semtechClients.remove(gweui);
				changed= true;
			}
		}
		new Timer(GATEWAY_TIMEOUT,this::processGatewayActivityTimout).start();
		if (changed) saveState();
	}
		
	
	public void addDevice(AppContext appCtx) {
		appCtxMap.put(appCtx.devEUI(),appCtx);
		var appDeviceSet= joinDevEuiMap.get(appCtx.joinEUI());
		if (appDeviceSet==null) {
			appDeviceSet= new HashSet<>();
			joinDevEuiMap.put(appCtx.joinEUI(),appDeviceSet);
		}
		appDeviceSet.add(appCtx.devEUI());
		//saveAppContexts(); // beautify the JSON?
	}

	
	public void removeDevice(EUI devEUI) {
		var sessCtx= sessCtxMap.get(devEUI);
		if (sessCtx!=null) {
			devEuiMap.remove(Bytes.toHex(sessCtx.devAddr()));
			sessCtxMap.remove(devEUI);
		}
		var appCtx= appCtxMap.get(devEUI);
		if (appCtx!=null) {
			var joinEUI= appCtx.joinEUI();
			var devEuiSet= joinDevEuiMap.get(joinEUI);
			if (devEuiSet!=null) {
				devEuiSet.remove(devEUI);
				if (devEuiSet.size()==0) joinDevEuiMap.remove(joinEUI);
			}
			appCtxMap.remove(devEUI);
		}
		//saveAppContexts(); // beautify the JSON?
		saveState();
	}


	/** 
	 * @return EUIs of all devices
	 */
	public EUI[] getDevices() {
		return appCtxMap.keySet().toArray(new EUI[0]);
	}

	
	/*
	 * @return EUIs of all devices with the given JoinEUI
	 */
	public EUI[] getDevices(EUI joinEUI) {
		var devEuiSet= joinDevEuiMap.get(joinEUI);
		return devEuiSet!=null? devEuiSet.toArray(new EUI[0]) : null;
	}

	
	/*
	 * @return the app context of the given DevEUI
	 */
	public AppContext getAppContext(EUI devEUI) {
		return appCtxMap.get(devEUI);
	}

	
	/*
	 * @return metadata of the last rx packet for the given DevEUI
	 */
	public RxMsgInfo getLastPacket(EUI devEUI) {
		return rxMsgMap.get(devEUI);
	}

	
	/** Accepts a Join Request.
	 * @param rxPktInfo
	 */
	public void accept(RxMsgInfo rxMsgInfo) {
		try {
			var joinReqMacMsg= (LorawanJoinRequestMacMessage)LorawanMacMessage.parseMessage(Base64.decode(rxMsgInfo.macMsg));
			var devEUI= joinReqMacMsg.getDevEui();
			var appEUI= joinReqMacMsg.getJoinEui();
			var devNonce= joinReqMacMsg.getDevNonce();
			if (VERBOSE) log("accept(): DEBUG: devAddrCounter: "+devAddrCounter);
			var devAddr= Bytes.fromInt32(devAddrCounter++);
			var appCtx= appCtxMap.get(devEUI);
			if (appCtx==null) {
				// TODO
				return;
			}
			var sessCtx= new SessionContext(devAddr,appCtx.appKey(),DEFAULT_JOIN_NONCE,homeNetId,devNonce);
			if (VERBOSE) log("accept(): new session context: "+sessCtx.toString());
			devEuiMap.put(Bytes.toHex(devAddr),devEUI);
			sessCtxMap.put(devEUI,sessCtx);
			saveState();
			
			if (VERBOSE) log("accept(): DEBUG: devEUI: "+devEUI+", appKey: "+Bytes.toHex(appCtx.appKey())+", joinNonce: "+Bytes.toHex(DEFAULT_JOIN_NONCE)+", devNonce: "+Bytes.toHex(devNonce)+", devAddr: "+Bytes.toHex(devAddr));
			var joinAcceptMacMsg= new LorawanJoinAcceptMacMessage(DEFAULT_JOIN_NONCE,homeNetId,devAddr,0,0,DEFAULT_CFList,appCtx.appKey());
			if (VERBOSE) log("accept(): sending Join Accept message: "+joinAcceptMacMsg);	
			// TODO: select the best gateway
			var gwEUI= rxMsgInfo.rxMetadata[0].getGwEUI();	
			var rxPktInfo= rxMsgInfo.rxMetadata[0].rxPktInfo;
			long tmst= rxPktInfo.getTmst()+JOIN_ACCEPT_DELAY1*1000;
			float freq= rxPktInfo.getFreq();
			int rfch= rxPktInfo.getRfch();
			// TODO: add support for different regional parameters
			var txPktInfo= new TxPacketInfo(tmst,freq,0,14,"SF7BW125","4/5",joinAcceptMacMsg.getBytes());
			if (VERBOSE) log("accept(): sending TxPktInfo to "+gwEUI+": "+Json.toJSON(txPktInfo));
			semtechServer.send(gwEUI,txPktInfo);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/** Instructs the server to send downlink data.
	 * @param devAddr DevAddr
	 * @param fPort FPort
	 * @param data the data to send
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public void sendData(EUI devEUI, int fPort, byte[] data) throws IOException, GeneralSecurityException {
		if (VERBOSE) log("sendData(): scheduling downlink data for DevEUI "+devEUI+": "+(data!=null?Bytes.toHex(data):null));
		var sessCtx= sessCtxMap.get(devEUI);
		if (sessCtx==null) {
			if (VERBOSE) log("sendData(): no active session found for DevEUI "+devEUI);
		}
		else {
			var dataMsg= createDataMacMessage(sessCtx,fPort,data);	
			var queue= downlinkQueues.get(devEUI);
			if (queue==null) {
				queue= new ArrayList<>();
				downlinkQueues.put(devEUI,queue);
			}
			queue.add(dataMsg);
			if (VERBOSE) log("sendData(): data enqueued");
		}		
	}
	
	
	private LorawanMacMessage createDataMacMessage(SessionContext sessCtx, int fPort, byte[] data) throws GeneralSecurityException {
		//if (VERBOSE) log("createDataMacMessage(): data: "+(data!=null?Bytes.toHex(data):null));		
		return new LorawanDataMacMessage(LorawanMacMessage.TYPE_UNCORFIRMED_DATA_DOWN,sessCtx.devAddr(),sessCtx.incFCntUp(),fPort,data,sessCtx.appSKey(),sessCtx.fNwkSIntKey());
	}
	

	private synchronized boolean isAllowedGateway(EUI gwEUI) {
		if (allowedGateways!=null && !allowedGateways.contains(gwEUI)) {
			if (VERBOSE) log("processReceivedRxPacketInfo(): message from unknown gateway "+gwEUI+": discarded");
			return false;
		}
		else {
			long time= System.currentTimeMillis();
			if (!associatedGateways.containsKey(gwEUI)) {
				if (listener!=null) listener.onConnectedGateway(this,gwEUI);
				associatedGateways.put(gwEUI,time);
				saveState();
			}
			else associatedGateways.put(gwEUI,time); // update last active time	
			return true;			
		}
	}
	
	private void processReceivedStatusInfo(EUI gwEUI, StatusInfo statusInfo) {
		if (VERBOSE) log("processReceivedStatusInfo(): from gw "+gwEUI+" status: "+Json.toJSON(statusInfo));
		if (!isAllowedGateway(gwEUI)) return;
		// else
		var client= semtechClients.get(gwEUI);
		if (client!=null) client.setPosition(statusInfo.getLati(),statusInfo.getLong());
	}
	
	
	/** Processes a packet sent by a gateway.
	 * @param gwEUI gateway EUI
	 * @param rxPktInfo metadata of the packet
	 */
	private void processReceivedRxPacketInfo(EUI gwEUI, RxPacketInfo rxPktInfo) {
		if (VERBOSE) log("processReceivedRxPacketInfo(): from gw "+gwEUI+" pktInfo: "+Json.toJSON(rxPktInfo));
		if (!isAllowedGateway(gwEUI)) return;
		// else
		long time= System.currentTimeMillis();		
		var macMsgString= rxPktInfo.getData();
		var macMsgBytes= Base64.decode(macMsgString);
		var macMsg= LorawanMacMessage.parseMessage(macMsgBytes);
		if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): received LoRaWAN message: "+macMsg);
		var type= macMsg.getMType();
		
		if (type==LorawanMacMessage.TYPE_JOIN_REQUEST) {
			var joinReqMacMsg= (LorawanJoinRequestMacMessage)macMsg;
			if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): join request: "+joinReqMacMsg);
			var devEUI= joinReqMacMsg.getDevEui();
			var rxPktMetadata= getMetadata(time,devEUI,macMsgString,gwEUI,rxPktInfo);
			if (rxPktMetadata.rxMetadata.length>1) {
				if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): duplicate");
				return;
			}
			// else	
			var appCtx= appCtxMap.get(devEUI);
			if (appCtx==null) {
				if (VERBOSE) log("processReceivedRxPacketInfo(): join request: no app context found for DevEUI "+devEUI);
				if (backendNetworkServer!=null) forward(gwEUI,rxPktInfo);
			}
			else {
				if (listener!=null) try {
					listener.onJoinRequest(this,rxPktMetadata);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else
		if (type==LorawanMacMessage.TYPE_UNCORFIRMED_DATA_UP || type==LorawanMacMessage.TYPE_CORFIRMED_DATA_UP) {
			var confirmed= type==LorawanMacMessage.TYPE_CORFIRMED_DATA_UP;
			var dataMessagePayload= new LorawanDataMacPayload(macMsg.getMacPayload(),true);
			if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): "+(confirmed?"confirmed":"unconfirmed")+" data up: "+dataMessagePayload.toString());
			//byte[] framePayload=dataMessagePayload.getFramePayload();
			if (dataMessagePayload.getFramePayload()!=null) {
				var devAddr= dataMessagePayload.getDevAddr();
				var devEUI= devEuiMap.get(Bytes.toHex(devAddr));
				var fport= dataMessagePayload.getFPort();
				var sessCtx= devEUI!=null? sessCtxMap.get(devEUI) : null;
				if (sessCtx==null) {
					if (VERBOSE) log("processReceivedRxPacketInfo(): no session found for DevAddr "+Bytes.toHex(devAddr));						
					if (backendNetworkServer!=null) forward(gwEUI,rxPktInfo);
				}
				else {
					var rxPktMetadata= getMetadata(time,devEUI,macMsgString,gwEUI,rxPktInfo);
					if (rxPktMetadata.rxMetadata.length>1) {
						if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): duplicate");
						return;
					}
					// else
					try {
						if (confirmed) {
							// TODO
							// send ack?
						}
						dataMessagePayload.decryptFramePayload(sessCtx.appSKey());
						var framePayload= dataMessagePayload.getFramePayload();
						if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): decrypted frame payload: "+Bytes.toHex(framePayload));
						if (listener!=null) try {
							rxPktMetadata.fport= fport;
							rxPktMetadata.payload= framePayload;
							listener.onReceivedData(this,rxPktMetadata);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						// downlink
						var queue= downlinkQueues.get(devEUI);
						if (queue!=null && queue.size()>0) {
							var dataMsg= queue.remove(0);
							if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): ready to send downlink data frame to DevEUI "+devEUI+": "+dataMsg);
							long tmst= rxPktInfo.getTmst()+RECEIVE_DELAY1*1000;
							float freq= rxPktInfo.getFreq();
							int rfch= rxPktInfo.getRfch();
							// TODO: add support for different regional parameters
							var txPktInfo= new TxPacketInfo(tmst,freq,0,14,"SF7BW125","4/5",dataMsg.getBytes());							
							// TODO: select the best gateway
							if (VERBOSE) log("processReceivedRxPacketInfo(): sending TxPacketInfo to "+gwEUI+": "+txPktInfo);
							semtechServer.send(gwEUI,txPktInfo);
						}
					}
					catch (GeneralSecurityException e) {
						e.printStackTrace();
					}				
				}
			}
		}
		// process other message types
		// TODO	
	}
	
	
	/** Forward a rxpkt to the backend network server.
	 * @throws IOException 
	 */
	private synchronized void forward(EUI gwEUI, RxPacketInfo rxPktInfo) {
		try {
			var client= semtechClients.get(gwEUI);
			if (client==null) {
				client= new SemtechClient(gwEUI,0,0,-1,backendNetworkServer,(txpkt)->processBackendMessage(gwEUI,txpkt));
				semtechClients.put(gwEUI,client);
				if (VERBOSE && VERY_VERBOSE) log("processReceivedRxPacketInfo(): new SemtechClient running on port "+client.getPort());
			}
			if (VERBOSE) log("processReceivedRxPacketInfo(): forwarding packet to backend ns: "+backendNetworkServer);
			client.send(rxPktInfo);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/** Gets metadata for this packet.
	 * @param time
	 * @param devEUI
	 * @param macMsg
	 * @param gwEUI
	 * @param rxPktInfo
	 * @return metadata
	 */
	private RxMsgInfo getMetadata(long time, EUI devEUI, String macMsg, EUI gwEUI, RxPacketInfo rxPktInfo) {
		RxMetadata rxMetadata= new RxMetadata(gwEUI.hex(),rxPktInfo);
		var rxMsgInfo= rxMsgMap.get(devEUI);
		if (rxMsgInfo!=null && (time-rxMsgInfo.timestamp)<PACKET_DUPLICATION_INTERVAL && macMsg.equals(rxMsgInfo.macMsg)) {
			// it is a duplicate
			rxMsgInfo.addRxMetadata(rxMetadata);
			rxMsgMap.put(devEUI,rxMsgInfo);
		}
		else {
			rxMsgInfo= new RxMsgInfo(time,devEUI,macMsg,new RxMetadata[]{rxMetadata});
			rxMsgMap.put(devEUI,rxMsgInfo);
		}
		return rxMsgInfo;
	}
	
	
	/** Processes a message sent by the backend network server and received by this server while acting as gateway forwarder.
	 * This packet is forwarded to the actual gateway.
	 * @param gwEUI EUI of the target gateway
	 * @param msg the received txpkt message.
	 */
	private void processBackendMessage(EUI gwEUI, TxPacketMessage msg) {
		if (VERBOSE) log("processBackendMessage(): forwarding from backend ns "+backendNetworkServer+" to gw "+gwEUI+": "+msg);
		semtechServer.send(gwEUI,msg.getTxpk());
	}
	
	
	public String getStatus() {
		var status= new JsonObject();
		status.add("homeNetId",Bytes.toHex(homeNetId));
		status.add("gateways",ArrayUtils.toStringArray(associatedGateways.keySet()));
		status.add("devEuiMap",devEuiMap.size());
		status.add("appCtxMap",appCtxMap.size());
		status.add("appDevMap",joinDevEuiMap.size());
		int tot= 0;
		for (var app: joinDevEuiMap.values()) tot+= app.size();
		status.add("appDevTot",tot);		
		status.add("sesCtxMap",sessCtxMap.size());
		status.add("downlinkQueues",downlinkQueues.size());
		int size= 0;
		for (var queue: downlinkQueues.values()) size+= queue.size();
		status.add("downlinkQueueSize",size);
		status.add("uplinkCnt",semtechServer.getRxCount());
		status.add("downlinkCnt",semtechServer.getTxCount());
		if (backendNetworkServer!=null) {
			status.add("backendClients",semtechClients.size());
			long countUp= 0;
			long countDown= 0;
			for (var client: semtechClients.values()) {
				countUp+= client.getTxCount();
				countDown+= client.getRxCount();
				status.add("backendUplinkCnt",countUp);
				status.add("backendDownlinkCnt",countDown);
			}
		}
		return status.toString();
	}

}
