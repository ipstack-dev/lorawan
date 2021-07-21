package it.unipr.netsec.ipstack.lorawan;


import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


abstract class AesCipher {

	public static Cipher getEncryptionInstance(byte[] key) throws GeneralSecurityException {
		Cipher cipher=Cipher.getInstance("AES/ECB/NoPadding");
		SecretKey secret_key=new SecretKeySpec(key,"AES");
		cipher.init(Cipher.ENCRYPT_MODE,secret_key);
		return cipher;
	}

	public static Cipher getDecryptionInstance(byte[] key) throws GeneralSecurityException {
		Cipher cipher=Cipher.getInstance("AES/ECB/NoPadding");
		SecretKey secret_key=new SecretKeySpec(key,"AES");
		cipher.init(Cipher.DECRYPT_MODE,secret_key);
		return cipher;
	}

}
