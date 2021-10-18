package it.unipr.netsec.thingsstack.lorawan.semtech.json;


import org.zoolu.util.Base64;
import org.zoolu.util.json.JsonUtils;


/** Semtech structure containing a RF packet to be emitted and associated metadata.
 */
public class TxPacketInfo {
	
	boolean imme; // Send packet immediately (will ignore tmst & time)

	//long tmst; // Send packet on a certain timestamp value (will ignore time)

	long tmms; // Send packet at a certain GPS time (GPS synchronization required)

	float freq; // TX central frequency in MHz (unsigned float, Hz precision)

	int rfch; // Concentrator "RF chain" used for TX (unsigned integer)

	int powe; // TX output power in dBm (unsigned integer, dBm precision)

	String modu; // Modulation identifier "LORA" or "FSK"

	String datr; // LoRa datarate identifier (eg. SF12BW500)

	//long datr; // FSK datarate (unsigned, in bits per second)

	String codr; // LoRa ECC coding rate identifier

	//float fdev; // FSK frequency deviation (unsigned integer, in Hz) 

	boolean ipol; // Lora modulation polarization inversion

	//int prea; // RF preamble size (unsigned integer)

	int size; // RF packet payload size in bytes (unsigned integer)

	boolean ncrc; // If true, disable the CRC of the physical layer (optional)

	String data; // Base64 encoded RF packet payload, padding optional


	/** Creates a new empty object. */
	public TxPacketInfo() {
	}
		
	/** Creates a new object. */
	public TxPacketInfo(byte[] data) {
		this(10000,868.1F,0,14,"SF7BW125","4/5",data);
	}
		
	/** Creates a new object. */
	public TxPacketInfo(long tmms, float freq, int rfch, int powe, String datr, String codr, byte[] data) {
		this.imme=false;
		this.tmms=tmms;
		this.freq=freq;
		this.rfch=rfch;
		this.powe=powe;
		this.modu="LORA";
		this.datr=datr;
		this.codr=codr;
		this.ipol=true;
		this.ncrc=true;
		this.size=data.length;
		this.data=Base64.encode(data);
	}
	
	/**
	 * @return the imme
	 */
	public boolean isImme() {
		return imme;
	}
	
	/**
	 * @return the tmms
	 */
	public long getTmms() {
		return tmms;
	}
	
	/**
	 * @return the freq
	 */
	public float getFreq() {
		return freq;
	}
	
	/**
	 * @return the rfch
	 */
	public int getRfch() {
		return rfch;
	}
	
	/**
	 * @return the powe
	 */
	public int getPowe() {
		return powe;
	}
	
	/**
	 * @return the modu
	 */
	public String getModu() {
		return modu;
	}
	
	/**
	 * @return the datr
	 */
	public String getDatr() {
		return datr;
	}
	
	/**
	 * @return the codr
	 */
	public String getCodr() {
		return codr;
	}
	
	/**
	 * @return the ipol
	 */
	public boolean isIpol() {
		return ipol;
	}
	
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * @return the ncrc
	 */
	public boolean isNcrc() {
		return ncrc;
	}
	
	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return JsonUtils.toJson(this);
	}

}
