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
		while(true){
			Socket s = ss.accept();
			ServerThread t = new ServerThread(s);
			t.start();
		}
	}
	public static void sendUDPMessage(byte[] msg, int type) throws IOException {
		if (type == 1){
			DatagramPacket first = new DatagramPacket("--TXT--".getBytes(), 7, to, 8888);
			socket.send(first);
		}
		else if (type == 2){
			DatagramPacket first = new DatagramPacket("--IMG--".getBytes(), 7, to, 8888);
			socket.send(first);
		}
		else if (type == 3){
			DatagramPacket first = new DatagramPacket("--VID--".getBytes(), 7, to, 8888);
			socket.send(first);
		}
		DatagramPacket packet = new DatagramPacket(msg, msg.length, to, 8888);
		socket.send(packet);
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
			byte[] data = new byte[1024];
			DataInputStream input = new DataInputStream(soc.getInputStream());
			input.readFully(type,0,7);
			String s = new String(type);

			if (s.equals("--TXT--")){
				System.out.println("Client - Text");
				while ( input.read(data) >=0 ){
					String str = new String(data);
					MulticastServer.sendUDPMessage(data);
				}
			}

			else if (s.equals("--IMG--")){
				System.out.println("Client - Image");
				BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream("./images/" + MulticastServer.img));
				int count = 0;
				int chunks = 0;
				while(( count=input.read(data)) >= 0){
					output.write(data, 0, count);
					output.flush();
					chunks++;
				}
				output.close();
				BufferedInputStream fin = new BufferedInputStream(new FileInputStream("./images/" + MulticastServer.img));
				for (int i = 0; i < chunks; i++){
					fin.read(data);
					MulticastServer.sendUDPMessage(data);
				}
				fin.close();
				MulticastServer.img++;
			}

			else if (s.equals("--VID--")){
			}
		}
		catch (EOFException e) { System.out.println("End of data.");}
		catch (IOException e) { e.printStackTrace(); }
	}
}
