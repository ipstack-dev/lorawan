package run.server.connector;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.zoolu.util.Bytes;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;

import io.ipstack.http.HttpClient;
import io.ipstack.http.HttpRequest;
import io.ipstack.http.HttpRequestHandle;
import io.ipstack.http.HttpRequestURL;
import io.ipstack.http.HttpServer;
import io.ipstack.http.uri.AbsolutePath;
import io.ipstack.http.uri.Parameter;
import io.ipstack.http.uri.Query;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.server.LorawanServer;
import io.ipstack.lorawan.server.json.RxMsgInfo;
import io.ipstack.lorawan.server.json.TxMsgInfo;


public class HttpConnector {
	
	/** Verbose mode */
	public static boolean VERBOSE= false;
	
	private void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,this.getClass(),str);
	}
	
	
	private LorawanServer lorawanServer= null;
	
	private HttpClient httpClient;

	private HashSet<ConnectorEndpoint> endpoints= new HashSet<>();
	
	private HashSet<String> authTokens= new HashSet<>();
	
	
	public HttpConnector(int port, LorawanServer lorawanServer) throws IOException {
		this.lorawanServer= lorawanServer;
		new HttpServer(port,this::processHttpRequest);
		httpClient= new HttpClient();
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
							if (VERBOSE) log("postData(): "+endpoint.soaddr);
							HashMap<String,String> hdr= new HashMap<>();
							hdr.put("Authorization","Bearer "+endpoint.authToken);
							var resp= httpClient.request(HttpRequest.POST,"http://"+endpoint.soaddr+"/"+rxMsgInfo.devEUI+"/up/"+rxMsgInfo.fport,hdr,"application/json",Json.toJSON(rxMsgInfo).getBytes());
							if (VERBOSE) log("postData(): "+endpoint.soaddr+": "+resp.getFirstLine());
						}
						catch (IOException e) {
							if (VERBOSE) log("postData(): "+endpoint.soaddr+": "+e.getMessage());
						}	
					}		
				}
			}	
		}).start();
	}
		
	
	private void processHttpRequest(HttpRequestHandle req_handle) {
		try {
			String method=req_handle.getMethod();
			HttpRequestURL request_url=req_handle.getRequestURL();
			if (VERBOSE) log("processHttpRequest(): "+method+" "+request_url);
			if (request_url==null) {
				req_handle.setResponseCode(400);
				return;
			}
			var authorized= false;
			var authHdr= req_handle.getRequest().getHeaderField("Authorization");
			String token= (authHdr!=null && authHdr.trim().startsWith("Bearer "))? authHdr.trim().substring(7).trim() : null;
			if (authTokens.contains(token)) authorized= true;
			if (!authorized) {
				if (VERBOSE) log("processHttpRequest(): unauthorized: "+token);
				req_handle.setResponseCode(401);
				return;
			}
			if (method.equals(HttpRequest.OPTIONS) && processOption(request_url,req_handle)) return;
			// else
			String[] resource_path=new AbsolutePath(request_url.getAbsPath()).getPath();
			Parameter[] query_params=new Query(request_url.getQuery()).getQueryParameters();
			// else
			if (resource_path==null || resource_path.length==0) {
				if (VERBOSE) log("processHttpRequest(): invalid path: "+resource_path);
				req_handle.setResponseCode(404);
				return;
			}
			if (!method.equals("POST")) {
				if (VERBOSE) log("processHttpRequest(): invalid method: "+method);
				req_handle.setResponseCode(405);
				return;
			}
			// else
			//var appEUI= resource_path[0];
			var devEUI= resource_path[0];
			var down= resource_path[1];
			if (!down.equals(MqttConnector.TOPIC_DOWNLINK)) throw new RuntimeException("Invalid API path for downlink: "+request_url.getAbsPath());
			int fport= Integer.parseInt(resource_path[2]);
			var txMsgInfo= Json.fromJSON(new String(req_handle.getRequest().getBody()),TxMsgInfo.class);
			String hexData= Bytes.toHex(txMsgInfo.payload);
			if (VERBOSE) log("processHttpRequest(): send data to "+devEUI+": "+hexData);
			System.out.println("data to "+devEUI+": "+hexData);
			lorawanServer.sendData(new EUI(devEUI),fport,txMsgInfo.payload);
			req_handle.setResponseCode(200);
		}
		catch (Exception e) {
			e.printStackTrace();
			req_handle.setResponseCode(400);
			return;
		}
	}
	
	
	private boolean processOption(HttpRequestURL request_url, HttpRequestHandle req_handle) {
		req_handle.setResponseCode(200);
		req_handle.setResponseHeaderField("Allow",HttpRequest.GET+","+HttpRequest.POST+","+HttpRequest.PUT+","+HttpRequest.DELETE+","+HttpRequest.OPTIONS);
		return true;
	}


}
