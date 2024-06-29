package io.ipstack.lorawan.mac;


import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.zoolu.util.Bytes;


/** Data frame that is the Payload of a Data MAC message.
 * <p>
 * it is formed by a frame header (FHDR) followed by an optional port field (FPort) and an optional frame payload field (FRMPayload).
 */
public class LorawanDataMacPayload {

	/** Device address */
	protected byte[] devAddr;
	
	/** Frame control (FCtrl) */
	protected FCtrl fCtrl;
	
	/** Frame counter (FCnt) */
	protected int fCnt;

	/** Frame options (FOpts) */
	protected byte[] fOpts;
	
	/** Frame port (FPort) */
	protected int fPort=-1;
	
	/** Whether it is an uplink frame */
	protected boolean uplink;

	/** Decrypted frame payload (FRMPayload) */
	protected byte[] decryptedPayload= null;

	/** Encrypted frame payload (FRMPayload) */
	protected byte[] encryptedPayload= null;


		
	/** Creates a new Data MAC Payload frame.
	 * @param devAddr Device address
	 * @param fCtrl Frame control (FCtrl)
	 * @param fCnt Frame counter (FCnt)
	 * @param fOpts buffer containing the Frame options (FOpts); the length is specified in the FCtrl field
	 * @param fPort Frame port (FPort)
	 * @param uplink whether it is an up link message
	 * @param payload Frame payload (FRMPayload)
	 * @param key encryption key (use NwkSKey/NwkSEncKey if FPort=0, AppSKey if FPort>0)
	 * @throws GeneralSecurityException */
	public LorawanDataMacPayload(byte[] devAddr, FCtrl fCtrl, int fCnt, byte[] fOpts, int fPort, boolean uplink, byte[] payload, byte[] key) throws GeneralSecurityException {
		this.devAddr= devAddr;
		this.fCtrl= fCtrl;
		this.fCnt= fCnt;
		this.fOpts= fOpts;
		this.fPort= fPort;
		this.uplink= uplink;
		decryptedPayload= payload;
		encryptedPayload= encrypt(key,payload);
	}

	/** Creates a new Data MAC Payload frame.
	 * @param data bytes of the Data MAC Payload frame
	 * @param uplink whether it is an up link message */
	 public LorawanDataMacPayload(byte[] data, boolean uplink) {
		this(data,0,data.length,uplink);
	}

	/** Creates a new Data MAC Payload frame.
	 * @param buf buffer containing the frame
	 * @param off offset within the buffer
	 * @param len payload length
	 * @param uplink whether it is an up link message */
	public LorawanDataMacPayload(byte[] buf, int off, int len, boolean uplink) {
		this.uplink= uplink;
		devAddr= Bytes.reverseOrderCopy(buf,off,4);
		fCtrl= new FCtrl(buf[off+4]);
		fCnt= Bytes.toInt16LittleEndian(buf,off+5);
		int fOptsLen= fCtrl.getfOptsLen();
		fOpts= fOptsLen>0? Bytes.copy(buf,off+7,fOptsLen) : null;
		if (len>7+fOptsLen) {
			fPort= 0xff&buf[off+7+fOptsLen];
			encryptedPayload= len>8+fOptsLen? Bytes.copy(buf,off+8+fOptsLen,len-8-fOptsLen) : null;
		}
	}
	
	/** Gets frame length.
	 * @return the length */
	public int getLength() {
		int len= 7;
		if (fOpts!=null && fOpts.length>0) len+=fOpts.length&0x0f;
		if (encryptedPayload!=null && encryptedPayload.length>0) len+=1+encryptedPayload.length;
		return len;
	}

	/** Gets frame bytes.
	 * @return the bytes 
	 * @throws GeneralSecurityException */
	public byte[] getBytes() throws GeneralSecurityException {
		var data= new byte[getLength()];
		getBytes(data,0);
		return data;
	}

	/** Gets frame bytes.
	 * @param buf buffer where Data MAC Payload frame has to be written
	 * @param off offset within the buffer
	 * @return the payload length 
	 * @throws GeneralSecurityException */
	public int getBytes(byte[] buf, int off) throws GeneralSecurityException {
		// FHDR
		Bytes.reverseOrderCopy(devAddr,0,buf,off,devAddr.length);
		buf[off+4]= fCtrl.getVal();
		Bytes.fromInt16LittleEndian(fCnt,buf,off+5);
		int fOptsLen= fCtrl.getfOptsLen();
		if (fOptsLen>0) System.arraycopy(fOpts,0,buf,off+7,fOptsLen);
		int len= 7+fOptsLen;
		if (encryptedPayload!=null && encryptedPayload.length>0) {
			// FPort
			buf[off+7+fOptsLen]= (byte)fPort;
			// FRMPayload
			System.arraycopy(encryptedPayload,0,buf,off+7+fOptsLen+1,encryptedPayload.length);
			len+= 1+encryptedPayload.length;
		}
		return len;
	}
	
