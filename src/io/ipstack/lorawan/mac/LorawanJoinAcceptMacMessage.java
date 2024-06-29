package io.ipstack.lorawan.mac;


import java.security.GeneralSecurityException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.zoolu.util.Bytes;


/** LoRaWAN Join-Accept MAC message used in response to a Join-Request during the over-the-air activation (OTAA) procedure.
 */
public class LorawanJoinAcceptMacMessage extends LorawanMacMessage {
	
	/** Join Accept information */
	LorawanJoinAccept joinAcceptPayload= null;

	/** MIC in cleartext */
	byte[] decryptedMic= null;


	/** Creates a new Accept message.
	 * @param joinNonce
	 * @param homeNetId
	 * @param devAddr
	 * @param dlSettings
	 * @param rxDelay
	 * @param cfList
	 * @param key AppKey (LoRaWAN 1.0.2) or NwkKey (LoRaWAN 1.1) key 
	 * @throws GeneralSecurityException */
	public LorawanJoinAcceptMacMessage(byte[] joinNonce, byte[] homeNetId, byte[] devAddr, int dlSettings, int rxDelay, byte[] cfList, byte[] key) throws GeneralSecurityException {
		super(TYPE_JOIN_ACCEPT);
		joinAcceptPayload= new LorawanJoinAccept(joinNonce,homeNetId,devAddr,dlSettings,rxDelay,cfList);
		payload= joinAcceptPayload.getBytes();
		decryptedMic= Bytes.copy(new AesCmac(key).doFinal(Bytes.concat(new byte[]{(byte)getMHdr()},payload)),0,4);
		// encrypt
		byte[] ciphertext= AesCipher.getDecryptionInstance(key).doFinal(Bytes.concat(payload,decryptedMic));
		System.arraycopy(ciphertext,0,payload,0,payload.length);
		mic= Bytes.copy(ciphertext,payload.length,4);		
	}

	/** Creates a new Accept message.
	 * @param data the buffer containing the packet */
	public LorawanJoinAcceptMacMessage(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new Accept message.
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet length */
	public LorawanJoinAcceptMacMessage(byte[] buf, int off, int len) {
		super(buf,off,len);
		if (type!=TYPE_JOIN_ACCEPT) throw new RuntimeException("It isn't a Join Accept message ("+type+")");
	}

	/** Decrypts the message. 
	 * @throws GeneralSecurityException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException */
	public void decrypt(byte[] key) throws GeneralSecurityException {
		if (joinAcceptPayload==null) {
			byte[] cleartext= AesCipher.getEncryptionInstance(key).doFinal(Bytes.concat(payload,mic));
			//System.arraycopy(cleartext,0,payload,0,payload.length);
			//System.arraycopy(cleartext,payload.length,mic,0,mic.length);
			//joinAcceptPayload= new LorawanJoinAcceptMessagePayload(payload);
			joinAcceptPayload= new LorawanJoinAccept(cleartext,0,payload.length);
			decryptedMic= Bytes.copy(cleartext,payload.length,mic.length);
		}
	}

	/**
	 * @return the JoinNonce */
	public byte[] getJoinNonce() {
		return joinAcceptPayload.getJoinNonce();
	}
	
	/**
	 * @return the Home_NetID */
	public byte[] getHomeNetID() {
		return joinAcceptPayload.getHomeNetID();
	}

	/**
	 * @return the dDvAddr */
	public byte[] getDevAddr() {
		return joinAcceptPayload.getDevAddr();
	}

	/**
	 * @return the DLSettings */
	public int getDlSettings() {
		return joinAcceptPayload.getDlSettings();
	}
	
	/**
	 * @return the RxDelay */
	public int getRxDelay() {
		return joinAcceptPayload.getRxDelay();
	}
	
	/**
	 * @return the cfList */
	public byte[] getCfList() {
		return joinAcceptPayload.getCfList();
	}

	/**
	 * @return the decrypted MIC */
	public byte[] getDecryptedMIC() {
		return decryptedMic;
	}

	/**
	 * @return the decrypted payload */
	public LorawanJoinAccept getDecryptedPayload() {
		return joinAcceptPayload;
	}

}
