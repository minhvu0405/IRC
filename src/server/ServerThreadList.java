package server;

import java.util.LinkedList;
// ServerThreadList is a linked list to manage threads connecting to users
public class ServerThreadList extends LinkedList<ServerThread> {
	
	// find a specific client by name 
	// by looping through the linked list
	// and compare each name
	public synchronized ServerThread findClient(String user){
		for(int i = 0; i < this.size(); i++)
		{
			if(get(i).getUser().equals(user))
			{
				return get(i);
			}
		}
		return null;
	}
	
	// remove a specific client by name 
	// by looping through the linked list
	// and compare each name
	public synchronized ServerThread removeClient(String user){
		ServerThread client = findClient(user);
		if(client == null){		// cannot find this user
			return null;
		}
		this.remove(client);	// if any, remove
		return client;
	}
	
	// add a new client (thread) to the end of the linked list
	public synchronized void addClient(ServerThread client)
	{
		this.addLast(client);
	}
	
	// list all clients
	public synchronized String listClient(){
		String nameUsers = new String();
		for(int i = 0; i < this.size(); i++)
		{
			nameUsers += get(i).getUser() + " ";
		}
		return nameUsers;
	}
	
}
