package io.ipstack.lorawan.mac;

import java.io.IOException;
import java.util.function.Consumer;


/** Communication layer for exchanging LoRaWAN MAC messages.
 */
public interface LorawanMacMessageExchanger {
	
	/** Asynchronous receiver. It passes received LoRaWAN MAC messages to the given consumer. */
	public void receive(Consumer<LorawanMacMessage> receiver);
	
	/** Sends a LoRaWAN MAC message. 
	 * @throws IOException */
	public void send(LorawanMacMessage msg) throws IOException;
}
