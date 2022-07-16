package test;
import it.unipr.netsec.ipstack.analyzer.LibpcapReader;
import it.unipr.netsec.ipstack.analyzer.LibpcapRecord;
import it.unipr.netsec.ipstack.ethernet.EthPacket;
import it.unipr.netsec.ipstack.ip4.Ip4Packet;
import it.unipr.netsec.ipstack.udp.UdpPacket;
import it.unipr.netsec.thingsstack.lorawan.dragino.DraginoLHT65Payload;
import it.unipr.netsec.thingsstack.lorawan.dragino.DraginoLSE01Payload;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanDataMessagePayload;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanJoinAcceptMessage;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanJoinAcceptMessagePayload;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanJoinRequestMessagePayload;
import it.unipr.netsec.thingsstack.lorawan.mac.LorawanMacMessage;
import it.unipr.netsec.thingsstack.lorawan.semtech.SemtechJsonObjectPacket;
import it.unipr.netsec.thingsstack.lorawan.semtech.SemtechPacket;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;

import org.zoolu.util.Base64;
import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;


public abstract class LorawanParserOld {
	
	enum PayloadType {LORAWAN, LHT65, LSE01};
	
	public static int SEMTECH_LORAWAN_PORT=1700;
	
	static byte[] APP_KEY=null;
	static byte[] SESSION_KEY=null;



	/** Parses a payload that can be either a PHYPayload (i.e. a LoRaWAN MAC message) or application data.
	 * @param type the type of the payload
	 * @param data the payload
	 * @return the analyzed data */
	private static String parsePayload(PayloadType type, byte[] data) {
		switch (type) {
		case LORAWAN : return parseLorawanMacMessage(data);
		case LHT65 : return new DraginoLHT65Payload(data).toString();
		case LSE01 : return new DraginoLSE01Payload(data).toString();
		}
		return null; // never
	}

	
	/** Parses a LoRaWAN MAC message (i.e. PHYPayload).
	 * @param data the MAC message encoded in base64
	 * @return the analyzed data */
	public static String parseLorawanMacMessage(String data64) {
		byte[] macMsg=Base64.decode(data64);
		String str="\tMACMessage: "+Bytes.toHex(macMsg)+'\n';
		return str+parseLorawanMacMessage(macMsg);
	}
	

