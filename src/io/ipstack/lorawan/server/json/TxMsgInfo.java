package io.ipstack.lorawan.server.json;



public class TxMsgInfo {
	
	public boolean confirmed= false;
	//public int fport;
	public byte[] payload;
	
	protected TxMsgInfo() {
	}

	public TxMsgInfo(boolean confirmed, byte[] payload) {
		this.confirmed=confirmed;
		this.payload=payload;
	}

}
