package io.ipstack.lorawan.semtech.json;

import java.util.Date;

import org.zoolu.util.DateFormat;

public class StatusInfo {

	String time; // UTC 'system' time of the gateway, ISO 8601 'expanded' format
	
	float lati; // GPS latitude of the gateway in degree (float, N is +)
	
	float long_; // GPS latitude of the gateway in degree (float, E is +)
	
	int alti; // GPS altitude of the gateway in meter RX (integer)
	
	int rxnb; // Number of radio packets received (unsigned integer)
	
	int rxok; // Number of radio packets received with a valid PHY CRC
	
	int rxfw; // Number of radio packets forwarded (unsigned integer)
	
	float ackr; // Percentage of upstream datagrams that were acknowledged
	
	int dwnb; // Number of downlink datagrams received (unsigned integer)
	
	int txnb; // Number of packets emitted (unsigned integer)

	
	/** Creates a new object. */
	public StatusInfo() {
		this(44.76511F,10.30784F);
	}

	/** Creates a new object. */
	public StatusInfo(float lati, float longi) {
		this(lati,longi,0,0,0,0,0.0F,0,0);
	}

	/** Creates a new object. */
	public StatusInfo(float lati, float long_, int alti, int rxnb, int rxok, int rxfw, float ackr, int dwnb, int txnb) {
		this.time= DateFormat.formatISO8601Expanded(new Date());
		this.lati= lati;
		this.long_= long_;
		this.alti= alti;
		this.rxnb= rxnb;
		this.rxok= rxok;
		this.rxfw= rxfw;
		this.ackr= ackr;
		this.dwnb= dwnb;
		this.txnb= txnb;
	}
	
	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}
	
	/**
	 * @return the lati
	 */
	public float getLati() {
		return lati;
	}
	
	/**
	 * @return the long
	 */
	public float getLong() {
		return long_;
	}

	
	/**
	 * @return the alti
	 */
	public int getAlti() {
		return alti;
	}
	
	/**
	 * @return the rxnb
	 */
	public int getRxnb() {
		return rxnb;
	}
	
	/**
	 * @return the rxok
	 */
	public int getRxok() {
		return rxok;
	}
	
	/**
	 * @return the rxfw
	 */
	public int getRxfw() {
		return rxfw;
	}
	
	/**
	 * @return the ackr
	 */
	public float getAckr() {
		return ackr;
	}

	/**
	 * @return the dwnb
	 */
	public int getDwnb() {
		return dwnb;
	}
	
	/**
	 * @return the txnb
	 */
	public int getTxnb() {
		return txnb;
	}

}
