package run.dragino;


import java.util.ArrayList;

import org.zoolu.util.Bytes;
import org.zoolu.util.json.JsonMember;
import org.zoolu.util.json.JsonNumber;
import org.zoolu.util.json.JsonObject;
import org.zoolu.util.json.JsonString;


/** Dragino LSE01 payload.
 */
public class DraginoLSE01Payload {

	/** Battery Info (2 bytes) */
	int batt;
	
	/** Temperature (Reserve, Ignore now) (2 bytes) */
	//int temperature;
	
	/** Soil Moisture (2 bytes) */
	int soilMoisture;
	
	/** Soil Temperature (2 bytes) */
	int soilTemperature;
	
	/** Soil Conductivity (EC) (2 bytes) */
	int soilConductivity;
	
	/** Digital Interrupt (optional) (1 byte) */
	int interrupt;
	
	
	/** Creates a new payload.
	 * @param battery battery voltage [V]
	 * @param soilMoisture soil moisture [%]
	 * @param soilTemperature soil temperature [C]
	 * @param soilConductivity soil conductivity [uS/cm] */
	public DraginoLSE01Payload(double battery, double soilMoisture, double soilTemperature, int soilConductivity) {
		this(battery,soilMoisture,soilTemperature,soilConductivity,0);
	}

	/** Creates a new payload.
	 * @param battery battery voltage [V]
	 * @param soilMoisture soil moisture [%]
	 * @param soilTemperature soil temperature [C]
	 * @param soilConductivity soil conductivity [uS/cm]
	 * @param interrupt interrupt value */
	public DraginoLSE01Payload(double battery, double soilMoisture, double soilTemperature, int soilConductivity, int interrupt) {
		this.batt= (int)(battery*1000);
		this.soilMoisture= (int)(soilMoisture*100);
		this.soilTemperature= (int)(soilTemperature*100);
		this.soilConductivity= soilConductivity;
		this.interrupt= interrupt;
	}

	/** Creates a new payload.
	 * @param payload the payload */
	public DraginoLSE01Payload(byte[] payload) {
		this(payload,0);
	}
	
	/** Creates a new payload.
	 * @param buf buffer containing the payload
	 * @param off offset within the buffer */
	public DraginoLSE01Payload(byte[] buf, int off) {
		batt= Bytes.toInt16(buf,off);
		//temperature= Bytes.toInt16(buf,off+2);
		soilMoisture= Bytes.toInt16(buf,off+4);
		soilTemperature= Bytes.toInt16(buf,off+6);
		soilConductivity= Bytes.toInt16(buf,off+8);
		interrupt= 0xff&buf[off+10];
	}
	
	/** Gets payload data.
	 * @return the data */
	public byte[] getBytes() {
		var data= new byte[11];
		getBytes(data,0);
		return data;
	}
	
	/** Gets payload data.
	 * @param buf buffer where data has to be written
	 * @param off offset within the buffer
	 * @return the payload length */
	public int getBytes(byte[] buf, int off) {
		Bytes.fromInt16(batt,buf,off);
		Bytes.fromInt16(0,buf,off+2);
		Bytes.fromInt16(soilMoisture,buf,off+4);
		Bytes.fromInt16(soilTemperature,buf,off+6);
		Bytes.fromInt16(soilConductivity,buf,off+8);
		buf[off+10]= (byte)interrupt;
		return 11;
	}
	
	/**
	 * @return the battery voltage [V] */
	public float getBatteryVoltage() {
		return batt/1000.0F;
	}

	/**
	 * @return the soil moisture [%] */
	public float getSoilMoisture() {
		return soilMoisture/100.0F;
	}
	
	/**
	 * @return the soil temperature [C] */
	public float getSoilTemperature() {
		return ((short)soilTemperature)/100.0F;
	}
	
	/**
	 * @return the soil conductivity [uS/cm] */
	public int getSoilConductivity() {
		return soilConductivity;
	}
	
	/**
	 * @return the digital interrupt value */
	public int getInterrupt() {
		return interrupt;
	}
	
	@Override
	public String toString() {
		return toString(", ");
	}
	
	public String toString(String separator) {
		var sb=new StringBuffer();
		sb.append("battery=").append(getBatteryVoltage()).append("V");
		sb.append(separator);
		sb.append("soil moisture=").append(getSoilMoisture()).append('%');
		sb.append(separator);
		sb.append("soil temperature=").append(getSoilTemperature()).append('C');
		sb.append(separator);
		sb.append("soil conductivity=").append(getSoilConductivity()).append("uS/cm");
		sb.append(separator);
		sb.append("interrupt=0x").append(Bytes.int8ToHex(getInterrupt()));
		return sb.toString();
	}
	
	/**
	 * @return a JSON object representing the payload data */
	public String toJson() {
		var members= new ArrayList<JsonMember>();
		members.add(new JsonMember("battery",new JsonNumber(getBatteryVoltage())));
		members.add(new JsonMember("temperature",new JsonNumber(getSoilTemperature())));
		members.add(new JsonMember("soil_moisture",new JsonNumber(getSoilMoisture())));
		members.add(new JsonMember("conductivity",new JsonNumber(getSoilConductivity())));
		members.add(new JsonMember("interrupt",new JsonString(Bytes.int8ToHex(getInterrupt()))));
		return new JsonObject(members).toString();
	}

}
