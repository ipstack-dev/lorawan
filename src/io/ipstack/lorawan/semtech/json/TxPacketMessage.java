package io.ipstack.lorawan.semtech.json;

import org.zoolu.util.json.Json;



/**
 * Semtech txpk message.
 */
public class TxPacketMessage {
	
	TxPacketInfo txpk;

	/** Creates a new empty message. */
	public TxPacketMessage() {
	}
		
	/** Creates a new message. */
	public TxPacketMessage(TxPacketInfo txpk) {
		this.txpk= txpk;
	}
	
	/**
	 * @return the txpk
	 */
	public TxPacketInfo getTxpk() {
		return txpk;
	}

	@Override
	public String toString() {
		return Json.toJSON(this);
	}

}
