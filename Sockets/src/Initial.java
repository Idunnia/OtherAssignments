import java.io.*;
import java.net.*;

public class Initial {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ServerSocket serverSocket = null;
		try {
		     serverSocket = new ServerSocket(5000);
		} catch (IOException ex) { 
			
		}

		Client cThread = new Client();
		cThread.start();
		
		
		Socket temp;
		try {
			temp = serverSocket.accept();
			System.out.println("Server accepted");
			
			DataInputStream in =   new DataInputStream(temp.getInputStream());
			DataOutputStream out = new DataOutputStream(temp.getOutputStream());
			
			Server sThread = new Server(temp, in, out);
			sThread.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
