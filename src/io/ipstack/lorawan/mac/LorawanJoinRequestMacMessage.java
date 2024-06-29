package io.ipstack.lorawan.mac;


import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;


/** LoRaWAN Join-Request MAC message used during the over-the-air activation (OTAA) procedure.
 */
public class LorawanJoinRequestMacMessage extends LorawanMacMessage {
	
	/** Join Request information */
	LorawanJoinRequest joinRequestPayload= null;

	
	/** Creates a new Join message.
	 * @param joinEui JoinEUI
	 * @param devEui DevEUI
	 * @param devNonce DevNonce
	 * @param key AppKey (LoRaWAN 1.0.2) or NwkKey (LoRaWAN 1.1) key 
	 * @throws GeneralSecurityException */
	public LorawanJoinRequestMacMessage(EUI joinEui, EUI devEui, byte[] devNonce, byte[] key) throws GeneralSecurityException {
		super(TYPE_JOIN_REQUEST);
		joinRequestPayload= new LorawanJoinRequest(joinEui,devEui,devNonce);
		payload= joinRequestPayload.getBytes();
		mic= Bytes.copy(new AesCmac(key).doFinal(Bytes.concat(new byte[]{(byte)getMHdr()},payload)),0,4);
	}
	
	/** Creates a new Join message.
	 * @param data the buffer containing the packet */
	public LorawanJoinRequestMacMessage(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new Join message.
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet length */
	public LorawanJoinRequestMacMessage(byte[] buf, int off, int len) {
		super(buf,off,len);
		if (type!=TYPE_JOIN_REQUEST) throw new RuntimeException("It isn't a Join Request message ("+type+")");
		joinRequestPayload= new LorawanJoinRequest(payload);
	}
	
	/**
	 * @return the joinEui */
	public EUI getJoinEui() {
		return joinRequestPayload.getJoinEui();
	}
	
	/**
	 * @return the devEui */
	public EUI getDevEui() {
		return joinRequestPayload.getDevEui();
	}
	
	/**
	 * @return the devNonce */
	public byte[] getDevNonce() {
		return joinRequestPayload.getDevNonce();
	}

}
