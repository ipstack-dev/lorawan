package run.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.zoolu.util.Bytes;
import org.zoolu.util.DateFormat;
import org.zoolu.util.Flags;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.MultiLogger;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.semtech.SemtechServer;
import io.ipstack.lorawan.server.LorawanServer;
import io.ipstack.lorawan.server.LorawanServerListener;
import io.ipstack.lorawan.server.json.RxMsgInfo;
import io.ipstack.net.analyzer.LibpcapHeader;
import io.ipstack.net.analyzer.LibpcapWriter;
import run.server.connector.CoapConnector;
import run.server.connector.HttpConnector;
import run.server.connector.MqttConnector;
import run.server.managment.ManagementServer;


/** LoRaWAN Server platform (NetworkServer, JoinServer, and ApplicationServer).
 */
public final class Server {
	private Server() {}
	
	private static boolean VERBOSE= false;
	
	private static void log(String str) {
		//System.out.println(Server.class.getSimpleName()+": "+str);
		DefaultLogger.info(Server.class,str);
	}

	
	private static HashMap<String, BufferedWriter> dataWriters= null;
	
	static ManagementServer managementServer= null;
	static HttpConnector httpConnector= null;
	static CoapConnector coapConnector= null;
	static MqttConnector mqttConnector= null;
	
	
	public static void main(String[] args) throws Exception {
		var flags= new Flags(args);
		String pcapFile= flags.getString("-pcap",null,"file","write Semtech packets to a pcap file");	
		String configFile= flags.getString("-f",null,"file","server configuration file");
		String dataFolder=flags.getString("-w",null,"folder","writes device data to the given folder");
		boolean veryVerbose= flags.getBoolean("-vv","very verbose mode");
		boolean verbose= flags.getBoolean("-v","verbose mode");
		boolean help= flags.getBoolean("-h","prints this message");
		
		if (configFile==null) {
			System.err.println("Error: an configuration file must be specified");
			help= true;
		}
		if (help) {
			System.out.println(flags.toUsageString(Server.class));
			return;
		}	
		if (pcapFile!=null) {
			if (!pcapFile.endsWith(".pcap")) pcapFile+= ".pcap";
			SemtechServer.PCAP_WRITER= new LibpcapWriter(LibpcapHeader.LINKTYPE_IPV4,pcapFile);
		}
		
		Json.COMMENT_MARK= "#";
		var config= (ServerConfiguration)Json.fromJSONFile(new File(configFile),ServerConfiguration.class);
		if (VERBOSE) log("Configuration: "+Json.toJSON(config));
		
		if (config.homeNetId.length!=3) {
			System.err.println("Config file error: network ID must be 3 bytes, in hexadecimal");
			return;
		}
		
		if (verbose || veryVerbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.INFO));
			if (config.log.fileName!=null) {
				WriterLogger.MAX_SIZE= config.log.maxSize;
				WriterLogger.ROTATATIONS= config.log.rotations;
				DefaultLogger.setLogger(new MultiLogger(DefaultLogger.getLogger(),new WriterLogger(config.log.fileName,LoggerLevel.INFO)));
			}	
			VERBOSE= true;
			LorawanServer.VERBOSE= true;
			if (veryVerbose) {
				LorawanServer.VERY_VERBOSE= true;
				SemtechServer.VERBOSE= true;
				HttpConnector.VERBOSE= true;
				CoapConnector.VERBOSE= true;
				MqttConnector.VERBOSE= true;
			}
		}
		
		if (dataFolder!=null) dataWriters= new HashMap<>(); 
				
		var lorawanServer= new LorawanServer(config,new LorawanServerListener() {
			@Override
			public void onJoinRequest(LorawanServer server, RxMsgInfo rxMsgInfo) {
				if (VERBOSE) log("onJoinRequest(): from dev "+rxMsgInfo.devEUI);
				if (VERBOSE) log("onJoinRequest(): accepted");
				server.accept(rxMsgInfo);
			}
			@Override
			public void onReceivedData(LorawanServer server, RxMsgInfo rxMsgInfo) {
				if (VERBOSE) log("onReceivedData(): from dev "+rxMsgInfo.devEUI+": "+Bytes.toHex(rxMsgInfo.payload)+"\n");
				if (dataWriters!=null) {
					try {
						String id= rxMsgInfo.devEUI;
						if (!dataWriters.containsKey(id)) dataWriters.put(id,new BufferedWriter(new FileWriter(dataFolder+'/'+id+".log",true)));
						BufferedWriter wr= dataWriters.get(id);
						wr.write(DateFormat.formatYyyyMMddHHmmssSSS(new Date())+'\t'+Bytes.toHex(rxMsgInfo.payload)+"\r\n");
						wr.flush();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (httpConnector!=null) httpConnector.postData(rxMsgInfo);
				if (coapConnector!=null) coapConnector.postData(rxMsgInfo);
				if (mqttConnector!=null) mqttConnector.publishMessage(rxMsgInfo);
			}
			@Override
			public void onConnectedGateway(LorawanServer server, EUI gwEUI) {
				if (VERBOSE) log("onConnectedGateway(): gw "+gwEUI);
			}
		});
		if (VERBOSE) log("main(): LoRaWAN server running on port "+config.port);
		else System.out.println("LoRaWAN server running on port "+config.port);
		
		// Connectors
		//var connectors= Set.of(config.connectors);
		if (config.httpConnector!=null && config.httpConnector.port>0) {
			httpConnector= new HttpConnector(config.httpConnector.port,lorawanServer);
			if (config.httpConnector.endpoints!=null) for (var endpoint: config.httpConnector.endpoints) httpConnector.addEndpoint(endpoint);
			if (VERBOSE) log("HTTP connector: ok");
		}
		if (config.coapConnector!=null && config.coapConnector.port>0) {
			coapConnector= new CoapConnector(config.coapConnector.port,lorawanServer);
			if (config.coapConnector.endpoints!=null) for (var endpoint: config.coapConnector.endpoints) coapConnector.addEndpoint(endpoint);
			if (VERBOSE) log("CoAP connector: ok");
		}
		if (config.mqttConnector!=null && config.mqttConnector.broker!=null) {
			mqttConnector= new MqttConnector(config.mqttConnector.clientId,config.mqttConnector.broker,config.mqttConnector.user,config.mqttConnector.passwd,lorawanServer);
			if (VERBOSE) log("MQTT connector: ok");
		}	
		if (config.managementPort>0) {
			managementServer= new ManagementServer(config.managementPort,lorawanServer,config.deviceFile);
			if (VERBOSE) log("Management server: ok");
		}
	}
	
}
