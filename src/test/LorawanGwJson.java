package test;



public class LorawanGwJson {

	GwInfo gw;
	
	AppInfo app;
	
	DeviceInfo[] dev;
	
	
	public static class GwInfo {
		String eui;
		float latitude;
		float longitude;
		int port;
	}
	
	public static class AppInfo {
		String eui;
		String key;
		String server;
		int port;		
	}
	
	public static class DeviceInfo {
		String eui;
		String type;
		String param[];
		int fport;
		long time;
	}
		
}
