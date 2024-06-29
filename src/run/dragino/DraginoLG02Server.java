package run.dragino;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;


/** Simple TCP server that can be used for receiving sensor data from a Dragino LG02 gateway in TCP client mode.
 */
public class DraginoLG02Server {
	
	private byte[] buffer= new byte[16000];

	public DraginoLG02Server(int port) throws IOException {
		System.out.println("TCP server on port "+port);
		var serverSocket= new ServerSocket(port);
		while (true) {
			var socket= serverSocket.accept();
			System.out.println("\nnew connection: "+socket.getRemoteSocketAddress());
			InputStream is= socket.getInputStream();
			int len;
			while ((len= is.read(buffer))>0) {
				System.out.println("received: "+new String(buffer,0,len).trim());
			}
			socket.close();
			System.out.println("closed: "+socket.getRemoteSocketAddress());
		}
	}

	
	public static void main(String[] args) throws IOException {
		new DraginoLG02Server(8008);
	}

}
