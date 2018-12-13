import java.net.*;
import java.io.*;

public class MulticastServer {
	static DatagramSocket socket=null;
	static InetAddress to=null;
	static byte[] msg=null;

	public static void main(String[] args) throws IOException {
		socket = new DatagramSocket();
		to = InetAddress.getByName("230.0.0.1");
		ServerSocket ss = new ServerSocket(8889);
		while(true){
			// try { Thread.sleep(1000); }
			// catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
			// MulticastServer.sendUDPMessage("");	
			Socket s = ss.accept();
			ServerThread t = new ServerThread(s);
			t.start();
		}
	}
	public static void sendUDPMessage(String message) throws IOException {
		msg = message.getBytes();
		DatagramPacket packet = new DatagramPacket(msg, msg.length, to, 8888);
		socket.send(packet);
	}
}

class ServerThread extends Thread{
        Socket s;

        public ServerThread(Socket s){
                this.s = s;
        }
        public void run(){
		String str="";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			while ((str = br.readLine()) != null){
				// System.out.println("Receiving data from Client.");
				System.out.println(str);
				// if (str.equals("END")){
				// 	System.out.println("Closing connection with Client.");
				// 	MulticastServer.sendUDPMessage("END");
				// 	break;
				// }
				// else{
				MulticastServer.sendUDPMessage(str);	
					// MulticastServer.sendUDPMessage("END");
				// }
				// System.out.println("Sending data to Clients.");
			}
   			s.shutdownInput();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
}
