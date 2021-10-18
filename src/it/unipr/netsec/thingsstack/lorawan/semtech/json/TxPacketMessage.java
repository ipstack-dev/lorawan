package it.unipr.netsec.thingsstack.lorawan.semtech.json;


import org.zoolu.util.json.JsonUtils;


public class TxPacketMessage {
	
	TxPacketInfo txpk;

	/** Creates a new empty message. */
	public TxPacketMessage() {
	}
		
	/** Creates a new message. */
	public TxPacketMessage(TxPacketInfo txpk) {
		this.txpk=txpk;
	}
	
	/**
	 * @return the txpk
	 */
	public TxPacketInfo getTxpk() {
		return txpk;
	}

	@Override
	public String toString() {
		return JsonUtils.toJson(this);
	}

}
