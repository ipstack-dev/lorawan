package run.dragino;


import java.util.Date;

import org.zoolu.util.Bytes;
import org.zoolu.util.Random;

import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.device.service.DataUtils;


/** Dragino LSE01.
 */
public class DraginoLSE01 implements DataService {

	DraginoLSE01Payload payload= null;
	long startTime= System.currentTimeMillis();
	long count= 0;
	
	
	/** Creates a new device. */
	public DraginoLSE01() {
		//payload=new DraginoLSE01Payload(ByteUtils.fromHex("0cf4000000000338000001"));
	}

	/** Creates a new device.
	 * @param battery battery voltage [V]
	 * @param soilMoisture soil moisture [%]
	 * @param soilTemperature soil temperature [C]
	 * @param soilConductivity soil conductivity [uS/cm] */
	public DraginoLSE01(double battery, double soilMoisture, double soilTemperature, int soilConductivity) {
		this(battery,soilMoisture,soilTemperature,soilConductivity,0);
	}

	/** Creates a new device.
	 * @param battery battery voltage [V]
	 * @param soilMoisture soil moisture [%]
	 * @param soilTemperature soil temperature [C]
	 * @param soilConductivity soil conductivity [uS/cm]
	 * @param interrupt interrupt value */
	public DraginoLSE01(double battery, double soilMoisture, double soilTemperature, int soilConductivity, int interrupt) {
		setData(battery,soilMoisture,soilTemperature,soilConductivity,interrupt);
	}
	
	public void setData(double battery, double soilMoisture, double soilTemperature, int soilConductivity) {
		setData(battery,soilMoisture,soilTemperature,soilConductivity,0);
	}
	
	public void setData(double battery, double soilMoisture, double soilTemperature, int soilConductivity, int interrupt) {
		payload=new DraginoLSE01Payload(battery,soilMoisture,soilTemperature,soilConductivity,interrupt);
	}
	
	@Override
	public byte[] getData() {
		count++;
		if (payload!=null) return payload.getBytes();
		// else
		double battery= DataUtils.getBattery(3.4,4000,System.currentTimeMillis()-startTime,count); // maxPower=4000mAh, maxVoltage=3.4V?
		double temperature= DataUtils.getTemperature(new Date());
		double humidity= 75+Random.nextInt(150)/10;
		return new DraginoLSE01Payload(battery,humidity,temperature,0).getBytes();
	}

	@Override
	public void setData(byte[] data) {
		payload= new DraginoLSE01Payload(data);
	}

}
