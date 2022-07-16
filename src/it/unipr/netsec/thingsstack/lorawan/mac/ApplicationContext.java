package it.unipr.netsec.thingsstack.lorawan.mac;


import java.io.IOException;

import org.zoolu.util.Bytes;
import org.zoolu.util.config.Configure;
import org.zoolu.util.json.JsonUtils;


/** LoRaWAN Application Context.
 * <p>
 * It includes:
 * <ul>
 * <li>JoinEUI - global application ID in IEEE EUI64 address space that uniquely identifies the Join Server;</li>
 * <li>DevEUI - global end-device ID in IEEE EUI64 address space that uniquely identifies the end-device;</li>
 * <li>AppKey - AES-128 key specific to the end-device, used to derive the AppSKey session key. It is assigned to the end-device during fabrication.</li>
 * <li>NwkKey - AES-128 key specific to the end-device, used to derive the FNwkSIntKey, SNwkSIntKey and NwkSEncKey session keys. It that is assigned to the end-device during fabrication;</li>
 * <li> DevNonce - </li> join counter, that is a counter starting at 0 when the device is virtually initially powered up and incremented by 1 every Join-request
 * </ul>
 */
public class ApplicationContext {
	
	byte[] joinEUI;
	byte[] devEUI;
	byte[] nwkKey;
	byte[] appKey;
	int devNonce;

	
	/** Creates a new context.
	 */
	protected ApplicationContext() {
	}

	
	/** Creates a new context.
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param nwkKey NwkKey - AES-128 key used to derive the FNwkSIntKey, SNwkSIntKey and NwkSEncKey session keys
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	public ApplicationContext(String joinEUI, String devEUI, String nwkKey, String appKey, int devNonce) {
		this(fromHex(joinEUI),fromHex(devEUI),fromHex(appKey),fromHex(nwkKey), devNonce);
	}
	
	private static byte[] fromHex(String hex) {
		return hex!=null? Bytes.fromFormattedHex(hex) : null;
	}

	
	/** Creates a new AppContext.
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param nwkKey NwkKey - AES-128 key used to derive the FNwkSIntKey, SNwkSIntKey and NwkSEncKey session keys
 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	public ApplicationContext(byte[] joinEUI, byte[] devEUI, byte[] appKey, byte[] nwkKey, int devNonce) {
		this.joinEUI=joinEUI;
		this.devEUI=devEUI;
		this.appKey=appKey;
		this.nwkKey=nwkKey;
		this.appKey=appKey;
		this.devNonce=devNonce;
	}

	
	/**
	 * @return the JoinEUI
	 */
	public byte[] getJoinEUI() {
		return joinEUI;
	}

	
	/**
	 * @return the AppKey
	 */
	public byte[] getAppKey() {
		return appKey!=null? appKey : nwkKey;
	}

	
	/**
	 * @return the DevEUI
	 */
	public byte[] getDevEUI() {
		return devEUI;
	}

	
	/**
	 * @return the NwkKey
	 */
	public byte[] getNwkKey() {
		return nwkKey!=null? nwkKey : appKey;
	}
	
	
	/**
	 * @return the DevNonce
	 */
	public byte[] getDevNonce() {
		return Bytes.fromInt16(devNonce);
	}
	
	
	/** 
	 * @return the DevNonce and increments it by 1.
	 */
	public int incDevNonce() {
		return devNonce++;
	}
	
	
	/** Loads a context from a file.
	 * @param conextFile file where the context is read from
	 * @throws IOException 
	 */
	public static ApplicationContext fromFile(String conextFile) throws IOException {
		ApplicationContext appCtx=new ApplicationContext();
		Configure config=new Configure(appCtx,true);
		config.load(conextFile);
		return appCtx;
	}

	
	/** Save a context in file.
	 * @param conextFile file where the context is written
	 * @throws IOException 
	 */
	public void toFile(String conextFile) throws IOException {
		Configure config=new Configure(this,true);
		config.save(conextFile);
	}

	
	public String toJson() {
		return JsonUtils.toJson(this);
	}


	public static ApplicationContext fromJson(String json) throws IOException {
		return JsonUtils.fromJson(json,ApplicationContext.class);
	}

	
	@Override
	public String toString() {
		return JsonUtils.toJson(this);
	}

}
