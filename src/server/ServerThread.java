package server;
import java.net.*;
import java.io.*;
import java.util.*;
public class ServerThread extends Thread {
	private String user = null;					// name of user in this connection
	private ServerIRC server = null;			// server itself, need to share resource between threads
	private String[] cmds = {"CONNECT","SEND","JOIN","DISCONNECT","QUIT","LIST","CHANNEL","PM","PING","PONG"}; // list of commands
	private DataInputStream DataIn = null;		// Data input stream
	private DataOutputStream DataOut = null;	// Data output stream
	private ChannelList joinedChannel = null;	// channels this user has joined
	private Socket socket = null;				// socket for this connection
	
	public ServerThread(Socket socket,ServerIRC server){
		super();
		this.socket = socket;
		this.server = server;
		joinedChannel = new ChannelList();
	}
	// while the thread runs, it handles commands from users
	// handle connection errors
	public void run(){
		while(true){
			try {
				handleBlacklist();
				handleCommand(DataIn.readUTF());				
			}
			catch(Exception e)		// Connection error happens
			{
				System.out.println("Error while connecting with client...");
				try {		// try to ping the client
					System.out.println("Trying to ping the client");
					this.send("PING");
					String ping = DataIn.readUTF();
					if(ping.equals("PONG"))		// receive response's pong from client
						continue;
					else						// if receiving something else
						System.out.println("Client response :" + ping);
					}
				catch(IOException a) {		// cannot perform ping, client has crashed
					System.out.println("Cannot PING the client");
					System.out.println("Client suddenly disconnect");
					handleDisconnection();
				}
			
			}
		}
	}
	
	// create input/output stream
	public void open() throws IOException
	{ 
		DataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	    DataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}
	
	// send message to clients
	public void send(String msg){
		try {
			DataOut.writeUTF(msg);
			DataOut.flush();
		}
		catch(IOException e){
			System.out.println("Output stream invalid: " + e.getMessage());
			handleDisconnection();
		}
	}
	
	// get this username
	public String getUser(){
		return this.user;
	}
	
	// set this username
	public void setUser(String name){
		this.user = name;
	}
	
	// get the the list of channels this user has joined
	public ChannelList getJoinedChannel(){
		return this.joinedChannel;
	}
	
	// match commands user input with list of supported commands
	private int matchCommand(String usercmd)
	{
		for(int i = 0; i < this.cmds.length; i++)
		{
			if(this.cmds[i].equals(usercmd))
			{
				return i;
			}
		}
		return -1;
	}
	
	// check format of a command
	private boolean checkFormat(String input){
		for(int i = 0; i < input.length(); i++)
		{
			if(!Character.isLetterOrDigit(input.charAt(i)))
				return false;
		}
		return true;
	}
	
	// handle disconnection in case client crashed
	private void handleDisconnection(){
		if(this.user != null){
			while(!this.joinedChannel.isEmpty()){
				Channel chan = joinedChannel.getFirst();
				ServerManager chanlist = chan.getListClients();
				chanlist.removeClient(this.user);
				this.joinedChannel.remove(chan);
				server.getlistChannels().removeChannel(chan.getChannelName());
			}
			server.getlistUsers().quit(this.user);
		}
		this.stop();
	}
	
	// handle commands input by users
	private void handleCommand(String cmd){
		if(cmd.length() > 540){		// check the length of commands
			this.send("Your command is too long");
		}
		else{
			// divide the commands into 3 parts: command name, parameters, text message
			String command = null;		// command name	
			String params = null;		// parameters
			String text = null;			// text message
			String [] texts = cmd.split(":", 2);
			if (texts.length > 1)		// check if text is empty
				   text = texts[1];
			String [] cnp = cmd.split(" ", 0);
			command = cnp[0];			// get the command name
			for(int i = 1; i < cnp.length; i++){
				if(!cnp[i].equals("")){
					params = cnp[i];		// get the parameters
					break;
				}
			}
			if(this.user == null){			// check if user is active
				if(command.equals("DISCONNECT"))		
					handleDisconnectCommand();
				else if(command.equals("CONNECT"))
					handleConnectCommand(params);
				else
					this.send("You are not active");
			}
			else{
				switch(matchCommand(command)){
				case 0: handleConnectCommand(params);
						break;
				case 1: handleSendCommand(params, text);
						break;
				case 2: handleJoinCommand(params);
						break;
				case 3: handleDisconnectCommand();
						break;
				case 4: handleQuitCommand(params);
						break;
				case 5: handleListCommand(params);
						break;
				case 6: handleChannelCommand();
						break;
				case 7: handlePMCommand(params,text);
						break;
				case 8: handlePING();
						break;
				case 9: handlePONG();
						break;
				default: this.send("The command " + cmd + " is not supported ");
				}
			}	
		}
	}
	
