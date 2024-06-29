package run.dragino;

import static java.lang.System.out;

import java.io.IOException;

import org.zoolu.util.Base64;
import org.zoolu.util.Bytes;
import org.zoolu.util.Flags;

import run.dragino.DraginoLHT65Payload;


public final class DraginoLHT65Encoder {
	private DraginoLHT65Encoder() {}
	
	
	/** Decodes a payload.
	 * @param data the payload
	 * @return the analyzed data */
	public static String decodePayload(byte[] data) {
		System.out.println("DEBUG: "+Bytes.toHex(data));
		return new DraginoLHT65Payload(data).toString();
	}


	/** Encodes a payload.
	 * @param battery battery voltage [V]
	 * @param temperature temperature [C]
	 * @param humidity humidity [%]
	 * @return the payload */
	public static byte[] encodePayload(double battery, double temperature, int humidity) {
		return encodePayload(battery,temperature,humidity,0x01,0x7fff7fff);
	}

	
	/** Encodes a payload.
	 * @param battery battery voltage [V]
	 * @param temperature temperature [C]
	 * @param humidity humidity [%]
	 * @param extType external sensor type
	 * @param extValue external sensor value
	 * @return the payload */
	public static byte[] encodePayload(double battery, double temperature, int humidity, int extType, long extValue) {
		return new DraginoLHT65Payload(battery, temperature, humidity, extType, extValue).getBytes();
	}
	
	private static String toHex(byte[] data, boolean cstyle) {
		if (cstyle) {
			var sb= new StringBuffer();
			sb.append('{');
			for (byte b : data) {
				sb.append(sb.length()==1? " 0x" : ", 0x");
				sb.append(Bytes.int8ToHex(b));
			}
			if (sb.length()>1) sb.append(' ');
			return sb.append('}').toString();
		}
		else return Bytes.toHex(data);
	}

	
	public static void main(String[] args) throws IOException {
		var flags= new Flags(args);
		boolean help= flags.getBoolean("-h","prints this message");
		var payload= flags.getString("-d",null,"payload","decodes a payload");
		String[] values= flags.getStringTuple("-e",3,null,"battery temperature humidity","encode a payload");
		String[] extValues= flags.getStringTuple("-E",5,null,"battery temperature humidity extType extValue","encodes a payload with external value");
		boolean base64= flags.getBoolean("-base64","payload is encoded in base64");
		boolean cstyle= flags.getBoolean("-cstyle","comma-separated hex output");
			
		if (help || (payload==null && values==null && extValues==null)) {
			out.println(flags.toUsageString(DraginoLHT65Encoder.class));
			return;
		}
		// else
		
		if (payload!=null) {
			byte[] data= base64? Base64.decode(payload) : Bytes.fromHex(payload);
			out.println(decodePayload(data));
		}
		if (values!=null) {
			double battery= Double.parseDouble(values[0]);
			double temperature= Double.parseDouble(values[1]);
			int humidity= Integer.parseInt(values[2]);
			byte[] data= encodePayload(battery,temperature,humidity);
			out.println(base64? Base64.encode(data) : toHex(data,cstyle));
		}
		if (extValues!=null) {
			double battery= Double.parseDouble(values[0]);
			double temperature= Double.parseDouble(values[1]);
			int humidity= Integer.parseInt(values[2]);
			int extType= Integer.parseInt(values[3]);
			long extValue= Long.parseLong(values[4]);
			byte[] data= encodePayload(battery,temperature,humidity,extType,extValue);
			out.println(base64? Base64.encode(data) : toHex(data,cstyle));
		}
	}

}
