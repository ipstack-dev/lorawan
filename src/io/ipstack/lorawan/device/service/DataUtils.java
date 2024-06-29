package io.ipstack.lorawan.device.service;

import java.util.Calendar;
import java.util.Date;

import org.zoolu.util.json.JsonMember;
import org.zoolu.util.json.JsonNumber;
import org.zoolu.util.json.JsonObject;

public final class DataUtils {
	private DataUtils() {}

	/** Gets an artificial temperature value for a given date.
	 * @param date selected date
	 * @return temperature [C] */
	public static double getTemperature(Date date) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		long secondOfDay= cal.get(Calendar.SECOND)+60*cal.get(Calendar.MINUTE)+3600*cal.get(Calendar.HOUR_OF_DAY);
		double dayOfYear= cal.get(Calendar.DAY_OF_YEAR)+secondOfDay/(3600*24.0);
		return 18.0+13.0*Math.sin(2*Math.PI*dayOfYear/365-Math.PI/2)+0.4*Math.sin(2*Math.PI*secondOfDay/(3600*24.0)-Math.PI/2);
	}

	
	/** Gets an artificial power level.
	 * @param maxPower maximum power [mAh]
	 * @param elapsedTime elapsed time [msec]
	 * @param txNum number of transmissions
	 * @return the remaining power [mAh] */
	public static double getPower(double maxPower, long elapsedTime, long txNum) {
		double powerIdle= 0.003*elapsedTime/3600000.0; // consumption in idle mode: 3uA
		double powerTx= txNum*130*1.0/3600.0; // consumption for transmitting: 130mA*time_tx[sec]
		double power= maxPower-powerIdle-powerTx;
		if (power<0) power=0;
		return power;
	}
	
	/** Gets an artificial battery voltage level.
	 * @param maxVoltage maximum battery voltage [V]
	 * @param maxPower maximum battery power [mAh]
	 * @param elapsedTime elapsed time [msec]
	 * @param txNum number of transmissions
	 * @return the remaining voltage [V] */
	public static double getBattery(double maxVoltage, double maxPower, long elapsedTime, long txNum) {
		double power= getPower(maxPower,elapsedTime,txNum);
		return maxVoltage*(0.667+0.333*power/maxPower); // min: 2/3 ?
	}
	
	public static String getJson(String name, double value) {
		//return new JsonObject(new JsonMember(name,new JsonNumber(value))).toString();		
		return "{\""+name+"\":"+value+"}";
	}
	
	public static String getJson(String name, long value) {
		return "{\""+name+"\":"+value+"}";
	}

}
