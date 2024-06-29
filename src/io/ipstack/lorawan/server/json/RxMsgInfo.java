package io.ipstack.lorawan.server.json;

import java.util.Arrays;
import java.util.Date;

import org.zoolu.util.DateFormat;

import io.ipstack.lorawan.mac.EUI;

/** Metadata associated to a packet received by the server from one or more gateways.
 * It contains a timestamp, the DevEUI, decoded payload, together with one or more RxMetadata(s) with information received from each gateway for the same packet. 
 */
public class RxMsgInfo {

	public long timestamp;
	public String time;
	public String devEUI;
	public String macMsg;
	public int fport= 0;
	public byte[] payload= null;
	public RxMetadata[] rxMetadata;
	
	protected RxMsgInfo() {}
	
	public RxMsgInfo(long timestamp, EUI devEUI, String macMsg, RxMetadata[] rxMetadata) {
		this.timestamp= timestamp;
		this.time= DateFormat.formatISO8601Compact(new Date(timestamp));
		this.devEUI= devEUI.hex();
		this.macMsg= macMsg;
		this.rxMetadata= rxMetadata;
	}
	
	public EUI getDevEUI() {
		return new EUI(devEUI);
	}
	
	public void addRxMetadata(RxMetadata rxMetadata) {
		this.rxMetadata= Arrays.copyOf(this.rxMetadata,this.rxMetadata.length+1);
		this.rxMetadata[this.rxMetadata.length-1]= rxMetadata;
	}

}
