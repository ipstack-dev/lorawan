package it.unipr.netsec.ipstack.lorawan.dragino;


import java.util.Calendar;
import java.util.Date;

import org.zoolu.util.Random;

import it.unipr.netsec.ipstack.lorawan.device.Device;


/** Dragino LHT65.
 * <p>
 * Some characteristics:
 * <ul>
 * <li>Battery power: 2400mAh.</li>
 * <li>Power consumption: Idle: 3uA. Transmit: max 130mA.</li>
 * </ul>
 */
public class DraginoLHT65 implements Device {
	
	DraginoLHT65Payload payload=null;	
	long startTime=System.currentTimeMillis();
	long count=0;
	
	
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
		double battery=getBattery(3.0,2400,System.currentTimeMillis()-startTime,count); // maxPower=2400mAh, maxVoltage=3V?
		double temperature=getTemperature(new Date());
		int humidity=75+Random.nextInt(15);
		int extType=DraginoLHT65Payload.Sensor_E1_Temperature;
		double extTemperature=temperature-0.1+Random.nextDouble()*0.2;
		return new DraginoLHT65Payload(battery,temperature,humidity,extType,(((long)(extTemperature*100))<<16)|0x7fff).getBytes();
	}

	@Override
	public void setData(byte[] data) {
		payload=new DraginoLHT65Payload(data);
	}
	
	
	/** Gets an artificial temperature value for a given date.
	 * @param date selected date
	 * @return temperature [C] */
	static double getTemperature(Date date) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		long secondOfDay=cal.get(Calendar.SECOND)+60*cal.get(Calendar.MINUTE)+3600*cal.get(Calendar.HOUR_OF_DAY);
		double dayOfYear=cal.get(Calendar.DAY_OF_YEAR)+secondOfDay/(3600*24.0);
		return 18.0+13.0*Math.sin(2*Math.PI*dayOfYear/365-Math.PI/2)+0.4*Math.sin(2*Math.PI*secondOfDay/(3600*24.0)-Math.PI/2);
	}

	
	/** Gets an artificial power level.
	 * @param maxPower maximum power [mAh]
	 * @param elapsedTime elapsed time [msec]
	 * @param txNum number of transmissions
	 * @return the remaining power [mAh] */
	static double getPower(double maxPower, long elapsedTime, long txNum) {
		double powerIdle=0.003*elapsedTime/3600000.0; // consumption in idle mode: 3uA
		double powerTx=txNum*130*1.0/3600.0; // consumption for transmitting: 130mA*time_tx[sec]
		double power=maxPower-powerIdle-powerTx;
		if (power<0) power=0;
		return power;
	}
	
	/** Gets an artificial battery voltage level.
	 * @param maxVoltage maximum battery voltage [V]
	 * @param maxPower maximum battery power [mAh]
	 * @param elapsedTime elapsed time [msec]
	 * @param txNum number of transmissions
	 * @return the remaining voltage [V] */
	static double getBattery(double maxVoltage, double maxPower, long elapsedTime, long txNum) {
		double power=getPower(maxPower,elapsedTime,txNum);
		return maxVoltage*(0.667+0.333*power/maxPower); // min: 2/3 ?
	}
}
