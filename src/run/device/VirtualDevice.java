package run.device;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

import org.zoolu.net.InetAddrUtils;
import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;
import org.zoolu.util.Random;
import org.zoolu.util.config.Configure;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.lorawan.client.LorawanClient;
import io.ipstack.lorawan.device.DataDevice;
import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.mac.AppContext;
import io.ipstack.lorawan.mac.UdpMacMessageExchanger;
import run.dragino.DraginoLHT65;


/** Virtual LoRaWAN device.
 */
public final class VirtualDevice {
	private VirtualDevice() {}
	
	public static long DEFAULT_DATA_TIMEOUT=20*60*1000;
	
	
	public static void main(String[] args) throws Exception {
		var flags= new Flags(args);

		String dataKey= flags.getString("-datakey",null,"key","data key for applying additional data encryption (experimental)");

		String devEUI= flags.getString("-d",null,"EUI","devEUI");
		String joinEUI= flags.getString("-j",null,"EUI","joinEUI");		
		String appKey= flags.getString("-k",null,"key","appKey");
		int fPort= flags.getInteger("-p",1,"fPort","fPort value");
		long devTime= flags.getInteger("-t",20,"time","device sending interval, in seconds");
		
		String configFile= flags.getString("-f",null,"file","device configuration file");
		String configJsonFile= flags.getString("-F",null,"file","device configuration JSON file");

		boolean verbose= flags.getBoolean("-v","verbose mode");
		boolean help =flags.getBoolean("-h","prints this message");

		ArrayList<SocketAddress> gateways= new ArrayList<>();
		String gwSoaddr= flags.getString("-gw","127.0.0.1:7000","soaddr","gateway socket address");
		while (gwSoaddr!=null) {
			gateways.add(InetAddrUtils.parseInetSocketAddress(gwSoaddr));
			gwSoaddr= flags.getString("-gw",null,null,null);
		}
		
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.INFO));
			LorawanClient.VERBOSE= true;
		}
		
		VirtualDeviceConfig cfg= null;
		if (configFile!=null) {
			cfg=(VirtualDeviceConfig)Configure.fromFile(new File(configFile),VirtualDeviceConfig.class);
		}
		else if (configJsonFile!=null) {
			var jsonCfg= (VirtualDeviceJsonConfig)Json.fromJSONFile(new File(configJsonFile),VirtualDeviceJsonConfig.class);
			cfg= jsonCfg.toConfig(0);
		}
		else if (devEUI!=null && joinEUI!=null && appKey!=null) {
			cfg= new VirtualDeviceConfig(devEUI,joinEUI,appKey,fPort,"Counter",null,devTime);
		}
		else {
			System.out.println("Device configuration is missing.");
			help= true;			
		}
		
		if (gateways.size()==0) {
			System.out.println("No gateway has been set.");
			help= true;			
		}
		
		if (help) {
			System.out.println(flags.toUsageString(VirtualDevice.class));
			return;
		}
		
		if (cfg.devType.indexOf('.')<0) {
			if (cfg.devType.startsWith("Dragino")) cfg.devType= DraginoLHT65.class.getPackage().getName()+'.'+cfg.devType;
			else cfg.devType= DataService.class.getPackage().getName()+'.'+cfg.devType;
		}
		Class<?> devClass= Class.forName(cfg.devType);
		DataService service= (DataService)(cfg.devParam!=null && cfg.devParam.length>0? devClass.getDeclaredConstructor(String[].class).newInstance((Object)(cfg.devParam)) : devClass.getDeclaredConstructor().newInstance());		
		if (dataKey!=null) service=new EncryptedDataService(service,Bytes.fromFormattedHex(dataKey));
		var appCtx= new AppContext(cfg.getDevEUI(),cfg.getJoinEUI(),cfg.appKey,Random.nextInt(65536));
		var macMsgExchanger= new UdpMacMessageExchanger(gateways.toArray(new SocketAddress[0]));
		new DataDevice(appCtx,cfg.fport,macMsgExchanger,service,cfg.devTime*1000);
	}

}
