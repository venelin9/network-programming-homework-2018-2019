import java.net.*;
import java.io.*;

public class MulticastServer {
	static DatagramSocket socket=null;
	static InetAddress to=null;
	static int img = 1;
	static int video = 1;

	public static void main(String[] args) throws IOException {

		socket = new DatagramSocket();
		to = InetAddress.getByName("230.0.0.1");
		ServerSocket ss = new ServerSocket(8889);
		System.out.println("Server Started");
		while(true){
			Socket s = ss.accept();
			ServerThread t = new ServerThread(s);
			t.start();
		}
	}

	public static void sendEnd() throws IOException {
		DatagramPacket end = new DatagramPacket("--END--".getBytes(), 7, to, 8888);
		socket.send(end);
	}

	public static void sendMessage(byte[] msg) throws IOException {
		DatagramPacket packet = new DatagramPacket(msg, msg.length, to, 8888);
		socket.send(packet);
	}
	
	public static void sendStart(int type, int length) throws IOException {
		if (type == 1){
			String s = "--IMG--" + length;
			DatagramPacket first = new DatagramPacket(s.getBytes(), s.length(), to, 8888);
			socket.send(first);
		}
		else if (type == 2){
			String s = "--VID--" + length;
			DatagramPacket first = new DatagramPacket(s.getBytes(), s.length(), to, 8888);
			socket.send(first);
		}
		else if (type == 0){
			String s = "--TXT--";
			DatagramPacket first = new DatagramPacket(s.getBytes(), s.length(), to, 8888);
			socket.send(first);
		}
	}
		
}

class ServerThread extends Thread{
        Socket soc;

        public ServerThread(Socket s){
                this.soc = s;
        }
        public void run(){
		try {
			byte[] type = new byte[7];
			byte[] data = new byte[4096];
			DataInputStream input = new DataInputStream(soc.getInputStream());
			input.readFully(type,0,7);
			String s = new String(type);

			if (s.equals("--TXT--")){
				System.out.println(">> Text");
				// int count = 0;
				while ( input.read(data) >=0 ){
					// if ( count == 0 ){ MulticastServer.sendStart(0,0); count = 1; }
					MulticastServer.sendStart(0,0);
					MulticastServer.sendMessage(data);
				}
				MulticastServer.sendEnd();
				MulticastServer.sendEnd();
			}

			else if (s.equals("--IMG--")){
				System.out.println(">> Image");
				while (new File("./images/image_"+MulticastServer.img).isFile()) MulticastServer.img++;
				BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream("./images/image_" + MulticastServer.img));
				int count = 0;
				int size = 0;
				while(( count=input.read(data)) >= 0){
					output.write(data, 0, count);
					output.flush();
					size+=count;
				}
				output.close();
				count = 0;
				BufferedInputStream fin = new BufferedInputStream(new FileInputStream("./images/image_" + MulticastServer.img));
				while ( fin.read(data) >= 0 ){
					if ( count == 0 ) { MulticastServer.sendStart(1,size); count = 1; }
					MulticastServer.sendMessage(data);
				}
				fin.close();
				MulticastServer.img++;
				MulticastServer.sendEnd();
				MulticastServer.sendEnd();
			}

			else if (s.equals("--VID--")){
				System.out.println(">> Video");
				while (new File("./videos/video_"+MulticastServer.video).isFile()) MulticastServer.video++;
				BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream("./videos/video_" + MulticastServer.video));
				int count = 0;
				int size = 0;
				while(( count=input.read(data)) >= 0){
					output.write(data, 0, count);
					output.flush();
					size+=count;
				}
				output.close();
				count = 0;
				BufferedInputStream fin = new BufferedInputStream(new FileInputStream("./videos/video_" + MulticastServer.video));
				while ( fin.read(data) >= 0 ){
					if ( count == 0 ) { MulticastServer.sendStart(2,size); count = 1; }
					MulticastServer.sendMessage(data);
				}
				fin.close();
				MulticastServer.video++;
				MulticastServer.sendEnd();
				MulticastServer.sendEnd();
			}

			soc.shutdownInput();
		}
		catch (EOFException e) { System.out.println("End of data.");}
		catch (IOException e) { e.printStackTrace(); }
	}
}
