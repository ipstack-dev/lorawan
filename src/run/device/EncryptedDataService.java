package run.device;


import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.zoolu.util.Bytes;

import io.ipstack.lorawan.device.service.DataService;
import io.ipstack.lorawan.mac.AesCipher;


public class EncryptedDataService implements DataService {
	
	public static boolean VERBODE= true;
	
	private static void log(String str) {
		System.out.println("DEBUG: "+EncryptedDataService.class.getSimpleName()+": "+str);
	}
	
	DataService service;
	byte[] key;
	Cipher encryption= null, decryption= null;

	public EncryptedDataService(DataService service, byte[] key) {
		this.service= service;
		this.key= key;
	}
	
	@Override
	public byte[] getData() {
		byte[] plaintext= service.getData();
		if (VERBODE) log("getData(): plaitext: "+Bytes.toHex(plaintext));
		try {
			if (encryption==null) encryption= AesCipher.getEncryptionInstance(key);
			byte[] ciphertext= encryption.doFinal(pad(plaintext));
			if (VERBODE) log("getData(): ciphertext: "+Bytes.toHex(ciphertext));
			return ciphertext;
		}
		catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setData(byte[] ciphertext) {
		if (VERBODE) log("getData(): ciphertext: "+Bytes.toHex(ciphertext));
		try {
			if (decryption==null) decryption= AesCipher.getDecryptionInstance(key);
			byte[] plaintext= unpad(decryption.doFinal(ciphertext));
			if (VERBODE) log("getData(): plaitext: "+Bytes.toHex(plaintext));
			service.setData(plaintext);
		}
		catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] pad(byte[] data) {
		int len= 1+(16-(data.length+1)%16)%16;
		byte[] paddedData= new byte[data.length+len];
		System.arraycopy(data,0,paddedData,0,data.length);
		paddedData[paddedData.length-1]=(byte)data.length;
		return paddedData;
	}

	public static byte[] unpad(byte[] paddedData) {
		int len= paddedData[paddedData.length-1]&0xFF;
		var data =new byte[len];
		System.arraycopy(paddedData,0,data,0,len);
		return data;
	}

}
