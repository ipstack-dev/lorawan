package io.ipstack.lorawan.device.service;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zoolu.util.Bytes;


/** Service that reads data from and writes data to a file.
 * The file name is passed to the constructor.
 */
public class FileData implements DataService {
	
	public static final int MAXIMUM_PAYLOAD_SIZE=8096;
	
	String fileName;

	
	public FileData(String[] args) {
		this(args[0]);
	}
	
	public FileData(String fileName) {
		this.fileName=fileName;
	}
	
	@Override
	public synchronized byte[] getData() {
		try {
			var file= new File(fileName);
			long size=file.length();
			if (size>MAXIMUM_PAYLOAD_SIZE) throw new IOException("Payload size exceeds the maximum defined value ("+MAXIMUM_PAYLOAD_SIZE+"): "+size);
			InputStream is= new FileInputStream(file);
			
			byte[] data= new byte[(int)size];
			int len= is.read(data,0,data.length);
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