	/** Parses a LoRaWAN MAC message (i.e. PHYPayload).
	 * @param data the MAC message
	 * @return the analyzed data */
	public static String parseLorawanMacMessage(byte[] data) {
		LorawanMacMessage macMessage=LorawanMacMessage.parseMessage(data);
		String str="\t"+macMessage.toString("\n\t");
		int messageType=macMessage.getMType();
		byte[] macPayload=macMessage.getMacPayload();
		if (messageType==LorawanMacMessage.TYPE_JOIN_REQUEST) {
			str+="\n\tJoin Request message payload:";
			LorawanJoinRequestMessagePayload join=new LorawanJoinRequestMessagePayload(macPayload);
			str+="\n\t\t"+join.toString("\n\t\t");		
		}
		else
		if  (messageType==LorawanMacMessage.TYPE_JOIN_ACCEPT && APP_KEY!=null) {
			str+="\n\tJoin Accept message payload:";
			try {
				LorawanJoinAcceptMessage joinAcceptMsg=(LorawanJoinAcceptMessage)macMessage;
				joinAcceptMsg.decrypt(APP_KEY);
				LorawanJoinAcceptMessagePayload joinAcceptPayload=joinAcceptMsg.getDecryptedPayload();
				str+="\n\t\t"+joinAcceptPayload.toString("\n\t\t");									
				str+="\n\t\tMIC: "+Bytes.toHex(joinAcceptMsg.getDecryptedMIC());									
			}
			catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
		else
		if  (messageType==LorawanMacMessage.TYPE_CORFIRMED_DATA_UP || messageType==LorawanMacMessage.TYPE_UNCORFIRMED_DATA_UP || messageType==LorawanMacMessage.TYPE_CORFIRMED_DATA_DOWN || messageType==LorawanMacMessage.TYPE_UNCORFIRMED_DATA_DOWN) {
			str+="\n\tData message payload:";
			LorawanDataMessagePayload dataMessagePayload=new LorawanDataMessagePayload(macPayload);
			if (SESSION_KEY!=null) {
				try {
					dataMessagePayload.decryptFramePayload(SESSION_KEY);
				}
				catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
			}
			str+="\n\t\t"+dataMessagePayload.toString("\n\t\t");
		}
		return str;
	}
	
	
	private static void processHexPayloadFile(String hex_file, PayloadType type, PrintStream out) throws IOException {
		BufferedReader in=new BufferedReader(new FileReader(hex_file));
		String line=in.readLine();
		int count=0;
		while (line!=null && line.length()>0) {
			count++;
			line=line.trim();
			byte[] data=Bytes.fromHex(line);
			switch (type) {
				case LHT65 : {
					out.println(parsePayload(PayloadType.LHT65,data));
					break;
				}
				case LSE01 : {
					out.println(parsePayload(PayloadType.LSE01,data));
					break;
				}
				case LORAWAN : {
					out.println("payload #"+count);
					try {
						String str=parseLorawanMacMessage(data);
						out.println("\tframe: "+line);
						out.println(str);
					}
					catch (Exception e) {
						out.println("Error in frame: "+line);
						e.printStackTrace();
					}					
				}
			}
			line=in.readLine();
		}
		in.close();
		out.println();
		out.println("Processed "+count+" lines");
	}
	

	public static void processPcapFile(String pcap_file, int lorawan_port, PrintStream out) throws IOException {
		LibpcapReader pcapReader=new LibpcapReader(pcap_file);
		out.println("pcap type: "+pcapReader.getHeader().getLinkType());
		int count=0;
		int i=0;
		for (; pcapReader.hasMore(); i++) {
			LibpcapRecord pr=pcapReader.read();
			EthPacket ethPkt=EthPacket.parseEthPacket(pr.getPacketData());
			if (ethPkt.getType()==EthPacket.ETH_IP4) {
				Ip4Packet ipPkt=Ip4Packet.parseIp4Packet(ethPkt);
				if (ipPkt.getProto()==Ip4Packet.IPPROTO_UDP) {
					UdpPacket udpPkt=UdpPacket.parseUdpPacket(ipPkt);
					if (udpPkt.getSourcePort()==lorawan_port || udpPkt.getDestPort()==lorawan_port) {
						count++;
						byte[] data=udpPkt.getPayload();
						out.println((i+1)+" "+processSemtechPacket(data));
					}
				}
			}
		}
		out.println();
		out.println("Processed "+count+" Semtech packets out of "+i+" packets.");
	}

	
	public static String processSemtechPacket(byte[] data) {
		return processSemtechPacket(data,0,data.length);
	}
	
	
	public static String processSemtechPacket(byte[] buf, int off, int len) {
		SemtechPacket semPkt=SemtechPacket.parseSemtechPacket(buf,off,len);
		int type=semPkt.getType();
		int token=semPkt.getToken();
		String jsonBody=null;
		if (semPkt instanceof SemtechJsonObjectPacket) {
			jsonBody=((SemtechJsonObjectPacket)semPkt).getJsonObject();	
			int index=jsonBody.indexOf("\"data\":");
			if (index>0) {
				String data64=jsonBody.substring(index+7).split("\"")[1];
				jsonBody+="\n\tbase64-MACMessage: "+data64;
				jsonBody+="\n"+parseLorawanMacMessage(data64);
			}
		}
		return Bytes.toHex(Bytes.fromInt16(token))+" "+semPkt.getTypeString()+(jsonBody!=null? " "+jsonBody : "");
	}

	
	public static void semtechToLorawan(String pcap_file, int lorawan_port) throws IOException {
		LibpcapReader pcapReader=new LibpcapReader(pcap_file);
		int semtechCount=0;
		int lorawanCount=0;
		int total=0;
		for (; pcapReader.hasMore(); total++) {
			LibpcapRecord pr=pcapReader.read();
			EthPacket ethPkt=EthPacket.parseEthPacket(pr.getPacketData());
			if (ethPkt.getType()==EthPacket.ETH_IP4) {
				Ip4Packet ipPkt=Ip4Packet.parseIp4Packet(ethPkt);
				if (ipPkt.getProto()==Ip4Packet.IPPROTO_UDP) {
					UdpPacket udpPkt=UdpPacket.parseUdpPacket(ipPkt);
					if (udpPkt.getSourcePort()==lorawan_port || udpPkt.getDestPort()==lorawan_port) {
						semtechCount++;
						byte[] data=udpPkt.getPayload();
						SemtechPacket semPkt=SemtechPacket.parseSemtechPacket(data);
						if (semPkt instanceof SemtechJsonObjectPacket) {
							String jsonBody=((SemtechJsonObjectPacket)semPkt).getJsonObject();
							int index=jsonBody.indexOf("\"data\":");
							if (index>0) {
								String macMessageBase64=jsonBody.substring(index+7).split("\"")[1];
								byte[] macMessage=Base64.decode(macMessageBase64);
								out.println(Bytes.toHex(macMessage));
								lorawanCount++;
							}
						}
					}
				}
			}
		}
		out.println();
		out.println("Processed "+total+" packets. Found "+lorawanCount+" LoRaWAN packets out of "+semtechCount+" Semtech packets.");
	}

	
	public static void main(String[] args) throws IOException {
		Flags flags=new Flags(args);
		int lorawan_port=flags.getInteger("-p",SEMTECH_LORAWAN_PORT,"port","UDP port of the Semtech-LoRaWAN trace");
		boolean help=flags.getBoolean("-h","prints this message");		
		String hex_payload=flags.getString("-X",null,"payload","parses a given hexadecimal LoRaWAN payload");
		String base64_payload=flags.getString("-B",null,"payload","parses a given base64-encoded LoRaWAN payload");
		String hex_file=flags.getString("-hex",null,"file","parses a file with hexadecimal payloads");
		String pcap_file=flags.getString("-pcap",null,"file","parses a pcap file");
		boolean semtech_to_lorawan=flags.getBoolean("-semtolora","extraxts LoRaWAN payloads from Semtech-LoRaWAN packets in a pcap file");	
		boolean lht65_payload=flags.getBoolean("-LHT65","payload is from Dragino LHT65 sensor");	
		boolean lse01_payload=flags.getBoolean("-LSE01","payload is from Dragino LSE01 sensor");	
		String app_key=flags.getString("-appkey",null,"key","the join/applidcation AppKey");
		String app_s_key=flags.getString("-appskey",null,"key","the application session key AppSKey");
		
		if (help || (pcap_file==null && hex_file==null && hex_payload==null && base64_payload==null)) {
			out.println(flags.toUsageString(LorawanParserOld.class));
			return;
		}
		// else
		PayloadType payload_type=lht65_payload? PayloadType.LHT65 : lse01_payload? PayloadType.LSE01 : PayloadType.LORAWAN;
		
		if (app_key!=null) APP_KEY=Bytes.fromHex(app_key);
		if (app_s_key!=null) SESSION_KEY=Bytes.fromHex(app_s_key);
		
		if (hex_payload!=null) {
			out.println(parsePayload(payload_type,Bytes.fromHex(hex_payload)));
			return;
		}
		// else
		if (base64_payload!=null) {
			out.println(parseLorawanMacMessage(base64_payload));
			return;
		}
		// else
		if (hex_file!=null) {
			processHexPayloadFile(hex_file,payload_type,out);
			return;
		}		
		// else
		if (pcap_file!=null) {
			if (semtech_to_lorawan) semtechToLorawan(pcap_file,lorawan_port);
			else processPcapFile(pcap_file,lorawan_port,out);
			return;
		}
	}

}
