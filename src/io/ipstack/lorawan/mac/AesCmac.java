package io.ipstack.lorawan.mac;

import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/** AES-CMAC algorithm as specified by RFC 4493.
 */
public class AesCmac {

	static final byte[] ZEROS= new byte[16];
	static final AlgorithmParameterSpec IV0= new IvParameterSpec(ZEROS);
	
	Cipher aesCbc;
	byte[] k1;
	byte[] k2;	
	byte[] lastBlock= new byte[16];
	int lastLen= 0;	

	
	/** Initializes the algorithm with a given secret key.
	 * @param key the secret key
	 * @throws GeneralSecurityException */
	public AesCmac(byte[] key) throws GeneralSecurityException {
		SecretKeySpec secretKey= new SecretKeySpec(key,"AES");
		aesCbc=Cipher.getInstance("AES/CBC/NoPadding");
		aesCbc.init(Cipher.ENCRYPT_MODE,secretKey,IV0);
		byte[] L= aesCbc.doFinal(ZEROS);
		k1= generateSubkey(L);
		k2= generateSubkey(k1);
		aesCbc.init(Cipher.ENCRYPT_MODE,secretKey,IV0);
	}
	
	/** Processes a portion of the input message.
	 * @param data the input bytes to be processed
	 * @throws ShortBufferException */
	public void update(byte[] data) throws ShortBufferException {
		if (data.length==0) return;
		// else
		int len= lastLen+data.length;
		int newLastLen= (len-1)%16+1;
		len-= newLastLen;
		if (len>0) {
			if (lastLen>0) aesCbc.update(lastBlock,0,lastLen);
			aesCbc.update(data,0,len-lastLen);
			System.arraycopy(data,len-lastLen,lastBlock,0,newLastLen);
		}
		else {
			System.arraycopy(data,0,lastBlock,lastLen,newLastLen-lastLen);
		}
		lastLen= newLastLen;
	}
	
	/** Processes the last input message bytes and computes the MAC.
	 * @param data the input bytes to be processed
	 * @return the MAC
	 * @throws GeneralSecurityException */
	public byte[] doFinal(byte[] data) throws GeneralSecurityException {
		update(data);
		return doFinal();
	}

	/** Computes the MAC.
	 * @return the MAC
	 * @throws GeneralSecurityException */
	public byte[] doFinal() throws GeneralSecurityException {
		byte[] k;
		if (lastLen==16) k= k1;
		else {
			k= k2;
			lastBlock[lastLen++]= (byte)0x80;
			while (lastLen<16) lastBlock[lastLen++]= 0x00;
		}
		for (int i=0; i<16; i++) lastBlock[i]^= k[i];
		return aesCbc.doFinal(lastBlock);
	}
	
	/** Generates subkey i from subkey i-1.
	 * @param k0 the input subkey
	 * @return the new subkey
	 * @throws GeneralSecurityException */
	private static byte[] generateSubkey(byte[] k0) throws GeneralSecurityException {
		var k1= new byte[16];
		for (int i=0; i<15; i++) k1[i]= (byte)((k0[i]<<1)|(k0[i+1]>>7)&0x01);
		k1[15]= (k0[0]&0x80)==0x80? (byte)((k0[15]<<1)^0x87) : (byte)(k0[15]<<1);
		return k1;
	}
}
