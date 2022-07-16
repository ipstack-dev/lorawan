package test.old;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;
import org.zoolu.util.config.Configure;
import org.zoolu.util.json.JsonUtils;

import it.unipr.netsec.thingsstack.lorawan.device.service.DataService;
import it.unipr.netsec.thingsstack.lorawan.dragino.DraginoLHT65;
import it.unipr.netsec.thingsstack.lorawan.semtech.SemtechClient;


/** LoRaWAN virtual gateway and LoRaWAN virtual devices.
 * <p>
 * It creates a virtual gateway and connects it to a remote LoRaWAN network server using the Semtech protocol.
 * <p>
 * It also creates a virtual device and connects it to the network server via the gateway.
 * <p>
 * The LoRaWAN virtual gateway can be configured via a configuration file, e.g.:
 * <bre>java -cp lorawan.jar test.LorawanGw -f gw.cfg
 * </bre>
 * <p>
 * or by using command line options, e.g.:
 * <bre>java -cp lorawan.jar test.LorawanGw -gweui FEFFFFabcdef0000 -appServer router.eu.thethings.network -deveui FEFFFFabcdef0001 -t 40 -appeui 0000000000000000 -appkey 00000000000000000000000000000000 -v
 * </bre>
 * Replaces the EUIs and key with the proper values.
 * <p>
 * If no network server is specified, TTN is used by default.
 * <p>
 * Different types of virtual devices are available:
 * <ul>
 * <li>CountDevice - simple device with readable and writable integer value that is incremented at each reading; the integer is encoded as four bytes in big-endian;</li>
 * <li>CurrentTimeDevice - simple device with read-only data that is the current time returned as YYYY-MM-dd HH:mm:ss string;</li>
 * <li>DataDevice - device with readable and writable data maintained in RAM; the data has to be passed as parameter (byte array as hexadecimal string);</li>
 * <li>FileDevice - device with readable and writable data stored in a file; the file name has to be passed as parameter;</li>
 * <li>DraginoLHT65 - Dragino LHT65 with artificial temperature and humidity values;</li>
 * <li>DraginoLSE01 - Dragino LSE01 with artificial temperature and soil moisture values.</li>
 * </ul>
 * <p>
 * Other types virtual devices can be used by specifying the complete class name; for example: "it.unipr.netsec.ipstack.lorawan.device.CurrentTimeDevice".
 */
