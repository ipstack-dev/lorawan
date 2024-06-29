package io.ipstack.lorawan.semtech;

import io.ipstack.lorawan.mac.EUI;
import io.ipstack.lorawan.semtech.json.RxPacketInfo;
import io.ipstack.lorawan.semtech.json.StatusInfo;


public interface SemtechServerListener {
	/**
	 * @param gwEUI remote gateway EUI, source of the received packet
	 * @param rxPktInfo received rxpk JSON object
	 */
	public void onRxPacket(EUI gwEUI, RxPacketInfo rxPktInfo);
	
	/**
	 * @param gwEUI remote gateway EUI, source of the received packet
	 * @param statusInfo received status JSON object
	 */
	public void onStatus(EUI gwEUI, StatusInfo statusInfo);
}
