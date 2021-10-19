package it.unipr.netsec.thingsstack.lorawan.device.service;


import java.net.SocketException;
import java.net.URISyntaxException;

import org.zoolu.util.Bytes;

import it.unipr.netsec.thingsstack.coap.client.CoapClient;
import it.unipr.netsec.thingsstack.coap.message.CoapRequestMethod;
import it.unipr.netsec.thingsstack.coap.message.CoapResponse;
import it.unipr.netsec.thingsstack.coap.provider.CoapURI;


/** Data is obtained from a remote CoAP server.
 */
public class CoapData implements Service {
	
	/** Default CoAP request timeout */
	public long TIMEOUT=2000;
	
	/** CoAP URL of the server resource */
	String coapResource;
	
	/** CoAP client */
	CoapClient coapClient;

	
	/** Creates a new service. 
	 * @param coapResource CoAP URL of the server resource
	 * @throws SocketException 
	 */
	public CoapData(String[] args) throws SocketException {
		this(args[0]);
	}

	
	/** Creates a new service. 
	 * @param coapResource CoAP URL of the server resource
	 * @throws SocketException 
	 */
	public CoapData(String coapResource) throws SocketException {
		this.coapResource=coapResource;
		coapClient=new CoapClient();
		coapClient.setTimeout(TIMEOUT);
	}
	

	@Override
	public byte[] getData() {
		try {
			//CoapClient coapClient=new CoapClient();
			//coapClient.setTimeout(TIMEOUT);
			CoapResponse resp=coapClient.request(CoapRequestMethod.GET,new CoapURI(coapResource),-1);
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
			//CoapClient coapClient=new CoapClient();
			//coapClient.setTimeout(TIMEOUT);
			coapClient.request(CoapRequestMethod.PUT,new CoapURI(coapResource),-1,data);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
