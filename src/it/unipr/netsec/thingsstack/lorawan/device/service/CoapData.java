package it.unipr.netsec.thingsstack.lorawan.device.service;


import java.net.SocketException;
import java.net.URISyntaxException;

import org.zoolu.util.Bytes;

import it.unipr.netsec.mjcoap.coap.client.CoapClient;
import it.unipr.netsec.mjcoap.coap.message.CoapRequestMethod;
import it.unipr.netsec.mjcoap.coap.message.CoapResponse;
import it.unipr.netsec.mjcoap.coap.provider.CoapURI;


/** Data is obtained from a remote CoAP server.
 */
public class CoapData implements Service {
	
	/** CoAP URL of the server resource */
	String coapResource;
	
	/** CoAP client */
	//CoapClient coapClient;

	
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
		//coapClient=new CoapClient();
	}
	

	@Override
	public byte[] getData() {
		try {
			CoapResponse resp=new CoapClient().request(CoapRequestMethod.GET,new CoapURI(coapResource),-1);
			return resp.getPayload();
		}
		catch (URISyntaxException | SocketException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public void setData(byte[] data) {
		try {
			new CoapClient().request(CoapRequestMethod.PUT,new CoapURI(coapResource),-1,data);
		}
		catch (URISyntaxException | SocketException e) {
			e.printStackTrace();
		}
	}

}
