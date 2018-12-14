import java.util.Scanner;
import java.net.*;
import java.io.*;

public class MulticastClient implements Runnable {
	static volatile int run=1;
	static Scanner in = new Scanner(System.in);
	static Thread tmc = null;
	public static void main(String[] args) {
		tmc = new Thread(new MulticastClient());
		tmc.start();
		prompt();
	}
	private static void prompt(){
		String c = "";
		System.out.println("Please select an option.\t1) Send a text message.\t2) Send an image file.\t3) Send a video file.\te/q) Exit.");
		while (!c.equals("e") && !c.equals("q")){
			c=in.nextLine();
			switch (c){
				case "1": send_text(); break;
				// case '2': send_image(); break;
				// case '3': send_video(); break;
				default: break;
			}
		}
		System.out.println("Exiting...");
		in.close();
		run=0;
		try { tmc.join(); }
		catch (InterruptedException e){ e.printStackTrace(); }
	}
	// private static void send_image(){
	// 	String str = "";
	// 	System.out.print("Enter image name: ");
	// 	str = in.nextLine();
	// 	if(new File("./images/"+str).isFile()) {
	// 		try {
	// 			BufferedReader in = new BufferedReader(new FileReader("./logs/"+file_name));
	// 			System.out.println("Printing contents of: " + file_name + '\n');
	// 			String line = null;
	// 			while((line = in.readLine()) != null){
	// 				System.out.println(line);
	// 			}
	// 		}
	// 		catch (IOException e){
	// 			e.printStackTrace();
	// 		}
	// 	}
	// 	else {
    	// 		System.out.println("Invalid file: " + str);
	// 	}
	//
	// }
	private static void send_text(){
		try { 
			Socket s = new Socket("127.0.0.1", 8889);
			OutputStream out=s.getOutputStream();
			String str = "";

			out.write("--TEXT--".getBytes(),0,8);

			System.out.print("Enter message: ");
			str = in.nextLine();
			byte[] msg = str.getBytes();
			out.write(msg,0,msg.length);

			out.write("--END--".getBytes(),0,7);
			s.shutdownOutput();
		}
		catch (UnknownHostException e) { System.out.println("No server found on this host: 127.0.0.1\n"); }
		catch (SocketException ex) { System.out.println("No listening process found on this port: 8889\n"); }
		catch (IOException e) { e.printStackTrace(); }
	}
	private static void receiveUDPMessage() throws IOException {
		byte[] buffer = new byte[1024];
		MulticastSocket socket = new MulticastSocket(8888);
		InetAddress group = InetAddress.getByName("230.0.0.1");
		socket.joinGroup(group);
		while( run == 1 ) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			if (!msg.equals("")) System.out.println(msg);
		}
		socket.leaveGroup(group);
		socket.close();
	}
	@Override
	public void run(){
		try { receiveUDPMessage();}
    		catch (IOException e) { e.printStackTrace(); }
	}
}
