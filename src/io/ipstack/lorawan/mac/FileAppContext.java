package io.ipstack.lorawan.mac;


import java.io.File;
import java.io.IOException;

import org.zoolu.util.config.Configure;


/** LoRaWAN Application Context stored in a file.
 */
public class FileAppContext extends AppContext {
	
	String fileName;
	
	
	/** Creates a new context.
	 * @param fileName context file name
	 * @throws IOException 
	 */
	public FileAppContext(String fileName) throws IOException {
		this.fileName= fileName;
		Configure.fromFile(new File(fileName),this);
	}

	/** Save the context.
	 * @throws IOException 
	 */
	public void save() throws IOException {
		var config= new Configure(this,true);
		config.saveChanges(fileName);
	}
	
	/** 
	 *Increments the DevNonce it by 1.
	 */
	public void incDevNonce() {
		devNonce++;
		try {
			save();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
