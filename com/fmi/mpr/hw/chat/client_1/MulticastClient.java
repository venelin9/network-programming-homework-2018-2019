import java.util.Scanner;
import java.util.Arrays;
import java.net.*;
import java.io.*;

public class MulticastClient implements Runnable {
	static volatile int run=1;
	static Scanner in = new Scanner(System.in);
	static Thread tmc = null;
	static int img = 1;
	static int video = 1;

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
				case "3": send_video(); break;
				default: break;
			}
		}
		System.out.println("Exiting...");
		in.close();
		run=0;
		try { tmc.join(); }
		catch (InterruptedException e){ e.printStackTrace(); }
	}

	private static void send_video(){
		System.out.print("Enter video name: ");
		String str = in.nextLine();
		if(new File("./videos/"+str).isFile()) {
			try {
				Socket s = new Socket("127.0.0.1", 8889);
				OutputStream out = s.getOutputStream();
				out.write("--VID--".getBytes(),0,7);

				BufferedInputStream in = new BufferedInputStream(new FileInputStream("./videos/"+str));
				int count = 0;
				byte[] buffer = new byte[4096];
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
				byte[] buffer = new byte[4096];
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
			byte[] buffer = new byte[4096];
			MulticastSocket socket = new MulticastSocket(8888);
			InetAddress group = InetAddress.getByName("230.0.0.1");
			socket.joinGroup(group);
			byte[] end = new byte[7];
			end = "--END--".getBytes();

			while( run == 1 ) {
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				socket.receive(p);
				String msg = new String(p.getData(), p.getOffset(), p.getLength());

				if (msg.substring(0,7).equals("--END--")) continue;

				else if (msg.substring(0,7).equals("--IMG--")){
					while (new File("./images/image_"+MulticastClient.img).isFile()) MulticastClient.img++;

					int size = Integer.parseInt(msg.substring(7,msg.length()));
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("./images/image_" + MulticastClient.img));
					System.out.println("Receiving: image_" + MulticastClient.img);
					MulticastClient.img++;
					byte[] tmp = new byte[7];
					for (int i=0; i<7; i++) tmp[i]=buffer[i];

					while ( size > 0 && !Arrays.equals(tmp,end) ){
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						socket.receive(packet);
						for (int i=0; i<7; i++) tmp[i]=buffer[i];
						if (size > 4096) out.write(buffer,0,4096);
						else out.write(buffer,0,size);
						size-=4096;
						out.flush();
					}
					out.close();
				}

				else if (msg.substring(0,7).equals("--VID--")){
					while (new File("./videos/video_"+MulticastClient.video).isFile()) MulticastClient.video++;

					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("./videos/video_" + MulticastClient.video));
					int size = Integer.parseInt(msg.substring(7,msg.length()));
					System.out.println("Receiving: video_" + MulticastClient.video);
					MulticastClient.video++;
					byte[] tmp = new byte[7];
					for (int i=0; i<7; i++) tmp[i]=buffer[i];

					while ( size > 0 && !Arrays.equals(tmp,end) ){
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						socket.receive(packet);
						for (int i=0; i<7; i++) tmp[i]=buffer[i];
						if (size > 4096) out.write(buffer,0,4096);
						else out.write(buffer,0,size);
						size-=4096;
						out.flush();
					}
					out.close();
				}

				else if (msg.substring(0,7).equals("--TXT--")){
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					String s = new String(buffer);
					System.out.println(">> " + s);
				}
			}
			socket.leaveGroup(group);
			socket.close();
		}
		catch (UnknownHostException e) { System.out.println("No server found on this host: 127.0.0.1\n"); }
		catch (SocketException ex) { System.out.println("No listening process found on this port: 8889\n"); }
    		catch (IOException e) { e.printStackTrace(); }
	}
}
