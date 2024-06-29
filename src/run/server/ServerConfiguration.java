package run.server;

import io.ipstack.lorawan.server.LorawanServerConfiguration;
import run.server.connector.ConnectorEndpoint;


/** Server configuration.
 */
public class ServerConfiguration extends LorawanServerConfiguration {
	
	// Management server port
	public int managementPort= 0;

	// HTTP connector
	public static class HttpConnector {
		public int port= 0;
		public ConnectorEndpoint[] endpoints= null;
	}
	public HttpConnector httpConnector;
	
	// CoAP connector
	public static class CoapConnector {
		public int port= 0;
		public ConnectorEndpoint[] endpoints= null;
	}
	public CoapConnector coapConnector;
	
	// MQTT connector
	public static class MqttConnector {
		public String broker;
		public String clientId= "1";
		public String user;	
		public String passwd;
	}
	public MqttConnector mqttConnector;
	
	// Log
	public static class Log {
		public String fileName= "server.log";
		public int rotations= 1;
		public long maxSize= 1024*1024L;		
	}
	public Log log;

}
