package run.device;


public class VirtualDeviceJsonConfig {

	public DeviceInfo[] dev;

	public AppInfo app;
	
	public static class AppInfo {
		public String eui;
		public String key;
	}

	public static class DeviceInfo {
		public String eui;
		public String type;
		public String[] param;
		public int fport;
		public long time;
	}
	
	public VirtualDeviceConfig toConfig(int i) {
		var cfg= new VirtualDeviceConfig();
		cfg.joinEUI= app.eui;
		cfg.appKey= app.key;
		cfg.devEUI= dev[i].eui;
		cfg.devType= dev[i].type;
		cfg.devParam= dev[i].param.clone();
		cfg.devTime= dev[i].time;
		cfg.fport= dev[i].fport;
		return cfg;
	}
		
}
