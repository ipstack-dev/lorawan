package it.unipr.netsec.ipstack.lorawan.device;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zoolu.util.Bytes;


/** Device with readable and writable data stored in a file.
 * The file name is passed to the constructor.
 */
public class FileDevice implements Device {
	
	public static final int MAXIMUM_PAYLOAD_SIZE=8096;
	
	String fileName;

	
	public FileDevice(String[] args) {
		this(args[0]);
	}
	
	public FileDevice(String fileName) {
		this.fileName=fileName;
	}
	
	@Override
	public synchronized byte[] getData() {
		try {
			File file=new File(fileName);
			long size=file.length();
			if (size>MAXIMUM_PAYLOAD_SIZE) throw new IOException("Payload size exceeds the maximum defined value ("+MAXIMUM_PAYLOAD_SIZE+"): "+size);
			InputStream is=new FileInputStream(file);
			
			byte[] data=new byte[(int)size];
			int len=is.read(data,0,data.length);
			is.close();
			if (len==data.length) return data;
			else return Bytes.copy(data,0,len);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public synchronized void setData(byte[] data) {
		try {
			OutputStream os=new FileOutputStream(fileName);
			os.write(data);
			os.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
