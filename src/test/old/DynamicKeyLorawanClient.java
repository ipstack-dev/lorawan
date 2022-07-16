package test.old;

import java.io.IOException;
import java.net.SocketAddress;

import it.unipr.netsec.thingsstack.lorawan.device.LorawanClient;
import it.unipr.netsec.thingsstack.lorawan.device.LorawanClientListener;


public class DynamicKeyLorawanClient extends LorawanClient {

	public DynamicKeyLorawanClient(byte[] devEUI, String appCtxFile, byte[] joinEUI, byte[] appKey, int fPort, SocketAddress gwSoaddr, LorawanClientListener listener) throws IOException {
		super(devEUI,appCtxFile,joinEUI,appKey,fPort,gwSoaddr,listener);
	}
	
	public byte[] getAppKey() {
		//return appKey; // NOTE: appKey must be protected (not private/friend)
		return null;
	}

	public void setAppKey(byte[] appKey) {
		//this.appKey=appKey; // NOTE: appKey must be protected (not private/friend)
	}

}
