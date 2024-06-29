package io.ipstack.lorawan.server;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.server.json.RxMsgInfo;


public interface LorawanServerListener {

	public void onConnectedGateway(LorawanServer server, EUI gwEUI);

	public void onJoinRequest(LorawanServer server, RxMsgInfo rxMsgInfo);
	
	public void onReceivedData(LorawanServer server, RxMsgInfo rxMsgInfo);
}
