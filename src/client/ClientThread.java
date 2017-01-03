
import java.net.*;
import java.io.*;

public class ClientThread extends Thread {
	private Socket socket = null;
	private IRCClient client = null;
	private DataInputStream DataIn = null;
	
	// Contructor: set up the socket, ircclient, try to get data
	public ClientThread(IRCClient client, Socket socket)
	{
		this.socket = socket;
		this.client = client;
		try {
			DataIn = new DataInputStream(socket.getInputStream());
		}
		catch(IOException e) {
			System.out.println("Cannot get incoming data " + e);
		}
	}
	// while running, listen to server's response
	public void run(){
		while(true) {
			try {			// contansly listen to the server
				String msg = DataIn.readUTF();
				handleMSG(msg);
			}
			catch(IOException e) {			// if connection error with server happens
				System.out.println("Trying to ping the server");
				try {						// send ping to server and wait for response
					client.send("PING");
					String ping = DataIn.readUTF();
					if(ping.equals("PONG"))		// if server can response with ping, every thing is fine
						continue;
					else						// if server response something else
					{
						System.out.println("Server response " + e);
						continue;
					}
				}
				catch(IOException a) {			// if cannot ping the server
					System.out.println("Cannot PING the server");
					System.out.println("You have been disconnected from the server");
					this.close();
					client.close();
					System.exit(0);
				}
			}
		}
	}
	
	// close all resource used by this thread
	public void close(){
		try {
			if(DataIn != null)
				DataIn.close();
		}
		catch(IOException e){
			System.out.println("Error " + e);
		}
	}
	
	// handle message from server
	public void handleMSG(String msg) {
		if(msg.equals("PING"))				// if server sends ping, reply pong
			client.send("PONG");
		else if(msg.equals("cmd DISCONNECT executed"))			// if client wants to disconnect 
		{
			System.out.println("You shut down the client, Bye!");
			System.exit(0);
		}
		else if(msg.length() > 1)								// if a normal message received
			System.out.println(msg);
		else													// null message received
			System.out.println("received null message");
	}

}