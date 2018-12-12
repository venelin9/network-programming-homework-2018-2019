import java.net.*;
import java.io.*;

public class MulticastClient implements Runnable {
	
	public static void main(String[] args) {
		
		Thread t = new Thread(new MulticastClient());
		t.start();
	}

	public void receiveUDPMessage() throws IOException {
      
		byte[] buffer = new byte[1024];
		MulticastSocket socket = new MulticastSocket(8888);
		
		InetAddress group = InetAddress.getByName("230.0.0.1");
		socket.joinGroup(group);
		
		while(true) {
			System.out.println("Waiting for multicast message...");
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			System.out.println(msg);
			
			if("END".equals(msg)) {
				System.out.println("No more messages. Exiting : " + msg);
				break;
			}
		}
		socket.leaveGroup(group);
		socket.close();
	}

	private static void prompt(){
		char c = '_';
		while (c != 'e' && c != 'q'){
			print();
			System.out.println("Please select an option.\n1) Send text message.\n2) Send image file.\n3) Send video file.\ne/q) Exit.");
			try {
				c = (char) System.in.read(); 
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			switch (c){
				case '1': send_text(); break;
				// case '2': send_image(); break;
				// case '3': send_video(); break;
				default: break;
			}
			receiveUDPMessage();
		}
	}

	private static void send_text(){



	}


	@Override
	public void run(){
		
		try {
			prompt();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

}
