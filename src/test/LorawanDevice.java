package test;


import java.io.File;
import java.net.InetSocketAddress;
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

import it.unipr.netsec.thingsstack.lorawan.device.DataDevice;
import it.unipr.netsec.thingsstack.lorawan.device.LorawanClient;
import it.unipr.netsec.thingsstack.lorawan.device.service.Service;
import it.unipr.netsec.thingsstack.lorawan.dragino.DraginoLHT65;


/** LoRaWAN device.
 */
public class LorawanDevice {
	
	public static long DEFAULT_DATA_TIMEOUT=20*60*1000;
	
	
	public static void main(String[] args) throws Exception {
		Flags flags=new Flags(args);

		String appEui=flags.getString("-appeui",null,"EUI","join/application EUI");
		String appKey=flags.getString("-appkey",null,"key","application key");
		
		int devPort=flags.getInteger("-devPort",4444,"port","local port for communicating with virtual devices");
		
		String devEui=flags.getString("-deveui",null,"EUI","device EUI");
		String devType=flags.getString("-devtype","CurrentTime","type","device type (types: Counter, CurrentTime, Data, FileData, DraginoLHT65, DraginoLSE01; default type is 'CurrentTimeDevice')");
		// device specific parameters
		List<String> devParamList=new ArrayList<>();
		String[] devParams=flags.getStringArray("-devparams",null,"num-and-values","device specific parameters preceded by their number (e.g. '2 aaa bbb' for two parameters 'aaa' and 'bbb')");
		if (devParams!=null) devParamList.addAll(Arrays.asList(devParams));
		String devParam=flags.getString("-devparam",null,"value","device specific parameter (e.g. file name for 'FileDevice', or hexadecimal data for 'DataDevice')");
		while (devParam!=null) {
			devParamList.add(devParam);
			devParam=flags.getString("-devparam",null,null,null);
		}
		
		long devTime=flags.getLong("-t",DEFAULT_DATA_TIMEOUT/1000,"time","data transmission inter-time [sec] (default is 1200 = 20min)");
		int fPort=flags.getInteger("-fport",1,"port","value of FPort field in the LoraWAN DATA messages (default is 1)");
		//String devCtxFile=flags.getString("-devctx",null,"file","device context file containing the DevEUI and the current DevNonce value");

		String configJsonFile=flags.getString("-j",null,"file","gateway configuration JSON file");
		String configFile=flags.getString("-f",null,"file","gateway configuration file");

		boolean verbose=flags.getBoolean("-v","verbose mode");
		boolean help=flags.getBoolean("-h","prints this message");
		
		if (verbose) {
			SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.INFO));
			LorawanClient.VERBOSE=true;
		}

		if (help) {
			System.out.println(flags.toUsageString(LorawanDevice.class));
			return;
		}
		
		if (configJsonFile!=null) {
			LorawanDeviceJson cfg=(LorawanDeviceJson)JsonUtils.fromJsonFile(new File(configJsonFile),LorawanDeviceJson.class);
			appEui=cfg.app.eui;
			appKey=cfg.app.key;
			devEui=cfg.dev[0].eui;
			devType=cfg.dev[0].type;
			if (cfg.dev[0].param!=null) devParamList=Arrays.asList(cfg.dev[0].param);
			devTime=cfg.dev[0].time;
			fPort=cfg.dev[0].fport;
		}
		else
		if (configFile!=null) {
			LorawanDeviceConfig cfg=(LorawanDeviceConfig)Configure.fromFile(new File(configFile),LorawanDeviceConfig.class);
			appEui=cfg.appEui;
			appKey=cfg.appKey;
			devEui=cfg.devEui;
			devType=cfg.devType;
			if (cfg.devParam!=null) devParamList=Arrays.asList(cfg.devParam);
			devTime=cfg.devTime;
			fPort=cfg.fport;
		}
		
		if (devType.indexOf('.')<0) {
			if (devType.startsWith("Dragino")) devType=DraginoLHT65.class.getPackage().getName()+'.'+devType;
			else devType=Service.class.getPackage().getName()+'.'+devType;
		}
		Class<?> devClass=Class.forName(devType);
		Service service=(Service)(devParamList.size()>0? devClass.getDeclaredConstructor(String[].class).newInstance((Object)(devParamList.toArray(new String[0]))) : devClass.newInstance());		
		new DataDevice(Bytes.fromFormattedHex(devEui),null,Bytes.fromFormattedHex(appEui),Bytes.fromFormattedHex(appKey),fPort,new InetSocketAddress("127.0.0.1",devPort),service,devTime*1000);
	}

}
