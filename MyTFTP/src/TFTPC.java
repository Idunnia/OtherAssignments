import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
public class TFTPC{

	DatagramSocket datagramSocket;
	DatagramPacket incomingPacket;
	InetAddress ServerAddress;
	InetAddress HostAddress;
	public static void main(String[] args) throws IOException {
		TFTPC myClient = new TFTPC();
		//Declare file name
		String filename = "justinryan.gif";
		myClient.write(filename);
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("Requesting");
		//myClient.request(filename);
		
	}
	
	
	public void request(String file) throws IOException {
		//Put server IP here
		ServerAddress = InetAddress.getByName("199.17.161.104");
		//Call method to make our request packet
		byte[] requestPacket = createRequestPacket(file);
		//Put the packet in the packet
		DatagramPacket outgoing = new DatagramPacket(requestPacket, requestPacket.length,ServerAddress,69);
		//Complete Socket setup and send the request
		datagramSocket = new DatagramSocket();
		datagramSocket.send(outgoing);
		
		//Recieve File from server
		ByteArrayOutputStream fileOS = receiveFile();
		
		//Write the file locally
		writeFile(fileOS, file);
		datagramSocket.close();
		System.out.println("Done");
	}
	
	public void write(String file) throws IOException{
		//Put server IP here
				ServerAddress = InetAddress.getByName("199.17.161.104");
				byte[] requestPacket = createWritePacket(file);
				DatagramPacket outgoing = new DatagramPacket(requestPacket, requestPacket.length,ServerAddress,69);
				datagramSocket = new DatagramSocket();
				datagramSocket.send(outgoing);
				System.out.println("Sent Request");
				byte[] fileBytes = Files.readAllBytes(Paths.get(file));
				FileInputStream fis = new FileInputStream(file);
				fis.read(fileBytes); //read file into bytes[]
				fis.close();
				//Recieve Ack Packet back before beginning to send the file
				System.out.print(sendFile(fileBytes));
				datagramSocket.close();
				
	}
	


	public byte[] createRequestPacket(String filename) {
		String mode = "octet";
		byte opCode = 1;
		byte[] result = new byte[filename.length()+mode.length() + 4];
		result[0] = 0;
		result[1] = opCode;
		//File bytes followed by zero byte
		int counter = 2;
		for (int i = 0; i < filename.length(); i++) {
			result[counter] = (byte) filename.charAt(i);
			counter++;
		}
		result[counter] = 0;
		counter++;
		// mode bytes followed by zero byte
		for (int i = 0; i < mode.length(); i++) {
			result[counter] = (byte) mode.charAt(i);
			counter++;
		}
		result[counter] = 0;
		return result;
	}
	
	public byte[] createWritePacket(String filename) {
		String mode = "octet";
		byte opCode = 2;
		byte[] result = new byte[filename.length()+mode.length() + 4];
		result[0] = 0;
		result[1] = opCode;
		//File bytes followed by zero byte
		int counter = 2;
		for (int i = 0; i < filename.length(); i++) {
			result[counter] = (byte) filename.charAt(i);
			counter++;
		}
		result[counter] = 0;
		counter++;
		// mode bytes followed by zero byte
		for (int i = 0; i < mode.length(); i++) {
			result[counter] = (byte) mode.charAt(i);
			counter++;
		}
		result[counter] = 0;
		return result;
	}
	
	
	public ByteArrayOutputStream receiveFile() throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		do {
			//Setup to receive packet
			byte[] buffer = new byte[516];
			incomingPacket = new DatagramPacket(buffer, buffer.length,ServerAddress,datagramSocket.getLocalPort());
			//actually get the packet
			datagramSocket.receive(incomingPacket);
			
			//Check the opcode
			byte[] opCode = { buffer[0], buffer[1] };
			if(opCode[1] == 5) {
				System.out.print("Error detected");
			} else if (opCode[1] == 3) {
				//Add data to the result so we can write it locally later
				DataOutputStream dOS = new DataOutputStream(result);
				dOS.write(incomingPacket.getData(),4, incomingPacket.getLength()-4);
				
				//send acknowledgement
				byte[] ack = {0,4,buffer[2], buffer[3]};
				DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, ServerAddress, incomingPacket.getPort());
				datagramSocket.send(ackPacket);
			}
			
		} while(incomingPacket.getLength() > 512);
		return result;
	}
	
	public boolean sendFile(byte[] file) throws IOException{
		
		//Creating all packets to be sent
		ArrayList<byte[]> filePackets = new ArrayList<byte[]>();
		//fill remaining bytes of data array with file bytes
		for(int i = 0; i<Math.ceil(file.length/512); i++) {
			byte[] data = new byte[516];
						data[0] = 0;
						data[1] = 3;
						ByteBuffer dbuf = ByteBuffer.allocate(4);
						dbuf.putInt(i+1);
						byte[] bytes = dbuf.array();
						data[2] = bytes[2];
						data[3] = bytes[3];
			for(int j = 0; j<512; j++) {
						data[j+4] = file[i*512 + j];				
			}
			filePackets.add(data);
			
		}
		
		System.out.print(filePackets.size());
		short counter = 0;
		
		do {
			//Setup to receive packet
			byte[] buffer = new byte[4];
			incomingPacket = new DatagramPacket(buffer, buffer.length,HostAddress,datagramSocket.getLocalPort());
			//actually get the packet
			datagramSocket.receive(incomingPacket);
			//Translates the ack packet and does a send
			byte[] opCode = { buffer[0], buffer[1] };
			if(opCode[1] == 5) {
				System.out.print("Error detected");
			} else if (opCode[1] == 4) {
				byte[] blockNumber = {buffer[2], buffer[3]};
				// Convert bytes to one value to compare easier
				ByteBuffer wrapped = ByteBuffer.wrap(blockNumber);
				short num = wrapped.getShort();
				//compare the block number to counter
				if(counter == num) {
				//Send data
				byte[] datatosend = filePackets.get(counter);	
				DatagramPacket dataPacket = new DatagramPacket(datatosend, datatosend.length, ServerAddress, incomingPacket.getPort());
				datagramSocket.send(dataPacket);
				counter++;
				} else {
					System.out.print("Block Numbers did not match");
				}
				
			}
			
		
		} while(counter < filePackets.size());
		//Setup to receive packet
		byte[] buffer = new byte[4];
		incomingPacket = new DatagramPacket(buffer, buffer.length,HostAddress,datagramSocket.getLocalPort());
		//actually get the packet
		datagramSocket.receive(incomingPacket);
		
		return true;
	}
	

	
	
	private void writeFile(ByteArrayOutputStream fileOS, String file) {
		// TODO Auto-generated method stub
		try {
			OutputStream outputStream = new FileOutputStream(file);
			fileOS.writeTo(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