public abstract class LorawanGw {
	
	
	public static void main(String[] args) throws Exception {
		Flags flags=new Flags(args);

		String gwEui=flags.getString("-gweui",null,"EUI","gateway EUI (eg. XXXXXXfffeYYYYYY, where: XXXXXX=MAC[0-2] and YYYYYY=MAC[3-5] from 48bit MAC address)");
		String[] gwCoordinates=flags.getStringTuple("-gwpos",2,null,"lati longi","gateway latitude and longitude");
		float gwLatitude=gwCoordinates!=null? Float.parseFloat(gwCoordinates[0]) : 44.76492876F;
		float gwLongitude=gwCoordinates!=null? Float.parseFloat(gwCoordinates[1]) : 10.30846590F;
		int gwPort=flags.getInteger("-gwport",-1,"port","local UDP port");

		String appEui=flags.getString("-appeui",null,"EUI","join/application EUI");
		String appKey=flags.getString("-appkey",null,"key","application key");
		String appServer=flags.getString("-appServer","router.eu.thethings.network","address","address of the network server (default is 'router.eu.thethings.network:')");
		int appPort=flags.getInteger("-appPort",1700,"port","port of the network server (default is 1700)");
		
		String devEui=flags.getString("-deveui",null,"EUI","device EUI");
		String devType=flags.getString("-devtype","CurrentTimeDevice","type","device type (types: CounterDevice, CurrentTimeDevice, DataDevice, FileDevice, DraginoLHT65, DraginoLSE01; default type is 'CurrentTimeDevice')");
		// device specific parameters
		List<String> devParamList=new ArrayList<>();
		String[] devParams=flags.getStringArray("-devparams",null,"num-and-values","device specific parameters preceded by their number (e.g. '2 aaa bbb' for two parameters 'aaa' and 'bbb')");
		if (devParams!=null) devParamList.addAll(Arrays.asList(devParams));
		String devParam=flags.getString("-devparam",null,"value","device specific parameter (e.g. file name for 'FileDevice', or hexadecimal data for 'DataDevice')");
		while (devParam!=null) {
			devParamList.add(devParam);
			devParam=flags.getString("-devparam",null,null,null);
		}
		
		long devTime=flags.getLong("-t",DeviceClient.DEFAULT_DATA_TIMEOUT/1000,"time","data transmission inter-time [sec] (default is 1200 = 20min)");
		int fPort=flags.getInteger("-fport",1,"port","value of FPort field in the LoRaWAN DATA messages (default is 1)");
		//String devCtxFile=flags.getString("-devctx",null,"file","device context file containing the DevEUI and the current DevNonce value");

		String configJsonFile=flags.getString("-j",null,"file","gateway configuration JSON file");
		String configFile=flags.getString("-f",null,"file","gateway configuration file");

		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this message");
		
		if (verbose) {
			SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.INFO));
			SemtechClient.VERBOSE=true;
			DeviceClient.VERBOSE=true;
		}

		if (help) {
			System.out.println(flags.toUsageString(LorawanGw.class));
			return;
		}
		
		if (configJsonFile!=null) {
			LorawanGwJson cfg=(LorawanGwJson)JsonUtils.fromJsonFile(new File(configJsonFile),LorawanGwJson.class);
			gwEui=cfg.gw.eui;
			gwLatitude=cfg.gw.latitude;
			gwLongitude=cfg.gw.longitude;
			gwPort=cfg.gw.port;
			appEui=cfg.app.eui;
			appKey=cfg.app.key;
			appServer=cfg.app.server;
			appPort=cfg.app.port;
			devEui=cfg.dev[0].eui;
			devType=cfg.dev[0].type;
			if (cfg.dev[0].param!=null) devParamList=Arrays.asList(cfg.dev[0].param);
			devTime=cfg.dev[0].time;
			fPort=cfg.dev[0].fport;
		}
		else
		if (configFile!=null) {
			LorawanGwConfig cfg=(LorawanGwConfig)Configure.fromFile(new File(configFile),LorawanGwConfig.class);
			gwEui=cfg.gwEui;
			gwLatitude=cfg.gwLatitude;
			gwLongitude=cfg.gwLongitude;
			gwPort=cfg.gwPort;
			appEui=cfg.appEui;
			appKey=cfg.appKey;
			appServer=cfg.appServer;
			appPort=cfg.appPort;
			devEui=cfg.devEui;
			devType=cfg.devType;
			if (cfg.devParam!=null) devParamList=Arrays.asList(cfg.devParam);
			devTime=cfg.devTime;
			fPort=cfg.fport;
		}
		
		if (devType.indexOf('.')<0) {
			if (devType.startsWith("Dragino")) devType=DraginoLHT65.class.getPackage().getName()+'.'+devType;
			else devType=DataService.class.getPackage().getName()+'.'+devType;
		}
		Class<?> devClass=Class.forName(devType);
		DataService device=(DataService)(devParamList.size()>0? devClass.getDeclaredConstructor(String[].class).newInstance((Object)(devParamList.toArray(new String[0]))) : devClass.newInstance());	
		
		SemtechClient client=new SemtechClient(gwEui,gwLatitude,gwLongitude,gwPort,appServer+':'+appPort);
		new DeviceClient(device,Bytes.fromFormattedHex(devEui),null,Bytes.fromFormattedHex(appEui),Bytes.fromFormattedHex(appKey),fPort,client,devTime*1000);
	}

}
