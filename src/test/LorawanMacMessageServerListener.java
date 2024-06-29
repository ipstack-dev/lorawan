package test;

import io.ipstack.lorawan.mac.LorawanJoinRequestMacMessage;

public interface LorawanMacMessageServerListener {

	public void onJoinRequest(LorawanMacMessageServer server, LorawanJoinRequestMacMessage joinReq);
	
	public void onReceivedData(LorawanMacMessageServer server, byte[] data);
}
