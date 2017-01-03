package server;

import java.util.*;

// Channel list is used to manage a list of many channels on server
// by using linked list
public class ChannelList extends LinkedList<Channel> {
	// create a new channel then add it to the linked list
	public synchronized Channel createChannel(String name,ServerThread client){
		Channel chan = new Channel(name, client);
		this.addLast(chan);
		return chan;
	}
	
	// find a specific channel's name by looping through the linked list
	// and compare the name
	public synchronized Channel findChannel(String name){
		for(int i = 0; i < this.size(); i++)
		{
			if(this.get(i).getChannelName().equals(name)){
				return this.get(i);
			}
		}
		return null;
	}
	
	// remove a specific channel's name by looping through the linked list
	// and compare the name
	public synchronized void removeChannel(String name){
		Channel chan = findChannel(name);
		if(chan.getListClients().size() == 0 && chan != null)
		{
			this.remove(chan);
		}
	}
	
	// list all channels' name by looping through the linked list
	// and print out each name
	public synchronized void listAllChannels(ServerThread client){
		String msg = "List of current channels: \n";
		for(int i = 0; i < this.size(); i++)
		{
			Channel chan = this.get(i);
			msg += chan.getChannelName() + "\n";
		}
		client.send(msg);
	}
	
	// list all usernames by looping through the linked list
	// and print out each name
	public synchronized void listAllClients(ServerThread client){
		String msg = "List of current users: \n";
		for(int i = 0; i < this.size(); i++)
		{
			Channel chan = this.get(i);
			msg = chan.getListClients() + "\n";
		}
		client.send(msg);
	}
	
	// join a specific channel by using its name
	public synchronized void joinChannel(String name, ServerThread client){
		Channel chan = findChannel(name);
		String msg = null;
		if(chan == null){			// if there is no such channel with this name
			chan = createChannel(name,client);		// create a new one
			msg = "New channel '" + name + "' is created \n";
		}
		else{						// if channel exists
			ServerManager chanlist = chan.getListClients();	
			// check whether this user already joined
			if(chanlist.findClient(chan.getChannelName()) == null){ 
				chanlist.addClient(client);
				msg = "You join the channel '" + name + "'\n";
				client.getJoinedChannel().addLast(chan);
			}
		}
		client.send(msg);
	}
}
