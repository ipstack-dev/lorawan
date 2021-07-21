package it.unipr.netsec.ipstack.lorawan;



/** LoraWAN Proprietary message.
 */
public class LorawanProprietaryMessage extends LorawanMacMessage {

	/** Creates a new message.
	 * @param data the buffer containing the packet */
	public LorawanProprietaryMessage(byte[] data) {
		this(data,0,data.length);
	}

	/** Creates a new message.
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet length */
	public LorawanProprietaryMessage(byte[] buf, int off, int len) {
		super(buf,off,len);
		if (type!=TYPE_PROPRIETARY) throw new RuntimeException("It isn't a Proprietary message ("+type+")");
	}
	
}
