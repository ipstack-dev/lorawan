/*
 * Copyright (c) 2018 NetSec Lab - University of Parma (Italy)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND. IN NO EVENT
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package test;

import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.coap.client.CoapClient;
import io.ipstack.coap.client.CoapResponseHandler;
import io.ipstack.coap.message.CoapRequest;
import io.ipstack.coap.message.CoapRequestMethod;
import io.ipstack.coap.message.CoapResponse;
import io.ipstack.coap.option.CoapOption;
import io.ipstack.coap.option.UriQueryOption;
import io.ipstack.coap.provider.CoapURI;
import io.ipstack.coap.server.CoapResource;
import io.ipstack.lorawan.server.json.TxMsgInfo;
import run.server.Server;

import java.net.SocketException;
import java.net.URISyntaxException;


/** Simple CoAP client.
 * It may send CoAP GET, PUT, and DELETE requests, or register for observing a remote resource.
 * <p>
 * It supports resource observation (RFC 7641) and blockwise transfer (RFC 7959). 
 */
public class CoapSender {

	private static boolean VERBOSE= false;
	
	private static void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,Server.class,str);
	}

	
	/** The main method.
	 * @param args command-line arguments 
	 * @throws URISyntaxException 
	 * @throws SocketException */
	public static void main(String[] args) throws URISyntaxException, SocketException {
		Flags flags= new Flags(args);
		boolean help= flags.getBoolean("-h","prints this help");	
		VERBOSE= flags.getBoolean("-v","verbose level");
		//String[] params= flags.getRemainingStrings(false,"serverAddr authToken devEUI fport hexdata",null);
		var serverAddr= flags.getString(Flags.PARAM,null,"server","server address (and port), eg. \"127.0.0.1:5000\"");
		var authToken= flags.getString(Flags.PARAM,null,"token","authorization token");
		var devEUI= flags.getString(Flags.PARAM,null,"devEUI","device EUI");
		int fport= flags.getInteger(Flags.PARAM,1,"fport","fport value");
		var data= Bytes.fromHex(flags.getString(Flags.PARAM,null,"data","data in hexadecimal"));
		var txMsgInfo= Json.toJSON(new TxMsgInfo(false,data));
				
		if (help) {
			System.out.println(flags.toUsageString(SimpleCoapServer.class.getSimpleName()));
			System.exit(0);
		}
		
		if (VERBOSE) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
		}
				
		var coapClient= new CoapClient();
		var coapURI= new CoapURI("coap://"+serverAddr+"/"+devEUI+"/down/"+fport);
		if (VERBOSE) log("coapURI: "+coapURI);
		var options= new CoapOption[] { new UriQueryOption("t="+authToken) };
		coapClient.request(CoapRequestMethod.POST,coapURI,options,CoapResource.FORMAT_APP_OCTECT_STREAM,txMsgInfo.getBytes(),new CoapResponseHandler() {
			@Override
			public void onResponse(CoapRequest req, CoapResponse resp) {
				if (VERBOSE) log("onResponse(): "+resp.getCodeAsString());
			}
			@Override
			public void onRequestFailure(CoapRequest req) {
				if (VERBOSE) log("onRequestFailure()");
			}					
		});
		
		SystemUtils.exitAfter(3000);
	}

}
