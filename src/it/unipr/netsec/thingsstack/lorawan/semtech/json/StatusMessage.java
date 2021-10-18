package it.unipr.netsec.thingsstack.lorawan.semtech.json;


import org.zoolu.util.json.JsonUtils;


public class StatusMessage {
	
	StatusInfo status;

	/** Creates a new empty message. */
	public StatusMessage() {
	}
		
	/** Creates a new message. */
	public StatusMessage(StatusInfo status) {
		this.status=status;
	}
	
	/**
	 * @return the status
	 */
	public StatusInfo getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return JsonUtils.toJson(this);
	}

}
