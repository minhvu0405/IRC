package server;

public class ServerManager extends ServerThreadList {
	
	// send messages to all users in a specific channel 
	public synchronized void sendMessage(String channelName, String user, String message, ServerThread client){
		if(findClient(user) == null){			// check if user is valid
			client.send("Can not send to this channel " + channelName);
		}
		else									// if user is valid
		{
			for(int i = 0; i < this.size(); i++)			// loop through the list of threads on server
			{
				String msg = channelName + " " + user + " : " + message;
				get(i).send(msg);
			}
		}
	}
	
	public synchronized ServerThread findClient(String user, Channel chan, ServerThread client){
		for(int i = 0; i < this.size(); i++)
		{
			if(get(i).getUser().equals(user))
			{
				return get(i);
			}
		}
		client.send("You are not in the channel " + chan.getChannelName());
		return null;
	}
	
	// Handle connection commands
	public synchronized void connect(ServerThread request, String name){
		ServerThread client = findClient(name);			// find the user with the given name
		if(request == client || client == null){		// if no user with that name
			request.setUser(name);						// set up this user 
			add(request);
			request.send("You has joined the IRC");
		}
		else{											// if there is another user with this name
			request.send("Username: " + name + " has been used, please choose another one");
		}
	}
	
	// handle the quit command, remove the user with a specific name
	// by calling the function removeClient from ServerThreadList
	public synchronized void quit(String user){
		removeClient(user);
	}
	// send private messages to another user
	public synchronized void sendPM(String user, String message, ServerThread client, String receiver){
		if(findClient(user) == null){				// check if user is valid
			client.send("Username is null");
		}
		else if(receiver == null){					// check if the receiver name is null
			client.send("No receiver name is given");
		}
		else if(findClient(receiver) == null){		// check if the receiver exists
			client.send("There is no user with this name");
		}
		else
		{	
			for(int i = 0; i < this.size(); i++)
			{
				if(get(i).getUser().equals(receiver)){
					String msg = "PM from '" + user + "' : " + message;
					get(i).send(msg);
					break;
				}
			}
		}
	}
}
