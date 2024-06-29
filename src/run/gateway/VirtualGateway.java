package run.gateway;

import java.io.File;

import org.zoolu.util.Flags;
import org.zoolu.util.config.Configure;
import org.zoolu.util.json.Json;
import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;
import org.zoolu.util.log.WriterLogger;

import io.ipstack.lorawan.gateway.LorawanGateway;
import io.ipstack.lorawan.gateway.LorawanGatewayConfig;
import io.ipstack.lorawan.gateway.LorawanGatewayJsonConfig;
import io.ipstack.lorawan.mac.UdpMacMessageExchanger;
import io.ipstack.lorawan.semtech.SemtechClient;


/** Virtual LoRaWAN gateway.
 * It communicates with LoRaWAN devices using UDP in place of the LoRaWAN physical layer.
 */
public final class VirtualGateway {
	private VirtualGateway() {}
		
	
	public static void main(String[] args) throws Exception {
		var flags= new Flags(args);
		//String[] coordinates= flags.getStringTuple("-pos",2,new String[]{"41.890236", "12.492353"},"lati long","latidude and longitude of the gateway"); // Rome
		String[] coordinates= flags.getStringTuple("-pos",2,new String[]{"44.801656", "10.328002"},"lati long","latidude and longitude of the gateway"); // Parma
		int port= flags.getInteger("-p",7001,"port","local UDP port for communicating with virtual devices (default 7000)");				
		String configFile= flags.getString("-f",null,"file","gateway configuration file");
		String configJsonFile= flags.getString("-j",null,"file","gateway configuration JSON file");
		String gwEUI= flags.getString("-g",null,"EUI","gateway EUI");
		String networkServer= flags.getString("-s",null,"addr","address of the network server");
		
		boolean verbose= flags.getBoolean("-v","verbose mode");
		boolean help= flags.getBoolean("-h","prints this message");
		
		if (verbose) {
			DefaultLogger.setLogger(new WriterLogger(System.out,LoggerLevel.INFO));
			SemtechClient.VERBOSE=true;
			LorawanGateway.VERBOSE=true;
		}

		LorawanGatewayConfig cfg= null;
		if (configFile!=null) {
			cfg= (LorawanGatewayConfig)Configure.fromFile(new File(configFile),LorawanGatewayConfig.class);
		}
		else if (configJsonFile!=null) {
			LorawanGatewayJsonConfig cfgJson= (LorawanGatewayJsonConfig)Json.fromJSONFile(new File(configJsonFile),LorawanGatewayJsonConfig.class);
			cfg= cfgJson.toConfig();
		}
		else if (gwEUI!=null && networkServer!=null) {
			float latitude= Float.parseFloat(coordinates[0]);
			float longitude= Float.parseFloat(coordinates[1]);
			cfg= new LorawanGatewayConfig(gwEUI,latitude,longitude,networkServer);
		}
		else {
			System.out.println("Gateway configuration is missing.");
			help= true;
		}
		
		if (help) {
			System.out.println(flags.toUsageString(VirtualGateway.class));
			return;
		}

		new LorawanGateway(cfg,new UdpMacMessageExchanger(port));				
	}

}
