package io.ipstack.lorawan.mac;


import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;
import org.zoolu.util.json.Json;

import io.ipstack.lorawan.semtech.json.RxPacketInfo;


/** LoRaWAN Session Context.
 * It contains Network Session and Application Session.
 * <p>
 * The Network Session consists of the following state:
 * <ul>
 * <li>F/SNwkSIntKey</li>
 * <li>NwkSEncKey</li>
 * <li>FCntUp</li>
 * <li>FCntDwn (LW 1.0) or NFCntDwn (LW 1.1)</li>
 * <li>DevAddr</li>
 * </ul>
 * The Application Session consists of the following state:
 * <ul>
 * <li>AppSKey</li>
 * <li>FCntUp</li>
 * <li>FCntDown (LW 1.0) or AFCntDwn (LW 1.1)</li>
 * </ul>
 */
public class SessionContext {
	
	// Network Session state:
	byte[] fNwkSIntKey;
	byte[] sNwkSIntKey;
	byte[] nwkSEncKey;
	int fCntUp= 0;
	int fCntDown= 0; // (LW 1.0)
	int nFCntDwn= 0; //(LW 1.1)
	byte[] devAddr;

	// Application Session state:
	byte[] appSKey;
	//long fCntUp;
	//long fCntDown; // (LW 1.0)
	int aFCntDown= 0; // (LW 1.1)

	
	/** Creates a new empty session context.
	 */
	protected SessionContext() {
	}
	
	/** Creates a new session context (when OptNeg is unset). 
	 * @throws GeneralSecurityException */
	public SessionContext(byte[] devAddr, byte[] nwkKey, byte[] joinNonce, byte[] netID, byte[] devNonce) throws GeneralSecurityException {
		this.devAddr= devAddr;
		appSKey= gen(nwkKey,0x02,joinNonce,netID,devNonce);
		sNwkSIntKey= nwkSEncKey= fNwkSIntKey= gen(nwkKey,0x01,joinNonce,netID,devNonce);
	}

	/** Creates a new session context (when OptNeg is set). 
	 * @throws GeneralSecurityException */
	public SessionContext(byte[] devAddr, byte[] nwkKey, byte[] appKey, byte[] joinNonce, byte[] netID, byte[] devNonce) throws GeneralSecurityException {
		this.devAddr= devAddr;
		appSKey= gen(appKey,0x02,joinNonce,netID,devNonce);
		fNwkSIntKey= gen(nwkKey,0x01,joinNonce,netID,devNonce);
		sNwkSIntKey= gen(nwkKey,0x03,joinNonce,netID,devNonce);
		nwkSEncKey= gen(nwkKey,0x04,joinNonce,netID,devNonce);
	}
	
	private static byte[] gen(byte[] nwkKey, int val, byte[] joinNonce, byte[] netID, byte[] devNonce) throws GeneralSecurityException {
		var aes= AesCipher.getEncryptionInstance(nwkKey);
		//return aes.doFinal(Bytes.concat(new byte[]{(byte)val},joinNonce,netID,devNonce,new byte[16-((joinNonce.length+netID.length+devNonce.length+1)%16)])); 
		return aes.doFinal(Bytes.concat(new byte[]{(byte)val},Bytes.reverseOrderCopy(joinNonce),Bytes.reverseOrderCopy(netID),Bytes.reverseOrderCopy(devNonce),new byte[16-((joinNonce.length+netID.length+devNonce.length+1)%16)])); 
	}
	
	/**
	 * @return the FNwkSIntKey
	 */
	public byte[] fNwkSIntKey() {
		return fNwkSIntKey;
	}

	/**
	 * @return the SNwkSIntKey
	 */
	public byte[] sNwkSIntKey() {
		return sNwkSIntKey;
	}
	
	/**
	 * @return the NwkSEncKey
	 */
	public byte[] nwkSEncKey() {
		return nwkSEncKey;
	}
	
	/**
	 * @return the FCntUp
	 */
	public int fCntUp() {
		return fCntUp;
	}
	
	/**
	 * @return the FCntUp and increments it by 1
	 */
	public int incFCntUp() {
		return fCntUp++;
	}
	
	/**
	 * @return the FCntDown
	 */
	public int fCntDown() {
		return fCntDown;
	}
	
	/**
	 * @return the FCntDown and increments it by 1
	 */
	public int incFCntDown() {
		return fCntDown++;
	}
	
	/**
	 * @return the NfCntDwn
	 */
	public int nFCntDwn() {
		return nFCntDwn;
	}
	
	/**
	 * @return the NfCntDwn and increments it by 1
	 */
	public long incNFCntDwn() {
		return nFCntDwn++;
	}
	
	/**
	 * @return the devAddr
	 */
	public byte[] devAddr() {
		return devAddr;
	}
	
	/**
	 * @return the appSKey
	 */
	public byte[] appSKey() {
		return appSKey;
	}

	/**
	 * @return the AFCntDown
	 */
	public int aFCntDown() {
		return aFCntDown;
	}

	/**
	 * @return the AFCntDown and increments it by 1
	 */
	public int incAFCntDown() {
		return aFCntDown++;
	}
	
	@Override
	public String toString() {
		return Json.toJSON(this);
	}

}
