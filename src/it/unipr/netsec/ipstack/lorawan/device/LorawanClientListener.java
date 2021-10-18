package it.unipr.netsec.ipstack.lorawan.device;


public interface LorawanClientListener {

	public void onJoinAccept(LorawanClient client);
	
	public void onReceivedData(LorawanClient client, byte[] data);
}
