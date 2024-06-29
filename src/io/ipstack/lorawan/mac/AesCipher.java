package io.ipstack.lorawan.mac;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public final class AesCipher {
	private AesCipher() {}

	public static Cipher getEncryptionInstance(byte[] key) throws GeneralSecurityException {
		var cipher= Cipher.getInstance("AES/ECB/NoPadding");
		SecretKey secret_key=new SecretKeySpec(key,"AES");
		cipher.init(Cipher.ENCRYPT_MODE,secret_key);
		return cipher;
	}

	public static Cipher getDecryptionInstance(byte[] key) throws GeneralSecurityException {
		var cipher= Cipher.getInstance("AES/ECB/NoPadding");
		SecretKey secret_key= new SecretKeySpec(key,"AES");
		cipher.init(Cipher.DECRYPT_MODE,secret_key);
		return cipher;
	}

}
