package io.ipstack.lorawan.semtech.json;

import java.util.Date;

import org.zoolu.util.Base64;
import org.zoolu.util.DateFormat;


/** Semtech structure containing a received RF packet and associated metadata.
 */
public class RxPacketInfo {
	
	static class EPOCH {
		//public static long TIME= System.currentTimeMillis();
		public static long NANOTIME= System.nanoTime();
	}
	
	String time; // UTC time of pkt RX, us precision, ISO 8601 'compact' format
	
	//long tmms; // GPS time of pkt RX, number of milliseconds since 06.Jan.1980
	
	long tmst; // Internal timestamp of "RX finished" event (32b unsigned)
	
	float freq; // RX central frequency in MHz (unsigned float, Hz precision)
	
	int chan; // Concentrator "IF" channel used for RX (unsigned integer)
	
	int rfch; // Concentrator "RF chain" used for RX (unsigned integer)
	
	int stat; // CRC status: 1 = OK, -1 = fail, 0 = no CRC
	
	String modu; // Modulation identifier "LORA" or "FSK"
	
	String datr; // LoRa datarate identifier (eg. SF12BW500)
	
	//long datr; // FSK datarate (unsigned, in bits per second)
	
	String codr; // LoRa ECC coding rate identifier
	
	int rssi; // RSSI in dBm (signed integer, 1 dB precision)
	
	float lsnr; // Lora SNR ratio in dB (signed float, 0.1 dB precision)
	
	int size; // RF packet payload size in bytes (unsigned integer)
	
	String data; // Base64 encoded RF packet payload, padded
	

	/** Creates a new empty object. */
	public RxPacketInfo() {
	}
	
	/** Creates a new object. */
	public RxPacketInfo(byte[] data) {
		this(868.1F,0,1,"SF7BW125","4/5",-65,7.8F,data);
	}
	
	/** Creates a new object. */
	public RxPacketInfo(float freq, int chan, int rfch, String datr, String codr, int rssi, float lsnr, byte[] data) {
		this.time=DateFormat.formatISO8601Compact(new Date());
		//Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		//calendar.clear();
		//calendar.set(1980,Calendar.JANUARY,6);
		//this.tmms= (System.currentTimeMillis()-calendar.getTimeInMillis());
		//this.tmst= System.currentTimeMillis()-EPOCH.TIME;
		long epoch= EPOCH.NANOTIME; // to force the initialization of EPOCH
		this.tmst= ((System.nanoTime()-epoch)/1000)&0xffffffffL;
		this.freq= freq;
		this.chan= chan;
		this.rfch= rfch;
		this.stat= 1;
		this.modu= "LORA";
		this.datr= datr;
		this.codr= codr;
		this.rssi= rssi;
		this.lsnr= lsnr;
		this.size= data.length;
		this.data= Base64.encode(data);
	}
	
	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}
	
	/**
	 * @return the tmst
	 */
	public long getTmst() {
		return tmst;
	}
	
	/**
	 * @return the freq
	 */
	public float getFreq() {
		return freq;
	}

	/**
	 * @return the chan
	 */
	public int getChan() {
		return chan;
	}
	
	/**
	 * @return the rfch
	 */
	public int getRfch() {
		return rfch;
	}
	
	/**
	 * @return the stat
	 */
	public int getStat() {
		return stat;
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
	 * @return the rssi
	 */
	public int getRssi() {
		return rssi;
	}
	
	/**
	 * @return the lsnr
	 */
	public float getLsnr() {
		return lsnr;
	}
	
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}
	
}
