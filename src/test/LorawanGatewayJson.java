package test;



public class LorawanGatewayJson {

	public GwInfo gw;
	
	public AppInfo app;
	
	
	public static class GwInfo {
		public String eui;
		public float latitude;
		public float longitude;
		public int port;
	}
	
	public static class AppInfo {
		public String networkServer;
	}
		
}