	private void handleConnectCommand(String userID){
		if(userID == null)
			this.send("No username is given");
		else
		{
			if(userID.length() > 10)
				this.send("Username is longer than 10 characters");
			else if(!checkFormat(userID))
				this.send("Username invalid " + userID);
			else{
				server.getlistUsers().connect(this, userID);
			}
			// this.send("PING");
		}
	}
	
	private void handleSendCommand(String params, String content){
		if(params == null)
			this.send("cmd SEND needs more parameters");
		else if(content == null || content.equals(""))
			this.send("cmd SEND has no content");
		else if(params.charAt(0) != '#' || !checkFormat(params.substring(1)))
			this.send("cmd SEND has no channel name");
		else{
			Channel chan = server.getlistChannels().findChannel(params);
			if(chan != null)
			{
				ServerManager channelList = chan.getListClients();
				channelList.sendMessage(params, this.user, content, this);
			}
			else
				this.send("No such channel exists");
		}
	}
	
	private void handleJoinCommand(String params){
		if(params == null)
			this.send("cmd JOIN needs more parameters");
		else if(params.charAt(0) != '#' || !checkFormat(params.substring(1)))
			this.send("cmd JOIN has no channel name");
		else {
			server.getlistChannels().joinChannel(params, this);
		}
	}
	
	private void handleDisconnectCommand(){
		if(this.user != null){
			while(!joinedChannel.isEmpty()){
				this.handleQuitCommand(joinedChannel.getFirst().getChannelName());
			}
			System.out.println("User: " + this.user + " disconnected");
			server.getlistUsers().quit(this.user);
			System.out.println("cmd DISCONNECT executed");
		}
		else {
			System.out.println("cmd DISCONNECT on null user");
			return;
		}
		try {
			this.send("cmd DISCONNECT executed");
			if(DataIn != null)
				DataIn.close();
			if(DataOut!= null)
				DataOut.close();
			if(socket != null)
				socket.close();
		}
		catch(IOException e){
			System.out.println("cmd DISCONNECT cannot be executed: " + e);
		}
		this.stop();
	}
	
	private void handleQuitCommand(String params){
		if(params == null)
			this.send("cmd QUIT needs more parameters");
		else if(params.charAt(0) != '#' || !checkFormat(params.substring(1)))
			this.send("cmd QUIT has no channel name");
		else {
			Channel chan = server.getlistChannels().findChannel(params);
			if(chan != null) {
				ServerManager channelList = chan.getListClients();
				if(channelList.findClient(this.user) != null) {
					channelList.removeClient(this.user);
					this.joinedChannel.remove(chan);
					server.getlistChannels().removeChannel(params);
					this.send("You quited " + params);
				}
			}
			else
				this.send("cmd QUIT: cannot find this channel to quit");
		}
	}
	
	private void handleListCommand(String params){
		if(params == null)
			this.send("cmd LIST needs more parameter");
		else if(params.charAt(0) != '#' || !checkFormat(params.substring(1)))
			this.send("cmd LIST has no channel name");
		else {
			Channel chan = server.getlistChannels().findChannel(params);
			if(chan != null) {
				String msg = "Channel " + chan.getChannelName() + "\n";
				msg +=	"Users on channel: " + chan.getListClients().listClient() + "\n";
				this.send(msg);
			}	
			else
				this.send("cmd LIST: cannot find this channel to list");
		}		
	}
	
	private void handleChannelCommand(){
		server.getlistChannels().listAllChannels(this);
	}
	
	private void handleBlacklist(){
		String[] blacklist = {"harry","tom","tree"};
		if(Arrays.asList(blacklist).contains(this.user)) {
			System.out.println("Username '"+ this.user + "' is in blacklist. Kicking out.");
			System.out.println("User '" + this.user + "' has been terminated");
			String msg = "Blacklisted username, diconnected...\n";
			this.send(msg);
			handleDisconnection();	
		}
	}
	
	private void handlePMCommand(String params, String content){
		if(params == null)
			this.send("cmd PM needs more parameters");
		else if(content == null || content.equals(""))
			this.send("cmd PM has no content");
		else{
			ServerManager sm = server.getlistUsers();
			sm.sendPM(this.user, content, this,params);;
		}
	}
	
	private void handlePING(){
		this.send("PONG");
	}
	
	private void handlePONG(){
		System.out.println("received PONG from client");
	}
	
}