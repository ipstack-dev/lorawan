package io.ipstack.lorawan.semtech.json;

import org.zoolu.util.json.Json;


public class StatusMessage {
	
	StatusInfo stat;

	/** Creates a new empty message. */
	public StatusMessage() {
	}
		
	/** Creates a new message. */
	public StatusMessage(StatusInfo stat) {
		this.stat= stat;
	}
	
	/**
	 * @return the status
	 */
	public StatusInfo getStatus() {
		return stat;
	}

	@Override
	public String toString() {
		return Json.toJSON(this);
	}

}
