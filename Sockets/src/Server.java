import java.io.*;
import java.net.Socket;

public class Server extends Thread {
	
	DataOutputStream out;
	DataInputStream in;
	Socket s;
	public Server(Socket y, DataInputStream r, DataOutputStream w) {
		in = r;
		out = w;
		s = y;
	}
	
	public void run() {
		
			try {
				out.writeUTF("go");
				
				
				while(true) {
				String j = in.readUTF();
				if(j.equals("end")) {
					System.out.println("Ending the Server");
					s.close();
					in.close();
					out.close();
				} else {
					int counter = Integer.parseInt(j) + 1;
					System.out.println(counter);
					out.writeUTF(""+counter);
				}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
}
