package run.device;

import io.ipstack.lorawan.mac.EUI;

public class VirtualDeviceConfig {

	public String devEUI;
	public String joinEUI;
	public String appKey;
	
	public int fport;
	public String devType;
	public String[] devParam;
	public long devTime;
	
	protected VirtualDeviceConfig() {}
	
		
	/**
	 * @param devEUI
	 * @param joinEUI
	 * @param appKey
	 * @param fport
	 * @param devType
	 * @param devParam
	 * @param devTime
	 */
	public VirtualDeviceConfig(String devEUI, String joinEUI, String appKey, int fport, String devType, String[] devParam, long devTime) {
		this.devEUI=devEUI;
		this.joinEUI=joinEUI;
		this.appKey=appKey;
		this.fport=fport;
		this.devType=devType;
		this.devParam=devParam;
		this.devTime=devTime;
	}

	public EUI getDevEUI() {
		return new EUI(devEUI);
	}
	
	public EUI getJoinEUI() {
		return new EUI(joinEUI);
	}
}