	/** Gets device address.
	 * @return the address */
	public byte[] getDevAddr() {
		return devAddr;
	}
		
	/** Gets frame control (FCtrl).
	 * @return the FCtrl value */
	public FCtrl getFControl() {
		return fCtrl;
	}
		
	/** Gets frame counter (FCnt).
	 * @return the FCnt value */
	public int getFCounter() {
		return fCnt;
	}
		
	/** Gets frame options (FOpts).
	 * @return the FOpts value */
	public byte[] getFOptions() {
		return fOpts;
	}
		
	/** Gets frame port (FPort).
	 * @return the FPort value */
	public int getFPort() {
		return fPort;
	}
		
	/** Gets frame payload.
	 * @return the payload */
	public byte[] getFramePayload() {
		return decryptedPayload!=null? decryptedPayload : encryptedPayload;
	}

	/** Decrypts frame payload.
	 * @param key secret key
	 * @throws GeneralSecurityException */
	public void decryptFramePayload(byte[] key) throws GeneralSecurityException {
		decryptedPayload= encryptedPayload!=null? encrypt(key,encryptedPayload) : null;
	}

	/** Gets frame payload.
	 * @param key secret key
	 * @return the payload 
	 * @throws GeneralSecurityException */
	public byte[] getFramePayload(byte[] key) throws GeneralSecurityException {
		return decryptedPayload= encryptedPayload!=null? encrypt(key,encryptedPayload) : null;
	}

	/** Encrypts/decrypts the given data.
	 * @param key encryption key
	 * @param src input data
	 * @return output data
	 * @throws GeneralSecurityException */
	private byte[] encrypt(byte[] key, byte[] src) throws GeneralSecurityException {
		byte[] dst= new byte[src.length];
		byte[] stream= getKeyStream(key,uplink,devAddr,fCnt,src.length);
		for (int i=0; i<src.length; i++) {
			dst[i]= (byte)(src[i]^stream[i]);
		}
		return dst;		
	}
	
	/** Gets encryption stream.
	 * @param key NwkSEncKey or AppSKey (if FPort=0 then NwkSEncKey, otherwise AppSKey)
	 * @param uplink whether it is uplink (or downlink)
	 * @param devAddr DevAddr
	 * @param counter FCntUp or NFCntDwn or AFCntDnw
	 * @param len payload/stream length
	 * @return the stream
	 * @throws GeneralSecurityException */
	private static byte[] getKeyStream(byte[] key, boolean uplink, byte[] devAddr, int counter, int len) throws GeneralSecurityException {
		len= ((len-1)/16+1)*16;
		var s= new byte[len];
		var a= new byte[16]; // Ai
		a[0]= 0x01;
		Bytes.fill(a,1,4,(byte)0);	
		a[5]= uplink? (byte)0 : (byte)1;
		Bytes.reverseOrderCopy(devAddr,0,a,6,4);
		Bytes.fromInt32LittleEndian(counter,a,10);
		Cipher cipher= AesCipher.getEncryptionInstance(key);
		a[14]= (byte)0;
		for (int i=0; i<len/16; i++) {
			a[15]= (byte)(i+1);
			cipher.doFinal(a,0,16,s,i*16);
		}
		return s;
	}

	@Override
	public String toString() {
		return toString(", ");
	}

	/** Gets a string representation of this object.
	 * Different field descriptions are separated by the given delimiter.
	 * @param delim the field delimiter
	 * @return the string representation */
	public String toString(String delim) {
		var sb= new StringBuffer();
		sb.append("DevAddr: ").append(Bytes.toHex(devAddr));
		sb.append(delim).append("FCtrl: ").append(fCtrl.toString());
		sb.append(delim).append("FCnt: ").append(fCnt);
		if (fOpts!=null) sb.append(delim).append("FOpts: ").append(Bytes.toHex(fOpts));
		sb.append(delim).append("FPort: ").append(fPort);
		if (decryptedPayload!=null) sb.append(delim).append("FRMPayload: ").append(Bytes.toHex(decryptedPayload));		
		else sb.append(delim).append("EncryptedFRMPayload: ").append(encryptedPayload!=null? Bytes.toHex(encryptedPayload) : null);		
		return sb.toString();
	}

}
