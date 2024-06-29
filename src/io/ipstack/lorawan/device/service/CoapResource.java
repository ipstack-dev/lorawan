package io.ipstack.lorawan.device.service;


import java.net.SocketException;
import java.net.URISyntaxException;

import org.zoolu.util.Bytes;

import io.ipstack.coap.client.CoapClient;
import io.ipstack.coap.message.CoapRequestMethod;
import io.ipstack.coap.message.CoapResponse;
import io.ipstack.coap.provider.CoapURI;


/** Data is obtained from a remote CoAP server.
 */
public class CoapResource implements DataService {
	
	/** Default CoAP request timeout */
	public long TIMEOUT= 2000;
	
	/** CoAP URL of the server resource */
	String coapResource;
	
	/** CoAP client */
	CoapClient coapClient;

	
	/** Creates a new service. 
	 * @param coapResource CoAP URL of the server resource
	 * @throws SocketException 
	 */
	public CoapResource(String[] args) throws SocketException {
		this(args[0]);
	}

	
	/** Creates a new service. 
	 * @param coapResource CoAP URL of the server resource
	 * @throws SocketException 
	 */
	public CoapResource(String coapResource) throws SocketException {
		this.coapResource= coapResource;
		coapClient= new CoapClient();
		coapClient.setTimeout(TIMEOUT);
	}
	

	@Override
	public byte[] getData() {
		try {
			//CoapClient coapClient=new CoapClient();
			//coapClient.setTimeout(TIMEOUT);
			var resp= coapClient.request(CoapRequestMethod.GET,new CoapURI(coapResource),-1);
			return resp!=null? resp.getPayload() : null;
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public void setData(byte[] data) {
		try {
			//CoapClient coapClient= new CoapClient();
			//coapClient.setTimeout(TIMEOUT);
			coapClient.request(CoapRequestMethod.PUT,new CoapURI(coapResource),-1,data);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
