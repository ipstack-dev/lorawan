package run.server.managment;


public class DeviceInfo {
	
	public String devEUI;
	//public String name;
	public String type;
	//public String description;
	public String decoder;
	
	protected DeviceInfo() {
	}

	public DeviceInfo(String devEUI, String type) {
		this.devEUI= devEUI;	
		this.type= type;
	}
	
	public DeviceInfo(String devEUI, String type, String decoder) {
		this(devEUI,type);	
		this.decoder= decoder;
	}

}
