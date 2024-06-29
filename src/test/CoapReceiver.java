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

import io.ipstack.coap.message.CoapRequest;
import io.ipstack.coap.message.CoapResponseCode;
import io.ipstack.coap.option.CoapOptions;
import io.ipstack.coap.option.UriQueryOption;
import io.ipstack.coap.provider.CoapProvider;
import io.ipstack.coap.server.CoapServer;
import io.ipstack.lorawan.server.json.RxMsgInfo;

import java.net.SocketException;


/** Ready-to-use simple stateful CoAP server.
 * It handles CoAP GET, PUT, and DELETE requests statefully, automatically handling request and response retransmissions.
 * <p>
 * It supports resource observation (RFC 7641) and blockwise transfer (RFC 7959). 
 */
public class CoapReceiver {

	private static boolean VERBOSE= false;
	
	private static void log(String str) {
		DefaultLogger.log(LoggerLevel.INFO,CoapReceiver.class,str);
	}

	
	/** The main method. 
	 * @throws SocketException */
	public static void main(String[] args) throws SocketException {
		Flags flags= new Flags(args);
		boolean help= flags.getBoolean("-h","prints this help");
		int local_port= flags.getInteger("-p",CoapProvider.DEFAUL_PORT,"<port>","server UDP port (default port is "+CoapProvider.DEFAUL_PORT+")");
		VERBOSE= flags.getBoolean("-v","verbose level");
		boolean exit= flags.getBoolean("-x","exits when 'return' is pressed");
		
		if (help) {
			System.out.println(flags.toUsageString(CoapReceiver.class.getSimpleName()));
			System.exit(0);
		}
		if (VERBOSE) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.DEBUG));
		}

		CoapServer server=new CoapServer(local_port) {
			@Override
			protected void handlePostRequest(CoapRequest req) {
				var uriQueries= req.getOptions(CoapOptions.UriQuery);
				for (var opt: uriQueries) {
					var uriQuery= new UriQueryOption(opt).getQuery().split("=");
					if (uriQuery.length>1 && uriQuery[0].trim().equals("t")) {
						var token= uriQuery[1].trim();
						if (VERBOSE) {
							char[] masked= token.toCharArray();
							for (int i=2; i<masked.length-2; ++i) masked[i]= '*';
							log("auth token: "+new String(masked));
						}
						break;
					}
				}
				String body= new String(req.getPayload());
				if (VERBOSE) log("POST "+req.getRequestUriPath()+" "+body);
				var rxMsgInfo= Json.fromJSON(body,RxMsgInfo.class);
				respond(req,CoapResponseCode._2_04_Changed);				
				System.out.println("data from "+rxMsgInfo.devEUI+": "+Bytes.toHex(rxMsgInfo.payload));
			}			
		};
			
		if (exit) {
			SystemUtils.readLine();
			server.halt();
			System.exit(0);
		}
	}

}
