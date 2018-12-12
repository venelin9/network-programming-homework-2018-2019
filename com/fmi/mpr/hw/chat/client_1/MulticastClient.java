import java.util.Scanner;
import java.net.*;
import java.io.*;

public class MulticastClient implements Runnable {
	
	public static void main(String[] args) {
		
		Thread tmc = new Thread(new MulticastClient());
		Thread tp = new Thread(new Prompt());
		tmc.start();
		tp.start();
	}

	@Override
	public void run(){
		try { receiveUDPMessage();}
    		catch (IOException e) { e.printStackTrace(); }
	}
	public static void receiveUDPMessage() throws IOException {
      
		byte[] buffer = new byte[1024];
		MulticastSocket socket = new MulticastSocket(8888);
		
		InetAddress group = InetAddress.getByName("230.0.0.1");
		socket.joinGroup(group);
		
		while(true) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());

			if("END".equals(msg)) {
				break;
			}

			System.out.println(msg);
		}
		socket.leaveGroup(group);
		socket.close();
	}
}

class Prompt implements Runnable {
	
	@Override
	public void run(){
		char c = ' ';
		while (c != 'e' && c != 'q'){
			System.out.println("\nPlease select an option.\n1) Send a text message.\n2) Send an image file.\n3) Send a video file.\ne/q) Exit.");
			try {
				c = (char) System.in.read(); 
			}
			catch (IOException e) { e.printStackTrace(); }
			switch (c){
				case '1': send_text(); break;
				// case '2': send_image(); break;
				// case '3': send_video(); break;
				default: break;
			}
		}
	}

	private static void send_text(){
		String str = "";
		Scanner in = new Scanner(System.in);
		in.nextLine();
		str = in.nextLine();

		try { 
			Socket s = new Socket("127.0.0.1", 8889); 
			PrintWriter out = new PrintWriter(s.getOutputStream(),true);
			out.print(str);
			out.close();
		}
    		catch (UnknownHostException e) { System.out.println("No server found on this host: 127.0.0.1\n"); }
		catch (SocketException ex) { System.out.println("No listening process found on this port: 8889\n"); }
		catch (IOException e) { e.printStackTrace(); }

	}
}
