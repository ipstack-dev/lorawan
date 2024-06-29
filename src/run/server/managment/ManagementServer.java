package run.server.managment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.zoolu.util.Bytes;
import org.zoolu.util.json.Json;
import org.zoolu.util.json.JsonArray;

import io.ipstack.http.HttpRequest;
import io.ipstack.http.HttpRequestHandle;
import io.ipstack.http.HttpRequestHandleProcessor;
import io.ipstack.http.HttpServer;
import io.ipstack.http.uri.Parameter;
import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.server.LorawanServer;
import run.dragino.DraginoLHT65Encoder;


/** HTTP server that allows a user to manage a LoRaWAN Server through a REST API.
 */
public class ManagementServer extends HttpRequestHandleProcessor {

	private HttpServer httpServer;
	
	private LorawanServer lorawanServer= null;
	
	HashMap<EUI,DeviceInfo> devInfoMap= new HashMap<>(); // maps DevEUI -> DeviceInfo


	
	/** Creates a new management server.
	 * @param port server port
	 * @param lorawanServer the LoRaWAN server
	 * @param deviceFile file containing device information */
	public ManagementServer(int port, LorawanServer lorawanServer, String deviceFile) throws IOException {
		super(new String[] {HttpRequest.GET, HttpRequest.POST, HttpRequest.DELETE});
		this.lorawanServer= lorawanServer;
		httpServer= new HttpServer(port,this);
		if (deviceFile!=null) {
			var devices= (DeviceInfo[])Json.fromJSONArrayFile(new File(deviceFile),DeviceInfo.class);
			for (DeviceInfo devInfo: devices) devInfoMap.put(new EUI(devInfo.devEUI),devInfo);
		}
	}
	
	
	@Override
	protected boolean processGet(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle) {
		// GET devices
		if (resourcePath.length==1 && resourcePath[0].equals("devices")) {
			var devEUIs= lorawanServer.getDevices();
			String[] devices= new String[devEUIs.length];
			for (int i=0; i<devices.length; ++i) devices[i]= devEUIs[i].hex();
			reqHandle.setResponseCode(200);
			reqHandle.setResponseContentType("application/json");
			reqHandle.setResponseBody(new JsonArray(devices).toString().getBytes());
			return true;
		}
		// GET device/<devEUI>
		if (resourcePath.length==2 && resourcePath[0].equals("device")) {
			var appContext= lorawanServer.getAppContext(new EUI(resourcePath[1]));
			if (appContext==null) reqHandle.setResponseCode(404);
			else {
				reqHandle.setResponseCode(200);
				reqHandle.setResponseContentType("application/json");
				var json= Json.toJSON(appContext);
				var appkey= Bytes.toHex(appContext.appKey());
				json= json.replaceAll(appkey,"********************************");
				reqHandle.setResponseBody(json.getBytes());
			}
			return true;
		}
		// GET device/<devEUI>/up
		if (resourcePath.length==3 && resourcePath[0].equals("device") && resourcePath[2].equals("up")) {
			var devEUI= new EUI(resourcePath[1]);
			var rxPktMetadata= lorawanServer.getLastPacket(devEUI);
			if (rxPktMetadata==null) reqHandle.setResponseCode(404);
			else {
				reqHandle.setResponseCode(200);
				reqHandle.setResponseContentType("application/json");
				var json= Json.toJSON(rxPktMetadata);
				var devInfo= devInfoMap.get(devEUI);
				if (devInfo!=null && devInfo.type!=null && devInfo.type.equalsIgnoreCase("LHT65")) {
					var decoded_payload= DraginoLHT65Encoder.decodePayload(rxPktMetadata.payload);
					json= json.replaceFirst("\"rxPktInfo\"","\"decoded_payload\":\""+decoded_payload+"\",\"rxPktInfo\"");
				}
				reqHandle.setResponseBody(json.getBytes());
			}
			return true;
		}
		// GET status
		if (resourcePath.length==1 && resourcePath[0].equals("status")) {
			reqHandle.setResponseCode(200);
			reqHandle.setResponseContentType("application/json");
			reqHandle.setResponseBody(lorawanServer.getStatus().getBytes());
			return true;
		}	
		return false;
	}
	
	
	@Override
	protected boolean processPost(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Override
	protected boolean processPut(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle) {
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	protected boolean processDelete(String[] resourcePath, Parameter[] queryParams, HttpRequestHandle reqHandle) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/** Closes the server. */
	public void close() {
		httpServer.close();
	}


}
