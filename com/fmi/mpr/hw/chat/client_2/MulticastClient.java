import java.util.Scanner;
import java.net.*;
import java.io.*;

public class MulticastClient implements Runnable {
	static int run=1;
	static Scanner in = new Scanner(System.in);
	public static void main(String[] args) {
		Thread tmc = new Thread(new MulticastClient());
		tmc.start();
		prompt();
	}
	public static void prompt(){
		char c = ' ';
		while (c != 'e' && c != 'q'){
			System.out.println("Please select an option.\t1) Send a text message.\t2) Send an image file.\t3) Send a video file.\te/q) Exit.");
			try { c = (char) System.in.read(); }
			catch (IOException e) { e.printStackTrace(); }
			switch (c){
				case '1': send_text(); break;
				// case '2': send_image(); break;
				// case '3': send_video(); break;
				default: break;
			}
		}
		System.out.println("ending");
		run=0;
		in.close();
	}
	private static void send_text(){
		try { 
			Socket s = new Socket("127.0.0.1", 8889);
			in.nextLine();
			String str = "";
			System.out.print("Enter message: ");
			str = in.nextLine();
			byte[] msg = str.getBytes();
			OutputStream out=s.getOutputStream();
			out.write(msg,0,msg.length);
			s.shutdownOutput();
		}
		catch (UnknownHostException e) { System.out.println("No server found on this host: 127.0.0.1\n"); }
		catch (SocketException ex) { System.out.println("No listening process found on this port: 8889\n"); }
		catch (IOException e) { e.printStackTrace(); }
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
		while( run == 1 ) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			if (run==0) break;
			socket.receive(packet);
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			System.out.println(msg);
		}
		socket.leaveGroup(group);
		socket.close();
	}
}
