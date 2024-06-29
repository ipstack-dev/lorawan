package run.server.connector;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.server.LorawanServer;
import io.ipstack.lorawan.server.json.RxMsgInfo;
import io.ipstack.lorawan.server.json.TxMsgInfo;
import io.ipstack.mqtt.MqttClient;
import io.ipstack.mqtt.MqttClientListener;
import io.ipstack.mqtt.PahoClient;


public class MqttConnector {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;
	
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}
	
	
	public static String TOPIC_DOWNLINK= "down";

	public static String TOPIC_UPLINK= "up";

	public static int DEFAULT_QOS= 2;
	
	private LorawanServer lorawanServer= null;
	
	private MqttClient mqttClient= null;
	
	
	public MqttConnector(String client_id, String broker, String username, String passwd, LorawanServer lorawanServer) throws IOException {
		this.lorawanServer= lorawanServer;
		mqttClient= new PahoClient(client_id,"tcp://"+broker,username,passwd,new MqttClientListener() {
			@Override
			public void onSubscribing(MqttClient client, String topic, int qos) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPublishing(MqttClient client, String topic, int qos, byte[] payload) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onMessageArrived(MqttClient client, String topic, int qos, byte[] payload) {
				processReceivedMessage(topic,payload);
			}
			@Override
			public void onConnectionLost(MqttClient client, Throwable cause) {
				// TODO Auto-generated method stub
			}	
		});
		try {
			mqttClient.connect();
			mqttClient.subscribe("+/"+TOPIC_DOWNLINK+"/#",DEFAULT_QOS);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void publishMessage(RxMsgInfo rxMsgInfo) {
		String topic= rxMsgInfo.devEUI+"/"+TOPIC_UPLINK+"/"+rxMsgInfo.fport;
		try {
			mqttClient.publish(topic,2,Json.toJSON(rxMsgInfo).getBytes());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void processReceivedMessage(String topic, byte[] payload) {
		if (lorawanServer!=null) try {
			String body= new String(payload);
			if (VERBOSE) log("processReceivedMessage(): received: "+topic+" "+body);
			String[] topicPath= topic.split("/");
			if (topicPath[1].equals(TOPIC_DOWNLINK)) {
				//EUI appEUI= new EUI(topicPath[0]);
				EUI devEUI= new EUI(topicPath[0]);
				int fport= Integer.parseInt(topicPath[2]);
				var txMsgInfo= Json.fromJSON(body,TxMsgInfo.class);
				String hexData= Bytes.toHex(txMsgInfo.payload);
				if (VERBOSE) log("processReceivedMessage(): send data to "+devEUI+": "+hexData);
				System.out.println("data to "+devEUI+": "+hexData);
				lorawanServer.sendData(devEUI,fport,txMsgInfo.payload);				
			}
		}
		catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

}
