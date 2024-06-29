package run.dragino;


import java.util.ArrayList;

import org.zoolu.util.Bytes;
import org.zoolu.util.json.JsonMember;
import org.zoolu.util.json.JsonNumber;
import org.zoolu.util.json.JsonObject;
import org.zoolu.util.json.JsonString;


/** Dragino LHT65 payload.
 */
public class DraginoLHT65Payload {

	/** Sensor E1, Temperature Sensor */
	static final int Sensor_E1_Temperature= 0x01;

	/** Sensor E4, Interrupt Sensor */
	static final int Sensor_E4_Interrupt= 0x04;

	/** Sensor E5, Illumination Sensor */
	static final int Sensor_E5_Illumination= 0x05;

	/** Sensor E6, ADC Sensor */
	static final int Sensor_E6_ADC= 0x06;

	/** Sensor E7, Counting Senor */
	static final int Sensor_E7_Counting= 0x07;

	
	/** Battery info (2 bytes) */
	int batt;
	
	/** Built-In temperature (2 bytes) */
	int temperature;
	
	/** Built-in humidity (2 bytes) */
	int humidity;
	
	/** External sensor type (1 byte) */
	int extType;
	
	/** External sensor value (4 bytes) */
	long extValue;
	
	
	/** Creates a new payload.
	 * @param battery battery voltage [V]
	 * @param temperature temperature [C]
	 * @param humidity humidity [%] */
	public DraginoLHT65Payload(double battery, double temperature, int humidity) {
		this(battery,temperature,humidity,0x01,0x7fff7fff);
	}

	
	/** Creates a new payload.
	 * @param battery battery voltage [V]
	 * @param temperature temperature [C]
	 * @param humidity humidity [%]
	 * @param extType external sensor type
	 * @param extValue external sensor value */
	public DraginoLHT65Payload(double battery, double temperature, int humidity, int extType, long extValue) {
		this.batt= ((battery>=2.65?0b11:battery>=2.55?0b10:battery>=2.50?0b01:0b00)<<14)|((int)(battery*1000))&0x3fff;
		this.temperature= (int)(temperature*100);
		this.humidity= (int)(humidity*10);
		this.extType= extType;
		this.extValue= extValue;
	}

	
	/** Creates a new payload.
	 * @param payload the payload */
	public DraginoLHT65Payload(byte[] payload) {
		this(payload,0);
	}

	
	/** Creates a new payload.
	 * @param buf buffer containing the payload
	 * @param off offset within the buffer */
	public DraginoLHT65Payload(byte[] buf, int off) {
		batt= Bytes.toInt16(buf,off);
		temperature= Bytes.toInt16(buf,off+2);
		humidity= Bytes.toInt16(buf,off+4);
		extType= 0xff&buf[off+6];
		extValue= Bytes.toInt32(buf,off+7);
	}

	
	/** Gets payload data.
	 * @return the data */
	public byte[] getBytes() {
		byte[] data= new byte[11];
		getBytes(data,0);
		return data;
	}

	
	/** Gets payload data.
	 * @param buf buffer where data has to be written
	 * @param off offset within the buffer
	 * @return the payload length */
	public int getBytes(byte[] buf, int off) {
		Bytes.fromInt16(batt,buf,off);
		Bytes.fromInt16(temperature,buf,off+2);
		Bytes.fromInt16(humidity,buf,off+4);
		buf[off+6]= (byte)extType;
		Bytes.fromInt32(extValue,buf,off+7);
		return 11;
	}

	
	/**
	 * @return the battery value */
	/*public int getBattery() {
		return batt;
	}*/

	
	/**
	 * @return the battery info */
	public String getBatteryInfo() {
		switch ((batt>>14)&0b11) {
		case 0b00 : return "Ultra Low";// (BAT <= 2.50v)";
		case 0b01 : return "Low";// (2.50v <= BAT <= 2.55v)";
		case 0b10 : return "OK";// (2.55v <= BAT <= 2.65v)";
		case 0b11 : return "Good";// (BAT >= 2.65v)";
		}
		return null; // never
	}

	
	/**
	 * @return the battery voltage [V] */
	public float getBatteryVoltage() {
		return (batt&0x3fff)/1000.0F;
	}

	
	/**
	 * @return the temperature [C] */
	public float getTemperature() {
		return ((short)temperature)/100.0F;
	}

	
	/**
	 * @return the humidity [%] */
	public float getHumidity() {
		return humidity/10.0F;
	}

	
	/**
	 * @return the external sensor type */
	public int getExtType() {
		return extType;
	}

	
	/**
	 * @return the external sensor value */
	public long getExternalValue() {
		return extValue;
	}

	/**
	 * @return the external sensor info */
	public String getExternalInfo() {
		switch (extType) {
		case Sensor_E1_Temperature : return "Temperature, "+((extValue>>16)==0x7fff? "Not connected" : "value="+((short)((extValue>>16)&0xffff))/100.0F+"C");
		case Sensor_E4_Interrupt : return "Interrupt, value="+(((extValue>>31)&0x01)==0b1? "Cable doesn't connect" : "pin level: "+(((extValue>>24)&0x01)==0? "low" : "high")+(((extValue>>16)&0x01)==0? "Downlink" : "Uplink"));
		case Sensor_E5_Illumination : return "Illumination, "+(((extValue>>31)&0x01)==0b1? "Cable doesn't connect" : "value="+(extValue>>16)+"lux");
		case Sensor_E6_ADC : return "ADC, value="+(((extValue>>31)&0x01)==0b1? "Cable doesn't connect" : "value="+((extValue>>16)&0xffff)/1000.0F+"V");
		case Sensor_E7_Counting : return "Counting Senor, value="+(((extValue>>31)&0x01)==0b1? "Cable doesn't connect" : "count="+(extValue>>16));
		}
		return "unknown";
	}
	
	@Override
	public String toString() {
		return toString(", ");
	}
	
	public String toString(String separator) {
		var sb=new StringBuffer();
		sb.append("battery=").append(getBatteryVoltage()).append("V ").append(getBatteryInfo());
		sb.append(separator);
		sb.append("temperature=").append(getTemperature()).append('C');
		sb.append(separator);
		sb.append("humidity=").append(getHumidity()).append('%');
		sb.append(separator);
		sb.append("Ext_sensor: ").append(getExternalInfo());
		return sb.toString();
	}
	
	/**
	 * @return a JSON object representing the payload data */
	public String toJson() {
		var members= new ArrayList<JsonMember>();
		members.add(new JsonMember("battery",new JsonNumber(getBatteryVoltage())));
		members.add(new JsonMember("temperature",new JsonNumber(getTemperature())));
		members.add(new JsonMember("humidity",new JsonNumber(getHumidity())));
		members.add(new JsonMember("Ext_sensor",new JsonString(getExternalInfo())));
		return new JsonObject(members).toString();
	}

}
