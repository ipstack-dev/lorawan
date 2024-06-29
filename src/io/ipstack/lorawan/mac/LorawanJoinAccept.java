package io.ipstack.lorawan.mac;


import org.zoolu.util.Bytes;


/** Join-Accept frame that is the Payload of a Join-Accept MAC message.
 */
public class LorawanJoinAccept {

	/** JoinNonce (3 bytes) */
	byte[] joinNonce;
	
	/** Home_NetID (3 bytes) */
	byte[] homeNetId;
	
	/** DevAddr (4 bytes) */
	byte[] devAddr;

	/** DLSettings (1 byte) */
	int dlSettings;

	/** RxDelay (1 byte) */
	int rxDelay;

	/** CFList (16 bytes), optional */
	byte[] cfList= null;

	
	/** Creates a new payload.
	 * @param joinNonce JoinNonce
	 * @param homeNetId Home_NetID
	 * @param devAddr DevAddr
	 * @param dlSettings DLSettings
	 * @param rxDelay RxDelay
	 * @param cfList CFList (optional) */
	public LorawanJoinAccept(byte[] joinNonce, byte[] homeNetId, byte[] devAddr, int dlSettings, int rxDelay, byte[] cfList) {
		this.joinNonce= joinNonce;
		this.homeNetId= homeNetId;
		this.devAddr= devAddr;
		this.dlSettings= dlSettings;
		this.rxDelay= rxDelay;
		this.cfList= cfList;
	}

	/** Creates a new payload.
	 * @param data the payload */
	public LorawanJoinAccept(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new payload.
	 * @param buf buffer containing the payload
	 * @param off offset within the buffer
	 * @param len payload length */
	public LorawanJoinAccept(byte[] buf, int off, int len) {
		joinNonce= Bytes.reverseOrderCopy(buf,off,3);
		homeNetId= Bytes.reverseOrderCopy(buf,off+3,3);
		devAddr= Bytes.reverseOrderCopy(buf,off+6,4);
		dlSettings= 0xff&buf[10];
		rxDelay= 0xff&buf[11];
		if (len>12) cfList=Bytes.copy(buf,off+12,len-12);
	}

	/** Gets payload length.
	 * @return the length */
	public int getLength() {
		int len= 12;
		if (cfList!=null && cfList.length>0) len+= cfList.length;
		return len;
	}

	/** Gets payload bytes.
	 * @return the bytes */
	public byte[] getBytes() {
		var data= new byte[getLength()];
		getBytes(data,0);
		return data;
	}

	/** Gets payload bytes.
	 * @param buf buffer where payload has to be written
	 * @param off offset within the buffer
	 * @return the payload length */
	public int getBytes(byte[] buf, int off) {
		Bytes.reverseOrderCopy(joinNonce,0,buf,off,3);
		Bytes.reverseOrderCopy(homeNetId,0,buf,off+3,3);
		Bytes.reverseOrderCopy(devAddr,0,buf,off+6,4);
		buf[off+10]= (byte)dlSettings;
		buf[off+11]= (byte)rxDelay;
		int len= 12;
		if (cfList!=null && cfList.length>0) {
			System.arraycopy(cfList,0,buf,off+12,cfList.length);
			len+= cfList.length;
		}
		return len;
	}

	/**
	 * @return the JoinNonce */
	public byte[] getJoinNonce() {
		return joinNonce;
	}
	
	/**
	 * @return the Home_NetID */
	public byte[] getHomeNetID() {
		return homeNetId;
	}

	/**
	 * @return the dDvAddr */
	public byte[] getDevAddr() {
		return devAddr;
	}

	/**
	 * @return the DLSettings */
	public int getDlSettings() {
		return dlSettings;
	}
	
	/**
	 * @return the RxDelay */
	public int getRxDelay() {
		return rxDelay;
	}
	
	/**
	 * @return the cfList */
	public byte[] getCfList() {
		return cfList;
	}
	
	@Override
	public String toString() {
		return toString(", ");
	}

	/** Gets a string representation of this object.
	 * Different field descriptions are separated by the given delimiter.
	 * @param delim the field delimiter
	 * @return the string representation */
	public String toString(String delim) {
		var sb= new StringBuffer();
		sb.append("DevAddr: ").append(Bytes.toHex(getDevAddr()));
		var homeNetId= Bytes.toHex(getHomeNetID());
		homeNetId+= homeNetId.equals("000013")? " (The Things Network)" : " (unknown)";
		sb.append(delim).append("Home_NetID: ").append(homeNetId);
		sb.append(delim).append("JoinNonce: ").append(Bytes.toHex(getJoinNonce()));
		sb.append(delim).append("CFList: ").append(cfList!=null? Bytes.toHex(cfList) : "null");
		
		return sb.toString();
	}

}
