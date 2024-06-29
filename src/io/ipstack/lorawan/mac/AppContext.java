package io.ipstack.lorawan.mac;


import java.io.IOException;

import org.zoolu.util.Bytes;
import org.zoolu.util.config.Configure;
import org.zoolu.util.json.Json;


/** LoRaWAN Application Context.
 * <p>
 * It includes:
 * <ul>
 * <li>DevEUI - global end-device ID in IEEE EUI64 address space that uniquely identifies the end-device;</li>
 * <li>JoinEUI - (aka AppEUI) global application ID in IEEE EUI64 address space that uniquely identifies the Join Server;</li>
 * <li>AppKey - AES-128 key specific to the end-device, used to derive the AppSKey session key. It is assigned to the end-device during fabrication.</li>
 * <li>NwkKey - AES-128 key specific to the end-device, used to derive the FNwkSIntKey, SNwkSIntKey and NwkSEncKey session keys. It that is assigned to the end-device during fabrication;</li>
 * <li> DevNonce - </li> join counter, that is a counter starting at 0 when the device is virtually initially powered up and incremented by 1 every Join-request
 * </ul>
 */
public class AppContext {
	
	protected byte[] devEUI;
	protected byte[] joinEUI;
	protected byte[] appKey;
	protected byte[] nwkKey;
	protected int devNonce;

	
	/** Creates a new context.
	 */
	protected AppContext() {
	}

	
	/** Creates a new context.
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	public AppContext(EUI devEUI, EUI joinEUI, String appKey, int devNonce) {
		this(devEUI,joinEUI,appKey,appKey,devNonce);
	}

	
	/** Creates a new context.
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param nwkKey NwkKey - AES-128 key used to derive the FNwkSIntKey, SNwkSIntKey and NwkSEncKey session keys
	 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	public AppContext(EUI devEUI, EUI joinEUI, String appKey, String nwkKey, int devNonce) {
		this(devEUI,joinEUI,fromHex(appKey),fromHex(nwkKey), Bytes.fromInt16(devNonce));
	}

	
	/** Creates a new AppContext.
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	public AppContext(EUI devEUI, EUI joinEUI, byte[] appKey, byte[] devNonce) {
		this(devEUI,joinEUI,appKey,appKey,devNonce);
	}

	
	/** Creates a new AppContext.
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param nwkKey NwkKey - AES-128 key used to derive the FNwkSIntKey, SNwkSIntKey and NwkSEncKey session keys
	 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	public AppContext(EUI devEUI, EUI joinEUI, byte[] appKey, byte[] nwkKey, byte[] devNonce) {
		this.devEUI= devEUI.bytes();
		this.joinEUI= joinEUI.bytes();
		this.appKey= appKey;
		this.nwkKey= nwkKey;
		this.devNonce= Bytes.toInt16(devNonce);
	}


	
	private static byte[] fromHex(String hex) {
		return hex!=null? Bytes.fromFormattedHex(hex) : null;
	}

	
	/** Creates a new AppContext.
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	/*public AppContext(EUI devEUI, EUI joinEUI, byte[] appKey, byte[] devNonce) {
		this(devEUI,joinEUI,appKey,appKey,devNonce);
	}*/

	
	/** Creates a new AppContext.
	 * @param devEUI DevEUI - global end-device ID that uniquely identifies the end-device
	 * @param joinEUI JoinEUI - global application ID that uniquely identifies the Join Server
	 * @param appKey AppKey - AES-128 key used to derive the AppSKey session key
	 * @param nwkKey NwkKey - AES-128 key used to derive the FNwkSIntKey, SNwkSIntKey and NwkSEncKey session keys
	 * @param devNonce DevNonce - join counter, a counter starting at 0 when the device is initially powered up and incremented by 1 every Join-request
	 */
	/*public AppContext(EUI devEUI, EUI joinEUI, byte[] appKey, byte[] nwkKey, byte[] devNonce) {
		this.devEUI= devEUI.bytes();
		this.joinEUI= joinEUI.bytes();
		this.appKey= appKey;
		this.nwkKey= nwkKey;
		this.devNonce= Bytes.toInt16(devNonce);
	}*/

	
	/**
	 * @return the DevEUI
	 */
	public EUI devEUI() {
		return new EUI(devEUI);
	}

	
	/**
	 * @return the JoinEUI
	 */
	public EUI joinEUI() {
		return new EUI(joinEUI);
	}

	
	/**
	 * @return the NwkKey
	 */
	public byte[] nwkKey() {
		return nwkKey;
	}
	
	
	/**
	 * @return the AppKey
	 */
	public byte[] appKey() {
		return appKey;
	}

	
	/**
	 * @return the DevNonce
	 */
	public byte[] devNonce() {
		return Bytes.fromInt16(devNonce);
	}
	
	
	/** 
	 *Increments the DevNonce it by 1.
	 */
	public void incDevNonce() {
		devNonce++;
	}
	
	
	/** Loads a context from a file.
	 * @param fileName file where the context is read from
	 * @throws IOException 
	 */
	public static AppContext fromFile(String fileName) throws IOException {
		var appCtx= new AppContext();
		var config= new Configure(appCtx,true);
		config.load(fileName);
		if (appCtx.nwkKey==null) appCtx.nwkKey= appCtx.appKey;
		return appCtx;
	}

	
	/** Save context in a file.
	 * @param fileName file where the context is written
	 * @throws IOException 
	 */
	public void toFile(String fileName) throws IOException {
		var config= new Configure(this,true);
		config.saveChanges(fileName);
	}

	
	public String toJson() {
		return Json.toJSON(this);
	}


	public static AppContext fromJson(String json) throws IOException {
		return Json.fromJSON(json,AppContext.class);
	}

	
	@Override
	public String toString() {
		return toJson();
	}

}
