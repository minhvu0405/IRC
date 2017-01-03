
import java.net.*;
import java.io.*;


public class IRCClient implements Runnable {
	private Socket socket = null;				// socket used by client
	private Thread thread = null;				// thread used by client to get input
	private DataInputStream DataIn = null;		// Data input
	private DataOutputStream DataOut = null;	// Data output
	private ClientThread client = null;			// client thread displays server's response
	// Contructor: set up socket, data input/output, connection socket
	public IRCClient(String serverHost, int serverPort){
		try {
			socket = new Socket(serverHost, serverPort);		// connect to the server
			 //set up input stream from user, and output stream to server
			DataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			DataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			System.out.println("Connected successfully to server socket");
			// create 2 thread
			client = new ClientThread(this, socket);			// thread to listen
			thread = new Thread(this);							// thread to send
			client.start();
			thread.start();
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	// while running, get client's command
	public void run(){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter cmd whenever you want: ");
		while(true){
		   	try{ 
				String cmd = br.readLine();		
				send(cmd);
			}
		   	catch(IOException e) {
				System.out.println("Error " + e);
				break;
		   	}
		}
	}
	// send messages to server
	public void send(String msg){
		try {
			DataOut.writeUTF(msg);
			DataOut.flush();
		}
		catch(IOException e){
			System.out.println("Cannot send the msg " + msg);
		}
	}	
	// close the client socket, input/output stream 
	public void close()
	{  
		try {
			if (DataIn  != null)  DataIn.close();
		    if (DataOut != null)  DataOut.close();
		    if (socket  != null)  socket.close();
		}
		catch(IOException e){
		    System.out.println("Error while closing: " + e);
		}
	}
		
	public static void main(String args[]) {
		IRCClient client = null;
		client = new IRCClient("localhost",5000);
	} 
}
