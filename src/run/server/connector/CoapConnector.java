package run.server.connector;

import java.io.IOException;
import java.util.HashSet;

import org.zoolu.util.Bytes;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.coap.client.CoapClient;
import io.ipstack.coap.client.CoapResponseHandler;
import io.ipstack.coap.message.CoapRequest;
import io.ipstack.coap.message.CoapRequestMethod;
import io.ipstack.coap.message.CoapResponse;
import io.ipstack.coap.message.CoapResponseCode;
import io.ipstack.coap.option.CoapOption;
import io.ipstack.coap.option.CoapOptions;
import io.ipstack.coap.option.UriQueryOption;
import io.ipstack.coap.provider.CoapURI;
import io.ipstack.coap.server.AbstractCoapServer;
import io.ipstack.coap.server.CoapResource;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.server.LorawanServer;
import io.ipstack.lorawan.server.json.RxMsgInfo;
import io.ipstack.lorawan.server.json.TxMsgInfo;


public class CoapConnector extends AbstractCoapServer {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;
	
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}
	
	private LorawanServer lorawanServer= null;
	
	private CoapClient coapClient;

	private HashSet<ConnectorEndpoint> endpoints= new HashSet<>();
	
	private HashSet<String> authTokens= new HashSet<>();
	
	
	public CoapConnector(int port, LorawanServer lorawanServer) throws IOException {
		super(port);
		this.lorawanServer= lorawanServer;
		coapClient= new CoapClient();
	}
	
	
	public void addEndpoint(ConnectorEndpoint endpoint) {
		endpoints.add(endpoint);
		if (endpoint.authToken!=null) authTokens.add(endpoint.authToken);
	}

	
	public void postData(RxMsgInfo rxMsgInfo) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (var endpoint: endpoints) {
					if (endpoint.soaddr!=null) {
						try {
							var coapURI= new CoapURI("coap://"+endpoint.soaddr+"/"+rxMsgInfo.devEUI+"/up/"+rxMsgInfo.fport);
							var options= new CoapOption[] { new UriQueryOption("t="+endpoint.authToken) };
							if (VERBOSE) log("postData(): "+coapURI);
							coapClient.request(CoapRequestMethod.POST,coapURI,options,CoapResource.FORMAT_APP_JSON,Json.toJSON(rxMsgInfo).getBytes(),new CoapResponseHandler() {
								@Override
								public void onResponse(CoapRequest req, CoapResponse resp) {
									if (VERBOSE) log("postData(): onResponse(): "+resp.getCodeAsString());
								}
								@Override
								public void onRequestFailure(CoapRequest req) {
									if (VERBOSE) log("postData(): onRequestFailure()");
								}					
							});
						}
						catch (Exception e) {
							if (VERBOSE) log("postData(): "+endpoint.soaddr+": "+e.getMessage());
						}
					}		
				}
			}	
		}).start();
	}
		
	
	@Override
	protected void handlePostRequest(CoapRequest req) {
		try {
			String uriPath= req.getRequestUriPath();
			String body= new String(req.getPayload());
			if (VERBOSE) log("handlePostRequest(): POST "+uriPath+": "+body);
			if (uriPath==null) {
				respond(req,CoapResponseCode._4_00_Bad_Request);
				return;
			}
			var authorized= false;
			String token= null;
			var uriQueries= req.getOptions(CoapOptions.UriQuery);
			for (var opt: uriQueries) {
				var uriQuery= new UriQueryOption(opt).getQuery().split("=");
				if (uriQuery.length>1 && uriQuery[0].trim().equals("t")) {
					token= uriQuery[1].trim();
					if (authTokens.contains(token)) authorized= true;
					break;
				}
			}
			if (!authorized) {
				if (VERBOSE) log("handlePostRequest(): unauthorized: "+token);
				respond(req,CoapResponseCode._4_01_Unauthorized);
				return;
			}
			while (uriPath.charAt(0)=='/') uriPath= uriPath.substring(1);
			String[] resourcePath= uriPath.split("/");
			var devEUI= resourcePath[0];
			var down= resourcePath[1];
			if (!down.equals(MqttConnector.TOPIC_DOWNLINK)) throw new RuntimeException("Invalid API path for downlink: "+uriPath);

			int fport= Integer.parseInt(resourcePath[2]);
			var txMsgInfo= Json.fromJSON(body,TxMsgInfo.class);
			String hexData= Bytes.toHex(txMsgInfo.payload);

			if (VERBOSE) log("handlePostRequest(): send data to "+devEUI+": "+hexData);
			System.out.println("data to "+devEUI+": "+hexData);
			lorawanServer.sendData(new EUI(devEUI),fport,txMsgInfo.payload);			
			respond(req,CoapResponseCode._2_04_Changed);
		}
		catch (Exception e) {
			e.printStackTrace();
			respond(req,CoapResponseCode._4_00_Bad_Request);
			return;
		}
	}	

}
