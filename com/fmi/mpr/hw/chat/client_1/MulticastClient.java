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
				case "2": send_image(); break;
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
	private static void send_image(){
		System.out.print("Enter image name: ");
		String str = in.nextLine();
		if(new File("./images/"+str).isFile()) {
			try {
				Socket s = new Socket("127.0.0.1", 8889);
				OutputStream out = s.getOutputStream();
				out.write("--IMG--".getBytes(),0,7);

				BufferedInputStream in = new BufferedInputStream(new FileInputStream("./images/"+str));
				int count = 0;
				byte[] buffer = new byte[1024];
				while( (count = in.read(buffer)) >= 0 ){
					out.write(buffer,0,count);
					out.flush();
				}
				in.close();
				s.shutdownOutput();
			}
			catch (UnknownHostException e) { System.out.println("No server found on this host: 127.0.0.1\n"); }
			catch (SocketException ex) { System.out.println("No listening process found on this port: 8889\n"); }
			catch (IOException e) { e.printStackTrace(); }
		}
		else {
    			System.out.println("Invalid file: " + str);
		}

	}
	private static void send_text(){
		try { 
			System.out.print("Enter message: ");
			String str = in.nextLine();
			str = "--TXT--" + str;

			Socket s = new Socket("127.0.0.1", 8889);
			OutputStream out = s.getOutputStream();
			out.write(str.getBytes(),0,str.length());
			s.shutdownOutput();
		}
		catch (UnknownHostException e) { System.out.println("No server found on this host: 127.0.0.1\n"); }
		catch (SocketException ex) { System.out.println("No listening process found on this port: 8889\n"); }
		catch (IOException e) { e.printStackTrace(); }
	}

	@Override
	public void run(){
		try { 
			byte[] buffer = new byte[1024];
			byte[] fir = new byte[7];
			MulticastSocket socket = new MulticastSocket(8888);
			InetAddress group = InetAddress.getByName("230.0.0.1");
			socket.joinGroup(group);

			while( run == 1 ) {
				// DatagramPacket first = new DatagramPacket(fir, fir.length);
				// socket.receive(first);
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
				System.out.println(msg);
			}
			socket.leaveGroup(group);
			socket.close();
		}
		catch (UnknownHostException e) { System.out.println("No server found on this host: 127.0.0.1\n"); }
		catch (SocketException ex) { System.out.println("No listening process found on this port: 8889\n"); }
    		catch (IOException e) { e.printStackTrace(); }
	}
}
