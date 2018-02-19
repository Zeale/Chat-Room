package org.alixia.chatroom.connections;

import java.util.HashMap;

public class ConnectionManager {

	public final HashMap<String, Client> clients = new HashMap<>();
	public final HashMap<String, Server> servers = new HashMap<>();

	private Client currentClient;
	private Server currentServer;

	public Client getCurrentClient() {
		return currentClient;
	}

	public Server getCurrentServer() {
		return currentServer;
	}

	public boolean isClientSelected() {
		return currentClient != null;
	}

	public boolean isServerSelected() {
		return currentServer != null;
	}

	public boolean selectClient(String clientName) {
		if (clients.containsKey(clientName)) {
			currentClient = clients.get(clientName);
			return true;
		}
		return false;
	}

	public boolean selectServer(String serverName) {
		if (servers.containsKey(serverName)) {
			currentServer = servers.get(serverName);
			return true;
		}
		return false;
	}

	public boolean addClient(String name, Client client) {
		// putIfAbsent returns null if there was no mapping, so it returns null if we
		// successfully added a client.
		return clients.putIfAbsent(name, client) == null;
	}

	public boolean addServer(String name, Server server) {
		return servers.putIfAbsent(name, server) == null;
	}

}
