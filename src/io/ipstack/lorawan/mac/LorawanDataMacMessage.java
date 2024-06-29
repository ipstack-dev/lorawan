package io.ipstack.lorawan.mac;


import java.security.GeneralSecurityException;

import org.zoolu.util.Bytes;


/** LoRaWAN Data MAC message, used to transfer both MAC commands and application data.
 */
public class LorawanDataMacMessage extends LorawanMacMessage {

	/** Data MAC payload */
	LorawanDataMacPayload dataMacPayload;

	
	/** Creates a new Data message.
	 * @param devAddr Device address
	 * @param fCnt Frame counter (FCnt)
	 * @param fPort Frame port (FPort)
	 * @param data Frame payload (FRMPayload)
	 * @param key encryption key (use NwkSKey/NwkSEncKey if FPort=0, AppSKey if FPort>0)
	 * @param key MIC key
	 * @throws GeneralSecurityException */
	public LorawanDataMacMessage(int type, byte[] devAddr, int fCnt, int fPort, byte[] data, byte[] eKey, byte[] iKey) throws GeneralSecurityException {
		this(type,devAddr,new FCtrl(false,false,false,false,0),fCnt,null,fPort,data,eKey,iKey);
	}

	/** Creates a new Data message.
	 * @param type Data message type (UNCORFIRMED_DATA_UP, UNCORFIRMED_DATA_DOWN, UORFIRMED_DATA_UP, or CORFIRMED_DATA_DOWN)
	 * @param devAddr Device address
	 * @param fCtrl Frame control (FCtrl)
	 * @param fCnt Frame counter (FCnt)
	 * @param fOpts Frame options (FOpts)
	 * @param fPort Frame port (FPort)
	 * @param data Frame payload (FRMPayload) 
	 * @param key encryption key (use NwkSKey/NwkSEncKey if FPort=0, AppSKey if FPort>0)
	 * @param key MIC key
	 * @throws GeneralSecurityException */
	public LorawanDataMacMessage(int type, byte[] devAddr, FCtrl fCtrl, int fCnt, byte[] fOpts, int fPort, byte[] data, byte[] eKey, byte[] iKey) throws GeneralSecurityException {
		super(type);
		dataMacPayload= new LorawanDataMacPayload(devAddr,fCtrl,fCnt,fOpts,fPort,isUplink(type),data,eKey);
		payload= dataMacPayload.getBytes();
		byte[] B0= Bytes.concat(new byte[]{(byte)0x49,0,0,0,0,(byte)(type%2)},Bytes.reverseOrderCopy(devAddr),Bytes.fromInt32LittleEndian(fCnt),new byte[]{0,(byte)(payload.length+1)});
		/*var cmac= new AesCmacJuho();
		cmac.init(new SecretKeySpec(iKey,"AES"));
		cmac.updateBlock(Bytes.concat(B0,new byte[]{(byte)getMHdr()},payload));
		mic= Bytes.copy(cmac.doFinal(),0,4);*/
		mic= Bytes.copy(new AesCmac(iKey).doFinal(Bytes.concat(B0,new byte[]{(byte)getMHdr()},payload)),0,4);
	}

	/** Creates a new Data message.
	 * @param data the buffer containing the packet
	 * @param uplink whether it is an up link message */
	public LorawanDataMacMessage(byte[] data, boolean uplink) {
		this(data,0,data.length,uplink);
	}

	/** Creates a new Data message.
	 * @param buf the buffer containing the packet
	 * @param off the offset within the buffer
	 * @param len packet length
	 * @param uplink whether it is an up link message */
	public LorawanDataMacMessage(byte[] buf, int off, int len, boolean uplink) {
		super(buf,off,len);
		if (type!=TYPE_UNCORFIRMED_DATA_UP && type!=TYPE_UNCORFIRMED_DATA_DOWN && type!=TYPE_CORFIRMED_DATA_UP && type!=TYPE_CORFIRMED_DATA_DOWN) throw new RuntimeException("It isn't a Data message ("+type+")");
		dataMacPayload= new LorawanDataMacPayload(payload,uplink);
	}
	
	/** Gets device address.
	 * @return the address */
	/*public byte[] getDevAddr() {
		return dataMacPayload.getDevAddr();
	}*/
		
	/** Gets frame control (FCtrl).
	 * @return the FCtrl value */
	/*public FCtrl getFControl() {
		return dataMacPayload.getFControl();
	}*/
		
	/** Gets frame counter (FCnt).
	 * @return the FCnt value */
	/*public int getFCounter() {
		return dataMacPayload.getFCounter();
	}*/
		
	/** Gets frame options (FOpts).
	 * @return the FOpts value */
	/*public byte[] getFOptions() {
		return dataMacPayload.getFOptions();
	}*/
		
	/** Gets frame port (FPort).
	 * @return the FPort value */
	/*public int getFPort() {
		return dataMacPayload.getFPort();
	}*/
		
	/** Gets frame payload.
	 * @return the payload */
	/*public byte[] getFramePayload() {
		return dataMacPayload.getFramePayload();
	}*/

	/** Gets frame payload.
	 * @param key secret key
	 * @return the payload 
	 * @throws GeneralSecurityException */
	/*public byte[] getFramePayload(byte[] key) throws GeneralSecurityException {
		return dataMacPayload.getFramePayload(key);
	}*/
	
	/** Gets Data MAC payload (MACPayload).
	 * @return the MAC payload */
	public LorawanDataMacPayload getDataMacPayload() {
		return dataMacPayload;
	}


}
