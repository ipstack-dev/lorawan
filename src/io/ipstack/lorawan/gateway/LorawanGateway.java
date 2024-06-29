package io.ipstack.lorawan.gateway;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.zoolu.util.Base64;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.lorawan.mac.LorawanMacMessage;
import io.ipstack.lorawan.mac.LorawanMacMessageExchanger;
import io.ipstack.lorawan.semtech.SemtechClient;
import io.ipstack.lorawan.semtech.json.RxPacketInfo;
import io.ipstack.lorawan.semtech.json.TxPacketMessage;


/** LoRaWAN gateway.
 */
public class LorawanGateway {
		
	/** Verbose mode */
	public static boolean VERBOSE=false;
	
	/** Prints a message. */
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}

	
	/** Semtech client used for communicating with the network server */
	SemtechClient semtechClient;	
	
	/** MAC message exchanger used to communicate with LoRaWAN devices */
	LorawanMacMessageExchanger macMsgExchanger;
	
	
	/**
	 * @param gwCfg
	 * @param macMsgExchanger
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public LorawanGateway(LorawanGatewayConfig gwCfg, LorawanMacMessageExchanger macMsgExchanger) throws IOException {
		semtechClient= new SemtechClient(gwCfg.getEUI(),gwCfg.latitude,gwCfg.longitude,-1,gwCfg.networkServer,this::processReceivedTxPacketMessage);
		this.macMsgExchanger= macMsgExchanger;
		macMsgExchanger.receive(this::processReceivedMacMessage);
	}

	
	private void processReceivedTxPacketMessage(TxPacketMessage pktMsg) {
		if (VERBOSE) log("processReceivedTxPacketMessage(): pktInfo: "+pktMsg);
		byte[] data= Base64.decode(pktMsg.getTxpk().getData());
		try {
			macMsgExchanger.send(LorawanMacMessage.parseMessage(data));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private void processReceivedMacMessage(LorawanMacMessage macMsg) {
		var pktInfo= new RxPacketInfo(macMsg.getBytes());
		if (VERBOSE) log("processReceivedDatagramPacket(): pktInfo: "+pktInfo);
		semtechClient.send(pktInfo);
	}
}
