package test;


public class LorawanDeviceJson {

	public DeviceInfo[] dev;

	public AppInfo app;
	
	
	public static class AppInfo {
		public String eui;
		public String key;
	}

	public static class DeviceInfo {
		public String eui;
		public String type;
		public String param[];
		public int fport;
		public long time;
	}
		
}
