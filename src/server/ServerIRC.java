package server;
import java.net.*;
import java.io.*;

public class ServerIRC implements Runnable {
	
	private ServerSocket server = null;		// server socket	
	private Thread thread = null;			// server thread
	private ServerManager listUsers = new ServerManager();	// list of users on server
	private ChannelList listChannels = new ChannelList();	// list of channels on server
	
	// Contructor: create server on a specific port, create the server thread
	public ServerIRC(int port){
		try {
			server = new ServerSocket(port);
			thread = new Thread(this);
			thread.start();
		}
		catch(IOException e){
			System.out.println("Port " + port + " is unavailable " + e.getMessage());
		}
	}
	
	// create a thread for each user connecting to the server
	private void createThread(Socket socket){
		ServerThread client = new ServerThread(socket, this);
		try {
			client.open();
			client.start();
		}
		catch(IOException e){
			System.out.println("Error creating thread " + e);
		}
		
	}
	// server gonna wait for connection from users, then assign socket to each user by calling createThread()
	public void run(){
		while(thread != null){
			try {
				System.out.println("Server listenning....");
				createThread(server.accept());
			}
			catch(IOException e){
				System.out.println("Cannot connect this thead: " + e);
				thread.stop();
				thread = null;
			}
		}
	}
	
	// get list of channels on server
	public ChannelList getlistChannels(){
		return this.listChannels;
	}
	
	// get list of users on server
	public ServerManager getlistUsers(){
		return this.listUsers;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerIRC server = null;
	    server = new ServerIRC(5000);
	}

}
