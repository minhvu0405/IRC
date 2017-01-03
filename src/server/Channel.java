package server;

public class Channel {
	private String name = null; 				// Channel's name
	private ServerManager listClients = null;	// list of users on this channel
	
	// Constructor: create a new channel when the first user executes JOIN #channelname
	public Channel(String name, ServerThread client)
	{
		this.name = name;
		listClients = new ServerManager();
		listClients.addClient(client);
	}
	
	// get list of clients on this channel
	public ServerManager getListClients(){
		return listClients;
	}
	
	// set name for the channel
	public void setName(String name){
		this.name = name;
	}
	
	// get the channel's name
	public String getChannelName(){
		return this.name;
	}
	
}
