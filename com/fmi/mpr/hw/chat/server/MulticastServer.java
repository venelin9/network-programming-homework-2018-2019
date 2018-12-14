import java.net.*;
import java.io.*;

public class MulticastServer {
	static DatagramSocket socket=null;
	static InetAddress to=null;

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
		byte[] msg = message.getBytes();
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
				if (str.substring(0,8).equals("--TEXT--")){
					System.out.println("Receiving text from Client.");
					System.out.println(str);
					MulticastServer.sendUDPMessage(str.substring(0,str.length()-7));
				}
				else if (str.substring(0,9).equals("--IMAGE--")){
					System.out.println("Receiving text from Client.");
					String[] tokens = str.split("--");
					System.out.println("Receiving image from Client: " + tokens[1]);
				}
			}
   			s.shutdownInput();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
}
