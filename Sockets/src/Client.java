import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread {
	Socket s;
	
	
	public Client() {
		// TODO Auto-generated constructor stub
		System.out.println("Client Thread is up");
		
	}
	
	public void run() {
		try {
			System.out.println("Attempting Client Connection");
			s = new Socket(InetAddress.getByName("localhost"), 5000);
			
			DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
      
			while(true) {
			
				String j = dis.readUTF();
			
				if(j.equals("go")) {
					dos.writeUTF(1+"");
					System.out.println("Starting Counter");
					System.out.println(1);
				} else if(Integer.parseInt(j)>99) {
					System.out.println("Client is done");
					dos.writeUTF("end");
					s.close();
					dis.close();
					dos.close();
				} else {
					int counter = Integer.parseInt(j) + 1;
					dos.writeUTF(counter+"");
					System.out.println(counter);
				}
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}
