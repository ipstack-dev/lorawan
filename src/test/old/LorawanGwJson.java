package test.old;



public class LorawanGwJson {

	public GwInfo gw;
	
	public AppInfo app;
	
	public DeviceInfo[] dev;
	
	
	public static class GwInfo {
		public String eui;
		public float latitude;
		public float longitude;
		public int port;
	}
	
	public static class AppInfo {
		public String eui;
		public String key;
		public String server;
		public int port;		
	}
	
	public static class DeviceInfo {
		public String eui;
		public String type;
		public String param[];
		public int fport;
		public long time;
	}
		
}
