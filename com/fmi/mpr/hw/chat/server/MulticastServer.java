import java.net.*;
import java.io.*;

public class MulticastServer {

	static int number = 0;
	
	public static void sendUDPMessage(String message) throws IOException {

		DatagramSocket socket = new DatagramSocket();
		
		InetAddress to = InetAddress.getByName("230.0.0.1");
		
		byte[] msg = message.getBytes();
		
		DatagramPacket packet = new DatagramPacket(msg, msg.length, to, 8888);
		
		socket.send(packet);
		socket.close();
	}
	
	public static void main(String[] args) throws IOException {

		ServerSocket ss = new ServerSocket(8889);

		while(true){
			Socket s = ss.accept();
			ServerThread t = new ServerThread(s);
			t.start();
		}
	}
}


class ServerThread extends Thread{
        Socket s;
	int num;

        public ServerThread(Socket s){
                this.s = s;
		num = ++MulticastServer.number;
		System.out.println("New connection established: Client #" + num);
        }

        public void run(){

		System.out.println("Receiving data from Client #" + num + "...");

		String str="";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

			while ((str = br.readLine()) != null){
				if (str.equals("END")){
					System.out.println("Closing connection with Client #" + num);
					MulticastServer.sendUDPMessage("END");
					break;
				}
				else{
					MulticastServer.sendUDPMessage("Client #" + num + ": " + str);	
					MulticastServer.sendUDPMessage("END");
				}
			}
   			br.close();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
}
