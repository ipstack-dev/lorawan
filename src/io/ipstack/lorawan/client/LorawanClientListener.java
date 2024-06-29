package io.ipstack.lorawan.client;


public interface LorawanClientListener {

	public void onJoinAccept(LorawanClient client);
	
	public void onReceivedData(LorawanClient client, byte[] data);
}
