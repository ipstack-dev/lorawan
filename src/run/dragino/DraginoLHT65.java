package run.dragino;


import java.util.Date;

import org.zoolu.util.Random;

import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.device.service.DataUtils;


/** Dragino LHT65.
 * <p>
 * Some characteristics:
 * <ul>
 * <li>Battery power: 2400mAh.</li>
 * <li>Power consumption: Idle: 3uA. Transmit: max 130mA.</li>
 * </ul>
 */
public class DraginoLHT65 implements DataService {
	
	DraginoLHT65Payload payload= null;	
	long startTime= System.currentTimeMillis();
	long count= 0;
	
	
	/** Creates a new device. */
	public DraginoLHT65() {
		//payload=new DraginoLHT65Payload(Bytes.fromHex("cb8f021102f5017fff7fff"));
	}
	
	/** Creates a new device.
	 * @param battery battery voltage [V]
	 * @param temperature temperature [C]
	 * @param humidity humidity [%] */
	public DraginoLHT65(double battery, double temperature, int humidity) {
		this(battery,temperature,humidity,0x01,0x7fff7fff);
	}
	
	/** Creates a new device.
	 * @param battery battery voltage [V]
	 * @param temperature temperature [C]
	 * @param humidity humidity [%]
	 * @param extType external sensor type
	 * @param extValue external sensor value */
	public DraginoLHT65(double battery, double temperature, int humidity, int extType, long extValue) {
		setData(battery,temperature,humidity,extType,extValue);
	}
	
	public void setData(double battery, double temperature, int humidity) {
		payload=new DraginoLHT65Payload(battery,temperature,humidity,0x01,0x7fff7ff);
	}
	
	public void setData(double battery, double temperature, int humidity, int extType, long extValue) {
		payload=new DraginoLHT65Payload(battery,temperature,humidity,extType,extValue);
	}
	
	@Override
	public byte[] getData() {
		count++;
		if (payload!=null)return payload.getBytes();
		// else
		double battery= DataUtils.getBattery(3.0,2400,System.currentTimeMillis()-startTime,count); // maxPower=2400mAh, maxVoltage=3V?
		double temperature= DataUtils.getTemperature(new Date());
		int humidity= 75+Random.nextInt(15);
		int extType =DraginoLHT65Payload.Sensor_E1_Temperature;
		double extTemperature= temperature-0.1+Random.nextDouble()*0.2;
		return new DraginoLHT65Payload(battery,temperature,humidity,extType,(((long)(extTemperature*100))<<16)|0x7fff).getBytes();
	}

	@Override
	public void setData(byte[] data) {
		payload= new DraginoLHT65Payload(data);
	}
	
}
