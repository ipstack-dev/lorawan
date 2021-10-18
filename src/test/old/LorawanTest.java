package test.old;


import java.io.IOException;

import org.zoolu.util.Bytes;
import org.zoolu.util.LoggerLevel;
import org.zoolu.util.LoggerWriter;
import org.zoolu.util.SystemUtils;

import it.unipr.netsec.ipstack.lorawan.device.service.CurrentTime;
import it.unipr.netsec.ipstack.lorawan.device.service.Service;
import it.unipr.netsec.ipstack.lorawan.semtech.SemtechClient;


public abstract class LorawanTest {

	public static void main(String[] args) throws IOException {
		
		SystemUtils.setDefaultLogger(new LoggerWriter(System.out,LoggerLevel.INFO));
		SemtechClient.VERBOSE=true;
		
		String gwEUI="0A0027fffe00000F";
		float latitude=44.76492876F;
		float longitude=10.30846590F;
		int gwPort=7000;
		String srvAddress="router.eu.thethings.network";
		int srvPort=1700;
		
		SemtechClient client=new SemtechClient(gwEUI,latitude,longitude,gwPort,srvAddress+':'+srvPort);
		
		// start Application Server
		// TODO
		
		// start Device Client
		String appEUI="70b3d57ed00392fd"; // TTN 'unipr-test6' application EUI
		String appKey="69f50c4c63feb58483e10b487dcfeaa3"; // TTN 'unipr-test6' application key
		long interTime=60000;
		String devCtxFile="cfg/DEV01.cfg";
		Service device=new CurrentTime();
		new DeviceClient(device,null,devCtxFile,Bytes.fromFormattedHex(appEUI),Bytes.fromFormattedHex(appKey),2,client,interTime);
		
	}

}
